package multiplayer.possible;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUI {
	
	private String title;
	
	private boolean printMessages, disabled;
	
	public GUI(String title) {
		this.title = title;
	}
	
	public void printMessages() {
		printMessages = true;
	}
	
	public void disable() {
		disabled = true;
	}
	
	public String prompt(String question, String... options) {
		if (printMessages) System.out.println(question);
		if (disabled) return null;
		String init = (options.length == 1) ? options[0] : null;
		String[] o = (options.length > 1) ? options : null;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String result = (String)JOptionPane.showInputDialog(frame, question, title, JOptionPane.QUESTION_MESSAGE, null, o, init);
		frame.dispose();
		if (result == null) System.exit(0);
		result = result.trim();
		return result;
	}
	
	public boolean confirm(String question) {
		if (printMessages) System.out.println(question);
		if (disabled) return true;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int result = JOptionPane.showConfirmDialog(frame, question, title, JOptionPane.OK_CANCEL_OPTION);
		frame.dispose();
		if (result != 0) {
			System.exit(0);
			return false;
		}
		return true;
	}
	
	public void info(String message) {
		if (printMessages) System.out.println(message);
		if (disabled) return;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
		frame.dispose();
	}

}
