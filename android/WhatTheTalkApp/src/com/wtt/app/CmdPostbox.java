package com.wtt.app;

import java.io.IOException;
import java.util.ArrayDeque;

import com.wtt.io.CmdObject;
import com.wtt.io.CmdWriter;

import android.util.Log;


public class CmdPostbox {
	private final String TAG = "wtt";
	private CmdWriter mCmdWriter;
	private ArrayDeque<CmdObject> mCmdDeque;
	private Thread mThread;
	
	public CmdPostbox(CmdWriter cmdWriter) {
		mCmdWriter = cmdWriter;
		mCmdDeque = new ArrayDeque<>();
		
		mThread = new Thread(mSendCmdRun);
		mThread.setDaemon(true);
		mThread.start();
	}
	
	synchronized public void postCmd(CmdObject cmdObj) {
		mCmdDeque.add(cmdObj);
		notify();
	}
	
	synchronized public CmdObject receiveCmd() {
		if (mCmdDeque.isEmpty()) {
			try {
				wait();
			} catch (InterruptedException e) {
				Log.e(TAG, "Error while calling wait()");
				throw new RuntimeException(e);
			}
		}
		
		CmdObject cmdObj = mCmdDeque.poll();
		return cmdObj;
	}
	
	private Runnable mSendCmdRun = new Runnable() {
		@Override
		public void run() {
			while (true) {
				CmdObject cmdObj = receiveCmd();
				try {
					mCmdWriter.writeCmd(cmdObj);
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
	};
}
