package multiplayer.possible;

import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.Socket;
import java.util.Random;
import java.util.Scanner;

@SuppressWarnings("resource")
public class SimplerExample {
	
	static String friends_ip_here = "127.0.0.1";

	static void run() throws Exception {
		
		Scanner sc = new Scanner(System.in);
		
		System.out.print("Enter your friend's public ip address here: ");
		
		InetAddress friend = InetAddress.getByName(sc.nextLine());
		int myPort = 40000 + new Random().nextInt(20000);
		Socket s = new Socket();
		s.setReuseAddress(true);
		
		s.bind(new InetSocketAddress("0.0.0.0", myPort));
		System.out.println("Your port is " + myPort);
		
		System.out.print("Enter your friend's port here: ");
		int friendPort = sc.nextInt();
		
		System.out.println("Press enter to start");
		
		sc.nextLine();
		
		
		for (int i = 0; i < 10; i++) {
			long sync = System.currentTimeMillis() % 10000;
			Thread.sleep(10000 - sync);
			System.out.println("Connecting...");
			try {
				s.connect(new InetSocketAddress(friend, friendPort), 5000);
			} catch (Exception ex) {}
			if (s.isConnected()) break;
		}
		
		if (!s.isConnected()) {
			System.out.println("Failed to establish a connection after 10 attempts");
		} else {
			System.out.println("Successfully established connection");
		}
	}

}
