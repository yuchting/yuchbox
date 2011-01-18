package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.RichTextField;
import net.rim.device.api.ui.container.MainScreen;




class WeiboItemField extends Manager{
	
	final static int		fsm_headImageWidth 			= 24;
	final static int		fsm_headImageTextInterval	= 8;
	
	final static int		fsm_weiboSignImageWidth		= 16;
	
	final static int		fsm_textWidth				= recvMain.fsm_display_width - fsm_headImageWidth - fsm_headImageTextInterval;
	

	static HyperlinkButtonField	sm_atBut				= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.AT_WEIBO_BUTTON_LABLE));
	static HyperlinkButtonField	sm_forwardBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.FORWARD_WEIBO_BUTTTON_LABLE));
	static HyperlinkButtonField	sm_favoriteBut			= new HyperlinkButtonField(recvMain.sm_local.getString(localResource.FAVORITE_WEIBO_BUTTON_LABLE));
	
	static Font		sm_defaultFont						= sm_atBut.getFont();
	
	static int		sm_fontHeight						= sm_defaultFont.getHeight();
	
	static int		sm_atButton_y						= fsm_weiboSignImageWidth + fsm_headImageWidth + fsm_headImageTextInterval;
	static int		sm_forwardButton_y					= sm_fontHeight + sm_atButton_y + fsm_headImageTextInterval;
	static int		sm_favoriteButton_y					= sm_fontHeight + sm_forwardButton_y + fsm_headImageTextInterval;
	
	static int		sm_functionAreaMinHeight			= sm_fontHeight + sm_favoriteButton_y + fsm_headImageTextInterval;
	
	static RichTextField 			sm_textArea			= new RichTextField();
	
	static WeiboItemField			sm_selectWeiboItem	= null;
	
	
	int						m_extendHeight 	= 20;
	int						m_height		= 20;
	
	int						m_textHeight	= 20;
	
	Bitmap					m_weiboSignImage = null;
	Bitmap					m_headImage 	= null;	
	
	String					m_testText 		= null;
	
	public WeiboItemField(String _text,Bitmap _headImage,Bitmap _weiboSignImage){
		super(Field.FOCUSABLE);
				
		m_testText = _text;
						
		m_weiboSignImage	= _weiboSignImage;
		m_headImage 		= _headImage;
		
		final int t_textWidth 	= sm_defaultFont.getAdvance(m_testText);
		m_textHeight			= (t_textWidth / fsm_textWidth) * sm_fontHeight;
		
		m_extendHeight = Math.max(m_textHeight,sm_functionAreaMinHeight);
	}
	
	public void sublayout(int width, int height){
		
		if(sm_selectWeiboItem == this){
			
			add(sm_atBut);
			add(sm_forwardBut);
			add(sm_favoriteBut);
			
			add(sm_textArea);
			
			setPositionChild(sm_atBut,0,sm_atButton_y);
			layoutChild(sm_atBut,sm_atBut.getPreferredWidth(),sm_atBut.getPreferredHeight());
			
			setPositionChild(sm_forwardBut,0,sm_forwardButton_y);
			layoutChild(sm_forwardBut,sm_forwardBut.getPreferredWidth(),sm_forwardBut.getPreferredHeight());
			
			setPositionChild(sm_favoriteBut,0,sm_favoriteButton_y);
			layoutChild(sm_favoriteBut,sm_favoriteBut.getPreferredWidth(),sm_favoriteBut.getPreferredHeight());
			
			setPositionChild(sm_textArea,fsm_headImageWidth + fsm_headImageTextInterval,0);			
			layoutChild(sm_textArea,fsm_textWidth,m_textHeight);
			
			height = m_extendHeight;
			
		}else{		
			height = sm_fontHeight;
		}
		
		setExtent(recvMain.fsm_display_width,height);
	}
		
	protected void subpaint(Graphics _g){
		
		if(sm_selectWeiboItem == this){
			
			_g.drawBitmap(0, 0, fsm_headImageWidth, fsm_headImageWidth, m_weiboSignImage, 0, 0);
			_g.drawBitmap(0, 0, fsm_headImageWidth, fsm_headImageWidth, m_headImage, 0, fsm_weiboSignImageWidth);
			
			paintChild(_g,sm_atBut);
			paintChild(_g,sm_forwardBut);
			paintChild(_g,sm_favoriteBut);
			
			paintChild(_g,sm_textArea);
			
		}else{
			
			_g.drawBitmap(0, 0, fsm_weiboSignImageWidth, fsm_weiboSignImageWidth, m_weiboSignImage, 0, 0);
			_g.drawText(m_testText.substring(0, 40), fsm_weiboSignImageWidth,0,Graphics.ELLIPSIS);
		}
	}
	
	public int GetCurrentHeight(){
		return sm_selectWeiboItem == this?m_extendHeight:m_height;
	}
	
}

class MainManager extends Manager {
	
	recvMain			m_mainApp;
	
	public MainManager(recvMain _mainApp){
		super(Field.FOCUSABLE);
		
		m_mainApp  = _mainApp;
	}
	
	public int getPreferredWidth(){
		return recvMain.fsm_display_width;
	}
	
	public int getPreferredHeight(){
		int t_totalHeight = 0;
		
		final int t_num = getFieldCount();
		for(int i = 0;i < t_num;i++){
			WeiboItemField t_item = (WeiboItemField)getField(i);			
			t_totalHeight += t_item.GetCurrentHeight();
		}
		
		return t_totalHeight;
	}
	
	protected void sublayout(int width, int height){
		int t_totalHeight = 0;
		
		final int t_num = getFieldCount();
		for(int i = 0;i < t_num;i++){
			WeiboItemField t_item = (WeiboItemField)getField(i);
			
			setPositionChild(t_item, 0,t_totalHeight);			
			layoutChild(t_item,recvMain.fsm_display_width,t_item.GetCurrentHeight());
			
			t_totalHeight += t_item.GetCurrentHeight();
		}
		
		setExtent(recvMain.fsm_display_width,t_totalHeight);
	}
	
	protected void subpaint(Graphics graphics){
		final int t_num = getFieldCount();
		for(int i = 0;i < t_num;i++){
			WeiboItemField t_item = (WeiboItemField)getField(i);
			paintChild(graphics, t_item);
		}
	}
	
	public void AddWeibo(String _text,Bitmap _headImage,Bitmap _signImage){
		WeiboItemField t_item = new WeiboItemField(_text,_headImage,_signImage);
		add(t_item);
		
		invalidate();
	}
}

public class weiboTimeLineScreen extends MainScreen{
	
	recvMain			m_mainApp;
	MainManager			m_mainMgr;
	
	public weiboTimeLineScreen(recvMain _mainApp){
		m_mainApp = _mainApp;
		
		m_mainMgr = new MainManager(_mainApp);
		add(m_mainMgr);
		
		try{
			byte[] bytes = IOUtilities.streamToBytes(m_mainApp.getClass().getResourceAsStream("/Unknown_resize.jpg"));		
			Bitmap t_headImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
			
			m_mainMgr.AddWeibo("Just a Test",t_headImage,t_headImage);
			m_mainMgr.AddWeibo("Just a Test2",t_headImage,t_headImage);
		}catch(Exception e){}
		
	}
	
	public boolean onClose(){
		close();	
		return true;
	}
	

}
