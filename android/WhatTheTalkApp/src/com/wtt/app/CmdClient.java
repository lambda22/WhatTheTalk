package com.wtt.app;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.Socket;

import com.wtt.io.C;
import com.wtt.io.CmdObject;
import com.wtt.io.CmdReader;
import com.wtt.io.CmdWriter;

import android.os.Handler;
import android.os.Message;
import android.util.Log;

public class CmdClient {	
	private final String TAG = "wtt";
	private Socket mSocket;
	private CmdReader mCmdReader;
	private CmdPostbox mCmdPostbox;
	private VoiceRnP mVoiceRnP;
	private Handler mOsmsgHander;
	private boolean mHasLoggedIn = false;
	
	public CmdClient(String ip, int port, Handler osmsgHandler) throws IOException {
		try {
			Log.d(TAG, "Connecting to " + ip + ":" + port + ".");
			mSocket = new Socket();
			mSocket.connect(new InetSocketAddress(ip, port), 3000);
			
			mCmdReader = new CmdReader(mSocket.getInputStream());
			mCmdPostbox = new CmdPostbox(new CmdWriter(mSocket.getOutputStream()));
			mVoiceRnP = new VoiceRnP(mCmdPostbox);
			
			mOsmsgHander = osmsgHandler;
			Log.d(TAG, "Connected to WhatTheTalk~");
		}
		catch (IOException e) {
			if (mSocket != null && mSocket.isConnected())
				mSocket.close();
			throw e;
		}
	}
	
	public void start() {
		new Thread(mRecvCmdRun).start();
	}
	
	public void startVoiceRnP() {
		mVoiceRnP.start();
	}
	
	public void sendCmd(CmdObject cmdObj) {
		mCmdPostbox.postCmd(cmdObj);
		
		if (cmdObj.getContentType() == C.CmdContent.text) {
			//show the message string on screen
			Message osmsg = new Message();
			osmsg.arg1 = 0; //is received or not
			osmsg.obj = cmdObj;
			mOsmsgHander.sendMessage(osmsg);
		}
	}
	
	public boolean isConnected() {
		if (mSocket != null)
			return mSocket.isConnected();
		else
			return false;
	}
	
	public void disconnect() {
		try {
			if (mSocket.isConnected())
				mSocket.close();
			mVoiceRnP.stop();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	private Runnable mRecvCmdRun = new Runnable() {
		@Override
		public void run() {
			try {
				while (true) {
					CmdObject cmdObj = mCmdReader.readCmd();
					if (cmdObj == null) { continue; }
					
					Log.d(TAG, "cmd action: " + cmdObj.getAction());
					
					if (!mHasLoggedIn) {
						if (cmdObj.getAction().equals(C.CmdAction.login)) {
							if (cmdObj.getStatus() != 200)
								Log.i(TAG, "Hey " + cmdObj.getID() + ", your log info is wrong.");
							else {
								Log.i(TAG, "Hey " + cmdObj.getID() + ", you are logged in now.");
								mHasLoggedIn = true;
							}
						}
						continue;
					}
					
					switch (cmdObj.getAction()) {
					case C.CmdAction.chat:
					case C.CmdAction.echo:
						if (cmdObj.getContentType().equals(C.CmdContent.text)) {
							Message osmsg = new Message();
							osmsg.arg1 = 1; //is received or not
							osmsg.obj = cmdObj;
							mOsmsgHander.sendMessage(osmsg);		
						}
						else if (cmdObj.getContentType().equals(C.CmdContent.audioPcm)) {
							mVoiceRnP.postCmd(cmdObj);
						}
						break;
					}
				}
			}
			catch (IOException e) {
				Log.d(TAG, "Byebye, WhatTheTalk~");
				disconnect();
			}			
		}
	};
}
