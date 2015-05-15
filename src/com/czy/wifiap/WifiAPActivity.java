package com.czy.wifiap;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.czy.download.Download;
import org.czy.log.Logger;

import com.avos.avoscloud.AVException;
import com.avos.avoscloud.AVObject;
import com.avos.avoscloud.AVQuery;
import com.avos.avoscloud.FindCallback;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.EncodeHintType;
import com.google.zxing.MultiFormatWriter;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;

import android.app.Activity;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.PackageManager.NameNotFoundException;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.net.wifi.p2p.WifiP2pManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.PowerManager;
import android.os.PowerManager.WakeLock;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.View.OnClickListener;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.CompoundButton.OnCheckedChangeListener;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

public class WifiAPActivity extends BaseActivity {

    public static final String    SSID                     = "share_dir";
    WifiManager                   wifiManager;
    private static final String   INT_PRIVATE_KEY          = "key_id";
    private static final String   INT_PHASE2               = "phase2";
    private static final String   INT_PASSWORD             = "password";
    private static final String   INT_IDENTITY             = "identity";
    private static final String   INT_EAP                  = "eap";
    private static final String   INT_CLIENT_CERT          = "client_cert";
    private static final String   INT_CA_CERT              = "ca_cert";
    private static final String   INT_ANONYMOUS_IDENTITY   = "anonymous_identity";
    final String                  INT_ENTERPRISEFIELD_NAME = "android.net.wifi.WifiConfiguration$EnterpriseField";
    final String                  ENTERPRISE_EAP           = "TLS";
    final String                  ENTERPRISE_CLIENT_CERT   = "www.jd.com";
    final String                  ENTERPRISE_PRIV_KEY      = "www.taobao.com";

    // CertificateName = Name given to the certificate while installing it

    /* Optional Params- My wireless Doesn't use these */
    final String                  ENTERPRISE_PHASE2        = "";
    final String                  ENTERPRISE_ANON_IDENT    = "ABC";
    final String                  ENTERPRISE_CA_CERT       = "";
    private String                webRoot;
    private HttpServer            httpServer;
    private boolean               wifiOpen;
    private ProgressDialog        progressDialog;
    private byte[]                dirBytes;
    private byte[]                videos;
    private byte[]                audios;
    private byte[]                files;
    private static WifiAPActivity wifiAPActivity;
    private WakeLock              mLock;
    private String                host;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        wifiAPActivity = this;
        wifiManager = (WifiManager)getSystemService(Context.WIFI_SERVICE);
        // getSharedPreferences("last_share_dir", Context.MODE_PRIVATE).getString("dir",
        // Environment.getExternalStorageState())
        OnClickListener l = new ClickListener();
        findViewById(R.id.choose_dir).setOnClickListener(l);
        findViewById(R.id.connect_ap).setOnClickListener(l);
        findViewById(R.id.share_ip_qr).setOnClickListener(l);
        findViewById(R.id.open_upload_dir).setOnClickListener(l);
        // BufferedWriter out = null;
        // try {
        // out = new BufferedWriter(new FileWriter("/mnt/sdcard/eap.txt"));
        // } catch (IOException e) {
        // e.printStackTrace();
        // }
        // new AP().readEapConfig(out, this);
        mLock = ((PowerManager)getSystemService(Context.POWER_SERVICE)).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
                "phone_server");
        checkUpdate();
        try {
            InputStream in = getAssets().open("dir.jpg");
            dirBytes = new byte[in.available()];
            in.read(dirBytes);
            in.close();
            in = getAssets().open("file.jpg");
            files = new byte[in.available()];
            in.read(files);
            in.close();
            in = getAssets().open("audio.jpg");
            audios = new byte[in.available()];
            in.read(audios);
            in.close();
            in = getAssets().open("video.jpg");
            videos = new byte[in.available()];
            in.read(videos);
            in.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
        Logger.d("dir length = " + dirBytes.length);
        startShare();
        Toast.makeText(this, "已经开始共享", Toast.LENGTH_LONG).show();
        CheckBox canUpload = (CheckBox)findViewById(R.id.can_upload);
        canUpload.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                App.getInstance().setCanUpload(isChecked);
            }
        });

        PackageInfo packageInfo = null;
        try {
            packageInfo = getPackageManager().getPackageInfo(getPackageName(), PackageManager.GET_ACTIVITIES);
        } catch (NameNotFoundException e) {
            e.printStackTrace();
        }
        if (packageInfo != null) {
            TextView version = (TextView)findViewById(R.id.version);
            version.setText(packageInfo.versionName);
        }
    }

    public static WifiAPActivity getInstance() {
        return wifiAPActivity;
    }

    public void setWebRoot(String webRoot) {
        this.webRoot = webRoot;
    }

    private void checkUpdate() {
        AVQuery<AVObject> query = new AVQuery<AVObject>("Update");
        // query.whereEqualTo("playerName", "steve");
        query.findInBackground(new FindCallback<AVObject>() {
            @Override
            public void done(List<AVObject> avObjects, AVException e) {
                Logger.d("av query over = " + e);
                if (e == null) {
                    if (avObjects.size() > 0) {
                        AVObject av = avObjects.get(0);
                        int ver = av.getInt("ver");
                        int must = av.getInt("must");
                        Logger.d("update =" + ver + "  " + must);
                        int version = 0;
                        try {
                            version = getPackageManager().getPackageInfo(getPackageName(), 0).versionCode;
                        } catch (NameNotFoundException e1) {
                            // TODO Auto-generated catch block
                            e1.printStackTrace();
                        }
                        if (version < must) {
                            String msg = av.getString("msg");
                            String url = av.getString("url");
                            showMustUpdate(WifiAPActivity.this, msg, url);
                        } else if (version < ver) {
                            String msg = av.getString("msg");
                            String url = av.getString("url");
                            showUpdate(WifiAPActivity.this, msg, url);
                        }
                    }
                }
            }
        });
    }

    private void showUpdate(Context context, final String msg, final String url) {
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        dialog.setContentView(R.layout.dialog_sure_cancel);
        dialog.setCancelable(false);
        dialog.show();
        final TextView message = (TextView)dialog.findViewById(R.id.dialog_message);
        message.setText(msg);
        TextView cancel = (TextView)dialog.findViewById(R.id.dialog_cancel);
        cancel.setText("暂不更新");
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        TextView sure = (TextView)dialog.findViewById(R.id.dialog_sure);
        sure.setText("更新");
        sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                v.setVisibility(View.INVISIBLE);
                Download.getInstance(WifiAPActivity.this).download(url, R.drawable.ic_launcher,
                        getString(R.string.app_name));
            }
        });
    }

    private void showMustUpdate(Context context, final String msg, final String url) {
        final Dialog dialog = new Dialog(context, R.style.Dialog);
        dialog.setContentView(R.layout.dialog_sure_cancel);
        dialog.setCancelable(false);
        dialog.show();
        final TextView message = (TextView)dialog.findViewById(R.id.dialog_message);
        message.setText("应用需要更新才能使用;\n" + msg);

        TextView cancel = (TextView)dialog.findViewById(R.id.dialog_cancel);
        cancel.setText("退出");
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                System.exit(0);
            }
        });
        TextView sure = (TextView)dialog.findViewById(R.id.dialog_sure);
        sure.setText("更新");
        sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                v.setVisibility(View.INVISIBLE);
                message.setText("正在下载，请稍候...");
                Download.getInstance(WifiAPActivity.this).download(url, R.drawable.ic_launcher,
                        getString(R.string.app_name));
            }
        });
    }

    private void showQRDialog() {
        if (host == null) {

            return;
        }
        final Dialog dialog = new Dialog(this, R.style.Dialog);
        dialog.setContentView(R.layout.dialog_host_qr);
        dialog.show();
        dialog.setCancelable(false);
        dialog.findViewById(R.id.dialog_cancel).setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });
        ImageView img = (ImageView)dialog.findViewById(R.id.dialog_host_qr);
        ViewGroup.LayoutParams imgParams = img.getLayoutParams();
        int width = getResources().getDisplayMetrics().widthPixels;
        imgParams.width = imgParams.height = width;
        img.setLayoutParams(imgParams);
        img.setImageBitmap(getQRBitmap(host, 480));
        WindowManager.LayoutParams params = dialog.getWindow().getAttributes();
        params.width = width;
        dialog.onWindowAttributesChanged(params);
    }

    /**
     * 获取二维码图片对象
     * 
     * @param qrString
     * @param size
     * @return
     */
    private Bitmap getQRBitmap(String qrString, int size) {
        MultiFormatWriter multiFormatWriter = new MultiFormatWriter();
        Map hints = new HashMap();
        hints.put(EncodeHintType.CHARACTER_SET, "UTF-8");
        BitMatrix bitMatrix = null;
        try {
            bitMatrix = multiFormatWriter.encode(qrString, BarcodeFormat.QR_CODE, size, size, hints);
        } catch (WriterException e) {
            e.printStackTrace();
        }
        int width = bitMatrix.getWidth();
        int height = bitMatrix.getHeight();
        Bitmap qrBitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_4444);
        for (int i = 0; i < width; i++) {
            for (int j = 0; j < height; j++) {
                qrBitmap.setPixel(i, j, bitMatrix.get(i, j) ? Color.BLACK : Color.WHITE);
            }
        }
        return qrBitmap;
    }

    class ClickListener implements OnClickListener {
        @Override
        public void onClick(View v) {
            switch (v.getId()) {
                case R.id.open_upload_dir:
                    File uploadDir = new File(Environment.getExternalStorageDirectory(), "share_czy_upload");
                    Intent openDirIntent = new Intent();
                    openDirIntent.setAction(android.content.Intent.ACTION_VIEW);
                    openDirIntent.setDataAndType(Uri.fromFile(uploadDir), "resource/folder");
                    try {
                        startActivity(openDirIntent);
                    } catch (Exception e) {
                        e.printStackTrace();
                        Toast.makeText(v.getContext(), "打开目录失败，安装‘es文件浏览器'便能打开目录。", Toast.LENGTH_LONG).show();
                    }
                    break;
                case R.id.share_ip_qr:
                    showQRDialog();
                    break;
                case R.id.connect_ap:
                    WifiConfiguration config = createWifiInfo(SSID, null, WifiCipherType.WIFICIPHER_NOPASS);
                    int netID = wifiManager.addNetwork(config);
                    boolean saved = wifiManager.saveConfiguration();
                    Toast.makeText(v.getContext(), "save wifi=" + saved, Toast.LENGTH_LONG).show();
                    boolean connected = wifiManager.enableNetwork(netID, false);
                    Toast.makeText(v.getContext(), "连接wifi=" + connected, Toast.LENGTH_LONG).show();
                    if (connected) {
                        if (progressDialog == null) {
                            progressDialog = new ProgressDialog(v.getContext());
                            progressDialog.setMessage("正在进行wifi连接");
                        }
                        progressDialog.show();
                        new Thread() {
                            public void run() {
                                try {
                                    Thread.sleep(1000);
                                } catch (InterruptedException e) {
                                    e.printStackTrace();
                                }
                                if (waitWifiEnabled(wifiManager)) {
                                    runOnUiThread(new Runnable() {
                                        @Override
                                        public void run() {
                                            if (progressDialog != null) {
                                                progressDialog.dismiss();
                                            }
                                            Intent intent = new Intent(Intent.ACTION_VIEW);
                                            intent.setData(Uri.parse("http://192.168.43.1:8080"));
                                            WifiAPActivity.this.startActivity(intent);

                                        }
                                    });
                                }
                            }
                        }.start();
                    }
                    break;
                case R.id.choose_dir:
                    Intent intent = new Intent(v.getContext(), ListDirActivity.class);
                    startActivityForResult(intent, 10);
                    break;
            }
        }
    }

    private void startShare() {
        {
            WifiInfo wifiInfo = wifiManager.getConnectionInfo() ; 
            String ssid = wifiInfo.getBSSID() ; 
            TextView shareDir = (TextView)findViewById(R.id.current_share_dir);
            shareDir.setText(getString(R.string.current_share_dir, webRoot));
            httpServer = new HttpServer(webRoot, dirBytes);
            TextView serverIP = (TextView)findViewById(R.id.share_ap_ip);
            TextView connectInfo = (TextView)findViewById(R.id.connect_info);
            if (wifiInfo.getIpAddress() != 0 && wifiInfo.getNetworkId() != 0) {
                connectInfo.setText(getString(R.string.to_connect, ssid));
                String ip = int2Ip(wifiInfo.getIpAddress()) + ":8080";
                host = "http://" + ip;
                serverIP.setText(host);
                httpServer.setHost(host);
                new Thread() {
                    public void run() {
                        mLock.acquire();
                        httpServer.await();
                    }
                }.start();
            } else {
                host = "http://192.168.43.1:8080";
                serverIP.setText(host);
                connectInfo.setText(getString(R.string.to_connect, "share_dir"));
                wifiOpen = wifiManager.isWifiEnabled();
                boolean wifiAp = setWifiApEnabled(true);
                if (wifiAp) {
                    configHttpServer();
                }
            }

        }

    }

    private static String int2Ip(int ip) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < 4; i++) {
            sb.append(ip & 0xff);
            ip = ip >> 8;
            if (i < 3)
                sb.append(".");
        }
        return sb.toString();
    }

    public boolean waitWifiEnabled(WifiManager wm) {
        int count = 0;
        while (true) {
            final WifiInfo info = wm.getConnectionInfo();
            if (info != null) {
                Logger.d("wifi ssid " + info.getSSID());
                if (SSID.equals(info.getSSID())) {
                    return true;
                }
            }
            count++;
            if (count > 100) {
                return false;
            }
            try {
                Thread.sleep(50);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }
    }

    private WifiConfiguration createWifiInfo(String SSID, String Password, WifiCipherType Type) {
        WifiConfiguration config = new WifiConfiguration();
        config.allowedAuthAlgorithms.clear();
        config.allowedGroupCiphers.clear();
        config.allowedKeyManagement.clear();
        config.allowedPairwiseCiphers.clear();
        config.allowedProtocols.clear();
        config.SSID = "\"" + SSID + "\"";
        if (Type == WifiCipherType.WIFICIPHER_NOPASS) {
            config.wepKeys[0] = "";
            // config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.LEAP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            // config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
            config.wepTxKeyIndex = 0;
        } else if (Type == WifiCipherType.WIFICIPHER_WEP) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            config.wepTxKeyIndex = 0;
        } else if (Type == WifiCipherType.WIFICIPHER_WPA) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
            config.status = WifiConfiguration.Status.ENABLED;
        } else if (Type == WifiCipherType.WIFICIPHER_WPA2) {
            config.preSharedKey = "\"" + Password + "\"";
            config.hiddenSSID = true;
            config.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            config.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
            config.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
            config.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
            config.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
            config.status = WifiConfiguration.Status.ENABLED;
        } else {
            return null;
        }
        return config;
    }

    public enum WifiCipherType {
        WIFICIPHER_WEP, WIFICIPHER_WPA, WIFICIPHER_NOPASS, WIFICIPHER_INVALID, WIFICIPHER_WPA2
    }

    private void configHttpServer() {
        new Thread() {
            public void run() {
                mLock.acquire();
                httpServer.await();
            }
        }.start();
        new Thread() {
            public void run() {
                int count = 0;
                while (count < 10) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    count++;
                    Process process = null;
                    try {
                        process = Runtime.getRuntime().exec("ip addr");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    if (process != null) {
                        LineNumberReader reader = new LineNumberReader(new InputStreamReader(process.getInputStream()));
                        String str = null;
                        try {
                            while ((str = reader.readLine()) != null) {
                                int index = str.indexOf("inet ");
                                if (index >= 0) {
                                    final String ip = str.substring(index + 5, str.indexOf('/'));
                                    if (!ip.equals("127.0.0.1") && !ip.equals("0.0.0.0")) {
                                        runOnUiThread(new Runnable() {
                                            @Override
                                            public void run() {
                                                if (!isFinishing()) {
                                                    TextView showIp = (TextView)findViewById(R.id.share_ap_ip);
                                                    host = "http://" + ip + ":8080";
                                                    showIp.setVisibility(View.VISIBLE);
                                                    httpServer.setHost(host);
                                                    showIp.setText(host);
                                                }
                                            }
                                        });
                                    }
                                }
                            }
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        process.destroy();
                    }
                }
            }
        }.start();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (resultCode == Activity.RESULT_OK) {
            TextView shareDir = (TextView)findViewById(R.id.current_share_dir);
            webRoot = data.getStringExtra(ListDirActivity.EXTRA_DIR);
            if (webRoot == null && hasSD()) {
                webRoot = Environment.getExternalStorageState();
            }
            shareDir.setText(getString(R.string.current_share_dir, webRoot));
            if (httpServer != null) {
                httpServer.setWebRoot(webRoot);
            }
        }
    }

    private boolean hasSD() {
        return Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED);
    }

    @Override
    public void onBackPressed() {
        showExitSureDialog();
    }

    private void showExitSureDialog() {
        final Dialog dialog = new Dialog(this, R.style.Dialog);
        dialog.setContentView(R.layout.dialog_sure_cancel);
        dialog.show();
        TextView message = (TextView)dialog.findViewById(R.id.dialog_message);
        message.setText("你确定要退出应用，并停止共享？");
        TextView cancel = (TextView)dialog.findViewById(R.id.dialog_cancel);
        cancel.setText("后台运行");
        cancel.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                moveTaskToBack(false);
            }
        });
        TextView sure = (TextView)dialog.findViewById(R.id.dialog_sure);
        sure.setText("退出应用");
        sure.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
                finish();
            }
        });
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        wifiAPActivity = null;
        if (httpServer != null) {
            if (wifiOpen)
                wifiManager.setWifiEnabled(wifiOpen);
            httpServer.close();
            if (!wifiManager.isWifiEnabled())
                setWifiApEnabled(false);
        }
        mLock.release();
        mLock = null;
    }

    public byte[] getVideos() {
        return videos;
    }

    public void setVideos(byte[] videos) {
        this.videos = videos;
    }

    public byte[] getAudios() {
        return audios;
    }

    public void setAudios(byte[] audios) {
        this.audios = audios;
    }

    public byte[] getFiles() {
        return files;
    }

    public void setFiles(byte[] files) {
        this.files = files;
    }

    // wifi�ȵ㿪��
    public boolean setWifiApEnabled(boolean enabled) {
        if (enabled) { // disable WiFi in any case
            // wifi���ȵ㲻��ͬʱ�򿪣����Դ��ȵ��ʱ����Ҫ�ر�wifi
            wifiManager.setWifiEnabled(false);
        }
        try {
            // �ȵ��������
            WifiConfiguration apConfig = new WifiConfiguration();
            apConfig.allowedAuthAlgorithms.clear();
            apConfig.allowedGroupCiphers.clear();
            apConfig.allowedKeyManagement.clear();
            apConfig.allowedPairwiseCiphers.clear();
            apConfig.allowedProtocols.clear();
            // �����ȵ�����(���������ֺ���ӵ������ʲô��)
            apConfig.SSID = SSID;
            // apConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
            // apConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
            // apConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.NONE);
            // Enterprise Settings
            // Reflection magic here too, need access to non-public APIs
            try {
                // Let the magic start
                Class[] wcClasses = WifiConfiguration.class.getClasses();
                // null for overzealous java compiler
                Class wcEnterpriseField = null;

                for (Class wcClass : wcClasses)
                    if (wcClass.getName().equals(INT_ENTERPRISEFIELD_NAME)) {
                        wcEnterpriseField = wcClass;
                        break;
                    }
                boolean noEnterpriseFieldType = false;
                if (wcEnterpriseField == null)
                    noEnterpriseFieldType = true; // Cupcake/Donut access enterprise settings directly

                Field wcefAnonymousId = null, wcefCaCert = null, wcefClientCert = null, wcefEap = null, wcefIdentity = null, wcefPassword = null, wcefPhase2 = null, wcefPrivateKey = null;
                Field[] wcefFields = WifiConfiguration.class.getFields();
                // Dispatching Field vars
                for (Field wcefField : wcefFields) {
                    if (wcefField.getName().equals(INT_ANONYMOUS_IDENTITY))
                        wcefAnonymousId = wcefField;
                    else if (wcefField.getName().equals(INT_CA_CERT))
                        wcefCaCert = wcefField;
                    else if (wcefField.getName().equals(INT_CLIENT_CERT))
                        wcefClientCert = wcefField;
                    else if (wcefField.getName().equals(INT_EAP))
                        wcefEap = wcefField;
                    else if (wcefField.getName().equals(INT_IDENTITY))
                        wcefIdentity = wcefField;
                    else if (wcefField.getName().equals(INT_PASSWORD))
                        wcefPassword = wcefField;
                    else if (wcefField.getName().equals(INT_PHASE2))
                        wcefPhase2 = wcefField;
                    else if (wcefField.getName().equals(INT_PRIVATE_KEY))
                        wcefPrivateKey = wcefField;
                }

                Method wcefSetValue = null;
                if (!noEnterpriseFieldType) {
                    for (Method m : wcEnterpriseField.getMethods())
                        // Logger.d(m.getName());
                        if (m.getName().trim().equals("setValue"))
                            wcefSetValue = m;
                }

                /* EAP Method */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefEap.get(apConfig), ENTERPRISE_EAP);
                } else {
                    if (apConfig != null && wcefEap != null)
                        wcefEap.set(apConfig, ENTERPRISE_EAP);
                }
                /* EAP Phase 2 Authentication */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefPhase2.get(apConfig), ENTERPRISE_PHASE2);
                } else {
                    if (wcefPhase2 != null)
                        wcefPhase2.set(apConfig, ENTERPRISE_PHASE2);
                }
                /* EAP Anonymous Identity */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefAnonymousId.get(apConfig), ENTERPRISE_ANON_IDENT);
                } else {
                    if (wcefAnonymousId != null)
                        wcefAnonymousId.set(apConfig, ENTERPRISE_ANON_IDENT);
                }
                /* EAP CA Certificate */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefCaCert.get(apConfig), ENTERPRISE_CA_CERT);
                } else {
                    if (wcefCaCert != null)
                        wcefCaCert.set(apConfig, ENTERPRISE_CA_CERT);
                }
                /* EAP Private key */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefPrivateKey.get(apConfig), ENTERPRISE_PRIV_KEY);
                } else {
                    if (wcefPrivateKey != null)
                        wcefPrivateKey.set(apConfig, ENTERPRISE_PRIV_KEY);
                }
                String userName = "";
                String passString = "";
                /* EAP Identity */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefIdentity.get(apConfig), userName);
                } else {
                    if (wcefIdentity != null)
                        wcefIdentity.set(apConfig, userName);
                }
                /* EAP Password */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefPassword.get(apConfig), passString);
                } else {
                    if (wcefPassword != null)
                        wcefPassword.set(apConfig, passString);
                }
                /* EAp Client certificate */
                if (!noEnterpriseFieldType) {
                    wcefSetValue.invoke(wcefClientCert.get(apConfig), ENTERPRISE_CLIENT_CERT);
                } else {
                    if (wcefClientCert != null)
                        wcefClientCert.set(apConfig, ENTERPRISE_CLIENT_CERT);
                }
                // Adhoc for CM6
                // if non-CM6 fails gracefully thanks to nested try-catch

                try {
                    Field wcAdhoc = WifiConfiguration.class.getField("SSID");
                    // Field wcAdhocFreq = WifiConfiguration.class.getField("frequency");
                    // wcAdhoc.setBoolean(apConfig, prefs.getBoolean(PREF_ADHOC,
                    // false));
                    if(Build.VERSION.SDK_INT<20){
                        wcAdhoc.setBoolean(apConfig, false);
                    }
                    else{
                        
                    }
                    // int freq = 2462; // default to channel 11
                    // int freq = Integer.parseInt(prefs.getString(PREF_ADHOC_FREQUENCY,
                    // "2462")); // default to channel 11
                    // System.err.println(freq);
                    // wcAdhocFreq.setInt(apConfig, freq);
                } catch (Exception e) {
                    e.printStackTrace();
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
            Method method = wifiManager.getClass().getMethod("setWifiApEnabled", WifiConfiguration.class, Boolean.TYPE);
            return (Boolean)method.invoke(wifiManager, apConfig, enabled);
        } catch (Exception e) {
            e.printStackTrace() ; 
            return false;
        }
    }
}
