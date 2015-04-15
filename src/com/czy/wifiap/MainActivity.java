package com.czy.wifiap;

import java.net.InetAddress;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

import org.czy.log.Logger;
import android.net.NetworkInfo;
import android.net.wifi.WpsInfo;
import android.net.wifi.p2p.WifiP2pConfig;
import android.net.wifi.p2p.WifiP2pDevice;
import android.net.wifi.p2p.WifiP2pDeviceList;
import android.net.wifi.p2p.WifiP2pInfo;
import android.net.wifi.p2p.WifiP2pManager;
import android.net.wifi.p2p.WifiP2pManager.ActionListener;
import android.net.wifi.p2p.WifiP2pManager.Channel;
import android.net.wifi.p2p.WifiP2pManager.ChannelListener;
import android.net.wifi.p2p.WifiP2pManager.ConnectionInfoListener;
import android.net.wifi.p2p.WifiP2pManager.PeerListListener;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.view.Menu;
import android.widget.TextView;
import android.widget.Toast;

/***
 * ����Wifiֱ������Ҫ��װ����Ӧ��app,���ֻ�����������
 * @author ciyo
 *
 */
public class MainActivity extends Activity {
	Channel channel ;
	boolean exit ;
	Receiver receiver ;
	WifiP2pManager p2pManager ;
	List<WifiP2pDevice> peers ;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		p2pManager = (WifiP2pManager) getSystemService(Context.WIFI_P2P_SERVICE);
		channel = p2pManager.initialize(this, handler.getLooper(), new ChannelListener() {
			@Override
			public void onChannelDisconnected() {
				Logger.d("p2p channel disconnection .... ");
			}
		});
		peers = new ArrayList<WifiP2pDevice> () ;
		IntentFilter intentFilter = new IntentFilter() ;
		//��ʾWi-Fi�Ե�����״̬�����˸ı�  
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION);  
	  
	    //��ʾ���õĶԵȵ���б?���˸ı�  
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION);  
	  
	    //��ʾWi-Fi�Ե����������״̬�����˸ı�  
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION);  
	  
	    //�豸������Ϣ�����˸ı�  
	    intentFilter.addAction(WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION);  
	    receiver = new Receiver();
	    registerReceiver(receiver, intentFilter);
	    p2pManager.discoverPeers(channel, new ActionListener() {
			@Override
			public void onSuccess() {
				Logger.d("discover peer success");
			}
			
			@Override
			public void onFailure(int reason) {
				Logger.d("discover peer fail ="+reason);
			}
		});
	    
	    PackageInfo packageInfo = null ; 
	    try {
			packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES) ;
		} catch (NameNotFoundException e) {
			e.printStackTrace();
		} 
	    if(packageInfo!=null){
	    	TextView version = (TextView) findViewById(R.id.version) ; 
	    	version.setText(packageInfo.versionName) ;
	    }
	}
	private ConnectionInfoListener connectionListener = new ConnectionInfoListener() {
		@Override
		public void onConnectionInfoAvailable(WifiP2pInfo info) {
			// InetAddress��WifiP2pInfo�ṹ���С�  
	        String groupOwnerAddress = info.groupOwnerAddress.getHostAddress(); 
	        Logger.d("connect available = "+groupOwnerAddress+"   "+info.groupFormed);
	        //��ȺЭ�̺󣬾Ϳ���ȷ��Ⱥ����  
	        if (info.groupFormed && info.isGroupOwner) {  
	        //���Ⱥ����ĳЩ����  
	        //һ�ֳ��õ������ǣ�����һ���������̲߳�������������  
	        } else if (info.groupFormed) {  
	        //�����豸����Ϊ�ͻ��ˡ�����������£����ϣ��һ���ͻ����߳�������Ⱥ����  
	        }
		}
	} ;
	public void onDestory(){
		super.onDestroy() ;
		exit =true ;
		p2pManager.cancelConnect(channel, null);
		unregisterReceiver(receiver);
	}
	private Handler handler = new Handler(){
		public void handleMessage(Message msg){
			Logger.d(msg.what+"   "+msg);
		}
	};
	private PeerListListener peerListListener = new PeerListListener() {  
        @Override  
        public void onPeersAvailable(WifiP2pDeviceList peerList) {  
            //�ɵĲ�ȥ���µĲ���  
            peers.clear();  
            peers.addAll(peerList.getDeviceList());  
  
        }  
    } ;
    public void connect() {  
    	  
        //ʹ�����������ҵ��ĵ�һ���豸��  
        WifiP2pDevice device = peers.get(0);  
        WifiP2pConfig config = new WifiP2pConfig();  
        config.deviceAddress = device.deviceAddress;  
        config.wps.setup = WpsInfo.PBC;  
        p2pManager.connect(channel, config, new ActionListener() {  
            @Override  
            public void onSuccess() {  
            // WiFiDirectBroadcastReceiver����֪ͨ���ǡ����ڿ����Ⱥ��ԡ�  
            	Logger.d("p2p connect success");
            }  
            @Override  
            public void onFailure(int reason) {  
                Logger.d("connect fail ="+reason);
            }  
        });  
    }  
	class Receiver extends BroadcastReceiver{
		@Override
		public void onReceive(Context arg0, Intent intent) {
			String action = intent.getAction();  
			Logger.d("receiver action = "+action);
	        if (WifiP2pManager.WIFI_P2P_STATE_CHANGED_ACTION.equals(action)) {  
	            //ȷ��Wi-Fi Directģʽ�Ƿ��Ѿ����ã�������Activity��  
	            int state = intent.getIntExtra(WifiP2pManager.EXTRA_WIFI_STATE, -1);  
//	            if (state == WifiP2pManager.WIFI_P2P_STATE_ENABLED) {  
//	                activity.setIsWifiP2pEnabled(true);  
//	            } else {  
//	                activity.setIsWifiP2pEnabled(false);  
//	            }  
	            Logger.d("state = "+state);
	        } else if (WifiP2pManager.WIFI_P2P_PEERS_CHANGED_ACTION.equals(action)) {  
	        	p2pManager.requestPeers(channel, peerListListener);
	    //�Եȵ��б��Ѿ��ı䣡���ǿ�����Ҫ�Դ��������?  
	  
	        } else if (WifiP2pManager.WIFI_P2P_CONNECTION_CHANGED_ACTION.equals(action)) {  
	        	 NetworkInfo networkInfo = (NetworkInfo) intent  
	                     .getParcelableExtra(WifiP2pManager.EXTRA_NETWORK_INFO);  
	   
	             if (networkInfo.isConnected()) {  
	             //����������������豸������������Ϣ�����ҵ�Ⱥ����IP��  
	                 p2pManager.requestConnectionInfo(channel, connectionListener);  
	             }
	    //����״̬�Ѿ��ı䣡���ǿ�����Ҫ�Դ��������?  
	  
	        } else if (WifiP2pManager.WIFI_P2P_THIS_DEVICE_CHANGED_ACTION.equals(action)) {  
	        	WifiP2pDevice device = (WifiP2pDevice) intent.getParcelableExtra(  
	                    WifiP2pManager.EXTRA_WIFI_P2P_DEVICE);
	        	if(device!=null){
	        		Logger.d("device change action = "+device.deviceAddress+"   "+device.deviceName);
	        	}
	        }
		}
	}
}
