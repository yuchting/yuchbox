package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;

public class WeiboButton extends Field
{
    private static final int fsm_textColour = 0xffffff;
    
    private static final int fsm_textBGColor = 0x7ebdff;
    private static final int fsm_focusBGColor = 0x478dd7;
    
    private static final int fsm_borderColor = 0x3c7cbf;
    
    private static final int fsm_borderArc = 5;
    
    public  static final int fsm_borderWidth = 1;
      
    private int _menuOrdinal = 0;
    private int _menuPriority = 0;
                
    String 	m_text;    

    public WeiboButton( String text){
        this( text,0,0,0);
    }
    
    public WeiboButton( String text,long _style){
        this(text,0,0,_style);
    } 

    public WeiboButton( String text, int menuOrdinal, int menuPriority, long style ){
        super(Field.FOCUSABLE | style );

        m_text = text;
        _menuOrdinal = menuOrdinal;
        _menuPriority = menuPriority;
    }
        
    protected void layout(int width,int height){
    	height = getFont().getHeight() + fsm_borderWidth;
    	width = getFont().getAdvance(m_text) + fsm_borderWidth * 2;
    	
    	setExtent(width,height);
    }
    
    protected void paint( Graphics g ){
    	focusPaint(g,isFocus());
    }
    
    private void focusPaint(Graphics g,boolean focus){
    	int oldColour = g.getColor();

    	try{
  	
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
                 
    	}finally{
    		g.setColor( oldColour );
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
        if( _menuOrdinal < 0
         || _menuPriority < 0 ) {
            return null;
        }
        return new MenuItem( getMenuText(), _menuOrdinal, _menuPriority ) {
            public void run() {
                fieldChangeNotify( 0 );
            }
        };
    }
            
}

