package com.wtt.io;

import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;

public class CmdWriter {
	private BufferedWriter mBufWriter;
	private BufferedOutputStream mBufOut;
	
	public CmdWriter(OutputStream out) {
		try {
			mBufWriter = new BufferedWriter(new OutputStreamWriter(out, "ISO-8859-1"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
		mBufOut = new BufferedOutputStream(out);		
	}
	
	public void writeCmd(CmdObject cmdObj) throws IOException {
		mBufWriter.write(cmdObj.toJsonString());
		mBufWriter.write("\r\n");
		mBufWriter.flush();
	
		byte[] content = cmdObj.getContent();
		if (content.length > 0) {
			mBufOut.write(cmdObj.getContent());
			mBufOut.flush();
		}
	}
}
