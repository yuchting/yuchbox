package com.yuchting.yuchberry.yuchsign.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class YuchPrepaidCard {
	
	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String		m_no	= "";
	
	@Persistent
	private long	 	m_genTime		= 0;
	
	@Persistent
	private long	 	m_		= 0;
	

}
