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
package com.yuchting.yuchberry.client.ui;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;


public class ImageButton extends Field{
	
    protected static final int fsm_textColour = 0xffffff;
    
    protected static final int fsm_textBGColor = 0x7ebdff;
    protected static final int fsm_focusBGColor = 0x478dd7;
    
    protected static final int fsm_borderColor = 0x3c7cbf;
    
    protected static final int fsm_borderArc = 5;
    
    public  static final int fsm_borderWidth = 2;

    protected String 	m_text = "";
    
    protected ImageUnit	m_image = null;
    protected ImageUnit	m_image_focus = null;
    protected ImageSets	m_imageSets = null;

    public ImageButton( String text,ImageUnit _image,ImageUnit _image_focus,ImageSets _imageSets){
        this( text,_image,_image_focus,_imageSets,0);
    }

    public ImageButton( String text,ImageUnit _image,ImageUnit _image_focus,ImageSets _imageSets,long style){
        super(Field.FOCUSABLE | style );

        m_text = text;
        m_image = _image;
        m_image_focus = _image_focus;
        m_imageSets = _imageSets;
    }
    
    public ImageUnit getImage(){return m_image;}
    public ImageUnit getFocusImage(){return m_image_focus;}
    
    public ImageSets getImageSets(){return m_imageSets;}
    
    public int getImageWidth(){
    	return m_image.getWidth();
    }
    
    public int getImageHeight(){
    	return m_image.getHeight();
    }
            
    protected void layout(int width,int height){
    	if(m_image != null && m_image_focus != null){
    		width = getImageWidth();
    		height= getImageHeight();
    	}else{
    		width = getFont().getAdvance(m_text) + fsm_borderWidth * 2;
    		height = getFont().getHeight() + fsm_borderWidth;
    	}
    	
    	setExtent(width,height);
    }
    
    protected void paint( Graphics g ){
    	focusPaint(g,isFocus());
    }
    
    protected void focusPaint(Graphics g,boolean focus){
    	int oldColour = g.getColor();
    	Font oldFont	= g.getFont();
    	try{
  	
    		if(m_image != null && m_image_focus != null){
    			
    			int x = (getImageWidth() - m_image_focus.getWidth()) / 2;
				int y = (getImageHeight() - m_image_focus.getHeight()) / 2;
				
    			if(focus){	
    				m_imageSets.drawImage(g,m_image_focus,x,y);
    			}else{
    				m_imageSets.drawImage(g,m_image,x,y);
    			}   			
    			
    		}else{

        		if( focus ) {
        			g.setColor(fsm_focusBGColor);
        		}else{
        			g.setColor(fsm_textBGColor);
        		}    		
        		g.fillRoundRect(0,0,getWidth(),getHeight(),fsm_borderArc,fsm_borderArc);
        		    		
        		g.setColor(fsm_borderColor);
        		g.drawRoundRect(0,0,getWidth(),getHeight(),fsm_borderArc,fsm_borderArc);
        		
        		g.setColor( fsm_textColour );
        		g.drawText(m_text,fsm_borderWidth,fsm_borderWidth);
    		}
    		
    	}finally{
    		g.setColor( oldColour );
    		g.setFont(oldFont);
    	}
    }
    
    protected void drawFocus( Graphics g, boolean on ){
    	focusPaint(g,on);
    }
    
    protected void onUnfocus(){
    	super.onUnfocus();
    	invalidate();
    }
            
            
    protected boolean keyChar( char character, int status, int time ) 
    {
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

    protected boolean invokeAction( int action ) 
    {
        switch( action ) {
            case ACTION_INVOKE: {
                fieldChangeNotify( 0 );
                return true;
            }
        }
        return super.invokeAction( action );
    }
            

    public void setDirty( boolean dirty ) 
    {
        // We never want to be dirty or muddy
    }
    
            
    public void setMuddy( boolean muddy ) 
    {
        // We never want to be dirty or muddy
    }
    
    
    public String getMenuText()
    {
        return m_text;
    }
    

    /**
     * Returns a MenuItem that could be used to invoke this link.
     */
    public MenuItem getMenuItem()
    {
        return new MenuItem( getMenuText(), 0, 0 ) {
            public void run() {
                fieldChangeNotify( 0 );
            }
        };
    }
            
}

