package com.yuchting.yuchberry.client.screen;

import java.io.ByteArrayInputStream;

import javax.microedition.media.Player;

import com.yuchting.yuchberry.client.recvMain;

public class audioViewScreen extends fileViewScreen{
	
	Player 		m_player		 = null;
	
	public audioViewScreen(String _filename,recvMain _mainApp)throws Exception{
		super(_filename,_mainApp,true);
	
		String t_type = null;
		if(_filename.indexOf(".mp3") != -1){
			t_type = "audio/mpeg";
		}else if(_filename.indexOf(".wav") != -1){
			t_type = "audio/wav";
		}else if(_filename.indexOf(".mid") != -1){
			t_type = "audio/midi";
		}else{
			t_type = "audio/basic";
		}
				
		m_player = javax.microedition.media.Manager.createPlayer(new ByteArrayInputStream(m_fileContain),t_type);
		
		m_player.realize();
		m_player.prefetch();
		m_player.start();
	}
	
	public boolean onClose(){
		m_player.close();
		
		return super.onClose();		
	}
}
