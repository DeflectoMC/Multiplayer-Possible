package multiplayer.possible;

import java.io.InputStream;
import java.io.OutputStream;
import java.net.ServerSocket;
import java.net.Socket;

public class Utils {
	
	public static final byte[] CLOSE_PACKET = new byte[1];
	
	public static void close(ServerSocket serverSocket) {
		try {
			serverSocket.close();
		} catch (Exception ex) {
			System.err.println("Exception while closing server socket " + serverSocket.getLocalPort() + ": " + ex);
		}
	}
	
	public static void close(Socket socket) {
		try {
			socket.close();
		} catch (Exception ex) {
			System.err.println("Exception while closing socket " + socket.getPort() + ": " + ex);
		}
	}
	
	public static boolean isClosed(Socket socket) {
		if (socket.isClosed()) return true;
		if (socket.isInputShutdown() || socket.isOutputShutdown()) {
			close(socket);
			return true;
		}
		return false;
	}
	
	public static int read(Socket socket, byte[] b) {
		return read(socket, b, 0, b.length);
	}
	
	public static int read(Socket socket, byte[] b, int off, int len) {
		try {
			InputStream in = socket.getInputStream();
			return in.read(b, off, len);
		} catch (Exception ex) {
			if (!Utils.isClosed(socket)) ex.printStackTrace();
			return -1;
		}
	}
	
	public static boolean write(Socket socket, int b) {
		try {
			OutputStream out = socket.getOutputStream();
			out.write(b);
			out.flush();
		} catch (Exception ex) {
			if (!Utils.isClosed(socket)) ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean write(Socket socket, byte[] b) {
		return write(socket, b, 0, b.length);
	}
	
	public static boolean write(Socket socket, byte[] b, int off, int len) {
		try {
			OutputStream out = socket.getOutputStream();
			out.write(b, off, len);
			out.flush();
		} catch (Exception ex) {
			if (!Utils.isClosed(socket)) ex.printStackTrace();
			return false;
		}
		return true;
	}
	
	public static boolean isOpen(Socket s) {
		return !s.isClosed()
				&& s.isConnected()
				&& !s.isInputShutdown()
				&& !s.isOutputShutdown();
	}

}
