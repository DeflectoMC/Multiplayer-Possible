package multiplayer.possible;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.Objects;
import java.util.Scanner;
import java.util.concurrent.CompletableFuture;

public class Main {
	
	private static int join_port = 35585;
	private static int host_port = 25565;
	
	private static Connection c;
	
	public static void main(String[] args) {
		try {
			while (run(args)) {}
		}  catch (Exception ex) {
			ex.printStackTrace();
		} finally {
			if (c != null) c.close();
		}
		System.exit(0);
	}

	@SuppressWarnings("resource")
	public static boolean run(String[] args) throws Exception {
		
		GUI gui = new GUI("Multiplayer-Possible");
		gui.printMessages();

		Scanner sc = new Scanner(System.in);
		String choice = gui.prompt("Select action", "Join", "Host", "Configure ports");
		
		if (choice == null) return false;
		
		if (choice.equalsIgnoreCase("Configure ports")) {
			String choice2 = gui.prompt("Select an option", "Set custom join port", "Set custom host port", "Go back");
			if (choice2 == null) return true;
			if (choice2.equalsIgnoreCase("Go back")) return true;
			
			String result = null;
			
			int port = -1;
			if (choice2.contains("join")) {
				result = gui.prompt("Enter your custom join port", "35585");
				if (result == null) return true;
				
				try {
					port = Integer.parseInt(result);
				} catch (Exception ex) {}
				if (port > 0 && port <= 65535) {
					join_port = port;
					gui.info("Join port has been changed to " + join_port);
					return true;
				}
			} else {
				result = gui.prompt("Enter your custom host port", "25565");
				if (result == null) return true;
				
				try {
					port = Integer.parseInt(result);
				} catch (Exception ex) {}
				if (port > 0 && port <= 65535) {
					host_port = port;
					gui.info("Host port has been changed to " + host_port);
					return true;
				}
			}
			
			if (port < 0 || port > 65535) {
				if (result.contains(".") || result.contains(",")) {
					gui.info("Invalid port. Port should be a whole number ranging from 0-65535");
				} else {
					gui.info("Invalid port. Port should be a number ranging from 0-65535");
				}
				return true;
			}
			
			
		}
		
		boolean isClient = choice.equalsIgnoreCase("Join");
		
		if (!isClient) {
			try (Socket test = new Socket("127.0.0.1", host_port)) {} catch (Exception ex) {
				gui.info(ex + "\n\nFailed to connect to your server running on port " + host_port + ".\nMake sure the server is running, or if it is not on " + host_port + ", then select Configure ports");
				return true;
			}
		}
		
		String peerName = isClient ? "server" : "player";
		String address = gui.prompt("Enter the " + peerName + "'s public IP address");
		if (address == null) return true;
		
		if (!address.contains(".") && !address.contains(":")) {
			gui.info("The address you entered is invalid.\nMake sure it is a valid IPV4 or IPV6 address\nExample: 127.0.0.1");
			return true;
		}

		InetAddress addr;
		try {
			addr = InetAddress.getByName(address);
		} catch (Exception ex) {
			gui.info("The address you entered is invalid.\nMake sure it is a valid IPV4 or IPV6 address\nExample: 127.0.0.1");
			return true;
		}

		String nick1 = gui.prompt("Enter your nickname");
		if (nick1 == null) return true;
		String nick2 = gui.prompt("Enter the " + peerName + "'s nickname");
		if (nick2 == null) return true;
		
		if (nick1.equals(nick2)) {
			gui.info("Please pick a different nickname.\nYou have to use a nickname different than the " + peerName + "'s");
			return true;
		}
		
		String combinedNicks = (nick1.compareTo(nick2) >= 0) ? nick1 + nick2 : nick2 + nick1;
		int hash1 = Objects.hash(nick1, combinedNicks);
		int hash2 = Objects.hash(nick2, combinedNicks);

		int port1 = 40_000 + (hash1 % 20_000);
		int port2 = 40_000 + (hash2 % 20_000);
		if (port1 == port2) {
			gui.info(String.format("Please pick a different nickname.\nThe nicknames \"%s\" and \"%s\" aren't compatible right now", nick1, nick2));
			return true;
		}

		c = new Connection();
		try {
			c.bind(port1);
		} catch (Exception ex) {
			c.close();
			gui.info("Please pick a different nickname.\nThe port calculcated from your nickname is still in use");
			return true;
		}

		System.out.println();
		gui.info("Press ok to start");

		if (!c.establish(new InetSocketAddress(addr, port2), 10, 10_000l)) {
			gui.info("Failed to establish a connection after 10 attempts");
			return true;
		}


		byte[] send = new byte[2 * 1024 * 1024];
		byte[] receive = new byte[2 * 1024 * 1024];

		if (isClient) {
			
			ServerSocket ss = new ServerSocket();
			//ss.setSoTimeout(10_000);
			try {
				ss.bind(new InetSocketAddress("127.0.0.1", join_port));
			} catch (Exception ex) {
				c.close();
				gui.info("Failed to connect: You have another program using port " + join_port + ".\n\nYou can configure port options at the start");
				return true;
			}
			CompletableFuture.runAsync(() -> {
				gui.info("Successfully established a connection! Waiting for you to join\n\nServer IP = 127.0.0.1:" + join_port + "\n\nPress ok to close this window (it won't close automatically)");
			});
			while (!ss.isClosed() && !c.isClosed()) {
				Socket player;
				ss.setSoTimeout(10_000);
				try {
					Thread.sleep(500);
					player = ss.accept();
				} catch (SocketTimeoutException ex) {
					//System.out.println("Waiting for you to connect");
					if (!c.writeClose()) {
						break;
					}
					continue;
				}
				ss.setSoTimeout(0);
				System.out.println("You're joining... notifying the server");

				c.writeOpen();
				System.out.println("Waiting for the host to respond");
				if (!c.readOpen()) {
					System.out.println("Host didn't open back");
					Utils.close(player);
					continue;
				}

				new Thread(() -> {
					int r;
					while (!Utils.isClosed(player) && !c.isClosed()) {
						r = c.readRegular(receive);
						if (r == -1) break;
						Utils.write(player, receive, 0, r);
					}
					c.isClosed();
					Utils.close(player);
				}).start();

				int read;

				while (!Utils.isClosed(player) && !c.isClosed()) {
					read = Utils.read(player, send);
					if (read == -1) break;
					System.out.println("Packet length " + read);
					c.write(send, 0, read);
				}

				Utils.close(player);
				if (c.isClosed()) break;

				//Notify closed
				c.writeClose();
				System.out.println("Waiting for you to connect");
			}
			Utils.close(ss);
		} else {
			CompletableFuture.runAsync(() -> {
				gui.info("Waiting for player to connect");
			});
			
			long fail = 0l;
			int fails = 0;
			
			boolean firstConnect = true;
			
			while (!c.isClosed()) {
				if (!c.readOpen()) {
					long lastFail = fail;
					fail = System.currentTimeMillis();
					if (lastFail > System.currentTimeMillis() - 1000) {
						if (fails >= 3) break;
						fails++;
						continue;
					}
					//System.out.println("Didn't receive join from player");
					continue;
				}
				fails = 0;
				fail = 0l;
				System.out.println("Received join from player");

				Socket s = new Socket();
				try {
					s.connect(new InetSocketAddress("127.0.0.1", host_port), 5000);
				} catch (Exception ex) {
					if (firstConnect) {
						Utils.close(s);
						c.close();
						gui.info("It appears there isn't a server running on port " + host_port + ".\n\nIf this is not your server's port, you can configure it at the starting options");
						return true;
					}
				}
				
				firstConnect = false;
				if (!s.isConnected()) {
					System.out.println("Failed to connect to server");
					Utils.close(s);
					if (!c.isClosed()) c.writeClose();
					continue;
				}
				System.out.println("Connected to server, notifying player");
				c.writeOpen();

				new Thread(() -> {
					int r;
					while (!Utils.isClosed(s) && !c.isClosed()) {
						r = c.readRegular(receive);
						if (r == -1) break;
						Utils.write(s, receive, 0, r);
					}
					c.isClosed();
					Utils.close(s);
				}).start();

				int read;

				while (!Utils.isClosed(s) && !c.isClosed()) {
					read = Utils.read(s, send);
					if (read == -1) break;
					System.out.println("Packet length " + read);
					c.write(send, 0, read);
				}

				Utils.close(s);
				if (!c.isClosed()) c.writeClose();
			}

		}
		
		c.close();
		c = null;

		System.out.println("Disconnected");
		
		return false;
	}

}