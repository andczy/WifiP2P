package org.czy.log;


import com.czy.wifiap.BuildConfig;

import android.util.Log;

public class Logger {
	private static final String TAG = "WIFI_P2P";
	public static void d(String msg){
		if(BuildConfig.DEBUG)
			Log.d(TAG, msg);
	}
	public static void e(String msg){
		Log.e(TAG , msg);
	}
	public static void w(String msg){
		if(BuildConfig.DEBUG)
			Log.w(TAG , msg);
	}
}
