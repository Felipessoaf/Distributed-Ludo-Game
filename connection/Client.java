package connection;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client
{
	class ClientThread implements Runnable
	{
		private Socket _socket;
		
		public ClientThread(Socket socket)
		{
			_socket = socket;
		}
		
		public void run()
		{
			Scanner in_serv;
			try {
				in_serv = new Scanner(this._socket.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
				return;
			}
			
			while (in_serv.hasNextLine()) 
			{
				System.out.println(in_serv.nextLine());
			}
			
			in_serv.close();
		}
	}
	
	private Scanner _serverScanner;
	private Socket _socket;
	private boolean _canPlay;
	
	public PrintStream ps;
	
	public Client()
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
		
		new Thread(new ClientThread(_socket)).start();
		Execute();
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		new Client();
	}
	
	void Execute()
	{
		Scanner teclado = new Scanner(System.in);
		PrintStream saida = null;
		try {
			saida = new PrintStream(_socket.getOutputStream());
		} catch (IOException e1) {
			// TODO Auto-generated catch block
			e1.printStackTrace();
		}

		while(!_canPlay){}
		
		String msg = teclado.nextLine();
		while(msg.compareTo("###")!=0) 
		{
			saida.println(msg);
			msg = teclado.nextLine();
		}		

		saida.close();
		teclado.close();
		try {
			_socket.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("O cliente terminou de executar!");
	}
}