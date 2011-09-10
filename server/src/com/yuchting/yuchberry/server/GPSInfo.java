package com.yuchting.yuchberry.server;

import java.io.InputStream;
import java.io.OutputStream;

public final class GPSInfo{
	public double m_longitude 				= 0;
	public double m_latitude				= 0;
	public float	 m_altitude				= 0;
	public float	 m_speed				= 0;
	public float	 m_heading				= 0;
	
	// statistics 
	//
	long			m_time					= 0;
	
	public void InputData(InputStream in)throws Exception{
		m_longitude = sendReceive.ReadDouble(in);
		m_latitude 	= sendReceive.ReadDouble(in);
		
		m_altitude	= sendReceive.ReadFloat(in);
		m_speed		= sendReceive.ReadFloat(in);
		m_heading	= sendReceive.ReadFloat(in);		
	}
	
	public void OutputData(OutputStream os)throws Exception{
		sendReceive.WriteDouble(os,m_longitude);
		sendReceive.WriteDouble(os,m_latitude);
		
		sendReceive.WriteFloat(os,m_altitude);
		sendReceive.WriteFloat(os,m_speed);
		sendReceive.WriteFloat(os,m_heading);
	}
}
