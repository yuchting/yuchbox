package com.yuchting.yuchberry.client.screen;

import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import local.localResource;
import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;


/**
 * A UI screen to display the camera display and buttons.
 */
public class CameraScreen extends MainScreen
{
    /** The camera's video controller. */
    private VideoControl m_videoControl;

    /** The field containing the feed from the camera. */
    private Field m_videoField;
            
    private final String fsm_encoding_800 = "encoding=jpeg&width=800&height=600&quality=fine";
    private final String fsm_encoding_1024 = "encoding=jpeg&width=1024&height=768&quality=fine";
    private final String fsm_encoding_1600 = "encoding=jpeg&width=1600&height=1200&quality=fine";

    private ICameraScreenCallback m_snapOKCallback = null;
    
    int			m_maxWidth			= 800;
    /**
     * Constructor. Initializes the camera 
     */
    public CameraScreen(ICameraScreenCallback _callback,int _maxWidth){
    	
    	m_snapOKCallback = _callback;
    	m_maxWidth		= _maxWidth;
    	
        //Initialize the camera object and video field.
        initializeCamera();
        
        addMenuItem(takePhoto);
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
            Player player = javax.microedition.media.Manager.createPlayer( "capture://video" );

            //Set the player to the REALIZED state (see Player docs.)
            player.realize();

            //Grab the video control and set it to the current display.
            m_videoControl = (VideoControl)player.getControl( "VideoControl" );   
            
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
            player.start();
            
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
    
    private MenuItem takePhoto = new MenuItem(recvMain.sm_local.getString(localResource.CAMERA_SCREEN_TAKE_LABEL), 1000, 10){
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
                this.close();
                return true;
            default:
                return super.keyChar(c, status, time);
        }
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