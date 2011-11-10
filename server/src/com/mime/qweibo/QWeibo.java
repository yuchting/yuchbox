package com.mime.qweibo;

import java.util.ArrayList;
import java.util.List;

import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONObject;

public class QWeibo {
	
	private String m_text;
	private String m_orgText;
	private int m_forwardCount;
	private int m_commentCount;
	private String m_source;
	private long m_id;
	private String m_imageURL = "";
	
	private String m_name;
	private String m_nickName;
	private String	m_headImageURL;
	
	private boolean m_isOwnWeibo;
	
	private boolean m_isVIP;
	
	private long	m_timestamp;
	
	//微博类型 1-原创发表、2-转载、3-私信 4-回复 5-空回 6-提及 7-评论
	private int	m_type;
	
	//微博状态 0-正常，1-系统删除 2-审核中 3-用户删除 4-根删除
	private int	m_status;
	
	private QWeibo m_sourceWeibo = null;
	
	public QWeibo(JSONObject _json)throws Exception{
		m_text 			= _json.getString("text");
		m_orgText		= _json.getString("orgtext");
		m_source = "<a href=\""+ _json.getString("fromurl") +"\">" + _json.getString("from") +"</a>";
		
		m_forwardCount	= _json.getInt("count");
		m_commentCount	= _json.getInt("mcount");
		
		m_id			= _json.getLong("id");
		
		String t_image = _json.getString("image");
		if(t_image != null && !t_image.equals("null") && t_image.length() != 0){
			m_imageURL	= _json.getJSONArray("image").getString(0);
		}		
		
		m_name			= _json.getString("name");
		m_nickName		= _json.getString("nick");
		
		m_headImageURL	= _json.getString("head"); // append the size
		if(m_headImageURL.length() != 0){
			m_headImageURL += "/50";
		}
		
		m_isOwnWeibo	= _json.getInt("self") == 1;
		m_isVIP			= _json.getInt("isvip") == 1;
		
		m_timestamp		= _json.getLong("timestamp") * 1000; // to convert to java Date time
		m_type			= _json.getInt("type");
		m_status		= _json.getInt("status");
		
		String t_source = _json.getString("source");
		if(t_source != null && !t_source.equals("null") && t_source.length() != 0 ){
			m_sourceWeibo = new QWeibo(new JSONObject(t_source));			
		}
		
		// get rid of all link tag
		//
		while(true){
			int t_a = m_text.indexOf("<a");
			if(t_a != -1){
				int t_a_ref = m_text.indexOf(">",t_a);
				int t_a_end = m_text.indexOf("</a>",t_a);
				
				if(t_a_ref != -1 && t_a_end != -1 && (t_a_ref < t_a_end) ){
					m_text = m_text.substring(0,t_a) + m_text.substring(t_a_ref + 1,t_a_end) + m_text.substring(t_a_end + 4);
				}else{
					break;
				}
				
			}else{
				break;
			}
		}
		
	}
	
	public String getText(){return m_text;}
	public String getOrigText(){return m_text;}
	
	public String getSource(){return m_source;}
	public int getForwardCount(){return m_forwardCount;}
	public int getCommentCount(){return m_commentCount;}
	public long getId(){return m_id;}
	
	public String getImage(){return m_imageURL;}
	public String getScreenName(){return m_name;}
	public String getNickName(){return m_nickName;}
	public String getHeadImageURL(){return m_headImageURL;}
	
	public boolean isOwnWeibo(){return m_isOwnWeibo;}
	public boolean isVIP(){return m_isVIP;}
	public long getTime(){return m_timestamp;}
	
	//微博类型 1-原创发表、2-转载、3-私信 4-回复 5-空回 6-提及 7-评论
	public int getType(){return m_type;}
	
	//微博状态 0-正常，1-系统删除 2-审核中 3-用户删除 4-根删除
	public int getWeiboStatus(){return m_status;}
	
	public QWeibo getSourceWeibo(){return m_sourceWeibo;}
	
	
	static public List<QWeibo> getWeiboList(JSONObject _json)throws Exception{
		
		List<QWeibo> t_list = new ArrayList<QWeibo>();
		
		if(_json.getInt("ret") != 0){
			throw new Exception("getWeiboList error :" + _json.getString("msg"));
		}
		
		try{
			JSONArray t_array = _json.getJSONObject("data").getJSONArray("info");
			int t_num = t_array.length();
			for(int i = 0;i < t_num;i++){
				t_list.add(new QWeibo(t_array.getJSONObject(i)));
			}
		}catch(Exception e){
			if(QWeiboSyncApi.sm_debug){
				e.printStackTrace();
			}
		}
		
		
		return t_list;
	}
	
	
}
