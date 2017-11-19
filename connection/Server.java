package connection;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;

public class Server
{ 
	class ServerThread implements Runnable
	{
		private Socket cliente;
		
		public ServerThread(Socket cli)
		{
			cliente = cli;
		}
		
		public void run()
		{
			Scanner in = null;
			try {
				in = new Scanner(cliente.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			while (in.hasNextLine()) 
			{
				String msg = in.nextLine();
				System.out.println(msg);
				distribuiMensagem(msg);
			}
			
			in.close();
			
//			try {
//				_clientList.remove(cliente.getOutputStream());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			try {
				cliente.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	
	List<PrintStream> _clientList;
	
	public static void main(String args[]) throws IOException 
	{
		new Server();
	}
	
	public Server() 
	{
		_clientList = new ArrayList<PrintStream>();
		ServerSocket servidor = null;
		try {
			servidor = new ServerSocket(5000);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("Porta 5000 aberta!");
		
		do
		{
			Socket cliente = null;
			
			try {
				cliente = servidor.accept();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			System.out.println("Nova conexão com o cliente " + cliente.getInetAddress().getHostAddress());
			
			try {
				_clientList.add(new PrintStream(cliente.getOutputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			catch (NullPointerException e) {
				// TODO: handle exception
				e.printStackTrace();
			}
			
			new Thread(new ServerThread(cliente)).start();
		} while (!_clientList.isEmpty());
		
		try {
			servidor.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("O servidor terminou de executar!");
	}
	
	void distribuiMensagem(String msg) 
	{
		for(PrintStream client : _clientList) 
		{
			client.println(msg);
		}
	}
}
