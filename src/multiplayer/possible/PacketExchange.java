package multiplayer.possible;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;

public class PacketExchange {
	
	private Socket socket;
	
	public PacketExchange(Socket socket) {
		this.socket = socket;
	}
	
	public void exchangePacketsUntilClosed(Socket connection) {
		try {
			_exchangePacketsUntilClosed(connection);
		} catch (Exception ex) {
			ex.printStackTrace();
		}
		try {
			socket.close();
		} catch (Exception ex2) {}
		try {
			if (Utils.isOpen(connection)) {
				connection.getOutputStream().write(Utils.CLOSE_PACKET);
				connection.getOutputStream().flush();
			}
		} catch (Exception ex) {
			ex.printStackTrace();
		}
	}
	
	private void _exchangePacketsUntilClosed(Socket connection) throws Exception {
		
		InputStream i1 = socket.getInputStream();
		InputStream i2 = connection.getInputStream();
		OutputStream o1 = socket.getOutputStream();
		OutputStream o2 = connection.getOutputStream();
		
		byte[] up = new byte[2 * 1024 * 1024];
		byte[] down = new byte[2 * 1024 * 1024];
		
		new Thread(() -> {
			while (Utils.isOpen(socket) && Utils.isOpen(connection)) {
				try {
					int r = i2.read(down);
					if (r == Utils.CLOSE_PACKET.length) System.out.println("Found close packet");
					if (r <= Utils.CLOSE_PACKET.length) break;
					System.out.println("UP: " + r);
					o1.write(down, 0, r);
					o1.flush();
				} catch (Exception ex) {
					break;
				}
			}
			try {
				socket.close();
			} catch (Exception ex) {}
		}).start();
		
		
		try {
			while (Utils.isOpen(socket) && Utils.isOpen(connection)) {
				int r = i1.read(up);
				if (r == Utils.CLOSE_PACKET.length) System.out.println("Found close packet... oops");
				if (r <= Utils.CLOSE_PACKET.length) break;
				System.out.println("DOWN: " + r);
				o2.write(up, 0, r);
				o2.flush();
			}
		} finally {
			socket.close();
		}
	}

}
