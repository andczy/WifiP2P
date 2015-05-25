package com.czy.wifiap;
import java.io.InputStream;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;

import org.czy.log.Logger;

public class Request {

	public static final String UPLOAD_FILE = "/_upload_czy_file" ;
  private InputStream input;
  private String uri;
  private String fileName ;
  private int contentLength ;
  private String boundary ;
  private String moreContent ; 
  public Request(InputStream input) {
    this.input = input;
  }
  public InputStream getInputStream(){
	  return input ;
  }
  public void parse() {
    // Read a set of characters from the socket
    StringBuffer request = new StringBuffer(1024);
    int i;
    byte[] buffer = new byte[1024];
    try {
      i = input.read(buffer);
    }
    catch (IOException e) {
      e.printStackTrace();
      i = -1;
    }
    for (int j=0; j<i; j++) {
      request.append((char) buffer[j]);
    }
    if(BuildConfig.DEBUG)
    	Logger.e(i+"  request header = "+request) ;
    String req = request.toString() ;
    uri = parseUri(req);
    if(uri == null)
        uri = "" ; 
    uri.replaceAll("/null", "");
    if(uri.endsWith(UPLOAD_FILE)){
    	contentLength = parseLength(req) ;
    	boundary = parseBoundary(req) ;
    	String line = "\r\n\r\n" ; 
    	int index = req.indexOf(line) ;
    	Logger.e(" first line index = "+index) ;
    	if(index>0){
    		moreContent = req.substring(index+line.length()) ;
    	}
    	Logger.e("more content ="+moreContent) ; 
    }
    else{
    	try {
			uri = URLDecoder.decode(uri , "utf-8") ;
		} catch (UnsupportedEncodingException e) {
			e.printStackTrace();
		}
    }
    Logger.e("request uri = "+uri+" contentLength =  "+contentLength+"  boundary = "+boundary);
  }
  public void clearMore(){
	  moreContent = null ; 
  }
  public String getMoreContent(){
	  return moreContent ; 
  }
  private String parseBoundary(String requestString){
	  String i = "boundary=" ;
	  int index = requestString.indexOf(i) ;
	  if(index>0){
		  index += i.length() ;
		  int content = requestString.indexOf("\r\n",index) ;
		  if(content>0){
			  return requestString.substring(index, content) ;
		  }
	  }
	  return "" ;
  }
  public String getBounday(){
	  return boundary ;
  }
  private int parseLength(String requestString){
	  String i = "Content-Length:" ;
	  int index = requestString .indexOf(i);
	  if(index>0){
		  index += i.length() ;
		  int content = requestString.indexOf("\n",index) ;
		  if(content>0){
			  String contentLength = requestString.substring(index , content) ;
			  contentLength = contentLength.trim() ;
			  int length = 0 ;
			  try{
				 length = Integer.valueOf(contentLength) ;
			  }catch(NumberFormatException e){}
			  Logger.d("parse content length = "+length) ;
			  return length ;
		  }
		  
	  }
	  return 0 ;
  }
  public String getFileName(){
	  return fileName ;
  }
  private String parseUri(String requestString) {
    int index1, index2;
    index1 = requestString.indexOf(' ');
    if (index1 != -1) {
      index2 = requestString.indexOf(' ', index1 + 1);
      if (index2 > index1)
        return requestString.substring(index1 + 1, index2);
    }
    return "";
  }

  public String getUri() {
    return uri;
  }
	public int getContentLength() {
		return contentLength;
	}

}