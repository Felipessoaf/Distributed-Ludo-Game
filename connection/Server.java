package connection;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Scanner;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.locks.ReentrantLock;
import java.util.regex.Pattern;

public class Server
{ 
	class Room
	{
		public int RoomId;
		private List<ServerThread> _players;
		private Game _game;
		
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
				_game = new Game(RoomId, _players);
				new Thread(_game).start();
			}
			return true;
		}
		
		public List<ServerThread> GetServerThreads()
		{
			return _players;
		}
		
		public void RemovePlayer(ServerThread pl)
		{
			if(_game != null)
			{
				_game.RemovePlayer(pl);
			}
			else
			{
				_players.remove(pl);
			}
		}
		
		public void EndGame()
		{
			_game.EndGame();
		}
		
		public void EndTurn()
		{
			_game.SetNextPlayer();
		}
	}
	
	class Game implements Runnable
	{
		public int GameId;
		private List<ServerThread> _players;
		private Timer _timer;
		private TimerTask _timerTask;
		private boolean _canTimeout;
		private boolean _gameRunning;
		private boolean _nextPlayer;
		private ServerThread _currentPlayer;
		private int _currentPlayerIndex;
		
		Game(int id, List<ServerThread> players)
		{
			_players = players;
			GameId = id;
			_canTimeout = true;
			_gameRunning = true;
			_nextPlayer = false;
			_currentPlayerIndex = -1;
		}
		
		void GetNextPlayer()
		{
			_currentPlayerIndex = (_currentPlayerIndex + 1)%_players.size();
			_currentPlayer = _players.get(_currentPlayerIndex);
		}
		
		void SetNextPlayer()
		{
			System.out.println("Set Next Player");
			_nextPlayer = true;
		}
		
		public void run() 
		{
			int i = 0;
			//start game
			for(ServerThread client : _players) 
			{
				String str = "Start " + i;
				client.GetPrintStream().println(str);
				i++;
			}
			
			System.out.println("Game " + GameId + " started");
			
			while(_gameRunning)
			{
				GetNextPlayer();
				
				_timer = new Timer();
				_timerTask = new TimerTask() {
					  @Override
					  public void run() {
						  if(_canTimeout)
						  {
							  EndGame();
							  System.out.println("Timeout terminando jogo");
							  _canTimeout = false;  
						  }
					  }
				};
				_timer.schedule(_timerTask, 60*1000);

				System.out.println("Current Player: " + _currentPlayerIndex);	
				_currentPlayer.GetPrintStream().println("Turno");
				while(!_nextPlayer)
				{
					try {
						Thread.sleep(500);
					} catch (InterruptedException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}

				_canTimeout = false;
				_nextPlayer = false;
				_timerTask.cancel();
				_timer.cancel();
			}
			
		}
		
		public void RemovePlayer(ServerThread pl)
		{
			int index = _players.indexOf(pl);
			System.out.println("Tentando remover player " + index);
			if(index >= 0)
			{
				System.out.println("Removendo player " + index);
				if(index <= _currentPlayerIndex)
				{
					_currentPlayerIndex--;
					if(_currentPlayerIndex < 0)
					{
						_currentPlayerIndex = _players.size()-1;
					}
				}
				_players.remove(pl);
				if(_players.size() == 1)
				{
					EndGame();
				}
			}
		}
		
		public void EndGame()
		{
			System.out.println("EndGame");
			_gameRunning = false;
			for(ServerThread st : _players)
			{
				st.GetPrintStream().println("Desconectar");
			}
		}
	}
	
	class ServerThread implements Runnable
	{
		private Socket _cliente;
		private PrintStream _ps;
		private String _nickname;
		private boolean _ready;
		private Room _room;
		private boolean _canTimeout;
		private Timer _timer;
		private TimerTask _timerTask;
		private int _roomId;
		
		public ServerThread(Socket cli, PrintStream p)
		{
			_cliente = cli;
			_ps = p;
			_ready = false;
			_canTimeout = true;
		}
		
		@Override
		public boolean equals(Object obj) 
		{
		    if (obj == null) 
		    {
		        return false;
		    }
		    if (this == obj)
		    {
				System.out.println("equals");
                return true;	
		    }
		    if (getClass() != obj.getClass()) 
		    {
		        return false;
		    }
		    final ServerThread other = (ServerThread) obj;
		    
		    if (this._nickname != other._nickname) 
		    {
		        return false;
		    }
			System.out.println("equals");
		    return true;
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
					_roomId = _rooms.size();
					_room = new Room(_roomId);
					_rooms.add(_room);
					_rooms.get(_roomId).AddPlayer(this);
					_lock.add(new ReentrantLock());
				}
				else
				{
					_roomId = _rooms.size()-1;
					_room = _rooms.get(_roomId);
				}
				
				_timer = new Timer();
				_timerTask = new TimerTask() {
					  @Override
					  public void run() {
						  if(_canTimeout)
						  {
							  _ps.println("Desconectar");
							  System.out.println("Desconectando");
							  _canTimeout = false;  
						  }
					  }
				};
				_timer.schedule(_timerTask, 2*60*1000);
							
				System.out.println("Esperando msg do cliente");
				while (in.hasNextLine()) 
				{
					String msg = in.nextLine();
					System.out.println("Peguei msg do cliente");
					if(Pattern.matches(msg, "Desconectar"))
					{
						_lock.get(_roomId).lock();
						try
						{
							distribuiMensagem("Desconectando...", _nickname, _room.GetServerThreads(), true);
							
							try {
								Thread.sleep(500);
							} catch (InterruptedException e) {
								// TODO Auto-generated catch block
								e.printStackTrace();
							}
							
							_room.RemovePlayer(this);
						}
						finally {
							_lock.get(_roomId).unlock();
						}
					}
					else if(Pattern.matches(msg, "FimTurno"))
					{
						System.out.println("Serverfimturno");
						_room.EndTurn();
					} 
					else if(Pattern.matches(msg, "Start"))
					{
						_canTimeout = false;
						_timerTask.cancel();
						_timer.cancel();
					} 
					else if(Pattern.matches(msg, "Finished"))
					{
						_ps.println("Desconectar");
					} 
					else if(Pattern.matches(msg, "CloseWindow"))
					{
						_room.EndGame();
					} 
					else if((msg.matches("Board ((\\d)+,)+")) || (msg.matches("Board ((\\d+),)+")) || (msg.matches("Board (\\d+,)+")))//Pattern.matches(msg, "Board (\\w+)"))
					{
						System.out.println("server board");
						distribuiMensagem(msg, _nickname, _room.GetServerThreads(), false);
					}
					else
					{
						System.out.println(_nickname + ": " + msg);
						distribuiMensagem(msg, _nickname, _room.GetServerThreads(), true);
					}
					System.out.println("Esperando msg do cliente");
				}
			//}

			System.out.println("Terminando serverthread");
			
			in.close();
			
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
	List<ReentrantLock> _lock;
	
	public static void main(String args[]) throws IOException 
	{
		new Server();
	}
	
	public Server() 
	{
		_nicknames = new HashMap<Integer, String>();
		_serverThreads = new ArrayList<ServerThread>();
		_rooms = new ArrayList<Room>();
		_lock = new ArrayList<ReentrantLock>();
		
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
	
	void distribuiMensagem(String msg, String nick, List<ServerThread> st, boolean useNick) 
	{
		for(ServerThread client : st) 
		{
			if(client.GetNickname() != nick)
			{
				String str = null;
				if(useNick)
				{
					str = nick + ": " + msg;
				}
				else
				{
					str = msg;
				}
				System.out.println("Distribuindo msg: " + msg);
				client.GetPrintStream().println(str);
			}
		}
	}
}
