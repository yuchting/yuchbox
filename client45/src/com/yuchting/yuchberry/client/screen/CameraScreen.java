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
package com.yuchting.yuchberry.client.screen;

import java.util.Vector;

import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import local.yblocalResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;

final class EncodeFormat{
	public int width = 0;
	public String encode = "";
}

/**
 * A UI screen to display the camera display and buttons.
 */
public class CameraScreen extends MainScreen
{
    /** The camera's video controller. */
    private VideoControl m_videoControl;

    /** The field containing the feed from the camera. */
    private Field m_videoField;
    
    private Player m_player;
            
    private static String fsm_encoding_800 = null;
    private static String fsm_encoding_1024 = null;
    private static String fsm_encoding_1600 = null;

    private ICameraScreenCallback m_snapOKCallback = null;
    
    int			m_maxWidth			= 800;

    /**
     * Constructor. Initializes the camera 
     */
    public CameraScreen(ICameraScreenCallback _callback,int _maxWidth)throws Exception{
    	
    	m_snapOKCallback = _callback;
    	m_maxWidth		= _maxWidth;
    	
    	initializeSnapshotSize();
    	
        //Initialize the camera object and video field.
        initializeCamera();
        
        addMenuItem(takePhoto);
        
    }
    
   
    
    private void initializeSnapshotSize()throws Exception{
    	
    	if(fsm_encoding_800 != null){
    		return;
    	}
    	
    	String t_encodeList = System.getProperty("video.snapshot.encodings");
    	
    	Vector t_encodings = new Vector();
    	
    	int t_start = 0;
    	int t_end = 0;
    	
    	while(true){
    		
    		if((t_end = t_encodeList.indexOf(' ', t_start)) == -1){
    			break;
    		}
    		
    		String t_encoder = t_encodeList.substring(t_start,t_end);
    		int t_widthIndex = 0;
    		if((t_widthIndex = t_encoder.indexOf("width=")) != -1){
    			int t_widthEndIndex = t_encoder.indexOf('&',t_widthIndex);
    			String t_width = t_encoder.substring(t_widthIndex + 6,t_widthEndIndex);


    			EncodeFormat t_formatEncode = new EncodeFormat();
    			t_formatEncode.width = Integer.valueOf(t_width).intValue();
    			t_formatEncode.encode = t_encoder;
    			
    			boolean added = false;
    			
    			for(int i = 0;i < t_encodings.size();i++){
    				EncodeFormat t_format = (EncodeFormat)t_encodings.elementAt(i);
    				if(t_format.width > t_formatEncode.width){
    					
    					added = true;
    					
    					t_encodings.insertElementAt(t_formatEncode, i);
    					break;
    				}
    			}
    			
    			if(!added){
    				t_encodings.addElement(t_formatEncode);
    			}
    		}
    		
    		t_start = t_end + 1;
    	}
    	
    	if(t_encodings.isEmpty()){
    		throw new Exception("can't snapshot in this deveice!");
    	}
    	
    	EncodeFormat t_low = (EncodeFormat)t_encodings.elementAt(0);
    	EncodeFormat t_mid = (EncodeFormat)t_encodings.elementAt(t_encodings.size() / 2);
    	EncodeFormat t_high = (EncodeFormat)t_encodings.elementAt(t_encodings.size() - 1);
    	
    	fsm_encoding_800 = t_low.encode;
    	fsm_encoding_1024 = t_mid.encode;
    	fsm_encoding_1600 = t_high.encode;    	
    }

    /**
     * Prevent the save dialog from being displayed.
     * @see net.rim.device.api.ui.container.MainScreen#onSavePrompt()
     */
    public boolean onSavePrompt(){
        return true;
    }

    /**
     * Initializes the Player, VideoControl and VideoField.
     */
    private void initializeCamera()
    {
        try{
        	
            //Create a player for the Blackberry's camera.
        	m_player = javax.microedition.media.Manager.createPlayer( "capture://video" );

            //Set the player to the REALIZED state (see Player docs.)
        	m_player.realize();

            //Grab the video control and set it to the current display.
            m_videoControl = (VideoControl)m_player.getControl( "VideoControl" );   
            
            if(m_videoControl != null){
                //Create the video field as a GUI primitive (as opposed to a
                //direct video, which can only be used on platforms with
                //LCDUI support.)
                m_videoField = (Field) m_videoControl.initDisplayMode (VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field");
                
                m_videoControl.setDisplayFullScreen(true);
                
                //Display the video control
                m_videoControl.setVisible(true);
                
            }

            //Set the player to the STARTED state (see Player docs.)
            m_player.start();
            
            if(m_videoField != null){
            	add(m_videoField);
            }
            
        }catch(Exception e){
            Dialog.alert( "ERROR " + e.getClass() + ":  " + e.getMessage() );
        }
    }
    
    private String getEncodeSize(){
    	
    	switch(m_maxWidth){
    	case 800:
    		return fsm_encoding_800;
    	case 1024:
    		return fsm_encoding_1024;
    	default:
    		return fsm_encoding_1600;
    	}
    }
    
    private MenuItem takePhoto = new MenuItem(recvMain.sm_local.getString(yblocalResource.CAMERA_SCREEN_TAKE_LABEL), 1000, 10){
        public void run(){
            try{

            	if(m_videoControl != null){
            		//Retrieve the raw image from the VideoControl and
                    //create a screen to display the image to the user.
                    createImageScreen( m_videoControl.getSnapshot( getEncodeSize() ) );
            	}
                
            }catch(Throwable e){
                Dialog.alert( "ERROR " + e.getClass() + ":  " + e.getMessage() );
            }
        }    
    };
    
    private void createImageScreen( byte[] raw ){    
    	if(m_snapOKCallback != null){
    		m_snapOKCallback.snapOK(raw);
    	}
    	close();
    }
    
	protected  boolean keyDown(int keycode,int time) {
	    //System.out.println("Input" + keycode + "/" + Keypad.key(keycode) + " C1 = " + Keypad.KEY_CONVENIENCE_1 +  " C2 = " + Keypad.KEY_CONVENIENCE_2);
	    if ( Keypad.key(keycode) == Keypad.KEY_CONVENIENCE_1 ) {
	        return true;
	    }
	    return super.keyDown(keycode, time);
	}

    protected  boolean keyChar(char c, int status, int time) {
        //System.out.println("Input" + c + ":" + Keypad.getKeyCode(c, status));
        switch (c) {
            case Characters.ESCAPE:
                close();
                return true;
            default:
                return super.keyChar(c, status, time);
        }
    }

    public void close(){
    	
    	if(m_player != null){
    		try{
    			m_player.stop();
    		}catch(Exception e){}
    		
    		m_player.close();
    	}
    	    	
    	super.close();
    }
    /**
     * Handle trackball click events.
     * @see net.rim.device.api.ui.Screen#invokeAction(int)
     */   
    protected boolean invokeAction(int action){
        boolean handled = super.invokeAction(action); 
                
        if(!handled && action == ACTION_INVOKE){
        	       
            try{
                
            	if(m_videoControl != null){
            		createImageScreen( m_videoControl.getSnapshot( getEncodeSize() ) );
            	}
                
            }catch(Throwable e){
                Dialog.alert( "ERROR " + e.getClass() + ":  " + e.getMessage() );
            }

            return true;
        }
               
        return handled;                
    }
}