package com.yuchting.yuchberry.yuchsign.server;

import java.util.Date;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchOrder {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	public long 	m_out_trade_no	= (new Date()).getTime();
	
	@Persistent
	public String 	m_subject		= "";
	
	@Persistent
	public String 	m_body			= "";
	
	@Persistent
	public int 	m_total_fee		= 0;
	
	@Persistent
	public String	m_trade_status	= "";
	
	@Persistent
	public String	m_alipay_trade_no = "";
	
	@Persistent
	public String	m_buyer_email	= "";
	
}
