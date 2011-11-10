package com.yuchting.yuchberry.client.im;

import java.io.InputStream;
import java.io.OutputStream;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;

public final class IMStatus{
	
	static public IMStatus		sm_currUseStatus = null;
	
	public int 	m_presence = fetchChatRoster.PRESENCE_AVAIL;
	public String	m_status		= "";
	
	public IMStatus(){}
	public IMStatus(int _presence,String _status){
		m_presence	= _presence;
		m_status	= _status;
	}
	
	public void Import(InputStream in)throws Exception{
		m_presence = in.read();
		m_status	= sendReceive.ReadString(in);
	}
	
	public void Ouput(OutputStream os)throws Exception{
		os.write(m_presence);
		sendReceive.WriteString(os,m_status);
	}
	

	public String toString(){
		return m_status;
	}
}

final class IMStatusField extends Field{
	public IMStatus		m_status;
				
	public IMStatusField(IMStatus _status){
		super(Field.FOCUSABLE);
		m_status = _status;
	}
	
	public int getPreferredWidth() {
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight() {
		return MainIMScreen.fsm_defaultFontHeight;
	}
	
	public void invalidate(){
		super.invalidate();
	}
	protected void layout(int _width,int _height){
		setExtent(getPreferredWidth(),getPreferredHeight());
	}
	
	protected void onUnfocus(){
	    super.onUnfocus();
	    invalidate();
	}
	
	protected void paint(Graphics _g){
		drawFocus(_g, isFocus());
	}
	
	
	protected void drawFocus(Graphics _g,boolean _on){
		// fill the IM field BG
		//
		RosterItemField.fillIMFieldBG(_g,0,0,getPreferredWidth(),getPreferredHeight());
		
		if(_on){
			// draw selected backgroud
			//
			WeiboHeadImage.drawSelectedImage(_g, getPreferredWidth(), getPreferredHeight());
		}
		
		// draw roster state
		//
		int t_x = RosterItemField.drawRosterState(_g,1,2,m_status.m_presence);
		Font font = _g.getFont();
		int color = _g.getColor();
		
		try{
			String t_status = m_status.m_status;
			if(IMStatus.sm_currUseStatus == m_status){
				_g.setFont(MainIMScreen.fsm_boldFont);
				t_status = "-" + t_status;
			}
			
			_g.setColor(RosterItemField.fsm_nameTextColor);
			_g.drawText(t_status,t_x,2);
			
		}finally{
			_g.setFont(font);
			_g.setColor(color);
		}
	}
}
