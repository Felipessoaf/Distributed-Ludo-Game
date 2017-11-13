package connection;

import java.io.IOException;
import java.io.PrintStream;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client extends Thread
{
	private Scanner _serverScanner;
	private Socket _socket;
	
	public PrintStream ps;
	
	public Client(Socket socket)
	{
		this._socket = socket;
		
		this.ps = new PrintStream(System.out);
	}
	
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		Socket cli = new Socket("127.0.0.1", 5000);
		System.out.println("O cliente se conectou ao servidor!");
		
		Scanner teclado = new Scanner(System.in);
		PrintStream saida = new PrintStream(cli.getOutputStream());
		
		String msg = teclado.nextLine();
		while(msg.compareTo("###")!=0) 
		{
			saida.println(msg);
			msg = teclado.nextLine();
		}		

		saida.close();
		teclado.close();
		cli.close();
		System.out.println("O cliente terminou de executar!");
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
	
	public void println(String msg)
	{
		
	}
}