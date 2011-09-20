package com.yuchting.yuchberry.client.im;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
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
	
	public final static int		fsm_nameTextColor			= 0xededed;
	public final static int		fsm_statusTextColor			= 0xdfdfdf;
	
	RosterChatData					m_currRoster;
	WeiboHeadImage					m_headImage;
	
	MainIMScreen					m_mainScreen;	
	boolean						m_isChatHistoryItem = false;
		
	public RosterItemField(MainIMScreen _mainScreen,
							RosterChatData _roster,WeiboHeadImage _head,
							boolean _isChatHistroyItem,
							FieldChangeListener _changedListener){
		super(Field.FOCUSABLE);
		
		m_mainScreen = _mainScreen;
		m_currRoster = _roster;
		m_headImage	= _head; 
		
		m_isChatHistoryItem = _isChatHistroyItem;
		
		setChangeListener(_changedListener);
	}

	public int getPreferredWidth() {
		return fsm_rosterItemFieldWidth;
	}
	
	public int getPreferredHeight() {
		return 2 * MainIMScreen.fsm_defaultFontHeight + 1;
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
		if(_on){
			if(m_isChatHistoryItem){
				m_mainScreen.m_currFocusHistoryRosterItemField = this;
			}else{
				m_mainScreen.m_currFocusRosterItemField = this;
			}			
		}
				
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
		drawRosterState(_g,1,1,m_currRoster.m_roster.getPresence());
		
		// draw the IM sign and head image
		//
		int t_x = WeiboHeadImage.displayHeadImage(_g,sm_rosterState[0].getWidth() + 2, 2, m_headImage);
		
		int color = _g.getColor();
		Font font = _g.getFont();
		try{
			_g.setColor(fsm_nameTextColor);
			_g.setFont(MainIMScreen.fsm_boldFont);
			
			_g.drawText(m_currRoster.m_roster.getName(),t_x,2);
			
			_g.setColor(fsm_statusTextColor);
			_g.setFont(font);
			
			int t_y = MainIMScreen.fsm_defaultFontHeight + 1;		
			
			if(m_isChatHistoryItem && !m_currRoster.m_chatMsgList.isEmpty()){
				// 
				//
				fetchChatMsg t_msg = (fetchChatMsg)m_currRoster.m_chatMsgList.elementAt(m_currRoster.m_chatMsgList.size() - 1);
				
				String t_textMsg = t_msg.getMsg();
				
				if(t_textMsg.length() > 30){
					t_textMsg = t_textMsg.substring(0,30);
				}
				
				if(t_msg.isOwnMsg()){
					
					ImageUnit t_unit = ChatField.sm_stateImage[t_msg.getSendState()];
					
					recvMain.sm_weiboUIImage.drawImage(_g,t_unit,t_x,t_y);
					_g.drawText(t_textMsg,t_x + t_unit.getWidth() + 2,t_y);
					
				}else{
					_g.drawText(t_textMsg,t_x,t_y);
				}
				
			}else{
				String t_status = m_currRoster.m_roster.getStatus();
				if(t_status.length() == 0){
					t_status = m_currRoster.m_roster.getAccount();
				}
				_g.drawText(t_status,t_x,MainIMScreen.fsm_defaultFontHeight + 1);
			}
						
		}finally{
			_g.setColor(color);
			_g.setFont(font);
		}
		
		drawChatSign(_g,getPreferredWidth(),getPreferredHeight(),m_currRoster.m_roster.getStyle(),m_currRoster.m_isYuch);
	}
	
	public static ImageUnit	sm_gtalkSign = null;
	public static ImageUnit	sm_MSNSign = null;
	public static ImageUnit	sm_YuchSign = null;
	
	public static int drawChatSign(Graphics _g,int _limitWidth,int _limitHeight,int _style,boolean _isYuch){

		int t_ret = 0;
		
		int x = _limitWidth - 3 - sm_rosterState[0].getWidth();
		int y = 3;
		
		if(_style == fetchChatMsg.STYLE_GTALK){
			if(sm_gtalkSign == null){
				sm_gtalkSign = recvMain.sm_weiboUIImage.getImageUnit("gtalk_sign");
			}
			
			recvMain.sm_weiboUIImage.drawImage(_g, sm_gtalkSign, x, y);
			
			t_ret = x;
			
		}else if(_style == fetchChatMsg.STYLE_MSN){
			if(sm_MSNSign == null){
				sm_MSNSign = recvMain.sm_weiboUIImage.getImageUnit("msn_sign");
			}
			
			recvMain.sm_weiboUIImage.drawImage(_g, sm_MSNSign, x, y);
			
			t_ret = x;
		}
		
		if(_isYuch){
			if(sm_YuchSign == null){
				sm_YuchSign = recvMain.sm_weiboUIImage.getImageUnit("yuch_sign");
			}
			
			t_ret -= sm_YuchSign.getWidth();
			
			recvMain.sm_weiboUIImage.drawImage(_g,sm_YuchSign,t_ret,y);
		}
		
		return t_ret;
		
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
		"away_state",
		"busy_state",
		"unavail_state",
		"far_away_state",
	};
	
	public static int drawRosterState(Graphics _g,int _x,int _y,int _presence){
		
		if(sm_rosterState[0] == null){
			for(int i = 0 ;i < sm_rosterState.length;i++){
				sm_rosterState[i] = recvMain.sm_weiboUIImage.getImageUnit(sm_rosterStateStr[i]);
			}
		}
		
		recvMain.sm_weiboUIImage.drawImage(_g, sm_rosterState[_presence], _x, _y);
		
		return _x + sm_rosterState[0].getWidth(); 
	}
	
	protected boolean keyChar( char character, int status, int time ){
        if( character == Characters.ENTER ) {
            fieldChangeNotify( 0 );
            return true;
        }
        return super.keyChar( character, status, time );
    }

    protected boolean navigationClick( int status, int time ) {        
        keyChar(Characters.ENTER, status, time );            
        return true;
    }
}
