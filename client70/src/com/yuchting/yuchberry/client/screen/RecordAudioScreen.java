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

import java.io.ByteArrayOutputStream;

import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import local.yblocalResource;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Screen;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.ImageUnit;
    
public class RecordAudioScreen{
	
	final class AudioRecorderThread extends Thread {
		
	    private Player _player;
	    private RecordControl _rcontrol;
	    private ByteArrayOutputStream _output;
	    private byte _data[];
	    AudioRecorderThread() {}

	    public byte[] getAudioBuffer() {
	       return _data;
	    }

	    public void run() {
	        try{
	            // Create a Player that records live audio.
	        	//_player = javax.microedition.media.Manager.createPlayer("capture://audio"); // 30KB/20sec same as audio/amr
	            _player = javax.microedition.media.Manager.createPlayer("capture://audio?encoding=audio/amr"); // 30KB/20sec
	            //_player = javax.microedition.media.Manager.createPlayer("capture://audio?encoding=audio/x-tone-seq"); // unsupported
	            //_player = javax.microedition.media.Manager.createPlayer("capture://audio?encoding=audio/mpeg"); // unsupported
	            //_player = javax.microedition.media.Manager.createPlayer("capture://audio?encoding=audio/x-wav");  // unsupported
	            //_player = javax.microedition.media.Manager.createPlayer("capture://audio?encoding=audio/pcm"); // unsupported
	            //_player = javax.microedition.media.Manager.createPlayer("capture://audio?encoding=audio/basic"); // 300KB/20sec (wav file)
	            
	            
	            _player.realize();

	            // Get the RecordControl, configure the record stream,
	            _rcontrol = (RecordControl)_player.getControl("RecordControl");

	            //Create a ByteArrayOutputStream to record the audio stream.
	            _output = new ByteArrayOutputStream();
	            _rcontrol.setRecordStream(_output);
	            _rcontrol.startRecord();
	            _player.start();
	        }catch(Exception e) {
	        	m_mainApp.SetErrorString("RAS-ARTR:"+e.getMessage()+e.getClass().getName());
	        }
	    }

	    public void stop() {
	    	
        	if(_player != null){
        		//Stop recording, record data from the OutputStream,
	            //close the OutputStream and player.
	        	//
        		try{
		            _rcontrol.commit();
		            _data = _output.toByteArray();
		            _output.close();
		            _player.close();
		            
		            _player = null;
	            
    	        }catch(Exception e){
    	        	m_mainApp.SetErrorString("RAS-ARTS:"+e.getMessage()+e.getClass().getName());
    		    } 
        	}
		}
	}
	    
	public final static int		fsm_maxRecordingTime = 20;
	
	public static int				fsm_screenWidth 	= 200;
	public final static int		fsm_screenHeight 	= 150;
	
	AudioRecorderThread m_recordThread = null;
	IRecordAudioScreenCallback m_callback = null;
	
	String m_remain = null;
	
	recvMain m_mainApp = null;
	Screen m_parentScreen = null;
	
	int m_remainTimer = fsm_maxRecordingTime;
	Thread m_remainTimerTheard = null;
	
	static ImageUnit	sm_microphone = recvMain.sm_weiboUIImage.getImageUnit("microphone");
	static BubbleImage	sm_microphone_block = new BubbleImage(
													recvMain.sm_weiboUIImage.getImageUnit("microphone_top_left"),
													recvMain.sm_weiboUIImage.getImageUnit("microphone_top"),
													recvMain.sm_weiboUIImage.getImageUnit("microphone_top_right"),
													recvMain.sm_weiboUIImage.getImageUnit("microphone_right"),
													
													recvMain.sm_weiboUIImage.getImageUnit("microphone_bottom_right"),
													recvMain.sm_weiboUIImage.getImageUnit("microphone_bottom"),
													recvMain.sm_weiboUIImage.getImageUnit("microphone_bottom_left"),
													recvMain.sm_weiboUIImage.getImageUnit("microphone_left"),
													
													recvMain.sm_weiboUIImage.getImageUnit("microphone_inner_block"),
													new ImageUnit[]{null,null,null,null,},
													recvMain.sm_weiboUIImage);
	
	public RecordAudioScreen(recvMain _mainApp,Screen _parentScreen,IRecordAudioScreenCallback _callback){
		m_mainApp = _mainApp;
		m_parentScreen = _parentScreen;
		m_callback = _callback;
		
		// get the length
		fsm_screenWidth = Font.getDefault().getAdvance(recvMain.sm_local.getString(yblocalResource.RECORDING_REMAIN_LABEL) + "00") + 10;
	}
	
	public void startRecord(){
		
		if(m_recordThread != null){
			m_recordThread.stop();
		}
		
		m_recordThread = new AudioRecorderThread();
		m_recordThread.start();
		
		m_remainTimer = fsm_maxRecordingTime;
		m_remain = recvMain.sm_local.getString(yblocalResource.RECORDING_REMAIN_LABEL) + Integer.toString(m_remainTimer);
		
		m_remainTimerTheard = new Thread(){
			public void run(){
				while(true){

					try{
						sleep(1000);
					}catch(Exception e){
						break;
					}
			
					m_remain = recvMain.sm_local.getString(yblocalResource.RECORDING_REMAIN_LABEL) + 
									Integer.toString(--m_remainTimer);
					if(m_remainTimer <= 0){
						close();
						
						break;
					}else{
						m_parentScreen.invalidate();
					}
				}					
			}
		};
		
		m_remainTimerTheard.start();
	}
	
	
	public void close(){
		
		if(m_recordThread != null){
			m_recordThread.stop();
			m_callback.recordDone(m_recordThread.getAudioBuffer());
			m_recordThread = null;
		}
		
		if(m_remainTimerTheard != null){
			if(m_remainTimerTheard.isAlive()){
				m_remainTimerTheard.interrupt();
			}
			m_remainTimerTheard = null;			
		}
		
		m_parentScreen.invalidate();
	}
	
	public void paint(Graphics _g){
		int gb_x = (recvMain.fsm_display_width - fsm_screenWidth) / 2;
		int gb_y = (recvMain.fsm_display_height - fsm_screenHeight) / 2;
		
		sm_microphone_block.draw(_g, gb_x, gb_y, fsm_screenWidth, fsm_screenHeight, BubbleImage.NO_POINT_STYLE);
		
		int x = gb_x + (fsm_screenWidth - sm_microphone.getWidth()) / 2;
		int y = gb_y + (fsm_screenHeight - sm_microphone.getHeight()) / 2;
		
		recvMain.sm_weiboUIImage.drawImage(_g, sm_microphone, x, y);

		// render the remain time label 
		//
		int t_color = _g.getColor();
		try{
			_g.setColor(0xd0d0d0);
			
			int t_length = m_parentScreen.getFont().getAdvance(m_remain);
			
			x = gb_x + (fsm_screenWidth - t_length) / 2;
			y = y + sm_microphone.getHeight();
			
			_g.drawText(m_remain,x,y);
			
		}finally{
			_g.setColor(t_color);
		}
	}
}
