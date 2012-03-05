package com.yuchting.yuchberry.server;

import java.io.InputStream;
import java.io.OutputStream;


/**
 * weibo account request from the server
 * @author yuch
 *
 */
public class WeiboAccount{
	
	private final static int		VERSION = 0;
	
	public String			name		= "";
	public long			id			= 0;
	public byte			weiboStyle	= fetchWeibo.SINA_WEIBO_STYLE;
	
	/**
	 * only use in client set always true when request from server
	 */
	public boolean			needUpdate	= true;
	
	public void Input(InputStream in)throws Exception{
		/*int t_version = */in.read();
		
		name 		= sendReceive.ReadString(in);
		id			= sendReceive.ReadLong(in);
		
		weiboStyle	= (byte)in.read();
		needUpdate	= sendReceive.ReadBoolean(in);
	}
	
	public void Output(OutputStream os)throws Exception{
		os.write(VERSION);
		
		sendReceive.WriteString(os,name,false);
		sendReceive.WriteLong(os,id);
		os.write(weiboStyle);
		sendReceive.WriteBoolean(os,needUpdate);
	}
}
