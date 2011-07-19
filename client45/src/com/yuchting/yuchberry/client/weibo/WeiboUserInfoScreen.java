package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.NullField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.component.TextField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;



public class WeiboUserInfoScreen extends MainScreen{

	fetchWeiboUser		m_weiboUser = null;	
	WeiboMainManager	m_weiboMgr	= null;
	recvMain			m_mainApp	= null;
	
	Bitmap				m_headImage = null;
	
	final class DescTextField extends TextField{
		public DescTextField(){
			super(Field.READONLY);
		}
		
		public void setText(String _text){
			super.setText(_text);
			layout(recvMain.fsm_display_width,1000);
		}
		
		public void paint(Graphics _g){
			super.paint(_g);
		}
	}	
	
	final class InfoField extends Field{
		
		private int		m_infoFieldHeight = 15;
		
		public InfoField(){
			super(Field.NON_FOCUSABLE);
		}
		
		public int getPreferredHeight(){
			return  m_infoFieldHeight;
		}
		
		public void setFieldHeight(int _height){
			m_infoFieldHeight = _height;
		}
		
		public int getPreferredWidth(){
			return recvMain.fsm_display_width; 
		}
		
		public void layout(int _width,int _height){
			setExtent(getPreferredWidth(), getPreferredHeight());
		}
			
		public void paint(Graphics _g){
			
			final int ft_interval = 3;
			
			int t_start_x = ft_interval;
			int t_start_y = ft_interval;
			
			int t_color = _g.getColor();
			try{
				_g.setColor(0xf0f0f0);
				_g.fillRect(0, 0, getPreferredWidth(),getPreferredHeight());
				
				_g.drawBitmap(t_start_x, t_start_y,
						WeiboItemField.fsm_weiboSignImageSize,WeiboItemField.fsm_weiboSignImageSize, 
						weiboTimeLineScreen.GetWeiboSign(m_weiboUser.getStyle()), 0, 0);
				
				
					
				if(m_weiboUser.isVerified()){
					_g.drawBitmap(t_start_x,WeiboItemField.fsm_weiboSignImageSize + t_start_y + ft_interval,
							WeiboItemField.fsm_weiboVIPImageSize,WeiboItemField.fsm_weiboVIPImageSize, 
							weiboTimeLineScreen.GetVIPSignBitmap(m_weiboUser.getStyle()), 0, 0);
				}
				
				t_start_x += WeiboItemField.fsm_weiboSignImageSize + ft_interval;
				
				// head image
				//
				if(m_headImage != null){
					_g.drawBitmap(t_start_x ,t_start_y,
							fetchWeibo.fsm_headImageSize_l,fetchWeibo.fsm_headImageSize_l,
							m_headImage,0,0);
					
					t_start_x += fetchWeibo.fsm_headImageSize_l + ft_interval;
				}
				
				// name and location
				//
				t_start_x += ft_interval;
				t_start_y += 2;
				drawRoundRect(_g,t_start_x,t_start_y,
						recvMain.fsm_display_width - t_start_x - ft_interval,
						WeiboItemField.sm_fontHeight * 2 + 4,true);
				
				t_start_x += ft_interval;
				t_start_y += 2;
				
				_g.setColor(0xa0a0a0);
				
				if(m_weiboUser.getName().equals(m_weiboUser.getScreenName())){
					_g.drawText(m_weiboUser.getName() , t_start_x,t_start_y);
				}else{
					_g.drawText(m_weiboUser.getName() + "(" + m_weiboUser.getScreenName() + ")", t_start_x,t_start_y);
				}
				
				t_start_y += WeiboItemField.sm_fontHeight + ft_interval;
				_g.drawText(m_weiboUser.getCity(), t_start_x,t_start_y);
				
				// description
				//
				t_start_x = 3;
				t_start_y += WeiboItemField.sm_fontHeight + ft_interval;
				m_descText.setText(m_weiboUser.getDesc());
				
				int t_desc_width = recvMain.fsm_display_width - ft_interval * 2;
				int t_desc_height = m_descText.getHeight() + ft_interval * 2;
				
				drawRoundRect(_g,t_start_x,t_start_y,t_desc_width,t_desc_height,false);
				
				boolean notEmpty = _g.pushContext( t_start_x + ft_interval ,t_start_y + ft_interval ,
						t_desc_width , t_desc_height, t_start_x + ft_interval, t_start_y + ft_interval);
				try {
					if(notEmpty){
						m_descText.paint(_g);
					}
				} finally {
					_g.popContext();
					_g.setColor(t_color);
				}
				
				t_start_y += m_descText.getHeight() + ft_interval * 3;
				
				// follow and fans count
				//
				int t_width		= recvMain.fsm_display_width / 3;
				int t_infoWidth = t_width - ft_interval * 2;
				
				int t_follow_x	= ft_interval;				
				int t_fans_x	= t_width + ft_interval;
				int t_weibo_x	= t_width * 2 + ft_interval;
				
				String t_follow_text = "follow";
				String t_fans_text = "fans";
				String t_weibo_text = "weibo";
				
				String t_follow_num = Integer.toString(m_weiboUser.getFollowNum());
				String t_fans_num = Integer.toString(m_weiboUser.getFansNum());
				String t_weibo_num = Integer.toString(m_weiboUser.getWeiboNum());
				
				int t_follow_text_x	= t_follow_x + (t_infoWidth - WeiboItemField.sm_defaultFont.getAdvance(t_follow_text)) / 2 ;
				int t_fans_text_x	= t_fans_x + (t_infoWidth - WeiboItemField.sm_defaultFont.getAdvance(t_fans_text)) / 2;
				int t_weibo_text_x	= t_weibo_x + (t_infoWidth - WeiboItemField.sm_defaultFont.getAdvance(t_weibo_text)) / 2;
				
				int t_follow_num_x	= t_follow_x + (t_infoWidth - WeiboItemField.sm_defaultFont.getAdvance(t_follow_num)) / 2 ;
				int t_fans_num_x	= t_fans_x + (t_infoWidth - WeiboItemField.sm_defaultFont.getAdvance(t_fans_num)) / 2;
				int t_weibo_num_x	= t_weibo_x + (t_infoWidth - WeiboItemField.sm_defaultFont.getAdvance(t_weibo_num)) / 2;
				
				drawRoundRect(_g,t_follow_x,t_start_y,t_infoWidth,WeiboItemField.sm_fontHeight * 2 + ft_interval * 2,true);
				drawRoundRect(_g,t_fans_x,t_start_y,t_infoWidth,WeiboItemField.sm_fontHeight * 2 + ft_interval * 2,true);
				drawRoundRect(_g,t_weibo_x,t_start_y,t_infoWidth,WeiboItemField.sm_fontHeight * 2 + ft_interval * 2,true);
				
				t_start_y += ft_interval;
				
				_g.drawText(t_follow_text,t_follow_text_x,t_start_y);
				_g.drawText(t_fans_text,t_fans_text_x,t_start_y);
				_g.drawText(t_weibo_text,t_weibo_text_x,t_start_y);
				
				t_start_y += WeiboItemField.sm_fontHeight + 2 + ft_interval;
				
				_g.drawText(t_follow_num,t_follow_num_x,t_start_y);
				_g.drawText(t_fans_num,t_fans_num_x,t_start_y);
				_g.drawText(t_weibo_num,t_weibo_num_x,t_start_y);
				
			}finally{
				_g.setColor(t_color);
			}
		}
	}
	
	DescTextField		m_descText = new DescTextField();
	InfoField			m_infoField = new InfoField();
	
	static void drawRoundRect(Graphics _g,int _x,int _y,int _width,int _height,boolean _middleLine){
		final int t_arc = 12;
		
		int t_color = _g.getColor();
		try{
			_g.setColor(0xffffff);
			_g.fillRoundRect(_x,_y,_width,_height,t_arc,t_arc);
			
			_g.setColor(0x8f8f8f);
			_g.drawRoundRect(_x,_y,_width,_height,t_arc,t_arc);
			
			if(_middleLine){
				_g.drawLine(_x,_y + _height / 2,_x + _width - 1,_y + _height / 2);
			}
			
		}finally{
			_g.setColor(t_color);
		}
	}
	
	public WeiboUserInfoScreen(recvMain _mainApp){
		m_mainApp = _mainApp;
		m_weiboMgr = new WeiboMainManager(_mainApp,_mainApp.m_weiboTimeLineScreen,false);
		
		add(m_infoField);
		add(new SeparatorField());
		add(m_weiboMgr);
	}
	
	private NullField m_nullField = new NullField();
	
	public void setWeiboUser(fetchWeiboUser _weiboUser){
		
		if(m_weiboUser != _weiboUser){
			m_weiboUser = _weiboUser;
			
			if(m_weiboUser != null){
							
				if(m_weiboUser.getHeadImage() != null){					
					try{
						m_headImage = EncodedImage.createEncodedImage(m_weiboUser.getHeadImage(), 0, m_weiboUser.getHeadImage().length).getBitmap();
					}catch(Exception e){
						m_mainApp.SetErrorString("SWU1:"+e.getMessage() + e.getClass().getName());
					}
				}
				
				if(m_headImage == null){
					try{
						byte[] bytes = IOUtilities.streamToBytes(m_mainApp.getClass().getResourceAsStream("/defaultHeadImage_l.png"));		
						m_headImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
					}catch(Exception e){
						m_mainApp.SetErrorString("SWU2:"+e.getMessage() + e.getClass().getName());
					}
				}
				
				m_weiboMgr.setCurrEditItem(null);
				m_weiboMgr.setCurrExtendedItem(null);
				m_weiboMgr.setCurrSelectedItem(null);
				
				m_weiboMgr.deleteAll();
				
				WeiboHeadImage t_headImage = new WeiboHeadImage();
				t_headImage.m_headImage = m_headImage;
								
				for(int i = 0;i < m_weiboUser.getUpdatedWeibo().size();i++){
					fetchWeibo weibo = (fetchWeibo)m_weiboUser.getUpdatedWeibo().elementAt(i);
					m_weiboMgr.AddWeibo(weibo, t_headImage, true);
				}
				
				// set the field height
				//
				m_descText.setText(m_weiboUser.getDesc());
				m_infoField.setFieldHeight(121 + m_descText.getHeight());
				
				// to rise the layout calling
				//
				replace(m_infoField,m_nullField);
				replace(m_nullField,m_infoField);				
			}
		}
	}
	
	
}
