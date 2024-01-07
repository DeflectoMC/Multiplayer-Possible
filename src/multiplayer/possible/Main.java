package multiplayer.possible;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.net.SocketTimeoutException;

public class Main {
	
	public static void main(String[] args) throws Exception {
		GUI g = new GUI("Multiplayer-Possible");
		String choice = g.prompt("Pick an option", "Host", "Join");
		boolean isClient = choice.equals("Join");
		String peerName = isClient ? "server" : "client";
		String addr = g.prompt("Enter the " + peerName + "'s public ip address");
		InetAddress address = InetAddress.getByName(addr);
		String nickname = (isClient) ? g.prompt("Enter a nickname") : g.prompt("Enter the client's nickname");
		
		int port = 40_000 + (nickname.hashCode() % 20_000);
		int port1 = (isClient) ? port : port+1;
		int port2 = (isClient) ? port+1 : port;
		
		if (isClient) {
			g.info("Press ok when the host is ready");
		} else {
			g.info("Press ok to be ready");
			Socket s = new Socket();
			s.setReuseAddress(true);
			s.bind(new InetSocketAddress("0.0.0.0", port1));
			try {
				s.connect(new InetSocketAddress(address, port2), 100);
			} catch (Exception ex) {
				s.close();
			}
		}
		
		Socket connection;
		ServerSocket ss = null;
		
		if (isClient) {
			connection = new Socket();
			connection.setReuseAddress(true);
			connection.bind(new InetSocketAddress("0.0.0.0", port1));
			try {
				connection.connect(new InetSocketAddress(address, port2), 10000);
			} catch (Exception ex) {
				g.info("Failed to connect");
				connection.close();
				return;
			}
		} else {
			ss = new ServerSocket();
			ss.setReuseAddress(true);
			ss.bind(new InetSocketAddress("0.0.0.0", port1));
			try {
				connection = ss.accept();
			} catch (Exception ex) {
				g.info("Failed to connect");
				ss.close();
				return;
			}
		}
		
		connection.setKeepAlive(true);
		if (isClient) {
			
			ServerSocket proxy2client = new ServerSocket();
			proxy2client.setReuseAddress(true);
			proxy2client.setSoTimeout(10000);
			proxy2client.bind(new InetSocketAddress("127.0.0.1", 35585));
			new Thread(() -> {
				g.info("Success! Waiting for you to join at 127.0.0.1:35585");
			}).start();
			
			while (Utils.isOpen(connection) && !proxy2client.isClosed()) {
				
				connection.setSoTimeout(2000);
				while (Utils.isOpen(connection)) {
					try {
						connection.getInputStream().read(new byte[Utils.CLOSE_PACKET.length]);
					} catch (SocketTimeoutException ex) {
						break;
					} catch (Exception ex) {
						ex.printStackTrace();
						break;
					}
				}
				
				connection.setSoTimeout(0);
				
				System.out.println("Ready");
				Socket player;
				try {
					player = proxy2client.accept();
				} catch (Exception ex) {
					if (!proxy2client.isClosed()) {
						connection.getOutputStream().write(Utils.CLOSE_PACKET);
						connection.getOutputStream().flush();
					}
					continue;
				}
				System.out.println("Player joining");
				PacketExchange p = new PacketExchange(player);
				p.exchangePacketsUntilClosed(connection);
				System.out.println("Sent close packet");
			}
		} else {
			System.out.println("Received connection from player");
			byte[] packet = new byte[2 * 1024 * 1024];
			while (Utils.isOpen(connection)) {
				int r = connection.getInputStream().read(packet);
				if (r < 0) break;
				if (r == Utils.CLOSE_PACKET.length) {
					connection.getOutputStream().write(Utils.CLOSE_PACKET);
					connection.getOutputStream().flush();
					System.out.println("Echoed close packet");
					continue;
				}
				Socket proxy2server = new Socket();
				proxy2server.bind(new InetSocketAddress("127.0.0.1", 0));
				proxy2server.connect(new InetSocketAddress("127.0.0.1", 25565));
				proxy2server.getOutputStream().write(packet, 0, r);
				proxy2server.getOutputStream().flush();
				PacketExchange p = new PacketExchange(proxy2server);
				p.exchangePacketsUntilClosed(connection);
				System.out.println("Sent close packet");
			}
		}
	}

}
