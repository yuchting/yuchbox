package com.yuchting.yuchberry.yuchsign.shared;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchbber {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String m_signinName = "";
	
	@Persistent
	private String m_password = "";
	
	@Persistent
	private long m_usingHours = 72;
	
	public yuchbber(final String _name,final String _pass){
		m_signinName	= _name;
		m_password		= _pass;
	}
	
	public void SetSigninName(final String _name){m_signinName = _name;}
	public String GetSigninName(){return m_signinName;}
	
	public void SetPassword(final String _pass){m_password = _pass;}
	public String GetPassword(){return m_password;}
	
	
	public String OuputXMLData(){
		StringBuffer t_output = new StringBuffer();
		t_output.append("<yuchbber ").append("name=\"").append(m_signinName).
									append("\" pass=\"").append(m_password).
									append("\" hour=\"").append(m_usingHours).
									append("\">");
		
		t_output.append("</yuchbber>");
		
		return t_output.toString();
	}
	
}
