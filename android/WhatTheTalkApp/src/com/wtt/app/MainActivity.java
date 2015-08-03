package com.wtt.app;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

import com.wtt.io.C;
import com.wtt.io.CmdObject;

import android.support.v7.app.ActionBarActivity;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;


public class MainActivity extends ActionBarActivity {
	private static final String TAG = "wtt";
	private EditText mIpEdit, mPortEdit, mNameEdit;
	private Button mConnectBtn, mWhatBtn;
	private CmdClient mCmdClient;
	private ListView mMsgListView;
	private ArrayAdapter<String> mMsgAdapter;
	private ArrayList<String> mMsgAryList = new ArrayList<String>();
	
	private final int CMD_CLIENT_SUCCESS = 0;
	private final int CMD_CLIENT_ERROR_IO = 1;
	private final int CMD_CLIENT_ERROR_NUMBER_FORMAT = 2;
	
	private Handler mOsmsgHandler = new Handler() {
		public void handleMessage(Message osmsg) {
			CmdObject cmdObj = (CmdObject)osmsg.obj;
			
			StringBuilder strBuilder = new StringBuilder();
			strBuilder.append((osmsg.arg1 == 1) ? "[receive]" : "[send]")
				.append("[" + cmdObj.getAction() + "] ")
				.append((cmdObj.getFrom().isEmpty()) ? "" : cmdObj.getFrom())
				.append(": " + cmdObj.getContentInText());
			
			mMsgAryList.add(strBuilder.toString());
			mMsgAdapter.notifyDataSetChanged();
		}
	};
	
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        setAllViews();
    }

    public void onConnectBtnClick(View view) {
    	Log.d(TAG, "onConnectBtnClick");
    	AsyncTask<String, Void, Integer> msgClientTask = new AsyncTask<String, Void, Integer>() {
    		@Override
    		protected Integer doInBackground(String... params) {
    			try {
    				String[] address = params[0].split(":");
    				String ip = address[0];
    				int port = Integer.parseInt(address[1]);
    				
    				mCmdClient = new CmdClient(ip, port, mOsmsgHandler);
    				mCmdClient.start();
    				
    				return CMD_CLIENT_SUCCESS;
    			}
    	    	catch (NumberFormatException e) {
    	    		Log.e(TAG, "Invalid port number: " + e.getMessage());
    	    		return CMD_CLIENT_ERROR_NUMBER_FORMAT;
    	    	}
    			catch (IOException e) {
    				Log.e(TAG, "Open socket error: " + e.getMessage());
    				return CMD_CLIENT_ERROR_IO;
    			}
    		}
    		
    		@Override
    		protected void onPostExecute(Integer result) {
    			switch (result) {
    			case CMD_CLIENT_SUCCESS:
    	    		CmdObject cmdObj = new CmdObject();
    	        	cmdObj.setAction(C.CmdAction.login);
    	        	cmdObj.setID(mNameEdit.getText().toString());
    				mCmdClient.sendCmd(cmdObj);
    				
    				mWhatBtn.setEnabled(true);
    				break;
    			case CMD_CLIENT_ERROR_IO:
    				mConnectBtn.setEnabled(true);
    				break;
    			case CMD_CLIENT_ERROR_NUMBER_FORMAT:
    				mConnectBtn.setEnabled(true);
    				mPortEdit.requestFocus();
    				break;
    			default:
    				throw new AssertionError("Unrecognized result of async task!!");
    			}
    		};
        };
        
        mConnectBtn.setEnabled(false);
        String address = mIpEdit.getText().toString() + ":" + mPortEdit.getText().toString();
        msgClientTask.execute(address);
    }
    
    public void onWhatBtnClick(View view) {
    	Log.d(TAG, "onWhatBtnClick");
    	try {
    		CmdObject cmdObj = new CmdObject();
        	cmdObj.setAction(C.CmdAction.chat);
        	cmdObj.setFrom(mNameEdit.getText().toString());
        	cmdObj.setContentType(C.CmdContent.text);
			cmdObj.setContent(new String("What the fuck!").getBytes("UTF-8"));
			mCmdClient.sendCmd(cmdObj);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }

    public void onEchoBtnClick(View view) {
    	Log.d(TAG, "onEchoBtnClick");
    	try {
    		CmdObject cmdObj = new CmdObject();
        	cmdObj.setAction(C.CmdAction.echo);
        	cmdObj.setFrom(mNameEdit.getText().toString());
        	cmdObj.setContentType(C.CmdContent.text);
			cmdObj.setContent(new String("What the fuck!").getBytes("UTF-8"));
			mCmdClient.sendCmd(cmdObj);
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    
    public void onDialBtnClick(View view) {
    	Log.d(TAG, "onDialBtnClick");
    	mCmdClient.startVoiceRnP();
    }
    
    @Override
    protected void onDestroy() {
    	Log.d(TAG, "onDestroy");
    	super.onDestroy();
    	if (mCmdClient != null)
    		mCmdClient.disconnect();
    }
    
    private void setAllViews() {
    	mIpEdit = (EditText)findViewById(R.id.ipEdit);
    	mPortEdit = (EditText)findViewById(R.id.portEdit);
    	mNameEdit = (EditText)findViewById(R.id.nameEdit);
    	mConnectBtn = (Button)findViewById(R.id.connectBtn);
    	mWhatBtn = (Button)findViewById(R.id.whatBtn);
    	mMsgListView = (ListView)findViewById(R.id.msgListView);
    	
    	mWhatBtn.setEnabled(false);
    	
    	mMsgAdapter = new ArrayAdapter<String>(this, R.layout.msg_list_item, R.id.msgText, mMsgAryList);
    	mMsgListView.setAdapter(mMsgAdapter);
    	mMsgListView.setTranscriptMode(ListView.TRANSCRIPT_MODE_ALWAYS_SCROLL);
    }
}
