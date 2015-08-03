package com.wtt.server;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.net.Socket;
import java.net.SocketAddress;

import com.wtt.io.C;
import com.wtt.io.CmdObject;
import com.wtt.io.CmdReader;
import com.wtt.io.CmdWriter;

public class CmdAgent {
	private Socket mSocket;
	private SocketAddress mClientAddress;
	private CmdServer mCmdServer;
	private BufferedWriter mBufWriter;
	private CmdReader mCmdReader;
	private CmdWriter mCmdWriter;
	private String mID = "";
		
	public CmdAgent(Socket socket, CmdServer cmdServer) throws IOException {
		try {
			mSocket = socket;
			mClientAddress = socket.getRemoteSocketAddress();
			mCmdServer = cmdServer;
			
			mCmdReader = new CmdReader(mSocket.getInputStream());
			mCmdWriter = new CmdWriter(mSocket.getOutputStream());
			mBufWriter = new BufferedWriter(new OutputStreamWriter(mSocket.getOutputStream(), "ISO-8859-1"));
			
			sendTextLn("Welcome to WhatTheTalk. Please login first!");
			System.out.println("\nClient connected! (" + mClientAddress + ").");
		}
		catch (IOException e) {
			if (mSocket != null && mSocket.isConnected())
				mSocket.close();
			throw new IOException("CommandAgent create stream error.");
		}
	}

	public void start() {
		new Thread(mRecvCmdRun).start();
	}
	
	private Runnable mRecvCmdRun = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					CmdObject cmdObj = mCmdReader.readCmd();
					if (cmdObj == null) { continue; }
					
					if (mID.isEmpty()) {
						if (!cmdObj.getAction().equals(C.CmdAction.login) || cmdObj.getID().isEmpty()) {
							System.out.println("Log in failed.");
							cmdObj.setStatus(400);
						}
						else {
							System.out.println(cmdObj.getID() + " logged in!");
							mID = cmdObj.getID();
							cmdObj.setStatus(200);
						}
						sendCmd(cmdObj);
						continue;
					}
					
					switch (cmdObj.getAction()) {
					case C.CmdAction.chat:
						if (cmdObj.getContentType().equals(C.CmdContent.text))
							System.out.println("User@" + mClientAddress + " says: " + cmdObj.getContentInText());
						cmdObj.setFrom(mID);
						mCmdServer.boradcast(CmdAgent.this, cmdObj);
						break;
					case C.CmdAction.echo:
						if (cmdObj.getContentType().equals(C.CmdContent.text))
							System.out.println("User@" + mClientAddress + " echos: " + cmdObj.getContentInText());
						sendCmd(cmdObj);
						break;
					}
				}
			} catch (IOException e) {
				System.out.println("Client disconnected! (" + mClientAddress + ")");
				mCmdServer.removeAgent(CmdAgent.this);
				disconnect();
			}
		}
	};

	public void sendTextLn(String text)
	{
		try {
			mBufWriter.write(text);
			if (!text.endsWith("\r\n"))
				mBufWriter.write("\r\n");
			mBufWriter.flush();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void sendCmd(CmdObject cmdObj) {
		try {
			mCmdWriter.writeCmd(cmdObj);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public void disconnect() {
		try {
			if (mSocket.isConnected())
				mSocket.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
