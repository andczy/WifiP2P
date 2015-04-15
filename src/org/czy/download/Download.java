package org.czy.download;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingDeque;

import org.czy.log.Logger;

import com.czy.wifiap.R;
import com.github.snowdream.android.app.downloader.DownloadListener;
import com.github.snowdream.android.app.downloader.DownloadManager;
import com.github.snowdream.android.app.downloader.DownloadTask;
import android.app.Activity;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.support.v4.app.NotificationCompat;
import android.view.View;
import android.widget.RemoteViews;

public class Download {
	
	private Context activity ;
	private NotificationManager mNm ;
	private Notification mNotification ;
	private static Download download ;
	private List<String > urls ;
	public static Download getInstance(Context acti){
		if(download == null){
			download = new Download(acti);
		}
		return download ;
	}
	private Download (Context activity){
		this.activity = activity ;
	}
	public void download( String url ,int icon , String ticker){
		if(urls == null){
			urls=  new ArrayList<String>();
		}
		for(String u :urls){
			if(u.equals(url)){
				return ;
			}
		}
		urls.add(url);
		DownloadManager dm = new DownloadManager(activity);
		DownloadTask task = new DownloadTask(activity); 
		task.setUrl(url);
		dm.start(task, l);
		mNm = (NotificationManager) activity.getApplicationContext().getSystemService(Context.NOTIFICATION_SERVICE);
		RemoteViews remote = new RemoteViews(activity.getPackageName(),R.layout.notification_view);
		String content = "正在下载"+ticker ;
		NotificationCompat.Builder builder= new NotificationCompat.Builder(activity);
		Intent intent = new Intent() ;
		PendingIntent pending = PendingIntent.getActivity(activity, 0, intent, 0);
		mNotification = builder.setSmallIcon(icon)
				.setTicker(content).setContentText(content).setContentTitle(content)
				.setAutoCancel(true).setWhen(System.currentTimeMillis()).setContentIntent(pending).build() ;
		mNotification.contentView = remote ;
//		mNotification.largeIcon = BitmapFactory.decodeResource(activity.getResources(), R.drawable.ic_launcher);
		mNm.notify(R.id.notify_progress, mNotification);
	}
	DownloadListener<Integer, DownloadTask> l = new DownloadListener<Integer,DownloadTask>(){
		private int notifyCount ;
		@Override
		public void onAdd(DownloadTask task) {
			Logger.d("onAdd..."+task.getPath());
			super.onAdd(task);
		}

		@Override
		public void onDelete(DownloadTask task) {
			Logger.d("onDelete ..."+task.getPath());
			super.onDelete(task);
		}

		@Override
		public void onStop(DownloadTask task) {
			super.onStop(task);
		}

		@Override
		public void onCancelled() {
			super.onCancelled();
		}

		@Override
		public void onError(Throwable thr) {
			Logger.d("onError..."+thr	) ;
			mNotification.contentView.setViewVisibility(R.id.notify_progress, View.GONE);
			mNotification.contentView.setTextViewText(R.id.notify_percent, "下载失败！");
			mNm.notify(R.id.notify_progress, mNotification);
			super.onError(thr);
		}

		@Override
		public void onFinish() {
			Logger.d("onFinish....");
			if(urls.size()>0)
				urls.remove(0);
			super.onFinish();
		}

		@Override
		public void onProgressUpdate(Integer... values) {
			notifyCount ++ ;
			if(notifyCount == 100){
				Logger.e("progress update ...."+values[0]+"   "+notifyCount);
				notifyCount = 0 ;
				mNotification.contentView.setProgressBar(R.id.notify_progress, 100, values[0], false);
				mNotification.contentView.setTextViewText(R.id.notify_percent, values[0]+"%");
				mNm.notify(R.id.notify_progress, mNotification);
			}
			super.onProgressUpdate(values);
		}

		@Override
		public void onStart() {
			Logger.d("onstart ....") ;
			if(mNotification!=null){
				mNotification.contentView.setViewVisibility(R.id.notify_progress, View.VISIBLE);
				mNotification.contentView.setViewVisibility(R.id.notify_text, View.VISIBLE);
			}
			super.onStart();
		}

		@Override
		public void onSuccess(DownloadTask result) {
			Logger.d("onSuccess..."+result.getPath());
			mNotification.contentView.setViewVisibility(R.id.notify_progress, View.GONE);
			mNotification.contentView.setTextViewText(R.id.notify_percent, "下载完成!");
			mNotification.contentView.setViewVisibility(R.id.notify_text, View.GONE);
//		PendingIntent pending = new PendingInten;
			installUpdate(activity, result.getPath());
			Intent intent=new Intent(Intent.ACTION_VIEW);
			intent.setDataAndType(Uri.parse("file://"+result.getPath())
					, "application/vnd.android.package-archive");
			PendingIntent pending = PendingIntent.getActivity(activity, 11, intent, PendingIntent.FLAG_CANCEL_CURRENT);
			mNotification.contentIntent = pending ;
//		mNotification.contentView.setOnClickFillInIntent(R.id.notify_parent, fillInIntent)
			mNm.notify(R.id.notify_progress, mNotification);
			super.onSuccess(result);
		}
		
	};
	private void installUpdate(Context context,String path)
	{
		Intent intent=new Intent(Intent.ACTION_VIEW);
		intent.setDataAndType(Uri.parse("file://"+path)
				, "application/vnd.android.package-archive");
//		try{
		intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
		context.startActivity(intent);
	}
}
