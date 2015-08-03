package com.wtt.client;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintStream;
import java.net.Socket;

public class MessageClient extends Thread {
	private Socket mSocket;
	private BufferedReader mBufReader;
	private PrintStream mPrintStream;
	
	public MessageClient(String svrAddress, int svrPort) throws IOException {
		try {
			mSocket = new Socket(svrAddress, svrPort);
			mBufReader = new BufferedReader(new InputStreamReader(mSocket.getInputStream()));
			mPrintStream = new PrintStream(mSocket.getOutputStream());
			System.out.println("Connected to WhatTheTalk~");
		}
		catch (IOException e) {
			if (mSocket != null && mSocket.isConnected())
				mSocket.close();
			throw e;
		}
	}
	
	public void sendMessage(String msg) {
		mPrintStream.print(msg + "\r\n");
		mPrintStream.flush();
	}
	
	public void disconnect() {
		try {
			if (mSocket.isConnected())
				mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void run() {
		try {
			while (true) {
				String msg = mBufReader.readLine();
				if (msg == null) {
					throw new IOException("Null message!");
				}
				System.out.println("someone says: " + msg);
			}
		}
		catch (IOException e) {
			System.out.println("\nByebye, WhatTheTalk~");
			disconnect();
		}
	}
}
