package com.wtt.io;

import java.io.UnsupportedEncodingException;

import org.json.JSONException;
import org.json.JSONObject;

import com.wtt.io.C.CmdAction;
import com.wtt.io.C.CmdProp;

public class CmdObject {
	private String mID;
	private String mTo;
	private String mFrom;
	private String mVersion;
	private String mAction;
	private long mUTC;
	private int mStatus;
	private String mContentType;
	private int mContentLen;
	private byte[] mContent;
	
	public CmdObject() {
		reset();
	}
	
	public void reset() {
		mID = "";
		mTo = "";
		mFrom = "";
		mVersion = "";
		mAction = "";
		mUTC = 0;
		mStatus = 0;
		mContentType = "";
		mContentLen = 0;
		mContent = new byte[0];			
	}
	
	public boolean parseJsonString(String jsonString) {
		try {
			JSONObject jsonObj = new JSONObject(jsonString);
			mID = jsonObj.optString(C.CmdProp.id);
			mVersion = jsonObj.optString(C.CmdProp.version);
			mAction = jsonObj.optString(C.CmdProp.action);
			mUTC = jsonObj.optLong(C.CmdProp.utc);
			mStatus = jsonObj.optInt(C.CmdProp.status);
			mContentType = jsonObj.optString(C.CmdProp.contentType);
			mContentLen = jsonObj.optInt(C.CmdProp.contentLength);
		} catch (JSONException e) {
			e.printStackTrace();
			reset();
			return false;
		}

		return true;
	}

	public String toJsonString()
	{
		JSONObject jsonObj = new JSONObject();
		try {
			jsonObj.put(C.CmdProp.version, C.wttpVersion);
			jsonObj.put(C.CmdProp.action, mAction);
			switch (mAction) {
			case C.CmdAction.login:
				jsonObj.put(C.CmdProp.utc, System.currentTimeMillis());
				jsonObj.put(C.CmdProp.id, mID);
				jsonObj.put(C.CmdProp.status, mStatus);
				break;
			case C.CmdAction.chat:
				jsonObj.put(C.CmdProp.utc, mUTC > 0 ? mUTC : System.currentTimeMillis());
				jsonObj.put(C.CmdProp.from, mFrom);
				jsonObj.put(C.CmdProp.contentType, mContentType);
				jsonObj.put(C.CmdProp.contentLength, mContent.length);
				break;			
			case C.CmdAction.echo:
				jsonObj.put(C.CmdProp.utc, mUTC > 0 ? mUTC : System.currentTimeMillis());
				jsonObj.put(C.CmdProp.contentType, mContentType);
				jsonObj.put(C.CmdProp.contentLength, mContent.length);
				break;
			default:
				System.err.print("Unknown action type!");
				return "";
			}
			
		} catch (JSONException e) {
			e.printStackTrace();
			return "";
		}
		
		return jsonObj.toString();
	}
	
	public void setID(String id) {
		mID = id;
	}
	public String getID() {
		return mID;
	}

	public void setTo(String to) {
		mTo = to;
	}
	public String getTo() {
		return mTo;
	}
	
	public void setFrom(String from) {
		mFrom = from;
	}
	public String getFrom() {
		return mFrom;
	}
	
	public void setVersion(String ver) {
		mVersion = ver;
	}
	public String getVersion() {
		return mVersion;
	}
	
	public void setAction(String action) {
		mAction = action;
	}	
	public String getAction() {
		return mAction;
	}	

	public void setUTC(long utc) {
		mUTC = utc;
	}
	public long getUTC() {
		return mUTC;
	}
	
	public void setStatus(int status) {
		mStatus = status;
	}
	public int getStatus() {
		return mStatus;
	}
	
	public void setContentType(String contentType) {
		mContentType = contentType;
	}
	public String getContentType() {
		return mContentType;
	}

	public int getContentLen() {
		return mContentLen;
	}

	public void setContent(byte[] content) {
		mContent = content;
		mContentLen = content.length;
	}
	public byte[] getContent() {
		return mContent;
	}
	public String getContentInText() {
		String text = "";
		try {
			text = new String(mContent, "UTF-8");
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		return text;
	}
}
