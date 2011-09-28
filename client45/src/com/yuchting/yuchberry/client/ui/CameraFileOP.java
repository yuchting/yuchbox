package com.yuchting.yuchberry.client.ui;

import java.io.InputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import local.localResource;

import net.rim.device.api.io.file.FileSystemJournal;
import net.rim.device.api.io.file.FileSystemJournalEntry;
import net.rim.device.api.io.file.FileSystemJournalListener;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.system.JPEGEncodedImage;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.XYPoint;
import net.rim.device.api.ui.component.Dialog;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;

public abstract class CameraFileOP implements FileSystemJournalListener{
	
	static private long 			sm_lastUSN = 0;

	public void fileJournalChanged() {
		long nextUSN = FileSystemJournal.getNextUSN();
		
		for(long lookUSN = nextUSN - 1; lookUSN >= sm_lastUSN ; --lookUSN) {
			
			FileSystemJournalEntry entry = FileSystemJournal.getEntry(lookUSN);
			if (entry == null) {
			    break; 
			}
			
			if(entry.getEvent() == FileSystemJournalEntry.FILE_ADDED){
				
				String entryPath = entry.getPath();
				
				if (entryPath != null && canAdded()){
					
					if(entryPath.indexOf(recvMain.fsm_mailAttachDir) != -1
						|| entryPath.indexOf(recvMain.fsm_weiboImageDir) != -1 
						|| entryPath.indexOf(recvMain.fsm_IMImageDir) != -1){
						
						// is not photo
						// is weibo/IM head image
						//
						continue;
					}
					
					
					if(addUploadingPic("file://" + entryPath)){
						sm_lastUSN = lookUSN + 1;
						break;
					}					
				}
			}
		}
	}
		
	public boolean addUploadingPic(String _file){
		
		String t_path = _file.toLowerCase();
		
		int t_type = 0;
		if(t_path.endsWith("png")){
			t_type = fetchWeibo.IMAGE_TYPE_PNG;
		}else if(t_path.endsWith("jpg")){
			t_type = fetchWeibo.IMAGE_TYPE_JPG;
		}else if(t_path.endsWith("bmp")){
			t_type = fetchWeibo.IMAGE_TYPE_BMP;
		}else if(t_path.endsWith("gif")){
			t_type = fetchWeibo.IMAGE_TYPE_GIF;
		}else{
			return false;
		}
		
		onAddUploadingPic(_file,t_type);		
		
		return true;
	}
	static public byte[] resizePicFile(String _imageFile,XYPoint _point)throws Exception{
		
		if(_imageFile != null){
			byte[] t_content = null;
			
			FileConnection t_file = (FileConnection)Connector.open(_imageFile,Connector.READ_WRITE);
			
			if(t_file.exists()){
				try{
					InputStream t_fileIn = t_file.openInputStream();
					try{
						
						byte[] t_buffer = new byte[(int)t_file.fileSize()];
						sendReceive.ForceReadByte(t_fileIn, t_buffer, t_buffer.length);
						
						EncodedImage t_origImage = EncodedImage.createEncodedImage(t_buffer, 0, t_buffer.length);
						
						int t_origWidth = t_origImage.getWidth();
						int t_origHeight = t_origImage.getHeight();
						
						XYPoint t_scaleSize = _point;
						
						try{
							if(t_scaleSize != null && t_origWidth > t_scaleSize.x && t_origHeight > t_scaleSize.y){
								
								int scaleX = Fixed32.div(Fixed32.toFP(t_origWidth), Fixed32.toFP(t_scaleSize.x));
								int scaleY = Fixed32.div(Fixed32.toFP(t_origHeight), Fixed32.toFP(t_scaleSize.y));
																	
								JPEGEncodedImage finalJPEG = JPEGEncodedImage.encode(t_origImage.scaleImage32(scaleX, scaleY).getBitmap(), 80);
								
								t_content = finalJPEG.getData();
								
							}else{
								t_content = t_buffer;
							}
																
						}finally{
							t_origImage = null;
							t_buffer = null;
						}
														
					}finally{
						t_fileIn.close();
						t_fileIn = null;
					}
					
					
				}finally{
					t_file.close();
					t_file = null;
				}
			}
			
			return t_content;			
		}
		
		return null;
	}
	
	
	public abstract boolean canAdded();
	
	public abstract void onAddUploadingPic(String _file,int _type);
	
}
