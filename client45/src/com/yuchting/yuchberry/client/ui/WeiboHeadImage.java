package com.yuchting.yuchberry.client.ui;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Graphics;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;

public final class WeiboHeadImage {

	public String 	m_userID;
	public byte	m_weiboStyle;
	
	public Bitmap	m_headImage;
	public int		m_dataHash;
	
	public final static boolean	fsm_largeHeadImage			= recvMain.fsm_display_width > 320;	
	public final static int		fsm_headImageWidth 			= recvMain.fsm_display_width>320?fetchWeibo.fsm_headImageSize_l:fetchWeibo.fsm_headImageSize;

	
	public static ImageUnit	sm_headImageMask = null;
	public static Bitmap 		sm_defaultHeadImage = null;
	public static int			sm_defaultHeadImageHashCode = 0;
	public static recvMain 	sm_mainApp;
		
	public static void AddWeiboHeadImage(Vector _imageList,int _style,String _id,byte[] _dataArray){
		
		synchronized (_imageList) {

			for(int i = 0 ;i < _imageList.size();i++){
				WeiboHeadImage t_image = (WeiboHeadImage)_imageList.elementAt(i);
				
				if(t_image.m_userID.equals(_id) && _style == t_image.m_weiboStyle){
					
					try{
						t_image.m_headImage = EncodedImage.createEncodedImage(_dataArray, 0, _dataArray.length).getBitmap();
						t_image.m_dataHash 	= _dataArray.length;
						
						sm_mainApp.SetErrorString("recv " + _style + " head image " + _id + " dataHash " + t_image.m_dataHash);					
								
					}catch(Exception ex){
						sm_mainApp.SetErrorString("AWHI:"+ _id + " " + ex.getMessage() + ex.getClass().getName() );
					}
					
					break;				
				}
			}
		}
	}
	
	public static void DelWeiboHeadImage(Vector _imageList,int _style,String _id){
		
		synchronized (_imageList) {

			for(int i = 0 ;i < _imageList.size();i++){
				WeiboHeadImage t_image = (WeiboHeadImage)_imageList.elementAt(i);
				
				if(t_image.m_userID.equals(_id) && _style == t_image.m_weiboStyle){
					_imageList.removeElementAt(i);
					
					break;				
				}
			}
		}
	}
	
	private static void SendHeadImageQueryMsg(String _imageID,int _style,boolean _isWeiboOrIM)throws Exception{
		
		if(sm_mainApp.m_dontDownloadWeiboHeadImage){
			return ;
		}
		
		if(!sm_mainApp.isSDCardAvaible()){
			return ;
		}
		
		int sign = _isWeiboOrIM?msg_head.msgWeiboHeadImage:msg_head.msgChatHeadImage;
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(sign);
		t_os.write(_style);		
		
		sendReceive.WriteString(t_os,_imageID);
		
		// whether large image 
		sendReceive.WriteBoolean(t_os,fsm_headImageWidth == fetchWeibo.fsm_headImageSize_l);
		
		sm_mainApp.m_connectDeamon.addSendingData(sign, t_os.toByteArray(),true);
	
	}
	
	private static Bitmap getDefaultHeadImage()throws Exception{
		
		if(sm_defaultHeadImage == null){
			byte[] bytes = IOUtilities.streamToBytes(sm_mainApp.getClass()
					.getResourceAsStream(fsm_largeHeadImage?"/defaultHeadImage_l.png":"/defaultHeadImage.png"));		
			sm_defaultHeadImage =  EncodedImage.createEncodedImage(bytes, 0, bytes.length).getBitmap();
			sm_defaultHeadImageHashCode = bytes.length;
		}
		
		return sm_defaultHeadImage;
	}
	
	
	public static WeiboHeadImage SearchHeadImage(Vector _imageList,String _imageID,
													byte _style,int _hashCode,
													boolean _isWeiboOrIM)throws Exception{
		
		synchronized (_imageList) {
			
			for(int i = 0 ;i < _imageList.size();i++){
				WeiboHeadImage t_image = (WeiboHeadImage)_imageList.elementAt(i);
								
				if(_style == t_image.m_weiboStyle 
					&& t_image.m_userID.equals(_imageID) ){
					
					if(t_image.m_dataHash != _hashCode 
						|| t_image.m_headImage == getDefaultHeadImage()){
						
						SendHeadImageQueryMsg(_imageID,_style,_isWeiboOrIM);
					}
					
					return t_image;
				}
			}
			
			// find/load from the local FileStore
			//
			WeiboHeadImage t_image = LoadWeiboImage(_imageID,_style,_isWeiboOrIM);
			if(t_image != null){
				if(t_image.m_dataHash != _hashCode){					
					SendHeadImageQueryMsg(_imageID,_style,_isWeiboOrIM);
				}
				
				_imageList.addElement(t_image);
				return t_image;
			}
			
			// load the default image and send head image query message
			//
			SendHeadImageQueryMsg(_imageID,_style,_isWeiboOrIM);			
			
			t_image = new WeiboHeadImage();
			
			t_image.m_userID = _imageID;
			t_image.m_headImage = getDefaultHeadImage();
			t_image.m_dataHash = _hashCode;
			t_image.m_weiboStyle = _style;
			
			_imageList.addElement(t_image);
			
			return t_image;
		}		
	}
	
	public static WeiboHeadImage LoadWeiboImage(String _imageID,byte _style,boolean _isWeiboOrIM)throws Exception{
		
		WeiboHeadImage t_image = new WeiboHeadImage();
		
		t_image.m_userID 		= _imageID;
		t_image.m_weiboStyle 	= _style;
		t_image.m_headImage 	= getDefaultHeadImage();
		t_image.m_dataHash 		= sm_defaultHeadImageHashCode;
		
		try{

			String t_imageFilename = null;

			if(fsm_largeHeadImage){
				if(_isWeiboOrIM){
					t_imageFilename = sm_mainApp.GetWeiboHeadImageDir(_style) + _imageID + "_l.png";
				}else{
					t_imageFilename = sm_mainApp.GetIMHeadImageDir(_style) + _imageID + "_l.png";
				}
				
			}else{
				if(_isWeiboOrIM){
					t_imageFilename = sm_mainApp.GetWeiboHeadImageDir(_style) + _imageID + ".png";
				}else{
					t_imageFilename = sm_mainApp.GetIMHeadImageDir(_style) + _imageID + ".png";
				}
			
			}
			
			FileConnection t_fc = (FileConnection)Connector.open(t_imageFilename,Connector.READ_WRITE);
			try{
				if(t_fc.exists()){
					
					InputStream t_fileIn = t_fc.openInputStream();
					try{
																	
						byte[] t_data = new byte[(int)t_fc.fileSize()];
						
						sendReceive.ForceReadByte(t_fileIn, t_data, t_data.length);
						
						t_image.m_headImage =  EncodedImage.createEncodedImage(t_data, 0, t_data.length).getBitmap();
						t_image.m_dataHash = t_data.length;
						
					}finally{
						
						t_fileIn.close();
						t_fileIn = null;
					}
				}else{
					sm_mainApp.SetErrorString("LWI:" + t_imageFilename);
				}
				
			}finally{
				t_fc.close();
				t_fc = null;
			}	
		}catch(Exception e){
			sm_mainApp.SetErrorString("LWI:"+ e.getMessage() + e.getClass().getName());
		}
		
		return t_image;
	}
		
	static public int displayHeadImage(Graphics _g,int _x,int _y,WeiboHeadImage _image){
		
		if(sm_headImageMask == null){
			if(fsm_largeHeadImage){
				sm_headImageMask = recvMain.sm_weiboUIImage.getImageUnit("headImageMask_l");
			}else{
				sm_headImageMask = recvMain.sm_weiboUIImage.getImageUnit("headImageMask");
			}
		}
		
		_g.drawBitmap(_x,_y,_image.m_headImage.getWidth(),_image.m_headImage.getHeight(),_image.m_headImage,0,0);
		
		recvMain.sm_weiboUIImage.drawImage(_g,sm_headImageMask, _x, _y);
		
		return _x + sm_headImageMask.getWidth();
	}
	
	private static ImageUnit sm_selectedImage = null;
	static public void drawSelectedImage(Graphics _g,int _limitWidth,int _limitHeight){
		
		if(sm_selectedImage == null){
			sm_selectedImage = recvMain.sm_weiboUIImage.getImageUnit("weibo_sel");
		}
		
		int t_draw_y = 0;
		int t_y = sm_selectedImage.getHeight() - _limitHeight;
		
		if(t_y < 0){
			t_y = 0;
			t_draw_y = -t_y;
		}
		
		// draw selected backgroud
		//
		recvMain.sm_weiboUIImage.drawImage(_g,sm_selectedImage,
						0, t_draw_y, _limitWidth, _limitHeight,0, t_y);
	}
}
