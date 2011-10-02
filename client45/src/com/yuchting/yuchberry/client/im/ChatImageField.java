package com.yuchting.yuchberry.client.im;

import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Characters;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;

public class ChatImageField extends Field{
	
	public final static int  fsm_imageWidth = 80;
	public final static int  fsm_imageHeight = 60;
	
	
	Bitmap	m_imageBitmap	= null;
	IChatFieldOpen	m_open	= null;
	
	public fetchChatMsg m_msg		= null;
	
	public ChatImageField(){
		super(Field.FOCUSABLE);
	}
	

	public void init(fetchChatMsg _msg,IChatFieldOpen _open){
		m_msg = _msg;
		m_open = _open;
		
		byte[] t_buffer = m_msg.getFileContent();
		
		EncodedImage t_origImage = EncodedImage.createEncodedImage(t_buffer, 0, t_buffer.length);
		
		int t_origWidth = t_origImage.getWidth();
		int t_origHeight = t_origImage.getHeight();
		
		int scaleX = Fixed32.div(Fixed32.toFP(t_origWidth), Fixed32.toFP(getPreferredWidth()));
		int scaleY = Fixed32.div(Fixed32.toFP(t_origHeight), Fixed32.toFP(getPreferredHeight()));
											
		m_imageBitmap = t_origImage.scaleImage32(scaleX, scaleY).getBitmap();
	}
	
	public int getPreferredWidth(){
		return fsm_imageWidth;
	}
	
	public int getPreferredHeight(){
		return fsm_imageHeight;
	}
	
	protected void layout(int width,int height){
    	setExtent(getPreferredWidth(),getPreferredHeight());
    }
	    
	protected void paint( Graphics g ){
	    focusPaint(g,isFocus());
	}
	
	protected void drawFocus( Graphics g, boolean on ){
	    focusPaint(g,on);
	}
	    
	protected void onUnfocus(){
	   	super.onUnfocus();
	   	invalidate();
	}
	
	protected void focusPaint(Graphics g,boolean focus){
		
		g.drawBitmap(0,0,getPreferredWidth(),getPreferredHeight(),m_imageBitmap,0,0);
		
		if(focus){
			int t_color = g.getColor();
			try{
				g.setColor(0x00a7e6);
				g.drawRect(0, 0, getPreferredWidth(), getPreferredHeight());
				g.drawRect(1, 1, getPreferredWidth() - 2, getPreferredHeight() - 2);
			}finally{
				g.setColor(t_color);
			}
		}
	}
	
	protected boolean keyChar( char character, int status, int time ){
        if( character == Characters.ENTER ) {
        	m_open.open(m_msg);
            return true;
        }
        return super.keyChar( character, status, time );
    }
	
	protected boolean navigationClick( int status, int time ) {        
	    keyChar(Characters.ENTER, status, time );            
	    return true;
	}
}
