package com.mime.qweibo;

import java.util.ArrayList;
import java.util.List;

import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONObject;

public class QWeibo {
	private
	public QWeibo(JSONObject _json)throws Exception{
		
	}

	static public List<QWeibo> getWeiboList(JSONObject _json)throws Exception{
		
		List<QWeibo> t_list = new ArrayList<QWeibo>();
		
		if(_json.getInt("ret") != 0){
			throw new Exception("getHomeList error :" + _json.getString("Msg"));
		}
		
		JSONArray t_array = _json.getJSONArray("Info");
		int t_num = t_array.length();
		for(int i = 0;i < t_num;i++){
			t_list.add(new QWeibo(t_array.getJSONObject(i)));
		}
		
		return t_list;
	}
}
