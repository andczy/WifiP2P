package com.czy.wifiap;

import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberInputStream;
import java.io.LineNumberReader;
import java.io.OutputStream;
import java.io.IOException;
import java.io.FileInputStream;
import java.io.File;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.net.Socket;
import java.net.URLEncoder;
import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.czy.log.Logger;

import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.PixelFormat;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Environment;
import android.provider.MediaStore;
import android.util.DisplayMetrics;
import android.util.Log;

/*
 HTTP Response = Status-Line
 *(( general-header | response-header | entity-header ) CRLF)
 CRLF
 [ message-body ]
 Status-Line = HTTP-Version SP Status-Code SP Reason-Phrase CRLF
 */

public class Response {

	private static final int BUFFER_SIZE = 1024;
	private static final String DIR_FILE = "dir.bitmap";
	private static final String VIDEO_FILE = "video.bitmap";
	private static final String AUDIO_FILE = "audio.bitmap";
	private static final String FILE_FILE = "file.bitmap";
	private static final String SEARCH_LABEL = "_czy_search" ;
	private static final String APK_FILE = "apk.bitmap"  ;
	Request request;
	OutputStream output;
	private String webRoot;
	private String host;
	private byte [] dirBytes ;
	public Response(OutputStream output, String root, String host , byte [] dirs ) {
		this.output = output;
		webRoot = root;
		this.host = host;
		this.dirBytes = dirs ;
	}

	public void setRequest(Request request) {
		this.request = request;
	}

	private void writeHeaders(long contentLength) {
		// application/vnd.ms-excel excel�ļ�
		// application/msword word�ĵ�
		// image/*
		// video/*
		// audio/*
		// application/pdf
		// application/x-png
		writeHeaders(contentLength, "text/html");
	}

	private void writeHeaders(long contentLength, String contentType) {
		StringBuffer sb = new StringBuffer();
		sb.append("HTTP/1.1 200 OK\r\n");
		if (contentType != null) {
			sb.append("Content-Type:");
			sb.append(contentType);
			sb.append(";");
			sb.append("charset=utf-8");
			sb.append("\r\n");
		}
		sb.append("Content-Length:");
		sb.append(contentLength);
		sb.append("\r\n");
		sb.append("Connection: close");
		// �ض��������ļ���
		// sb.append("\r\n");
		// sb.append("Content-Disposition:");
		// sb.append("attachment; filename=xijiao_2016.apk");
		sb.append("\r\n");
		sb.append("\r\n");
		Logger.d("send header = "+sb.toString());
		try {
			output.write(sb.toString().getBytes("utf-8"));
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	private String returnHtmlString(File dir) {
		if(dir.isFile()){
			return returnHtmlString(new File[]{dir}) ;
		}
		else{
			File [] files = dir.listFiles() ; 
			return returnHtmlString(sortFileByName(files)) ;
		}
	}
	
	public static File[] sortFileByName(File [] files){
		long time = System.currentTimeMillis() ;  
		List<File> dirs = new ArrayList<File>() ; 
		List<File> fs = new ArrayList<File>() ; 
		for(File f : files){
			if(f.isFile())
				fs.add(f) ; 
			else
				dirs.add(f) ; 
		}
		Collections.sort(dirs, new Comparator<File>(){
			@Override
			public int compare(File lhs, File rhs) {
				String name1 = lhs.getName().toLowerCase() ; 
				String name2 = rhs.getName().toLowerCase() ;  
				for(int i = 0  ; i < name1.length() ; i++){
					if(i>=name2.length()){
						return 1 ; 
					}
					else{
						byte b1 = (byte) name1.charAt(i) ; 
						byte b2 = (byte) name2.charAt(i) ; 
						if(b1>b2){
							return 1 ; 
						}
						else if(b1<b2){
							return -1 ; 
						}
					}
				}
				return -1;
			}
		}) ;
		Collections.sort(fs, new Comparator<File>(){
			@Override
			public int compare(File lhs, File rhs) {
				String name1 = lhs.getName().toLowerCase() ; 
				String name2 = rhs.getName().toLowerCase();  
				for(int i = 0  ; i < name1.length() ; i ++){
					if(i>=name2.length()){
						return 1 ; 
					}
					else{
						byte b1 = (byte) name1.charAt(i) ; 
						byte b2 = (byte) name2.charAt(i) ; 
						if(b1>b2){
							return 1 ; 
						}
						else if(b1<b2){
							return -1 ; 
						}
					}
				}
				return -1;
			}
		}) ;
		for(int i = 0 ; i < dirs.size() ; i ++){
			files[i] = dirs.get(i) ; 
		}
		for(int i = 0 ; i < fs.size() ; i ++){
			files[i+dirs.size()] = fs.get(i) ; 
		}
		Logger.d("sort file spend time = "+(System.currentTimeMillis() - time)) ;
		return files ;
	}
	private String returnHtmlString(File[] fs) {
		
		StringBuffer sb = new StringBuffer();
		if(fs == null){
			return sb.toString() ;
		}
		sb.append("<!DOCTYPE html>") ;
		sb.append("<html><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>");
		sb.append("<body>");
		sb.append("<style>");
		sb.append(".item:link,.item:visited{padding:1% ;display:block;width:100%;height:3rem;padding:4px;text-decoration:none;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;} .item:hover,.item:active{background-color:#7A991A;}");
		sb.append("img{width:3rem;height:3rem;vertical-align:middle;}");
//		sb.append(".parentDir a{display:block;width:100% ;height:3rem;padding:4px;text-decoration:none;overflow:hidden;}");
		sb.append(".right{float:right;width:auto;margin-top:-1rem;margin-right:0.4rem;}");
		sb.append(".top { z-index: 3;width: 100%; height: 3rem;text-align:center; line-height: 2rem; position: relative; background-color: #fafafa; color: #ffffff; position: fixed; top: 0px; }");
		sb.append("</style>") ;
		
		sb.append("<div class=\"top\">");
		sb.append("<form action=\""+SEARCH_LABEL+"\" method=\"get\" style=\"width:98%\" accept-charset=\"utf-8\" onsubmit=\"document.charset='utf-8';\">");
		sb.append("<input type=\"text\" name=\"search\" style=\"width:78%;height:1.6rem;\"/>");
		sb.append("<input type=\"submit\" value=\"搜索\" style=\"width:20%;height:1.8rem;\" />") ;
		sb.append("</form>") ;
		sb.append("</div>");
		sb.append("<div style=\"margin-top:3.1rem ;\">");
		if(App.getInstance().isCanUpload()){
			//上传
			sb.append("<div>");
			sb.append("<form action=\"_upload_czy_file\" method=\"post\" enctype=\"multipart/form-data\">");
			sb.append("<input type=\"file\" name=\"file_name\"/>");
			sb.append("<input type=\"submit\" value=\"开始上传\" />") ;
			sb.append("</form>") ;
			sb.append("</div>") ;
			sb.append("<hr/>");
		}
		File parentFile = fs[0].getParentFile() ; 
		if(parentFile!=null){
			parentFile = parentFile.getParentFile() ; 
			if(parentFile!=null){
				String path = parentFile.getAbsolutePath() ;
				if(path.indexOf(webRoot)>=0){
					sb.append("<div style=\"height:2.4rem;\">"); 
					sb.append("<a class=\"item\" href='");
					sb.append(host + path.replace(webRoot, ""));
					sb.append("'>");
					sb.append("上一层目录");
					sb.append("</a>");
					sb.append("</div>");
				}
			}
		}
		sb.append("<hr/>");
		if (fs != null && fs.length > 0) {
			for (File f : fs) {
				String path = f.getAbsolutePath().replace(webRoot, "");
				sb.append("<div>");
				sb.append("<a class=\"item\" href='");
				sb.append(host + path);
				sb.append("'>");
				sb.append("<img src='");
				if(f.isDirectory()){
					sb.append(host+"/");
					sb.append(DIR_FILE);
				}
				else if(f.isFile()){
					String name = f.getName().toLowerCase()	;
					if(isVideoFile(name)){
						sb.append(host+"/");
						sb.append(VIDEO_FILE);
					}
					else if(isAudioFile(name)){
						sb.append(host+"/");
						sb.append(AUDIO_FILE);
					}
					else if(isBitmapFile(name)){
						sb.append(host);
						sb.append(path);
					}
					else if(name.endsWith(".apk")){
						sb.append(host) ; 
						sb.append("/") ; 
						sb.append(APK_FILE) ; 
						sb.append("?apk_path=") ; 
						sb.append(f.getAbsolutePath()) ;
					}
					else {
						sb.append(host+"/");
						sb.append(FILE_FILE);
					}
				}
				sb.append("'");
				sb.append("/>");
				sb.append(f.getName());
				sb.append("</a>");
				if(f.isFile()){
					sb.append("<div class=\"right\">") ;
					sb.append(getFileSize(f.length())) ;
					sb.append("</div>") ;
				}
				sb.append("</div>");
				sb.append("<hr/>");
			}
		}
		sb.append("</div></body></html>");
		Logger.d("html = "+sb.toString()) ;
		return sb.toString();
	}
	private String getFileSize(float fileSize){
		String unit = "B" ;
		if(fileSize>1024){
			fileSize = fileSize / 1024 ;
			unit = "KB" ;
		}
		if(fileSize>1024){
			fileSize = fileSize / 1024 ;
			unit = "MB" ;
		}
		if(fileSize>1024){
			fileSize = fileSize / 1024 ;
			unit = "GB" ;
		}
		DecimalFormat df = new DecimalFormat("0.0") ;
		return df.format(fileSize)+unit ; 
	}
	private String returnHtmlString(String content){
		StringBuffer sb = new StringBuffer();
		sb.append("<html><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><body><p>");
		sb.append(content);
		sb.append("</p></body></html>");
		return sb.toString();
	}
	private String getPictureHtml(List<File> pictures){
		StringBuffer sb = new StringBuffer();
		sb.append("<html><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/><body>");
		for(File f:pictures){
			sb.append("<div>");
			sb.append("<img src=\"");
			sb.append(host+f.getAbsolutePath().replace(webRoot, ""));
			sb.append("\"/>");
			sb.append("</div>");
		}
		sb.append("</body></html>");
		Logger.d("picture html ="+sb.toString());
		return sb.toString() ;
	}
	private boolean isVideoFile(String name){
		return name.endsWith("mp4") || name.endsWith("3gp")
				|| name.endsWith("mov") || name.endsWith("mkv")
				|| name.endsWith("rm") || name.endsWith("rmvb")
				|| name.endsWith("wv") || name.endsWith("avi") ;
	}
	private boolean isAudioFile(String name){
		return name.endsWith("mp3")||name.endsWith("ape")||name.endsWith("amr")||name.endsWith("wav");
	}
	private String getFileContentType(String name) {
		String type = null;
		name = name.toLowerCase();
		if (name.endsWith("jpg") || name.endsWith("jpeg")
				|| name.endsWith("png") || name.endsWith("bmp")
				|| name.endsWith("jpe")) {
			type = "image/jpg";
		} else if (isVideoFile(name)) {
			type = "video/mp4";

		} else if (name.endsWith("xls")) {
			type = "application/vnd.ms-excel";
		} else if (name.endsWith("doc") || name.endsWith("docx")
				|| name.endsWith("wps")) {
			type = "application/msword";
		}
		else if(name.endsWith("txt")||name.endsWith("log")){
			type = "text/plain";
		}
		else if(name.endsWith("html")||name.endsWith("htm")||name.endsWith("jsp")||name.endsWith("js")||name.endsWith("xhtml"))
		{
			type = "text/html";
		}
		else if(isAudioFile(name)){
			type ="video/mp3" ;
		}
		else {
			type = "application/octet-stream";
		}
		return type;
	}
	private String getChineseHttpPath(String str){
		String []ps = str.split("/") ; 
		for(int k = 0 ; k < ps.length ; k ++){
			String p = ps[k];
			if(p.contains("%")){
				int i = p.indexOf('%') ;
				int j = i + 3 ;
				String nStr = "" ;
				while(true){
					while(p.length()>0&&p.charAt(j)=='%'){
						j+=3 ;
						if(j>=p.length()){
							ps[k] = nStr + p ;
							break ;
						}
					}
					{
						String start = "";
						if(i>0)
							start = p.substring(0 , i);
						String middle ="" ;
						if(j>i)
						{
							middle = p.substring(i , j) ;
							String []ms = middle.split("%");
							byte [] mbs = new byte[ms.length-1] ;
							for(int m = 0 ;  m < mbs.length ;  m ++){
								mbs[m] = Integer.decode("0x"+ms[m+1]).byteValue() ;
							}
							try {
								middle = new String(mbs , "utf-8");
							} catch (UnsupportedEncodingException e) {
								e.printStackTrace();
								
							}
							Logger.d("middle 2 ="+middle);
						}
						nStr = nStr + start + middle ;
						p = p.substring(j);
						i = p.indexOf('%') ;
						if(i < 0){
							ps[k] = nStr + p ;
							break ;
						}
						j = i + 3 ;
						if(p.length() ==0){
							ps[k] = nStr + p ;
							break ;
						}
					}
				}
				ps[k] = nStr+p ;
			}
		}
		str = "" ;
		for(int i = 0 ; i < ps.length ; i++){
			String s = ps[i];
			str+=s  ;
			if(i<ps.length -1)
				str+="/";
		}
		return str ;
	}
	private boolean isBitmapFile(String name){
		return name.endsWith("jpg")||name.endsWith("png")||name.endsWith("jpeg")||name.endsWith("gif")||name.endsWith("bmp") ;
	}
	private void writeFileHtml(FileInputStream fis ,File file) throws IOException{
		if (file.isFile()) {
			byte[] bytes = null;
			fis = new FileInputStream(file);
			String name = file.getName().toLowerCase() ;
			if(name.endsWith("txt")||name.endsWith("log")|name.endsWith("xml")){
				bytes = new byte[fis.available()] ;
				fis.read(bytes) ;
				fis.close() ;
				String htmlString = returnHtmlString(new String(bytes,"utf-8"));
				byte[] htmls = htmlString.getBytes("utf-8");
				writeHeaders(htmls.length);
				output.write(htmls);
			}
//			else if(isBitmapFile(name)){
//				File []fs = file.getParentFile().listFiles() ;
//				List<File> files = new ArrayList<File>() ;
//				if(fs!=null){
//					for(File f:fs){
//						if(isBitmapFile(f.getName())){
//							files.add(f);
//						}
//					}
//				}
//				if(files.size()>0){
//					String htmlString = getPictureHtml(files);
//					byte[] htmls = htmlString.getBytes("utf-8");
//					writeHeaders(htmls.length);
//					output.write(htmls);
//				}
//				else{
//					writeHeaders(file.length(),
//							getFileContentType(file.getName()));
//					bytes = new byte[BUFFER_SIZE] ;
//					int ch = fis.read(bytes, 0, BUFFER_SIZE);
//					while (ch != -1) {
//						output.write(bytes, 0, ch);
//						ch = fis.read(bytes, 0, BUFFER_SIZE);
//					}
//				}
//			}
			else{
				Logger.d("write file = "+file.getAbsolutePath());
				writeHeaders(file.length(),
						getFileContentType(file.getName()));
				bytes = new byte[BUFFER_SIZE] ;
				int ch = fis.read(bytes, 0, BUFFER_SIZE);
				while (ch != -1) {
					output.write(bytes, 0, ch);
					ch = fis.read(bytes, 0, BUFFER_SIZE);
				}
			}
		} else {
			String htmlString = returnHtmlString(file);
			byte[] htmls = htmlString.getBytes("utf-8");
			writeHeaders(htmls.length);
			output.write(htmls);
		}
	}
	private void receiveFile(){
		Logger.e("start receiver read ...."+request.getBounday());
		if(request.getBounday() == null){
			byte[] bs = returnHtmlString("上传失败！").getBytes() ;
			writeHeaders(bs.length) ;
			try {
				output.write(bs) ;
			} catch (IOException e) {
				e.printStackTrace();
			}
			return ;
		}
		Logger.e("upload go on ...") ;
		InputStream in = null ;
		in = request.getInputStream() ;
		String name = null ;
		int len = 0 ,count = 0 ;
		byte [] buf = new byte[4096] ;
		byte [] leaveBuf = null ;
		int oldLine = 0 ;
		boolean exit = true ;
		while(exit){
			if(request.getMoreContent()!=null&&request.getMoreContent().length()>0){
				buf = request.getMoreContent().getBytes() ; 
				len = buf.length ; 
				request.clearMore() ;
			}
			else{
				try {
					len = in.read(buf) ;
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
			Logger.e("read header = "+new String(buf));
//			count += len ;
			for(int i = 0 ; i < len ; i++){
				if(buf[i] == 13 && buf[i+1] == 10 ){
					i ++ ;
					String fileInfo = null;
						fileInfo = new String(buf , oldLine , i);
					String fileName = "filename=\"" ;
					int index = fileInfo.indexOf(fileName);
					if(index>0){
						index = index + fileName.length()  ;
						name = fileInfo.substring(index,fileInfo.indexOf("\"" , index)) ;
					}
					if(i - oldLine ==1){
						
						if(buf[i+1] == -13 || buf [i+1] == -10){
							i = i+ 1 ;
						}
						i = i+1 ;
						leaveBuf = new byte[len - i] ;
						for(int j = 0 ; j < leaveBuf.length ; j++){
							leaveBuf[j] = buf[i+j] ;
						}
						exit = false ;
						break ;
					}
					oldLine = i+1;
				}
			}
		}
		Logger.e("level data = "+(leaveBuf == null?null:new String(leaveBuf)));
		StringBuffer sb1 = new StringBuffer() ; 
		for(byte b :leaveBuf){
			sb1.append(b) ;
			
		}
		Logger.e("level buf = "+sb1.toString()) ;
		Logger.e("file name = "+name) ; 
		try {
			File uploadDir = new File(Environment.getExternalStorageDirectory(), "share_czy_upload");
			if(!uploadDir.exists())
				uploadDir.mkdir() ;
			File uploadSave = new File(uploadDir.getAbsoluteFile()+"/"+name) ;
			if(uploadSave.exists())
				uploadSave.delete() ; 
			OutputStream out = new FileOutputStream(uploadSave);
			if(leaveBuf!=null){
				out.write(leaveBuf) ;
				count += leaveBuf.length ;
			}
			while(count < request.getContentLength()){
				len = in.read(buf) ;
				count +=len ;
				if(request.getContentLength() - count <= 4096){
					StringBuffer sb = new StringBuffer() ; 
					for(byte b :buf)
						sb.append(b) ;
					Logger.d("last str .."+new String(buf));
					Logger.e("buf = " +sb.toString()) ;
					String bounday = request.getBounday() ; 
					bounday = bounday.substring(4) ;
					byte find[] = bounday.getBytes() ; 
					sb = new StringBuffer() ; 
					for(byte b :find)
						sb.append(b) ;
					Logger.e("end label = " +sb.toString()) ;
					int findEnd = 0 ; 
					for(int i = 0 ; i < buf.length ; i++){
						if(buf[i]==find[findEnd]){
							findEnd++ ;
							if(findEnd==find.length -1){
								findEnd = i - find.length -6 ;
								break ;
							}
						}
						else{
							findEnd = 0 ; 
						}
					}
					if(findEnd > 0){
						byte []endBuf = new byte[findEnd] ;
						for(int i = 0 ; i < findEnd ; i++)
							endBuf[i] = buf[i] ;
						sb = new StringBuffer() ; 
						for(byte b :endBuf)
							sb.append(b) ;
						Logger.e("write last = " +sb.toString()) ;
						out.write(endBuf) ;
						break ;
					}
				}
				out.write(buf , 0 , len) ;
			}
			out.flush() ;
			out.close() ;
			byte[] bs = returnHtmlString("上传成功").getBytes() ;
			writeHeaders(bs.length) ;
			output.write(bs) ;
			Logger.e("upload over ");
		} catch (IOException e) {
			Logger.e(e.toString());
		}
		
	}
	private ByteArrayOutputStream getApkIconStream(Drawable drawable){
//		Bitmap bitmap = Bitmap
//				.createBitmap(
//						drawable.getIntrinsicWidth(),
//						drawable.getIntrinsicHeight(),
//						drawable.getOpacity() != PixelFormat.OPAQUE ? Bitmap.Config.ARGB_8888
//								: Bitmap.Config.RGB_565);
//		Canvas canvas = new Canvas(bitmap);
//		// canvas.setBitmap(bitmap);
//		drawable.setBounds(0, 0, drawable.getIntrinsicWidth(),
//				drawable.getIntrinsicHeight());
//		drawable.draw(canvas);
		ByteArrayOutputStream stream = new ByteArrayOutputStream() ; 
		if(drawable instanceof BitmapDrawable){
			Bitmap bmp = ((BitmapDrawable)drawable).getBitmap() ; 
			if(bmp!=null){
				bmp.compress(Bitmap.CompressFormat.PNG, 100, stream) ; 
			}
		}
		return stream ; 
	}
	private Drawable showUninstallAPKIcon(String apkPath) {  
        String PATH_PackageParser = "android.content.pm.PackageParser";  
        String PATH_AssetManager = "android.content.res.AssetManager";  
        try {  
            // apk包的文件路径  
            // 这是一个Package 解释器, 是隐藏的  
            // 构造函数的参数只有一个, apk文件的路径  
            // PackageParser packageParser = new PackageParser(apkPath);  
            Class pkgParserCls = Class.forName(PATH_PackageParser);  
            Class[] typeArgs = new Class[1];  
            typeArgs[0] = String.class;  
            Constructor pkgParserCt = pkgParserCls.getConstructor(typeArgs);  
            Object[] valueArgs = new Object[1];  
            valueArgs[0] = apkPath;  
            Object pkgParser = pkgParserCt.newInstance(valueArgs);  
            Log.d("ANDROID_LAB", "pkgParser:" + pkgParser.toString());  
            // 这个是与显示有关的, 里面涉及到一些像素显示等等, 我们使用默认的情况  
            DisplayMetrics metrics = new DisplayMetrics();  
            metrics.setToDefaults();  
            // PackageParser.Package mPkgInfo = packageParser.parsePackage(new  
            // File(apkPath), apkPath,  
            // metrics, 0);  
            typeArgs = new Class[4];  
            typeArgs[0] = File.class;  
            typeArgs[1] = String.class;  
            typeArgs[2] = DisplayMetrics.class;  
            typeArgs[3] = Integer.TYPE;  
            Method pkgParser_parsePackageMtd = pkgParserCls.getDeclaredMethod("parsePackage",  
                    typeArgs);  
            valueArgs = new Object[4];  
            valueArgs[0] = new File(apkPath);  
            valueArgs[1] = apkPath;  
            valueArgs[2] = metrics;  
            valueArgs[3] = 0;  
            Object pkgParserPkg = pkgParser_parsePackageMtd.invoke(pkgParser, valueArgs);  
            // 应用程序信息包, 这个公开的, 不过有些函数, 变量没公开  
            // ApplicationInfo info = mPkgInfo.applicationInfo;  
            Log.d("ANDROID_LAB" , " package = "+pkgParserPkg) ;
            Field appInfoFld = pkgParserPkg.getClass().getDeclaredField("applicationInfo");  
            ApplicationInfo info = (ApplicationInfo) appInfoFld.get(pkgParserPkg);  
            // uid 输出为"-1"，原因是未安装，系统未分配其Uid。  
            Log.d("ANDROID_LAB", "pkg:" + info.packageName + " uid=" + info.uid);  
            // Resources pRes = getResources();  
            // AssetManager assmgr = new AssetManager();  
            // assmgr.addAssetPath(apkPath);  
            // Resources res = new Resources(assmgr, pRes.getDisplayMetrics(),  
            // pRes.getConfiguration());  
            Class assetMagCls = Class.forName(PATH_AssetManager);  
            Constructor assetMagCt = assetMagCls.getConstructor((Class[]) null);  
            Object assetMag = assetMagCt.newInstance((Object[]) null);  
            typeArgs = new Class[1];  
            typeArgs[0] = String.class;  
            Method assetMag_addAssetPathMtd = assetMagCls.getDeclaredMethod("addAssetPath",  
                    typeArgs);  
            valueArgs = new Object[1];  
            valueArgs[0] = apkPath;  
            assetMag_addAssetPathMtd.invoke(assetMag, valueArgs);  
            Resources res = App.getInstance().getResources();  
            typeArgs = new Class[3];  
            typeArgs[0] = assetMag.getClass();  
            typeArgs[1] = res.getDisplayMetrics().getClass();  
            typeArgs[2] = res.getConfiguration().getClass();  
            Constructor resCt = Resources.class.getConstructor(typeArgs);  
            valueArgs = new Object[3];  
            valueArgs[0] = assetMag;  
            valueArgs[1] = res.getDisplayMetrics();  
            valueArgs[2] = res.getConfiguration();  
            res = (Resources) resCt.newInstance(valueArgs);  
//            CharSequence label = null;  
//            if (info.labelRes != 0) {  
//                label = res.getText(info.labelRes);  
//            }  
            // if (label == null) {  
            // label = (info.nonLocalizedLabel != null) ? info.nonLocalizedLabel  
            // : info.packageName;  
            // }  
            // 这里就是读取一个apk程序的图标  
            if (info.icon != 0) {  
                Drawable icon = res.getDrawable(info.icon);  
                return icon ;   
            }  
        } catch (Exception e) {  
            e.printStackTrace();  
        }  
        return null ; 
    }  
	public void sendStaticResource() throws IOException {
		Logger.d("request uri = "+request.getUri()) ;
		if(App.getInstance().isCanUpload()&&request.getUri().endsWith(Request.UPLOAD_FILE)){
			receiveFile() ;
			return ;
		}
		else if(request.getUri().contains(SEARCH_LABEL)){  //搜索文件
			
			String search = request.getUri().substring(request.getUri().indexOf("search=")+7) ;
			Logger.d("search ="+search) ; 
			sendSearchHtml(search) ;
			return ;
		}
		else if(request.getUri().contains(APK_FILE)){
			String apkPath = request.getUri().substring(request.getUri().indexOf("apk_path=")+9) ;
			Logger.d("apk path = "+apkPath) ; 
			ByteArrayOutputStream stream = getApkIconStream(showUninstallAPKIcon(apkPath)) ;
			writeHeaders(stream.size(),
					"image/jpg");
			output.write(stream.toByteArray()) ;
		}
		FileInputStream fis = null;
		File file = new File(webRoot, request.getUri());
		Logger.d(webRoot+" request file = " + file);
		try {
			String name = file.getName() ;
			if (file.exists()) {
				writeFileHtml(fis , file);
				// output.write("/SHUTDOWN".getBytes());
			} 
			else if(name.equals(DIR_FILE))
			{
				writeHeaders(dirBytes.length,
						"image/jpg");
				Logger.d("write dir jpg "+dirBytes.length);
				output.write(dirBytes, 0, dirBytes.length);
			}
			else if(name.equals(FILE_FILE)){
				byte [] files = WifiAPActivity.getInstance().getFiles() ;
				writeHeaders(files.length,
						"image/jpg");
				output.write(files, 0, files.length);
			}
			else if(name.equals(VIDEO_FILE)){
				byte [] files = WifiAPActivity.getInstance().getVideos() ;
				writeHeaders(files.length,
						"image/jpg");
				output.write(files, 0, files.length);
			}
			else if(name.equals(AUDIO_FILE)){
				byte [] files = WifiAPActivity.getInstance().getAudios() ;
				writeHeaders(files.length,
						"image/jpg");
				output.write(files, 0, files.length);
			}
			else {
				// file not found
				// String errorMessage = "HTTP/1.1 404 File Not Found\r\n" +
				// "Content-Type: text/html\r\n" +
				// "Content-Length: 23\r\n" +
				// "\r\n" +
				// "<h1>File Not Found!!!</h1>";
				String path = file.getAbsolutePath() ;
				path = getChineseHttpPath(path);
				Logger.d("chinese path = "+path);
				file = new File(path) ;
				if(file.exists())
					writeFileHtml(fis , file);
				else{
					String htmlString = returnHtmlString(new File(webRoot));
					byte[] htmls = htmlString.getBytes("utf-8");
					writeHeaders(htmls.length);
					output.write(htmls);
				}
			}
		} catch (Exception e) {
			// thrown if cannot instantiate a File object
			Logger.d("wriet error = " + e.toString());
		} finally {
			if (fis != null)
				fis.close();
		}
	}

	private void sendSearchHtml(String search) {
		Cursor cursor = App.getInstance().getContentResolver().query(MediaStore.Files.getContentUri("external")
				, new String[]{ MediaStore.Files.FileColumns.DATA
			,MediaStore.Files.FileColumns.SIZE }, MediaStore.Files.FileColumns.DATA +" like ? and "
				+MediaStore.Files.FileColumns.DATA+" like ?", new String[]{"%"+search+"%" , webRoot+"%"}, null); 
		Logger.e(MediaStore.Files.getContentUri("external")+"  content cursor = "+cursor) ;
		StringBuffer sb = new StringBuffer();
		sb.append("<!DOCTYPE html>") ;
		sb.append("<html><meta name=\"viewport\" content=\"width=device-width, initial-scale=1\"/>");
		sb.append("<body>");
		sb.append("<style>");
		sb.append(".item:link,.item:visited{padding:1% ;display:block;width:100%;height:3rem;padding:4px;text-decoration:none;overflow:hidden;text-overflow:ellipsis;white-space:nowrap;} .item:hover,.item:active{background-color:#7A991A;}");
		sb.append("img{width:3rem;height:3rem;vertical-align:middle;}");
		sb.append(".right{float:right;width:auto;margin-top:-1rem;margin-right:0.4rem;}");
		sb.append("</style>") ;
		if(cursor!=null){
			while(cursor.moveToNext()){
				String allPath = cursor.getString(0) ; 
				long fileLength = cursor.getLong(1) ;
				Logger.e(allPath+"  "+fileLength) ;
				String path = allPath.replace(webRoot, "");
				sb.append("<div>");
				sb.append("<a class=\"item\" href='");
				sb.append(host + path);
				Logger.d("path = "+host+"  "+path) ;
				sb.append("'>");
				String name = path.substring(path.lastIndexOf("/")+1) ;
				sb.append("<img src='");
				{
					if(isVideoFile(name)){
						sb.append(host+"/");
						sb.append(VIDEO_FILE);
					}
					else if(isAudioFile(name)){
						sb.append(host+"/");
						sb.append(AUDIO_FILE);
					}
					else if(name.endsWith(".apk")){
						sb.append(host) ; 
						sb.append("/") ; 
						sb.append(APK_FILE) ; 
						sb.append("?apk_path=") ; 
						sb.append(allPath) ;
					}
					else if(isBitmapFile(name)){
						sb.append(host);
						sb.append(path);
					}
					else {
						sb.append(host+"/");
						sb.append(FILE_FILE);
					}
				}
				sb.append("'");
				sb.append("/>");
				sb.append(name);
				sb.append("</a>");
				sb.append("<div class=\"right\">") ;
				sb.append(getFileSize(fileLength)) ;
				sb.append("</div>") ;
				sb.append("</div>");
				sb.append("<hr/>");
			
			}
		}
		sb.append("</body></html>") ;
		byte []html = sb.toString().getBytes() ; 
		writeHeaders(html.length);
		try {
			output.write(html) ;
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
