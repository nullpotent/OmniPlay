package com.omnidev.omniplay.utils;

import android.os.Message;

public class Util {
	public static Message createHandlerMessage(final Object obj, final int what) {
		Message listMsg = new Message();
		listMsg.obj 	= obj;
		listMsg.what 	= what;
		return listMsg;
	}	
}
