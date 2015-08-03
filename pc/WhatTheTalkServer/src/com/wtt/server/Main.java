package com.wtt.server;

import java.io.IOException;
import java.util.Scanner;

public class Main {
	
	public static void main(String[] args) {
		try (Scanner scanner = new Scanner(System.in)) {
			int port = 5566;
			if (args.length > 0)
				port = Integer.parseInt(args[0]); //throws NumberFormatException
			
			CmdServer cmdServer = new CmdServer(port); //throws IOException
			cmdServer.start();
			
			String input;
			do {
				System.out.print("wtts> ");
				input = scanner.nextLine();
			} while (!input.equals("exit"));
			
			cmdServer.disconnect();
		}
		catch (NumberFormatException e) {
			System.err.println("Invalid port number: " + args[0]);
		}
		catch (IOException e) {
			System.err.println("Open socket error: " + e.getMessage());
		}
	}
}
