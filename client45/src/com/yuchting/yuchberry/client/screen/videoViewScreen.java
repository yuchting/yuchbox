package com.yuchting.yuchberry.client.screen;

import javax.microedition.media.Manager;
import javax.microedition.media.Player;
import javax.microedition.media.control.VideoControl;

import com.yuchting.yuchberry.client.recvMain;

public class videoViewScreen extends fileViewScreen{
	Player 		m_player			= null;
	VideoControl m_videoControl		= null;
	
	
	public videoViewScreen(String _filename,recvMain _mainApp) throws Exception{
		super(_filename,_mainApp,false);
		
		m_player = Manager.createPlayer(_filename);
		m_player.realize();
		
        //Create a new VideoControl.
        m_videoControl = (VideoControl)m_player.getControl("VideoControl");
        
        //Initialize the video mode using a Field.
        m_videoControl.initDisplayMode(VideoControl.USE_GUI_PRIMITIVE, "net.rim.device.api.ui.Field");

        //Set the video control to be visible.
        m_videoControl.setVisible(true);
        
        m_player.start();
	}
	
	public boolean onClose(){
		m_player.close();
		m_videoControl.setVisible(false);
		
		return super.onClose();
	}
}
