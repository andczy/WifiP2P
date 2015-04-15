package com.czy.wifiap;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.BaseAdapter;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

public class ListDirActivity extends BaseActivity{
	private File currentDir ;
	public static final String EXTRA_DIR = "share_dir" ;
	private File[] dirs ;
	private BaseAdapter baseAdapter ;
	public void onCreate(Bundle b){
		super.onCreate(b);
		getWindow().requestFeature(Window.FEATURE_NO_TITLE) ;
		if(Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED))	
			currentDir = Environment.getExternalStorageDirectory() ;
		else{
			Toast.makeText(this, "未找到存储卡", Toast.LENGTH_LONG).show();
			finish() ;
			return ;
		}
		setContentView(R.layout.choose_dir_list);
		findViewById(R.id.back).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				File tempDir = currentDir ;
				currentDir = currentDir.getParentFile();
				if(currentDir == null){
					currentDir = tempDir ;
					Toast.makeText(v.getContext(), "已到最顶层目录", Toast.LENGTH_LONG).show() ;
				}
				else{
					dirs = getDirFiles (currentDir );
					baseAdapter.notifyDataSetChanged() ;
				}
			}
		});
		findViewById(R.id.choose_dir_ok).setOnClickListener(new OnClickListener() {
			@Override
			public void onClick(View v) {
				Intent data =  new Intent() ;
				data.putExtra(EXTRA_DIR	, currentDir.getAbsolutePath());
				setResult(Activity.RESULT_OK, data);
				finish() ;
			}
		});
		ListView dirListView = (ListView)findViewById(R.id.choose_dir_list);
		dirListView.setOnItemClickListener(new OnItemClickListener(){
			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int arg2,
					long arg3) {
				File file = dirs[arg2] ;
				if(file.isDirectory()){
					
				}
				else{
					Toast.makeText(arg0.getContext(), "你选择一个文件！", Toast.LENGTH_LONG).show() ;
				}
				currentDir = file ;
				dirs = getDirFiles (currentDir );
				baseAdapter.notifyDataSetChanged() ;
			}
		});
		dirs = getDirFiles (currentDir );
		baseAdapter = new DirAdapter() ;
		dirListView.setAdapter(baseAdapter);
	}

	private File[] getDirFiles(File dir){
		if(dir!=null&&dir.isDirectory()){
			File [] fs = dir.listFiles() ;
			return Response.sortFileByName(fs) ;
		}
		else if(dir.isFile()){
			return new File[]{dir} ;
		}
		return null ;
	}
	class DirAdapter extends BaseAdapter{
		@Override
		public int getCount() {
			return dirs==null?0:dirs.length;
		}

		@Override
		public Object getItem(int position) {
			return null;
		}

		@Override
		public long getItemId(int position) {
			return 0;
		}

		@Override
		public View getView(int position, View convertView, ViewGroup parent) {
			if(convertView == null){
				convertView = getLayoutInflater().inflate(R.layout.dir_item, null);
			}
			File file = dirs[position] ;
			View dirIcon = convertView.findViewById(R.id.dir_icon) ;
			if(file.isDirectory()){
				if(dirIcon.getVisibility()!=View.VISIBLE)
					dirIcon.setVisibility(View.VISIBLE) ;
			}
			else{
				if(dirIcon.getVisibility()!=View.INVISIBLE)
					dirIcon.setVisibility(View.INVISIBLE) ;
			}
			TextView name = (TextView)convertView.findViewById(R.id.dir_name) ;
			name.setText(file.getName());
			return convertView;
		}
		
	}
	
}
