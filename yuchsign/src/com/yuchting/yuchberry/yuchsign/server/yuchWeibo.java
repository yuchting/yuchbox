package com.yuchting.yuchberry.yuchsign.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

import org.w3c.dom.Element;

import com.google.appengine.api.datastore.Key;


@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchWeibo {
	
	@PrimaryKey
    @Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
    private Key m_key;
	
	@Persistent
	public yuchbber m_yuchbber; 
	
	@Persistent
	public String m_typeName = "sina";
	
	@Persistent
	public String m_accoutName = "";
	
	@Persistent
	public String m_accessToken = "";
	
	@Persistent
	public String m_secretToken = "";	
	
	@Persistent
	public int m_timelineSum = 20;
	
	@Persistent
	public int m_directMsgSum = 2;
	
	@Persistent
	public int m_atMeSum = 2;
	
	@Persistent
	public int m_commentMeSum = 2;
	
	public void OuputXMLData(final StringBuffer _output){
		_output.append("\t<WeiboAccount ").append("type=\"").append(m_typeName).
				append("\" account=\"").append(m_accoutName).
				append("\" accessToken=\"").append(m_accessToken).
				append("\" secretToken=\"").append(m_secretToken).
				append("\" timelineSum=\"").append(m_timelineSum).
				append("\" directMessageSum=\"").append(m_directMsgSum).
				append("\" atMeSum=\"").append(m_atMeSum).
				append("\" commentSum=\"").append(m_atMeSum).
				append("\" />\n");
	}
	
	public void InputXMLData(final Element _elem)throws Exception{
		m_typeName 		= yuchbber.ReadStringAttr(_elem, "type");
		m_accoutName	= yuchbber.ReadStringAttr(_elem, "account");
		m_accessToken	= yuchbber.ReadStringAttr(_elem, "accessToken");
		m_secretToken	= yuchbber.ReadStringAttr(_elem, "secretToken");
		
		m_timelineSum	= yuchbber.ReadIntegerAttr(_elem, "timelineSum");
		m_directMsgSum	= yuchbber.ReadIntegerAttr(_elem, "directMessageSum");
		m_atMeSum		= yuchbber.ReadIntegerAttr(_elem, "atMeSum");
		m_commentMeSum	= yuchbber.ReadIntegerAttr(_elem, "commentSum");
	}
	
	public String toString(){
		return m_typeName + " Weibo<" + m_accoutName + ">"; 
	}
	
}


