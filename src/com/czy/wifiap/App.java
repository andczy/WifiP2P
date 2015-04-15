package com.czy.wifiap;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;

import android.app.Application;

public class App extends Application{

	private boolean canUpload ;
	private static App instance ;
	public void onCreate(){
		super.onCreate() ;
		AVOSCloud.initialize(this, "2mzxv7uzx5x8tish64t66sdi5anbw2s1h06fwja22u4jbe88"
				, "x9p3mdmyt86z6vwf4b156d3en6q4tczx0dxjiega6ov7n6vk");
		AVAnalytics.enableCrashReport(this, true);
		AVAnalytics.setSessionContinueMillis(60 * 1000);
		instance = this ;
	}
	public boolean isCanUpload() {
		return canUpload;
	}
	public void setCanUpload(boolean canUpload) {
		this.canUpload = canUpload;
	}
	public static App getInstance(){
		return instance ;
	}
}
