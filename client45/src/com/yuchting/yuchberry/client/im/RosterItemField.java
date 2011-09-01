package com.yuchting.yuchberry.client.im;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageUnit;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;

public class RosterItemField extends Field{

	public final static int		fsm_rosterItemFieldWidth	= recvMain.fsm_display_width;
	public final static boolean	fsm_largeHeadImage			= recvMain.fsm_display_width > 320;
	
	public final static int		fsm_headImageWidth 			= recvMain.fsm_display_width>320 ? fetchWeibo.fsm_headImageSize_l:fetchWeibo.fsm_headImageSize;
	
	public final static int		fsm_nameTextColor		= 0xededed;
	public final static int		fsm_statusTextColor		= 0x8f8f8f;
	
	fetchChatRoster					m_currRoster;
	WeiboHeadImage					m_headImage;
	
	boolean						m_isChatHistoryItem = false;
	String							m_lastChat = null;
	
	public RosterItemField(fetchChatRoster _roster,WeiboHeadImage _head,boolean _isChatHistroyItem){
		super(Field.FOCUSABLE);
		
		m_currRoster = _roster;
		m_headImage	= _head;
		
		m_isChatHistoryItem = _isChatHistroyItem;
	}
	
	public void setLastChat(String _chat){
		m_lastChat = _chat;
	}
	
	public int getPreferredWidth() {
		return fsm_rosterItemFieldWidth;
	}
	
	public int getPreferredHeight() {
		return 2 * MainIMScreen.sm_defaultFontHeight + 1;
	}
	
	protected void layout(int _width,int _height){
		setExtent(getPreferredWidth(),getPreferredHeight());
	}
	
	protected void paint(Graphics _g){
		drawFocus(_g, isFocus());
	}
	
	protected void onUnfocus(){
	    super.onUnfocus();
	    invalidate();
	}
	
	protected void drawFocus(Graphics _g,boolean _on){
		
		// fill the IM field BG
		//
		fillIMFieldBG(_g,0,0,getPreferredWidth(),getPreferredHeight());
		
		if(_on){
			// draw selected backgroud
			//
			WeiboHeadImage.drawSelectedImage(_g, getPreferredWidth(), getPreferredHeight());
		}
		// draw roster state
		//
		drawRosterState(_g,1,1,m_currRoster);
		
		// draw the IM sign and head image
		//
		int t_x = WeiboHeadImage.displayHeadImage(_g,sm_rosterState[0].getWidth() + 2, 1, m_headImage);
		
		int color = _g.getColor();
		Font font = _g.getFont();
		try{
			_g.setColor(fsm_nameTextColor);
			_g.setFont(MainIMScreen.sm_boldFont);
			
			_g.drawText(m_currRoster.getName(),t_x,1);
			
			_g.setColor(fsm_statusTextColor);
			_g.setFont(font);
			
			if(m_isChatHistoryItem && m_lastChat != null){
				_g.drawText(m_lastChat,t_x,MainIMScreen.sm_defaultFontHeight + 1);
			}else{
				_g.drawText(m_currRoster.getStatus(),t_x,MainIMScreen.sm_defaultFontHeight + 1);
			}
						
		}finally{
			_g.setColor(color);
			_g.setFont(font);
		}
		
		drawChatSign(_g,getPreferredWidth(),getPreferredHeight(),m_currRoster.getStyle());
	}
	
	public static ImageUnit	sm_gtalkSign = null;
	public static ImageUnit	sm_MSNSign = null;
	
	public static int drawChatSign(Graphics _g,int _limitWidth,int _limitHeight,int _style){
		
		int x = _limitWidth - 3 - sm_rosterState[0].getWidth();
		int y = 3;
		
		if(_style == fetchChatMsg.STYLE_GTALK){
			if(sm_gtalkSign == null){
				sm_gtalkSign = recvMain.sm_weiboUIImage.getImageUnit("gtalk_sign");
			}
			
			recvMain.sm_weiboUIImage.drawImage(_g, sm_gtalkSign, x, y);
			
			return sm_gtalkSign.getWidth() + x;
		}else if(_style == fetchChatMsg.STYLE_MSN){
			if(sm_MSNSign == null){
				sm_MSNSign = recvMain.sm_weiboUIImage.getImageUnit("msn_sign");
			}
			
			recvMain.sm_weiboUIImage.drawImage(_g, sm_MSNSign, x, y);
			
			return sm_MSNSign.getWidth() + x;
		}
		
		return 0;
		
	}
	
	private static ImageUnit sm_imFieldBG = null;
	private static ImageUnit sm_imFieldBG_spaceLine = null;
		
	public static void fillIMFieldBG(Graphics _g,int _x,int _y,int _width,int _height){
		
		if(sm_imFieldBG == null){
			sm_imFieldBG = recvMain.sm_weiboUIImage.getImageUnit("weibo_bg");
			sm_imFieldBG_spaceLine = recvMain.sm_weiboUIImage.getImageUnit("space_line");
		}
		
		recvMain.sm_weiboUIImage.fillImageBlock(_g, sm_imFieldBG, _x, _y, _width, _height);
		recvMain.sm_weiboUIImage.drawBitmapLine(_g, sm_imFieldBG_spaceLine, _x, _y, _width);
	}
	
	private static ImageUnit[] sm_rosterState = 
	{
		null,null,null,null,null
	};
	
	private static String[] sm_rosterStateStr = 
	{
		"avail_state",
		"unavail_state",
		"away_state",
		"far_away_state",
		"busy_state"
	};
	
	public static int drawRosterState(Graphics _g,int _x,int _y,fetchChatRoster _roster){
		
		if(sm_rosterState[0] == null){
			for(int i = 0 ;i < sm_rosterState.length;i++){
				sm_rosterState[i] = recvMain.sm_weiboUIImage.getImageUnit(sm_rosterStateStr[i]);
			}
		}
		
		recvMain.sm_weiboUIImage.drawImage(_g, sm_rosterState[_roster.getPresence()], _x, _y);
		
		return _x + sm_rosterState[0].getWidth(); 
	}
	
}
