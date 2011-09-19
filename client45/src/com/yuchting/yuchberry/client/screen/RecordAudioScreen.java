package com.yuchting.yuchberry.client.screen;

import java.io.ByteArrayOutputStream;
import javax.microedition.media.Player;
import javax.microedition.media.control.RecordControl;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import local.localResource;
import com.yuchting.yuchberry.client.recvMain;
    
public class RecordAudioScreen extends PopupScreen{
	
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
	            _player = javax.microedition.media.Manager.createPlayer("capture://audio");
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
	        try{
	            //Stop recording, record data from the OutputStream,
	            //close the OutputStream and player.
	        	//
	            _rcontrol.commit();
	            _data = _output.toByteArray();
	            _output.close();
	            _player.close();
	            
	        }catch(Exception e){
	        	m_mainApp.SetErrorString("RAS-ARTS:"+e.getMessage()+e.getClass().getName());
		    } 
		}
	}
	    
	public final static int		fsm_maxRecordingTime = 20;
	
	AudioRecorderThread m_recordThread = null;
	IRecordAudioScreenCallback m_callback = null;
	
	LabelField m_remain = null;
	
	recvMain m_mainApp = null;
	
	int m_remainTimer = fsm_maxRecordingTime;
	int m_remainTimerID = -1;
	
	public RecordAudioScreen(recvMain _mainApp,IRecordAudioScreenCallback _callback){
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL));
		m_mainApp = _mainApp;
		m_callback = _callback;
		
		LabelField t_recording = new LabelField(recvMain.sm_local.getString(localResource.RECORDING_LABEL));
		add(t_recording);
		
		m_remain = new LabelField(Integer.toString(fsm_maxRecordingTime),Field.FIELD_HCENTER);
		add(m_remain);
	}
	
	protected void onDisplay(){
		super.onDisplay();
		if(m_recordThread != null){
			m_recordThread.stop();
		}
		
		m_recordThread = new AudioRecorderThread();
		m_recordThread.start();
		
		m_remainTimer = fsm_maxRecordingTime;
		
		m_remainTimerID = m_mainApp.invokeLater(new Runnable(){
			public void run(){
				m_remain.setText(Integer.toString(--m_remainTimer));
				if(m_remainTimer <= 0){
					close();
				}
			}
		}, 1000, true);
	}
	
	public boolean onClose(){
		close();
		return true;
	}
	
	public void close(){
		
		if(m_recordThread != null){
			m_recordThread.stop();
			
			m_callback.recordDone(m_recordThread.getAudioBuffer());
		}
		
		if(m_remainTimerID != -1){
			m_mainApp.cancelInvokeLater(m_remainTimerID);
			m_remainTimerID = -1;	
		}
				
		super.close();		
	}
	
	
}
