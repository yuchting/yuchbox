/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.client.im;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageUnit;
import com.yuchting.yuchberry.client.ui.SliderHeader;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;

public class RosterItemField extends Field{

	public final static int		fsm_rosterItemFieldWidth	= recvMain.fsm_display_width;
	public final static boolean	fsm_largeHeadImage			= recvMain.fsm_display_width > 320;
	
	public final static int		fsm_headImageWidth 			= recvMain.fsm_display_width>320 ? fetchWeibo.fsm_headImageSize_l:fetchWeibo.fsm_headImageSize;
	
	public final static int		fsm_nameTextColor			=  0; //RRR:别名字体颜色 0xededed;
	public final static int		fsm_groupTitleTextColor		= 0x878787;//RRR:分组标题字体颜色0xb0b0b0;
	public final static int		fsm_statusTextColor			= 0x6d6f6f; //RRR:聊天状态字体颜色0xdfdfdf;
	
	static Font		fsm_addressFont			= MainIMScreen.fsm_defaultFont.derive(MainIMScreen.fsm_defaultFont.getStyle(),MainIMScreen.fsm_defaultFontHeight - 4);//RRR:第二行聊天字体
	public final static int		fsm_addressFontHeight	= fsm_addressFont.getHeight();
	
	MainIMScreen.RosterChatData		m_currRoster;
	WeiboHeadImage					m_headImage;
	
	MainIMScreen					m_mainScreen;	
	boolean						m_isChatHistoryItem = false;
		
	public RosterItemField(MainIMScreen _mainScreen,
							MainIMScreen.RosterChatData _roster,WeiboHeadImage _head,
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
		return Math.max(2 * MainIMScreen.fsm_defaultFontHeight ,fsm_headImageWidth  ); //RRR: 8号字体每列高度(2 * MainIMScreen.fsm_defaultFontHeight + 4,fsm_headImageWidth);
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
				m_mainScreen.m_selectGroupTitleField = null;
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
		drawRosterState(_g,2,20,m_currRoster.m_roster.getPresence());  //RRR:(_g,1,6,m_currRoster.m_roster.getPresence());
		
		// draw the IM sign and head image
		//
		int t_x = WeiboHeadImage.displayHeadImage(_g,sm_rosterState[0].getWidth() + 6, 3, m_headImage);//RRR:(_g,sm_rosterState[0].getWidth() + 2, 2, m_headImage)
	
		
		if(m_currRoster.m_hasNewMessage){
			recvMain.sm_weiboUIImage.drawImage(_g,SliderHeader.GetBBerSignBitmap(),2,6);//RRR:新消息(_g,SliderHeader.GetBBerSignBitmap(),1,20);
		}
		
		int color = _g.getColor();
		Font font = _g.getFont();
		//RRR: 选中后字体颜色
		try{
			if (_on){
				_g.setColor(fsm_nameTextColor);//选中字体颜色
				_g.setFont(font); //RRR:去除昵称字体加粗(MainIMScreen.fsm_boldFont);
				}else{
			_g.setColor(fsm_nameTextColor);
			_g.setFont(font); //RRR:去除昵称字体加粗(MainIMScreen.fsm_boldFont);
			}
			_g.drawText(m_currRoster.m_roster.getName(),t_x + 4,3); //RRR:昵称向右偏移4 _g.drawText(m_currRoster.m_roster.getName(),t_x,2);
			if (_on){
				_g.setColor(fsm_statusTextColor);//选中字体颜色
				_g.setFont(fsm_addressFont);
			}else{
			_g.setColor(fsm_statusTextColor);
			_g.setFont(fsm_addressFont);
			}
	/* 源码
		try{
			_g.setColor(fsm_nameTextColor);
			_g.setFont(font); //RRR:去除昵称字体加粗(MainIMScreen.fsm_boldFont);
			
			_g.drawText(m_currRoster.m_roster.getName(),t_x + 4,3); //RRR:昵称向右偏移4 _g.drawText(m_currRoster.m_roster.getName(),t_x,2);
			
			_g.setColor(fsm_statusTextColor);
			_g.setFont(fsm_addressFont);
	 */
			int t_y = getPreferredHeight() - fsm_addressFontHeight ; //RRR:微调 int t_y = getPreferredHeight() - fsm_addressFontHeight ;
			
			if(m_isChatHistoryItem && !m_currRoster.m_chatMsgList.isEmpty()){
				// 
				//
				fetchChatMsg t_msg = (fetchChatMsg)m_currRoster.m_chatMsgList.elementAt(m_currRoster.m_chatMsgList.size() - 1);
				
				String t_textMsg = MainIMScreen.getChatMsgAbsText(t_msg);
				
				if(t_textMsg.length() > 30){
					t_textMsg = t_textMsg.substring(0,30);
				}
				
				if(t_msg.isOwnMsg()){
					
					ImageUnit t_unit = ChatField.sm_stateImage[t_msg.getSendState()];
					
					recvMain.sm_weiboUIImage.drawImage(_g,t_unit,t_x + 4,t_y); //RRR:状态偏移4像素(_g,t_unit,t_x,t_y);
					_g.drawText(t_textMsg,t_x + 4 + t_unit.getWidth() ,t_y);//RRR:状态偏移4像素	_g.drawText(t_textMsg,t_x + t_unit.getWidth() + 2,t_y);
					
				}else{
					_g.drawText(t_textMsg,t_x + 4,t_y);//RRR:状态偏移4像素 _g.drawText(t_textMsg,t_x,t_y);
				}
				
			}else{
				String t_status = m_currRoster.m_roster.getStatus();
				if(t_status.length() == 0){
					t_status = m_currRoster.m_roster.getAccount();
				}
				_g.drawText(t_status,t_x + 4 ,t_y ); //RRR:状态偏移4像素_g.drawText(t_status,t_x ,t_y );
			}
						
		}finally{
			_g.setColor(color);
			_g.setFont(font);
		}
		
		drawChatSign(_g,getPreferredWidth(),getPreferredHeight(),m_currRoster.m_roster.getStyle(),m_currRoster.m_isYuch,0);
	}
	
	public static ImageUnit	sm_gtalkSign = null;
	public static ImageUnit	sm_MSNSign = null;
	public static ImageUnit	sm_YuchSign = null;
	
	/**
	 * draw the chat style and yuchbox sign
	 * @param _g
	 * @param _limitWidth
	 * @param _limitHeight
	 * @param _style
	 * @param _isYuch			is yuchbox client
	 * @param _rightOffset		the right edge distance
	 * @return
	 */
	public static int drawChatSign(Graphics _g,int _limitWidth,int _limitHeight,int _style,boolean _isYuch,int _rightOffset){

		int t_ret = 0;
		
		int x = _limitWidth - 3 - sm_rosterState[0].getWidth() - _rightOffset;
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
		
	public static void fillIMFieldBG(Graphics _g,int _x,int _y,int _width,int _height){
		
		if(sm_imFieldBG == null){
			sm_imFieldBG = recvMain.sm_weiboUIImage.getImageUnit("im_bg");//RRR:换回IM背景sm_imFieldBG = recvMain.sm_weiboUIImage.getImageUnit("weibo_bg");
		}
		
		recvMain.sm_weiboUIImage.fillImageBlock(_g, sm_imFieldBG, _x, _y, _width, _height);
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
    
    public void invalidate(){
    	super.invalidate();
    }
}
