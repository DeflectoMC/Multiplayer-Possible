package multiplayer.possible;

import javax.swing.JFrame;
import javax.swing.JOptionPane;

public class GUI {
	
	private String title;
	
	private boolean printMessages;
	
	public GUI(String title) {
		this.title = title;
	}
	
	public void printMessages() {
		printMessages = true;
	}
	
	public String prompt(String question, String... options) {
		if (printMessages) System.out.println(question);
		String init = (options.length == 1) ? options[0] : null;
		String[] o = (options.length > 1) ? options : null;
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		String result = (String)JOptionPane.showInputDialog(frame, question, title, JOptionPane.QUESTION_MESSAGE, null, o, init);
		frame.dispose();
		if (result != null) result = result.trim();
		return result;
	}
	
	public boolean confirm(String question) {
		if (printMessages) System.out.println(question);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		int result = JOptionPane.showConfirmDialog(frame, question, title, JOptionPane.OK_CANCEL_OPTION);
		frame.dispose();
		return result == 0;
	}
	
	public void info(String message) {
		if (printMessages) System.out.println(message);
		JFrame frame = new JFrame();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		JOptionPane.showMessageDialog(frame, message, title, JOptionPane.INFORMATION_MESSAGE);
		frame.dispose();
	}

}
