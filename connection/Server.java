package connection;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;

public class Server
{ 
	class ServerThread implements Runnable
	{
		private Socket cliente;
		private PrintStream ps;
		private String nickname;
		
		public ServerThread(Socket cli, PrintStream p)
		{
			cliente = cli;
			ps = p;
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
			
			do
			{
				ps.println("Informe um nickname: ");
				if(in.hasNextLine())
				{
					nickname = in.nextLine();
				}
			} while(_nicknames.containsValue(nickname));
			
			_nicknames.put(1, nickname);
			
			while (in.hasNextLine()) 
			{
				String msg = in.nextLine();
				System.out.println(nickname + ": " + msg);
				distribuiMensagem(msg, nickname);
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
		
		public PrintStream GetPrintStream()
		{
			return ps;
		}
		
		public String GetNickname()
		{
			return nickname;
		}
	}
	
	HashMap<Integer, String> _nicknames;
	List<ServerThread> _serverThreads;
	
	public static void main(String args[]) throws IOException 
	{
		new Server();
	}
	
	public Server() 
	{
		_nicknames = new HashMap<Integer, String>();
		_serverThreads = new ArrayList<ServerThread>();
		
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
						
			ServerThread st = null;
			try {
				st = new ServerThread(cliente, new PrintStream(cliente.getOutputStream()));
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			_serverThreads.add(st);
			new Thread(st).start();
		} while (!_serverThreads.isEmpty());
		
		try {
			servidor.close();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("O servidor terminou de executar!");
	}
	
	void distribuiMensagem(String msg, String nick) 
	{
		for(ServerThread client : _serverThreads) 
		{
			if(client.GetNickname() != nick)
			{
				String str = nick + ": " + msg;
				client.GetPrintStream().println(str);
			}
		}
	}
}
