package connection;
import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintStream;
import java.net.*;
import java.util.List;
import java.util.Scanner;

public class Server
{
	List<PrintStream> _clientList;
	
	public static void main(String args[]) throws IOException 
	{
		ServerSocket servidor = new ServerSocket(5000);
		System.out.println("Porta 5000 aberta!");
		
		
		//while(true) pega cliente e dispara thread
		Socket cliente = servidor.accept();
		
		//armazena clientes numa lista this.lista.add(new PrintStream(cliente.getOutputStream()));
		
		System.out.println("Nova conexão com o cliente " + cliente.getInetAddress().getHostAddress());
		
		Scanner in = new Scanner(cliente.getInputStream());
		while (in.hasNextLine()) 
		{
			System.out.println(in.nextLine());
		}
		
		in.close();
		cliente.close();
		servidor.close();
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
