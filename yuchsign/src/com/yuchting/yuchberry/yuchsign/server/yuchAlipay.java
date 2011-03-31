package com.yuchting.yuchberry.yuchsign.server;

import java.io.Serializable;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchAlipay implements Serializable{
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String m_partner = "";
	
	@Persistent
	private String m_key = "";
	
	
	public String GetPartnerID(){return m_partner;}
	public void SetPartnerID(String _id){m_partner = _id;}
	
	public String GetKey(){return m_key;}
	public void SetKey(String _key){m_key = _key;}
	
}
