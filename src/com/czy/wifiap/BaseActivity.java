package com.czy.wifiap;

import com.avos.avoscloud.AVAnalytics;

import android.app.Activity;
import android.os.Bundle;

public class BaseActivity extends Activity{

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		// TODO Auto-generated method stub
		super.onCreate(savedInstanceState);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	protected void onResume() {
		super.onResume();
		AVAnalytics.onResume(this);
	}
	@Override
	protected void onPause(){
		super.onPause() ;
		AVAnalytics.onPause(this);
	}
	
}
