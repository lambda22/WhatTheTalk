package com.wtt.app;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Arrays;

import android.media.AudioFormat;
import android.media.AudioManager;
import android.media.AudioRecord;
import android.media.AudioTrack;
import android.media.MediaRecorder.AudioSource;
import android.util.Log;

import com.wtt.io.C;
import com.wtt.io.CmdObject;

public class VoiceRnP {
	private final String TAG = "wtt";
	private CmdPostbox mCmdPostbox;
	private ArrayDeque<CmdObject> mCmdDeque;
	
	private AudioRecord mAudioRec;
	private byte[] mAudioBuf;
	
	int mSampleRate = 44100;
	int mAudioFormat = AudioFormat.ENCODING_PCM_16BIT;
	int mBufSize;
	
	private AudioTrack mAudioTrack;
	long mPlayDelay = 3000;
	
	public VoiceRnP(CmdPostbox cmdPostbox) {
		mCmdPostbox = cmdPostbox;
		mCmdDeque = new ArrayDeque<>();
		initAudioRecord();
		initAudioTrack();
	}
	
	public void start() {
		Log.i(TAG, "start VoiceRnP");
		if (mAudioRec.getRecordingState() == AudioRecord.RECORDSTATE_STOPPED) {
			mAudioRec.startRecording();
			new Thread(mRecRun).start();
		}
		if (mAudioTrack.getPlaybackRate() != AudioTrack.PLAYSTATE_PLAYING) {
	    	mAudioTrack.setStereoVolume(1.0f, 1.0f);
	    	mAudioTrack.play();
	    	new Thread(mPlayRun).start();
		}
	}
	
	public void stop() {
		Log.i(TAG, "stop VoiceRnP");
    	if (mAudioRec.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING)
    		mAudioRec.stop();
    	if (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING)
    		mAudioTrack.stop();
	}
	
	synchronized public void postCmd(CmdObject cmdObj) {
		mCmdDeque.add(cmdObj);
		
		if (cmdObj.getUTC() - mCmdDeque.peekFirst().getUTC() > mPlayDelay)
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
		
		while (mCmdDeque.peekLast().getUTC() - cmdObj.getUTC() < mPlayDelay) {
			try {
				wait();
			} catch (InterruptedException e) {
				Log.e(TAG, "Error while calling wait()");
				throw new RuntimeException(e);
			}
		}
		return cmdObj;
	}
	
    private void initAudioRecord() {
    	int audioSrc = AudioSource.MIC;
    	int channelInConfig = AudioFormat.CHANNEL_IN_MONO;
    	mBufSize = AudioRecord.getMinBufferSize(mSampleRate, channelInConfig, mAudioFormat);
    	Log.i(TAG, "min buffer size: " + mBufSize);
    	
    	mAudioBuf = new byte[mBufSize];
    	mAudioRec = new AudioRecord(audioSrc, mSampleRate, channelInConfig, mAudioFormat, mBufSize);
    }
    
    private void initAudioTrack() {
    	int streamType = AudioManager.STREAM_MUSIC;
    	mAudioTrack = new AudioTrack(streamType, mSampleRate, AudioFormat.CHANNEL_OUT_MONO,
    			mAudioFormat, mBufSize, AudioTrack.MODE_STREAM);
    }
    
    private Runnable mRecRun = new Runnable() {
    	@Override
		public void run() {
			while (mAudioRec.getRecordingState() == AudioRecord.RECORDSTATE_RECORDING) {
				int bufRead = mAudioRec.read(mAudioBuf, 0, mAudioBuf.length);
				if (bufRead < 0) {
					Log.e(TAG, "Audio recorder read error! (" + bufRead + ")");
					break;
				}
				
	    		CmdObject cmdObj = new CmdObject();
	        	cmdObj.setAction(C.CmdAction.echo);
	        	cmdObj.setContentType(C.CmdContent.audioPcm);
				cmdObj.setContent(Arrays.copyOf(mAudioBuf, bufRead));
				mCmdPostbox.postCmd(cmdObj);
			}    					
		}
	};
    
	private Runnable mPlayRun = new Runnable() {
		@Override
		public void run() {
			while (mAudioTrack.getPlayState() == AudioTrack.PLAYSTATE_PLAYING) {
				CmdObject cmdObj = receiveCmd();
				mAudioTrack.write(cmdObj.getContent(), 0, cmdObj.getContentLen());
			}
		}
	};
    
}
