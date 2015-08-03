package com.wtt.io;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;

public class CmdReader {
	private BufferedReader mBufReader;
	
	public CmdReader(InputStream in) {
		try {
			mBufReader = new BufferedReader(new InputStreamReader(in, "ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}
	
	public CmdObject readCmd() throws IOException {
		String line = mBufReader.readLine();
		System.out.println("line: " + line);
		
		if (line == null) { throw new IOException("End of stream reached."); }		
		if (!line.startsWith("{") || !line.endsWith("}")) { return null; }
		
		CmdObject cmdObj = new CmdObject();
		cmdObj.parseJsonString(line);
		
		int contentLen = cmdObj.getContentLen();
		if (contentLen > 0) {
			byte[] byteAry = new byte[contentLen];
			char[] charAry = new char[contentLen];
			
			int i = 0;
			while (i < contentLen) {
				int len = mBufReader.read(charAry, i, contentLen - i);
				for (int k = i; k < i + len; ++k)
					byteAry[k] = (byte)charAry[k];
				i += len;
			}
			cmdObj.setContent(byteAry);
		}
		
		return cmdObj;
	}
}
