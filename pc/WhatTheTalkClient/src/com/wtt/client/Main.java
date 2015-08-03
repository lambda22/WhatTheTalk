package com.wtt.client;

import java.io.IOException;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(System.in)) {
			String svrAddress = "127.0.0.1";
			int port = 5566;

			if (args.length > 0)
				svrAddress = args[0];
			if (args.length > 1)
				port = Integer.parseInt(args[1]);
			
			MessageClient msgClient = new MessageClient(svrAddress, port);
			msgClient.start();
			
			String input = "";
			do {
				System.out.print("wttc> ");
				input = scanner.nextLine();
				
				if (!input.equals("exit"))
					msgClient.sendMessage(input);
				
			} while ( !input.equals("exit") && msgClient.isAlive());
			
			msgClient.disconnect();
		}
		catch (NumberFormatException e) {
			System.err.println("Invalid port number: " + args[1]);
		}
		catch (IOException e) {
			System.err.println("Open socket error: " + e.getMessage());
		}
	}
}
