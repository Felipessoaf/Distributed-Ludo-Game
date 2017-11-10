import java.net.*;
import java.io.*;
import java.util.*;

public class Cliente 
{
	public static void main(String[] args) throws UnknownHostException, IOException 
	{
		Socket cli = new Socket("127.0.0.1", 12345);
		System.out.println("O cliente se conectou ao servidor!");
		
		Scanner teclado = new Scanner(System.in);
		PrintStream saida = new PrintStream(cli.getOutputStream());
		
		String msg = teclado.nextLine();
		while(msg.compareTo("###")!=0) 
		{
			saida.println(msg);
			msg = teclado.nextLine();
		}
		
		//rodar em outra thread
//		Scanner in_serv = new Scanner(cli.getInputStream());
//		while (in_serv.hasNextLine()) 
//		{
//			System.out.println(in_serv.nextLine());
//		}

		saida.close();
		teclado.close();
		cli.close();
		System.out.println("O cliente terminou de executar!");
	}
}