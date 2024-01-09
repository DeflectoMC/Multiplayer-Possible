package multiplayer.possible;

import java.awt.Desktop;
import java.awt.Desktop.Action;
import java.io.File;
import java.net.InetAddress;
import java.net.URI;
import java.util.concurrent.CompletableFuture;

public class Main {

	public static void main(String[] args) throws Exception {
		File f = new File(Main.class.getProtectionDomain().getCodeSource().getLocation().toURI());
		f = f.getParentFile();
		f = new File(f, "Multiplayer-Possible");
		if (!f.exists()) f.mkdirs();

		GUI gui = new GUI("Multiplayer-Possible");

		File g = null;
		for (File f2 : f.listFiles()) {
			if (f2.getName().startsWith("gole")) {
				if (f2.getName().contains(".zip") || f2.getName().contains(".tar")) {
					gui.info(f2.getName() + " is a compressed file and not the actual program.\nYou'll need to extract it into your Multiplayer-Possible folder");
					if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.OPEN)) {
						Desktop.getDesktop().open(f);
					}
					return;
				}
				f2.setExecutable(true);
				g = f2;
				break;
			}
		}

		if (g == null || !g.exists()) {

			String choice = gui.prompt("Welcome to Multiplayer-Possible!\nThis version uses a program called Gole\nto allow you to connect reliably with your friends without any issues.\nAs you're running this for the first time,\nyou'll need a version of Gole to be placed into your Multiplayer-Possible folder.", "Show official homepage", "Show official downloads");
			String url = choice.contains("homepage") ? "https://github.com/shawwwn/Gole" : "https://github.com/shawwwn/Gole/releases";

			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.BROWSE)) {
				Desktop.getDesktop().browse(new URI(url));
				Thread.sleep(500l);
			} else {
				gui.info("Your operating system does not support Java opening a link for you.\nThe link you need to go to is " + url);
				return;
			}

			if (Desktop.isDesktopSupported() && Desktop.getDesktop().isSupported(Action.OPEN)) {
				Desktop.getDesktop().open(f);
				Thread.sleep(500l);
			}
			return;
		}

		String choice = gui.prompt("Select an option", "Host", "Join");
		boolean isClient = choice.equals("Join");
		String peerName = isClient ? "server" : "client";
		String edition = gui.prompt("Select a game version", "Minecraft: Java Edition", "Minecraft: Bedrock Edition", "A different game");

		String protocol = edition.contains("Java") ? "tcp" : "udp";
		int gamePort = edition.contains("Java") ? 25565 : 19132;

		if (edition.contains("different")) {
			protocol = gui.prompt("Does this game use TCP or UDP?", "TCP", "UDP").toLowerCase();

			String s;

			if (!isClient) {
				s = gui.prompt("Enter the port that your server is running on");
			} else {
				s = gui.prompt("Enter the port that you will join the game from");
			}
			gamePort = -1;
			try {
				gamePort = Integer.parseInt(s);
			} catch (Exception ex) {}
			if (gamePort < 0 || gamePort > 65535) {
				gui.info("Invalid port: " + s + ". Should be a positive integer less than 65535");
				return;
			}
		} else if (isClient) {
			gamePort = protocol.contains("tcp") ? 35585 : 39332;
		}

		String addr = gui.prompt("Enter the " + peerName + "'s public ip address");
		try {
			InetAddress.getByName(addr);
		} catch (Exception ex) {
			gui.info("Couldn't parse IP address \"" + addr + "\"");
			return;
		}

		String nickname = (isClient) ? gui.prompt("Enter a nickname") : gui.prompt("Enter the client's nickname");

		int port = 40_000 + (nickname.hashCode() % 20_000);
		int port1 = isClient ? port : port+1;
		int port2 = isClient ? port+1 : port;

		CompletableFuture<Void> future = Executor.execute(g, protocol, addr, port1, port2, isClient, gamePort);

		long wait = System.currentTimeMillis() + (150000);
		while (!future.isDone() && System.currentTimeMillis() <= wait) {
			Thread.sleep(1000l);
		}

		if (!future.isDone()) {
			future.cancel(true);
			gui.info("Failed to connect after 2 minutes and 30 seconds");
			System.exit(0);
			return;
		}

		if (isClient) {
			gui.info("Connection established!\n\nWaiting for you to join @ 127.0.0.1:" + gamePort);
		} else {
			gui.info("Connection established!\nWaiting for the player to join");
		}

		gui.info("Press Ok to quit the program and close your connection");
		System.exit(0);


	}
}
