package com.mime.qweibo;

import java.util.ArrayList;
import java.util.List;

import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONObject;

public class QDirectMessage {
	
	public final static int			INBOX_TYPE = 0;
	public final static int			OUTBOX_TYPE = 1;
	/*
	 * direct type 0:inbox 1:outbox
	 */
	private int	m_directMessageType;
	
	private String m_tonick = "";
	private String m_toname = "";
	
	private QWeibo m_weiboItem;
	
	public QDirectMessage(QWeibo _weibo,int _type){
		m_weiboItem = _weibo;
		m_directMessageType = _type;
	}
	
	public QWeibo getWeiboContentItem(){
		return m_weiboItem;
	}
	
	public String getSendToScreenName(){return m_toname;}
	public String getSentToNickName(){return m_tonick;}
	
	/**
	 * get the direcet message type 
	 * @return 0:inbox 1:outbox
	 */
	public int getDirectMessageType(){return m_directMessageType;}
	
	
	public static List<QDirectMessage> getDMList(JSONObject _json,int _type)throws Exception{
		
		List<QDirectMessage> t_list = new ArrayList<QDirectMessage>();
		
		if(_json.getInt("ret") != 0){
			throw new Exception("getHomeList error :" + _json.getString("msg"));
		}
		
		try{
			if(!_json.isNull("data")){

				JSONArray t_array = _json.getJSONObject("data").getJSONArray("info");
				int t_num = t_array.length();
				for(int i = 0;i < t_num;i++){
					JSONObject json = t_array.getJSONObject(i);
					QDirectMessage t_msg= new QDirectMessage(new QWeibo(json), _type);
					
					t_msg.m_toname = json.getString("toname");
					t_msg.m_tonick = json.getString("tonick");
					
					t_list.add(t_msg);
				}
			}
		}catch(Exception e){
			if(QWeiboSyncApi.sm_debug){
				e.printStackTrace();
			}
		}
		
		
		return t_list;
	}
}
