package com.madserve.sdk;

public class Const {

	public static final String TAG = "mAdserve";
	public static final String ENCODING = "UTF-8";
	public static final String RESPONSE_ENCODING = "ISO-8859-1";
	public static final String VERSION = "1.2";
	public static final String PROTOCOL_VERSION = "3";

	public static final String IMAGE = "<body style='\"'margin: 0px; padding: 0px; text-align:center;'\"'><img src='\"'{0}'\"' width='\"'{1}'dp\"' height='\"'{2}'dp\"'/></body>";
	public static final String REDIRECT_URI = "REDIRECT_URI";

	public static final String HIDE_BORDER = "<style>* { -webkit-tap-highlight-color: rgba(0,0,0,0) }</style>";

	public static final int TOUCH_DISTANCE = 30;

	public static final int CONNECTION_TIMEOUT = 15000; // = 15 sec
	public static final int SOCKET_TIMEOUT = 15000; // = 15 sec

	public static final String PREFS_DEVICE_ID = "madserve_device_id";
}
