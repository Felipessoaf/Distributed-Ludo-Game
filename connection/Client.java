package connection;

import java.awt.event.WindowEvent;
import java.awt.event.WindowListener;
import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Pattern;

import connectionview.ClientView;
import interfacejogo.*;
import regras.GameFacade;

public class Client
{
	public class ClientThread implements Runnable, WindowListener
	{			
		public ClientThread()
		{
		}
		
		public void run()
		{
			Scanner in_serv;
			String msg;
			try {
				in_serv = new Scanner(_socket.getInputStream());
			} catch (IOException e) {
				e.printStackTrace();
				return;
			}

			System.out.println("Esperando msg do server");
			while (in_serv.hasNextLine()) 
			{
				msg = in_serv.nextLine();
				System.out.println("Peguei msg do server");
				if(Pattern.matches(msg, "Informe um nickname: "))
				{
					Nickname();
				} 
				else if(msg.matches("Start \\d"))
				{
					_boardFrame = new BoardFrame();

					_boardFrame.addWindowListener(this);
					
					int num = Integer.parseInt(msg.split(" ")[1]);
					System.out.println("Começou o jogo");
					GameFacade.GetJogoFacade().StartGame(num, this);
					_saida.println("Start");
				} 
				else if(Pattern.matches(msg, "Turno"))
				{
					System.out.println("meu turno");
					GameFacade.GetJogoFacade().SetLancarDadoEnabled(true);
				} 
				else if(Pattern.matches(msg, "Desconectar"))
				{
					End();
					break;
				}
				else if((msg.matches("Board ((\\d)+,)+")) || (msg.matches("Board ((\\d+),)+")) || (msg.matches("Board (\\d+,)+")))
				{
					System.out.println("client board");
					String board = msg.split(" ")[1];
					GameFacade.GetJogoFacade().UpdateBoardIn(board);
				}
				else
				{
					System.out.println("Default: " + msg + "lenght: " + msg.length());
				}
				System.out.println("Esperando msg do server");
			}
			
			in_serv.close();
		}
		
		public void UpdateBoardOut(String board)
		{
			System.out.println("Update Board Out");
			System.out.println("Updated Board " + board);
			_saida.println("FimTurno");
			_saida.println("Board " + board);
		}

		@Override
		public void windowActivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosed(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowClosing(WindowEvent e) 
		{
			_saida.println("CloseWindow");
		}

		@Override
		public void windowDeactivated(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowDeiconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowIconified(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}

		@Override
		public void windowOpened(WindowEvent e) {
			// TODO Auto-generated method stub
			
		}
	}
	
	/////////////////////////////////////////////////////////////////////////////////////
	
	private Socket _socket;
	
	private Scanner _teclado;
	private PrintStream _saida;
	
	private BoardFrame _boardFrame;
	
	public PrintStream ps;
			
	public Client()
	{
		Init();
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		new Client();
	}
	
	void Init()
	{
		try {
			_socket = new Socket("127.0.0.1", 5000);
			System.out.println("O cliente se conectou ao servidor!");
		} catch (UnknownHostException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		ps = new PrintStream(System.out);
		
		_teclado = new Scanner(System.in);
		_saida = null;
		try {
			_saida = new PrintStream(_socket.getOutputStream());
		} catch (IOException e1) {
			e1.printStackTrace();
		}
		
		new Thread(new ClientThread()).start();
	}
	
	void Nickname()
	{	
		ClientView nicknameView = new ClientView();
		
		while(nicknameView.getNickname().isEmpty())
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		String nickname = nicknameView.getNickname();
		nicknameView.exit();
		System.out.println(nickname);
		_saida.println(nickname);
	}
	
	void End()
	{		
		if(_socket.isClosed())
		{
			return;
		}
		
		try {
			Thread.sleep(500);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		_saida.println("Desconectar");
		_saida.close();

		try
		{
			_teclado.close();
		}
		catch (Exception e) {
			e.printStackTrace();
		}
		
		try {
			_socket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		
		System.out.println("O cliente terminou de executar!");
		_boardFrame.dispatchEvent(new WindowEvent(_boardFrame, WindowEvent.WINDOW_CLOSING));
	}
}