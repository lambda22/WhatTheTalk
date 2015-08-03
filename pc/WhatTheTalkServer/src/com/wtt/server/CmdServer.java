package com.wtt.server;

import java.io.IOException;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.ArrayList;

import com.wtt.io.CmdObject;

public class CmdServer {
	private ServerSocket svrSocket;
	private ArrayList<CmdAgent> cmdAgentlist;
	
	public CmdServer(int port) throws IOException {
		svrSocket = new ServerSocket(port);
		cmdAgentlist = new ArrayList<CmdAgent>();
		System.out.println("WhatTheTalk is open on port " + svrSocket.getLocalPort() + ".");
	}
	
	public void start() {
		new Thread(mWaitClientRun).start();
	}
	
	private Runnable mWaitClientRun = new Runnable() {
		@Override
		public void run() {
			while (true) {
				try {
					Socket agentSocket = svrSocket.accept();
					CmdAgent msgAgent = new CmdAgent(agentSocket, CmdServer.this);
					synchronized (cmdAgentlist) {
						cmdAgentlist.add(msgAgent);
					}
					msgAgent.start();
				}
				catch (IOException e) {
					if (e.getMessage().equals("CommandAgent create stream error."))
						continue;
					else {
						System.out.println("WhatTheTalk closed.");
						break;
					}
				}
			}
		}
	};
	
	public void removeAgent(CmdAgent leaver) {
		synchronized (cmdAgentlist) {
			cmdAgentlist.remove(leaver);
		}		
	}
	
	public void boradcast(CmdAgent sender, CmdObject cmdObj) {
		synchronized (cmdAgentlist) {
			for (CmdAgent cmdAgent : cmdAgentlist) {
				if (cmdAgent == sender)
					continue;
				cmdAgent.sendCmd(cmdObj);
			}
		}
	}
	
	public void disconnect() {
		try {
			svrSocket.close();
			for (CmdAgent item : cmdAgentlist) {
				item.disconnect();
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
