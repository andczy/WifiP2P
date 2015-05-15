package com.czy.wifiap;

import com.avos.avoscloud.AVAnalytics;
import com.avos.avoscloud.AVOSCloud;

import android.app.Application;

public class App extends Application{

	private boolean canUpload ;
	private static App instance ;
	public void onCreate(){
		super.onCreate() ;
		AVOSCloud.initialize(this, "h2we2ys1spxqq9ol3m1uyn4c77ntfz5wmaq2pxqenxnvwme1"
				, "k8ennd1dkpmvbv9vuvi5hkd6whyby1pa7qmqx1c8cjft0c5m");
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
