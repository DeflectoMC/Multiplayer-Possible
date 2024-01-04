package multiplayer.possible;

import java.net.InetSocketAddress;
import java.net.Socket;

public class Connection {
	
	private Socket socket = new Socket();
	
	public void bind(int localPort) throws Exception {
		try {
			//socket.setReceiveBufferSize(2 * 1024 * 1024);
			//socket.setSendBufferSize(2 * 1024 * 1024);
			//socket.setSoTimeout(15_000);
			socket.setReuseAddress(true);
			socket.bind(new InetSocketAddress(localPort));
		} catch (Exception ex) {
			Utils.close(socket);
			System.err.println("Failed to bind to port: " + ex);
			throw ex;
		}
	}
	
	public void close() {
		Utils.close(this.socket);
	}
	
	public boolean isClosed() {
		return Utils.isClosed(this.socket);
	}
	
	public boolean establish(InetSocketAddress remote, int maxAttempts, long interval) {
		System.out.println("Establishing connection...");
		int i;
		for (i = 0; i < maxAttempts; i++) {
			long sync = System.currentTimeMillis() % interval;
			try {
				Thread.sleep(interval - sync);
			} catch (InterruptedException ex) {
				System.err.println("Interrupted");
				break;
			}
			System.out.println("Connecting");
			try {
				socket.connect(remote, 5000);
			} catch (Exception ex) {}
			if (!socket.isConnected()) {
				System.out.println("Connection attempted (" + (i+1) + ")");
			} else break;
		}
		
		if (socket.isConnected()) {
			System.out.println("Connection established after " + i + " attempts");
			return true;
		}
		Utils.close(socket);
		System.out.println("Failed to establish connection after " + i + " attempts");
		return false;
	}
	
	public int read(byte[] b) {
		return Utils.read(this.socket, b);
	}
	
	public int read(byte[] b, int off, int len) {
		return Utils.read(this.socket, b, off, len);
	}
	
	public boolean write(int b) {
		return Utils.write(this.socket, b);
	}
	
	public boolean write(byte[] b) {
		return Utils.write(this.socket, b);
	}
	
	public boolean write(byte[] b, int off, int len) {
		return Utils.write(this.socket, b, off, len);
	}
	
	
	public boolean readOpen() {
		byte[] b = new byte[1];
		int read = this.read(b);
		return read == 1 && b[0] == 35;
	}
	
	public boolean readClose() {
		byte[] b = new byte[1];
		int read = this.read(b);
		return read == 1 && b[0] == 55;
	}
	
	public boolean writeOpen() {
		return this.write(35);
	}
	
	public boolean writeClose() {
		return this.write(55);
	}
	
	public int readRegular(byte[] b) {
		int read = this.read(b);
		if (read <= 1) return -1;
		return read;
	}
	

}
