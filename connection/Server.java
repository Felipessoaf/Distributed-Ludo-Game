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
	class Room
	{
		public int RoomId;
		private List<ServerThread> _players;
		
		public Room(int id)
		{
			RoomId = id;
			_players = new ArrayList<ServerThread>();
		}
		
		public boolean AddPlayer(ServerThread st)
		{
			if(_players.size() == 4)
			{
				return false;
			}
			
			_players.add(st);	
			
			if(_players.size() == 4)
			{
				//start game
				/*for(ServerThread client : _players) 
				{
					String str = "Room: " + RoomId;
					client.GetPrintStream().println(str);
				}*/
				new Thread(new Game(RoomId, _players)).start();
			}
			return true;
		}
	}
	
	class Game implements Runnable
	{
		public int GameId;
		private List<ServerThread> _players;
		
		Game(int id, List<ServerThread> players)
		{
			_players = players;
			GameId = id;
		}
		
		public void run() 
		{
			//start game
			for(ServerThread client : _players) 
			{
				String str = "Start. Room: " + GameId;
				client.GetPrintStream().println(str);
			}
			
			System.out.println("Game " + GameId + " started");
			
			/*while(true)
			{
				
			}*/
			
		}
		
	}
	
	class ServerThread implements Runnable
	{
		private Socket _cliente;
		private PrintStream _ps;
		private String _nickname;
		private boolean _ready;
		
		public ServerThread(Socket cli, PrintStream p)
		{
			_cliente = cli;
			_ps = p;
			_ready = false;
		}
		
		public void run()
		{
			Scanner in = null;
			try {
				in = new Scanner(_cliente.getInputStream());
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			
			do
			{
				_ps.println("Informe um nickname: ");
				if(in.hasNextLine())
				{
					_nickname = in.nextLine();
				}
			} while(_nicknames.containsValue(_nickname));
			
//			if(in.hasNextLine())
//			{
				_nicknames.put(_nicknames.size(), _nickname);
				if(_rooms.isEmpty() || !_rooms.get(_rooms.size()-1).AddPlayer(this))
				{
					_rooms.add(new Room(_rooms.size()));
					_rooms.get(_rooms.size()-1).AddPlayer(this);
				}
				
				while (in.hasNextLine()) 
				{
					String msg = in.nextLine();
					System.out.println(_nickname + ": " + msg);
					distribuiMensagem(msg, _nickname);
				}
			//}
			
			in.close();
			
//			try {
//				_clientList.remove(cliente.getOutputStream());
//			} catch (IOException e) {
//				// TODO Auto-generated catch block
//				e.printStackTrace();
//			}
			
			try {
				_cliente.close();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		
		public PrintStream GetPrintStream()
		{
			return _ps;
		}
		
		public String GetNickname()
		{
			return _nickname;
		}
	}
	
	
	HashMap<Integer, String> _nicknames;
	List<ServerThread> _serverThreads;
	List<Room> _rooms;
	
	public static void main(String args[]) throws IOException 
	{
		new Server();
	}
	
	public Server() 
	{
		_nicknames = new HashMap<Integer, String>();
		_serverThreads = new ArrayList<ServerThread>();
		_rooms = new ArrayList<Room>();
		
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
