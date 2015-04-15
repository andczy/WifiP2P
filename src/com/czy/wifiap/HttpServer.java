package com.czy.wifiap;
import java.net.Socket;
import java.net.ServerSocket;
import java.net.InetAddress;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.IOException;
import java.io.File;

import org.czy.log.Logger;

public class HttpServer
{
	/**
	 * WEB_ROOT is the directory where our HTML and other files reside. For
	 * this package, WEB_ROOT is the "webroot" directory under the working
	 * directory. The working directory is the location in the file system
	 * from where the java command was invoked.
	 */
	// shutdown command
	private static final String SHUTDOWN_COMMAND = "/SHUTDOWN";
	// the shutdown command received
	private boolean shutdown = false;
	private String webRoot ;
	private ServerSocket serverSocket ;
	private Socket socket = null;
	private String host = "";
	private byte [] dirBytes ;
	public HttpServer(String webRoot , byte []dirBytes){
		this.webRoot = webRoot ;
		this.dirBytes = dirBytes ;
	}
	public void setHost(String host){
		this.host = host ;
		Logger.d("http server host = "+host) ; 
	}
	public void close(){
		shutdown = true ;
		if(socket!=null){
			try {
				socket.close() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
		if(serverSocket!=null)
			try {
				serverSocket.close() ;
			} catch (IOException e) {
				e.printStackTrace();
			}
	}
	public void setWebRoot(String webRoot){
		this.webRoot = webRoot ;
	}
	public void await()
	{
		int port = 8080;
		try
		{
			serverSocket = new ServerSocket(port);
		}
		catch (IOException e)
		{
			e.printStackTrace();
			System.exit(1);
		}
		Logger.d("http server await ...");
		// Loop waiting for a request
		while (!shutdown)
		{
			InputStream input = null;
			OutputStream output = null;
			try
			{
				socket = serverSocket.accept();
				input = socket.getInputStream();
				output = socket.getOutputStream();
				// create Request object and parse
				Request request = new Request(input);
				request.parse();
				// create Response object
				Response response = new Response(output , webRoot , host , dirBytes);
				response.setRequest(request);
				response.sendStaticResource();
				// Close the socket
				socket.close();
				// check if the previous URI is a shutdown
				// command
				shutdown = request.getUri().equals(SHUTDOWN_COMMAND);
			}
			catch (Exception e)
			{
				e.printStackTrace();
				continue;
			}
		}
	}
}
