package connectionview;

import java.awt.Component;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JTextArea;


public class ClientView {
	JFrame _window;
	JTextArea textArea;
	String nickname = "";
	
	public ClientView() 
	{				
		_window = new JFrame("Cliente Ludo");
		
		_window.setSize(300, 80);
		_window.setLocationRelativeTo(null);
		_window.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);	
		
		_setupUI();
		
		_window.setVisible(true);
		_window.setResizable(false);
	}
	
	private void _setupUI()
	{
		JPanel panel = new JPanel();
		
		JLabel lbl = new JLabel("Nickname:");
		lbl.setAlignmentX(Component.CENTER_ALIGNMENT);
		panel.add(lbl);
		
		textArea = new JTextArea();
        textArea.setColumns(10);
        textArea.setRows(1);
        textArea.setAlignmentX(Component.CENTER_ALIGNMENT);
        textArea.setEditable(true);
		panel.add(textArea);
		
		JButton btn = new JButton("Confirmar");
		btn.setAlignmentX(Component.CENTER_ALIGNMENT);
		
		btn.addActionListener(e -> {
			nickname = textArea.getText();
		});
		
		panel.add(btn);
		
		_window.setContentPane(panel);
	}
	
	public static void main(String[] args)	{
		new ClientView();
	}
	
	public String getNickname()
	{
		return nickname;
	}
	
	public void exit()
	{
		_window.setVisible(false);
	}
}
