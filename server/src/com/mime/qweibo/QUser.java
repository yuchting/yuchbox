package com.mime.qweibo;

import java.util.ArrayList;
import java.util.List;

import weibo4j.org.json.JSONArray;
import weibo4j.org.json.JSONObject;

// yuchberry added @ 2011-5-27
//
public class QUser{
	
	private String	m_name = "";
	private String	m_nick = "";
	private long	m_id;
	private String	m_headImageURL = "";
	private String	m_location = "";
	private boolean	m_isVerified;
	private boolean	m_isEnterprise;
	private String	m_introduction = "";
	private String	m_verifyInfo = "";
	private int	m_birthYear;
	private int	m_birthMonth;
	private int	m_birthDay;
	private int	m_countryCode;
	private int	m_provinceCode;
	private int	m_cityCode;
	private int	m_sex; // why integer? heihei... 
	private int	m_fansNum;
	private int	m_idolNum;
	private int	m_weiboNum;
	
	private boolean m_isMyFans;
	private boolean m_hasBeenFollowed;
	
	private List<QUserTag>	m_tag	= null;
	private List<QUserEdu>	m_edu	= null;
	
	
	public QUser(String _jsonString)throws Exception{
		init(new JSONObject(_jsonString));
	}
	
	public QUser(JSONObject _json)throws Exception{
		init(_json);
	}
	
	public void init(JSONObject _json)throws Exception{
		/*
		Name: 用户帐户名
		Nick: 用户昵称
		Uid: 用户id(目前为空)
		Head: 头像URL
		Location: 所在地
		Isvip: 是否认证用户
		Isent: 是否企业机构
		Introduction: 个人介绍
		Verifyinfo: 认证信息
		Birth_year: 出生年
		Birth_month:出生月
		Birth_day:出生天
		Country_code: 国家ID,
		Province_code: 地区ID,
		City_code: 城市ID,
		Sex:用户性别 1男 2 女 0未知
		Fansnum:粉丝数
		Idolnum:偶像数
		
		Tweetnum:发表的微博数
		
		Tag:
			Id:个人标签ID
			Name:标签名
		Edu:
			Id:学历记录ID
			Year 入学年
			Schoolid 学校ID
			Departmentid 院系ID
			Level 学历级别
		*/
		m_name	= _json.getString("name");
		m_nick	= _json.getString("nick");
		m_id	= _json.getLong("uid");
		
		m_headImageURL	= _json.getString("head");
		m_location 		= _json.getString("location");
		
		// json format? oh my qq...
		//
		m_isVerified	= _json.getInt("isvip") == 1;
		m_isEnterprise	= _json.getInt("isent") == 1;
		
		m_introduction	= _json.getString("introduction");
		m_verifyInfo	= _json.getString("verifyinfo");
		
		m_birthYear		= _json.getInt("birth_year");
		m_birthMonth	= _json.getInt("birth_month");
		m_birthDay		= _json.getInt("birth_day");
		
		m_countryCode 	= _json.getInt("country_code");
		m_provinceCode 	= _json.getInt("province_code");
		m_cityCode 		= _json.getInt("city_code");
		m_sex 			= _json.getInt("sex");
		m_fansNum 		= _json.getInt("fansnum");
		m_idolNum 		= _json.getInt("idolnum");
		m_weiboNum 		= _json.getInt("tweetnum");
		
		m_isMyFans		= _json.getInt("ismyfans") == 1;
		m_hasBeenFollowed = _json.getInt("ismyidol") == 1;

		String t_tagStr = _json.getString("tag");
		if(t_tagStr != null && !t_tagStr.equals("null")){
			JSONArray t_tag = _json.getJSONArray("tag");
			if(t_tag != null){
				m_tag	= new ArrayList<QUserTag>();
					
				int size = t_tag.length();

	            for (int i = 0; i < size; i++) {
	            	QUserTag t_newTag = new QUserTag();
	            	JSONObject tag = t_tag.getJSONObject(i);
	            	
	            	t_newTag.m_tagId	= tag.getInt("id");
	            	t_newTag.m_tagName	= tag.getString("name");
	            	
	            	m_tag.add(t_newTag);
	            }
			
			}
		}
		
		
		t_tagStr = _json.getString("edu");
		
		if(t_tagStr != null && !t_tagStr.equals("null")){
			
			JSONArray t_edu = _json.getJSONArray("edu");
			m_edu	= new ArrayList<QUserEdu>();
			
			int size = t_edu.length();
				
            for (int i = 0; i < size; i++) {
            	QUserEdu t_newEdu = new QUserEdu();
            	JSONObject edu = t_edu.getJSONObject(i);
            	
            	t_newEdu.m_id 			= edu.getInt("id");
            	t_newEdu.m_enterYear	= edu.getInt("year");
            	t_newEdu.m_schoolId		= edu.getInt("schoolid");
            	t_newEdu.m_departmentId	= edu.getInt("departmentid");
            	t_newEdu.m_enterLevel	= edu.getInt("level");
            	
            	
            	m_edu.add(t_newEdu);
            }
		}
	}
	
	public String getScreenName(){
		return m_name;
	}

	public String getName(){
		return m_nick;
	}

	public long getId(){
		return m_id;
	}
	

	public String getHeadImageURL(){
		return m_headImageURL;
	}

	public String getLocation(){
		return m_location;
	}

	public boolean isVerified(){
		return m_isVerified;
	}

	public boolean isEnterprise(){
		return m_isEnterprise;
	}

	public String getIntroduction(){
		return m_introduction;
	}

	public String getVerifyInfo(){
		return m_verifyInfo;
	}

	public int getBirthYear(){
		return m_birthYear;
	}

	public int getBirthMonth(){
		return m_birthMonth;
	}

	public int getBirthDay(){
		return m_birthDay;
	}

	public int getCountryCode(){
		return m_countryCode;
	}

	public int getProvinceCode(){
		return m_provinceCode;
	}

	public int getCityCode(){
		return m_cityCode;
	}

	
	// 用户性别 1男 2 女 0未知
	public int getSex(){
		return m_sex;
	}

	public int getFansNum(){
		return m_fansNum;
	}
	
	public int getIdolNum(){
		return m_idolNum;
	}
	
	public int getWeiboNum(){
		return m_weiboNum;
	}
	
	public boolean isMyFans(){
		return m_isMyFans;
	}
	
	public boolean hasBeenFollowed(){
		return m_hasBeenFollowed;
	}

	public List<QUserTag> getTagList(){
		return m_tag;
	}

	public List<QUserEdu> getEduList(){
		return m_edu;
	}
	 
	
	
}
