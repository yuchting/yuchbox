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

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.UiApplication;

import com.yuchting.yuchberry.client.msg_head;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;
import com.yuchting.yuchberry.client.weibo.fetchWeibo;

public final class WeiboHeadImage {

	public String 	m_userID;
	public byte	m_weiboStyle;
	
	public Bitmap	m_headImage;
	public int		m_dataHash;
	
	public boolean m_isWeiboOrIM = true;
	
	public final static boolean	fsm_largeHeadImage			= recvMain.fsm_display_width > 320;	
	public final static int		fsm_headImageWidth 			= recvMain.fsm_display_width>320?fetchWeibo.fsm_headImageSize_l:fetchWeibo.fsm_headImageSize;

	public static ImageUnit	sm_headImageMask = null;
	public static Bitmap 		sm_defaultHeadImage = null;
	public static int			sm_defaultHeadImageHashCode = 0;
	public static recvMain 	sm_mainApp = null;
		
	private static Vector		sm_loadImageQueue = new Vector();
	private static Thread		sm_loadImageThread = null;
	
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
		
		if(!recvMain.isSDCardSupport() || !sm_mainApp.isSDCardAvailable(false)){
			return ;
		}
		
		int sign = _isWeiboOrIM?msg_head.msgWeiboHeadImage:msg_head.msgChatHeadImage;
		
		ByteArrayOutputStream t_os = new ByteArrayOutputStream();
		t_os.write(sign);
		t_os.write(_style);		
		
		if(_isWeiboOrIM){
			if(_style == fetchWeibo.QQ_WEIBO_STYLE){
				sendReceive.WriteString(t_os,_imageID);
			}else{
				sendReceive.WriteLong(t_os,Long.parseLong(_imageID));
			}
		}else{
			sendReceive.WriteString(t_os,_imageID);
		}
		
		// whether large image 
		sendReceive.WriteBoolean(t_os,fsm_headImageWidth == fetchWeibo.fsm_headImageSize_l);
		
		sm_mainApp.m_connectDeamon.addSendingData(sign, t_os.toByteArray(),true);
	
	}
	
	private static Bitmap getDefaultHeadImage()throws Exception{
		
		if(sm_defaultHeadImage == null){
			
			if(sm_mainApp == null && UiApplication.getUiApplication() instanceof recvMain){
				sm_mainApp = (recvMain)UiApplication.getUiApplication();
			}

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
					
					if((t_image.m_dataHash != _hashCode && _hashCode != 0 && t_image.m_dataHash != 0)
						|| t_image.m_headImage == getDefaultHeadImage()){
						
						SendHeadImageQueryMsg(_imageID,_style,_isWeiboOrIM);
					}
					
					return t_image;
				}
			}
			
			WeiboHeadImage t_image = new WeiboHeadImage();
			
			t_image.m_isWeiboOrIM 	= _isWeiboOrIM;
			t_image.m_userID 		= _imageID;
			t_image.m_headImage 	= getDefaultHeadImage();
			t_image.m_dataHash 		= _hashCode;
			t_image.m_weiboStyle 	= _style;
	
			_imageList.addElement(t_image);		
			
			if(recvMain.isSDCardSupport()){
				
				if(sm_loadImageThread == null){
					
					sm_loadImageThread = new Thread(){
						
						public void run(){
							
							while(true){
								
								try{
									synchronized (sm_loadImageQueue) {
										while(sm_loadImageQueue.isEmpty()){
											try{
												sm_loadImageQueue.wait();
											}catch(Exception e){}
										}
									}
									
									
									WeiboHeadImage t_image = (WeiboHeadImage)sm_loadImageQueue.elementAt(0);
									
									if(!LoadWeiboImage(t_image)){
										SendHeadImageQueryMsg(t_image.m_userID,t_image.m_weiboStyle,t_image.m_isWeiboOrIM);
									}
									
									sm_loadImageQueue.removeElementAt(0);
									
								}catch (Exception e){}
							}
						}
					};
					
					sm_loadImageThread.start();
				}
				
				
				synchronized (sm_loadImageQueue) {
					sm_loadImageQueue.addElement(t_image);
					
					
					try{
						sm_loadImageQueue.notify();
					}catch(Exception e){
						System.out.println("Error notify");
					}
				}			
			}
						
			return t_image;
		}		
	}
	
	
	private static boolean LoadWeiboImage(WeiboHeadImage _image){
		
		try{

			String t_imageFilename = null;

			if(fsm_largeHeadImage){
				if(_image.m_isWeiboOrIM){
					t_imageFilename = sm_mainApp.GetWeiboHeadImageDir(_image.m_weiboStyle) + _image.m_userID + "_l.png";
				}else{
					t_imageFilename = sm_mainApp.GetIMHeadImageDir(_image.m_weiboStyle) + _image.m_userID + "_l.png";
				}
				
			}else{
				if(_image.m_isWeiboOrIM){
					t_imageFilename = sm_mainApp.GetWeiboHeadImageDir(_image.m_weiboStyle) + _image.m_userID + ".png";
				}else{
					t_imageFilename = sm_mainApp.GetIMHeadImageDir(_image.m_weiboStyle) + _image.m_userID + ".png";
				}
			}

			FileConnection t_fc = (FileConnection)Connector.open(t_imageFilename,Connector.READ_WRITE);
			try{
				if(t_fc.exists()){
					InputStream t_fileIn = t_fc.openInputStream();
					try{
						
						byte[] t_data = new byte[(int)t_fc.fileSize()];
						sendReceive.ForceReadByte(t_fileIn, t_data, t_data.length);
															
						_image.m_headImage		= EncodedImage.createEncodedImage(t_data, 0, t_data.length).getBitmap();
						_image.m_dataHash 		= t_data.length;
						
					}finally{
						t_fileIn.close();
						t_fileIn = null;
					}
					
					return true;
				}
				
			}finally{
				t_fc.close();
				t_fc = null;
			}	
		}catch(Exception e){
			sm_mainApp.SetErrorString("LWI:"+ e.getMessage() + e.getClass().getName());
		}
		
		return false;
	}
		
	static public int displayHeadImage(Graphics _g,int _x,int _y,WeiboHeadImage _image){
		
		if(sm_headImageMask == null){
			if(fsm_largeHeadImage){
				sm_headImageMask = recvMain.sm_weiboUIImage.getImageUnit("headImageMask_l");
			}else{
				sm_headImageMask = recvMain.sm_weiboUIImage.getImageUnit("headImageMask");
			}
		}
		
		if(_image != null){
			_g.drawBitmap(_x,_y,fsm_headImageWidth,fsm_headImageWidth,_image.m_headImage,0,0);
		}else{
			try{
				_g.drawBitmap(_x,_y,fsm_headImageWidth,fsm_headImageWidth,getDefaultHeadImage(),0,0);
			}catch(Exception e){}			
		}		
		
		recvMain.sm_weiboUIImage.drawImage(_g,sm_headImageMask, _x, _y);
		
		return _x + sm_headImageMask.getWidth();
	}
	
	private static ImageUnit sm_selectedImage_weibo = null;
	private static ImageUnit sm_selectedImage_im = null;
	
	/**
	 * draw weibo or im selected background 
	 * @param _g
	 * @param _limitWidth
	 * @param _limitHeight
	 * @param _weiboOrIM  weibo or im flag
	 */
	static public void drawSelectedImage(Graphics _g,int _limitWidth,int _limitHeight,boolean _weiboOrIM){
		
		ImageUnit selImg = null;
		ImageSets imgSet = recvMain.sm_weiboUIImage;
		if(_weiboOrIM){
			
			if(sm_selectedImage_weibo == null){
				sm_selectedImage_weibo = recvMain.sm_weiboUIImage.getImageUnit("weibo_sel");
			}
			
			selImg = sm_selectedImage_weibo;
			
		}else{
						
			if(sm_selectedImage_im == null){
				sm_selectedImage_im = recvMain.sm_extUIImage.getImageUnit("im_sel");
			}
			
			selImg = sm_selectedImage_im;
			imgSet = recvMain.sm_extUIImage;
		}
			
		int t_draw_y = 0;
		int t_y = selImg.getHeight() - _limitHeight;
		
		if(t_y < 0){
			t_y = 0;
			t_draw_y = -t_y;
		}
		
		// draw selected backgroud
		//
		imgSet.drawImage(_g,selImg,
						0, t_draw_y, _limitWidth, _limitHeight,0, t_y);
	}
}
