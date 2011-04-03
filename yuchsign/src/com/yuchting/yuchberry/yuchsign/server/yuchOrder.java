package com.yuchting.yuchberry.yuchsign.server;

import javax.jdo.annotations.IdGeneratorStrategy;
import javax.jdo.annotations.IdentityType;
import javax.jdo.annotations.PersistenceCapable;
import javax.jdo.annotations.Persistent;
import javax.jdo.annotations.PrimaryKey;

@PersistenceCapable(identityType = IdentityType.APPLICATION)
public class yuchOrder {

	@PrimaryKey
	@Persistent(valueStrategy = IdGeneratorStrategy.IDENTITY)
	private String		m_out_trade_no	= "";
	
	@Persistent
	private String 	m_subject		= "";
	
	@Persistent
	private int		m_payType		= 0;	
		
	@Persistent
	private int 		m_total_fee		= 0;
	
	@Persistent
	private String		m_alipay_trade_no = "";
	
	@Persistent
	private String		m_buyer_email	= "";
	
	public String GetOutTradeNO(){return m_out_trade_no;}
	public void SetOutTradeNO(String _NO){m_out_trade_no = _NO;}
		
	public String GetSubject(){return m_subject;}
	public void SetSubject(String _sub){m_subject = _sub;}
	
	public int GetPayType(){return m_payType;}
	public void SetPayType(int _payType){m_payType = _payType;}
	
	public int GetTotalFee(){return m_total_fee;}
	public void SetTotalFee(int _fee){m_total_fee = _fee;}
	
	public void SetAlipayTradeNO(String _alipay){m_alipay_trade_no = _alipay;}
	public String GetAlipayTradeNO(){return m_alipay_trade_no;}
	
	public void SetBuyerEmail(String _email){m_buyer_email = _email;}
	public String GetBuyerEmail(){return m_buyer_email;}
	
	
		
}
