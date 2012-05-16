package com.mime.qweibo;

import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONObject;

public class QOpenIDs {

	private String[] m_nameList 	= null;
	private String[] m_openidList	= null;
	
	private boolean m_hasMoreIds	= false;
	
	public QOpenIDs(JSONObject _json)throws Exception{
		
		JSONObject t_data = _json.getJSONObject("data");
		if(t_data != null){
			m_hasMoreIds = t_data.getInt("hasnext") == 0;
			JSONArray arr = t_data.getJSONArray("info");
			
			final int len = arr.length();
			
			m_nameList 		= new String[len];
			m_openidList	= new String[len];
			
			JSONObject t_id;
			for(int i = 0;i < len;i++){
				t_id = arr.getJSONObject(i);
				
				m_nameList[i]	= t_id.getString("name");
				m_openidList[i]	= t_id.getString("openid");
			}
		}
	}
	
	/**
	 * has more ids
	 * @return
	 */
	public boolean hasMoreData(){
		return m_hasMoreIds;
	}
	
	/**
	 * get the list of name
	 * @return
	 */
	public String[] getNameList(){
		return m_nameList;
	}
	
	/**
	 * get the list of qq weibo openid
	 * @return
	 */
	public String[] getOpenidList(){
		return m_openidList;
	}
}
