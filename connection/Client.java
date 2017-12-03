package connection;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;
import java.util.regex.Pattern;

import interfacejogo.*;
import regras.Game;
import regras.GameFacade;

public class Client
{
	public class ClientThread implements Runnable
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
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}

//			System.out.println("Esperando msg do server");
			while (in_serv.hasNextLine()) 
			{
				msg = in_serv.nextLine();
//				System.out.println("Peguei msg do server");
				if(Pattern.matches(msg, "Informe um nickname: "))
				{
					Nickname();
				} 
				else if(msg.matches("Start \\d"))//Pattern.matches(msg, "Start \\d"))
				{
					int num = Integer.parseInt(msg.split(" ")[1]);
					System.out.println("Começou o jogo");
					GameFacade.GetJogoFacade().StartGame(num, this);
				} 
				else if(Pattern.matches(msg, "Turno"))
				{
//					System.out.println("meu turno");
					_canPlay = true;
				} 
				else if(Pattern.matches(msg, "Desconectar"))
				{
					_canPlay = false;
					End();
					break;
				}
				else if((msg.matches("Board ((\\d)+,)+")) || (msg.matches("Board ((\\d+),)+")) || (msg.matches("Board (\\d+,)+")))//Pattern.matches(msg, "Board (\\w+)"))
				{
					//TODO: pegar string board
//					System.out.println("client board");
					String board = msg.split(" ")[1];
					GameFacade.GetJogoFacade().UpdateBoardIn(board);
					break;
				}
				else
				{
					System.out.println("Default: " + msg + "lenght: " + msg.length());
				}
//				System.out.println("Esperando msg do server");
			}
			
			in_serv.close();
		}
		
		public void UpdateBoardOut(String board)
		{
			System.out.println("Update Board Out");
			System.out.println("Updated Board " + board);
			_saida.println("Board " + board);
//			_saida.println("FimTurno");
		}
	}
	
	private Scanner _serverScanner;
	private Socket _socket;
	
	private Scanner _teclado;
	private PrintStream _saida;
	
	private boolean _canPlay;
	
	public PrintStream ps;
	
	public Client()
	{
		Init();
//		_saida.println("Start");
		Turn();
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		ps = new PrintStream(System.out);
		
		new Thread(new ClientThread()).start();
		
		_teclado = new Scanner(System.in);
		_saida = null;
		try {
			_saida = new PrintStream(_socket.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}
		
		_canPlay = false;
		
		new BoardFrame();
	}
	
	void Nickname()
	{		
		System.out.println("Informe um Nickname: ");
		String msg = _teclado.nextLine();
		_saida.println(msg);
	}
	
	void Play()
	{		
		while(!_canPlay)
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			if(_socket.isClosed())
			{
				return;
			}
		}

		_saida.println("Start");
		System.out.println("Digite uma msg: ");
		String msg = _teclado.nextLine();
		while(msg.compareTo("###")!=0 && _canPlay) 
		{
			_saida.println(msg);
			msg = _teclado.nextLine();
		}
		
		End();
	}
	
	void Turn()
	{		
		while(!_canPlay)
		{
			try {
				Thread.sleep(500);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}		
			if(_socket.isClosed())
			{
				return;
			}
		}
		
		_saida.println("Start");
//		System.out.println("Digite uma msg: ");
//		String msg = _teclado.nextLine();
//		if(msg.compareTo("End")!=0 && _canPlay) 
		{
//			_saida.println(msg);
			_saida.println("FimTurno");
//			System.out.println("mandei fimturno pro server");
			_canPlay = false;
			Turn();
		}
//		else if(msg.compareTo("End")==0)
//		{
//			_canPlay = false;
//			End();	
//		}
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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		_saida.println("Desconectar");
//		System.out.println("end after _saida");
		_saida.close();
//		System.out.println("end after _saida close");

		try
		{
			_teclado.close();
		}
		catch (Exception e) {
			// TODO: handle exception
			e.printStackTrace();
		}
		
		System.out.println("end after _teclado close");
		try {
			_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("O cliente terminou de executar!");
	}
}