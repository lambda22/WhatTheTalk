package com.wtt.io;

public final class C {
	public static final String wttpVersion = "1";
	
	public static final class CmdProp {
		public static final String id = "id";
		public static final String status = "status";
		public static final String os = "os";
		public static final String action = "action";
		public static final String version = "version";
		public static final String utc = "utc";
		public static final String to = "to";
		public static final String from = "from";
		public static final String contentLength = "content-length";
		public static final String contentType = "content-type";
	}
	
	public static final class CmdContent {
		public static final String text = "text";
		public static final String audioPcm = "audio/pcm";
	}
	
	public static final class CmdAction {
		public static final String login = "login";
		public static final String chat = "chat";
		public static final String echo = "echo";
	}
}
