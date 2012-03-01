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

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import local.yblocalResource;
import net.rim.device.api.system.Application;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Keypad;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.connectDeamon;
import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.ImageSets;
import com.yuchting.yuchberry.client.ui.ImageUnit;

public class uploadFileScreen extends MainScreen{
		
	FileFieldManager		m_fileList 		= new FileFieldManager();
	
	int m_menuOrder			= 0;
	
	MenuItem	m_ok		= new MenuItem("",m_menuOrder++,0){
		public void run() {
			if(m_currFocusIconItem != null){
				if(m_currFocusIconItem.m_isFolder){
					DisplayFileList(m_currFocusIconItem.m_filename_full);
					
				}else{
					
					// add a attachment file
					//
					if(m_delScreen){
						m_clickCallback.clickDel(m_currFocusIconItem.m_filename_full);
						
						m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.DEL_ATTACHMENT_SUCC));
					}else{
						
						if(!m_clickCallback.clickOK(m_currFocusIconItem.m_filename_full,m_currFocusIconItem.m_fileSize)){
							return ;
						}
						
						m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.ADD_ATTACHMENT_SUCC));
					}				
					close();
				}
			}
		}		
	};
	
	MenuItem	m_check		= new MenuItem(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_CHECK),m_menuOrder++,0){
		public void run() {
			if(m_currFocusIconItem != null){
				m_mainApp.PushViewFileScreen(m_currFocusIconItem.m_filename_full);
			}
		}
	};
	
	MenuItem	m_topMenu	= new MenuItem(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_TOP_MENU),m_menuOrder++,0){
		public void run() {
			if(m_fileList.getFieldCount() > 0){
				m_fileList.setVerticalScroll(0);
				m_fileList.getField(0).setFocus();
			}
		}
	};
	
	MenuItem	m_bottomMenu	= new MenuItem(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_BOTTOM_MENU),m_menuOrder++,0){
		public void run() {
			if(m_fileList.getFieldCount() > 0){
				m_fileList.setVerticalScroll(m_fileList.getVirtualHeight());
				m_fileList.getField(m_fileList.getFieldCount() - 1).setFocus();
			}
		}
	};
	
	
	
	public final static int fsm_bitmap_width	= 32;
	public final static int fsm_bitmap_height = 32;
	
	ImageUnit				m_textFileBitmap= null;
	ImageUnit				m_audioFileBitmap = null;
	ImageUnit				m_binFileBitmap	= null;
	ImageUnit				m_folderBitmap	= null;
	ImageUnit				m_pictureBitmap	= null;
	ImageUnit				m_movieBitmap	= null;
		
	connectDeamon		m_deamon	= null; 
	
	boolean			m_addAttachment = false;
	
	recvMain			m_mainApp		= null;
		
	public final static String fsm_rootPath_back		= "file:///store/home/user/";
	public final static String fsm_rootPath_default	= "file:///SDCard/";
	
	String				m_rootPath = null;
	
	String				m_currDisplayPath = null;
	
	boolean			m_delScreen		=false;
	
	IUploadFileScreenCallback	m_clickCallback = null;
	
	ImageSets			m_imageSets = null;
	
	// current selection file icon
	FileIcon			m_currFocusIconItem = null;
	
	// delay load runnable id
	LoadFileThread		m_delayLoadRunnable	= null;
			

	public uploadFileScreen(connectDeamon _deamon,recvMain _app,
							boolean _del,IUploadFileScreenCallback _callback)throws Exception {
		
		super(Manager.NO_VERTICAL_SCROLL);
		
		if(_callback == null){
			throw new IllegalArgumentException("uploadFileScreen _callback == null");
		}
		
		m_deamon = _deamon;
		m_mainApp = _app;
		m_delScreen = _del;
		m_clickCallback = _callback;
		
		m_imageSets			= _app.m_allImageSets;
		
		m_textFileBitmap 	= _app.m_allImageSets.getImageUnit("text_resize");
		m_audioFileBitmap 	= _app.m_allImageSets.getImageUnit("audio_resize");
		m_binFileBitmap 	= _app.m_allImageSets.getImageUnit("unknown_resize");
		m_folderBitmap 		= _app.m_allImageSets.getImageUnit("folder_resize");
		m_pictureBitmap		= _app.m_allImageSets.getImageUnit("picture_resize");
		m_movieBitmap		= _app.m_allImageSets.getImageUnit("movie_resize");
		
		
		add(m_fileList);
		m_fileList.setFocus();
		
		if(_del){
			m_ok.setText(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_DEL));
		}else{
			m_ok.setText(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_ADD));
		}
		
		try{
			FileConnection fc = (FileConnection) Connector.open(fsm_rootPath_default,Connector.READ_WRITE);
			m_rootPath = fsm_rootPath_default;			
			fc.close();
		}catch(Exception _e){
			m_rootPath = fsm_rootPath_back;
		}
		
		DisplayFileList(m_mainApp.m_uploadFileFavorPath == null?m_rootPath:m_mainApp.m_uploadFileFavorPath);
	}
		
	public void DisplayFileList(final String _path){
		
		if(m_delayLoadRunnable != null){
			synchronized (this){
				m_delayLoadRunnable.closeLoad();
				m_delayLoadRunnable = null;
			}
		}		
				
		// clear all former file list
		//
		m_fileList.deleteAll();
		m_currFocusIconItem = null;
		
		if(m_delScreen){
			try{	
				
				setTitle("Del your uploading attachment");
				
				int t_index = 0;
				Vector t_files = m_deamon.GetAttachmentFile();
				for(int i = 0;i < t_files.size();i++){
					
					connectDeamon.ComposingAttachment t_att = (connectDeamon.ComposingAttachment)t_files.elementAt(i);
					String t_name = null;
					
					final int t_lastSplash = t_att.m_filename.lastIndexOf('/');
					if(t_lastSplash == -1){
						t_name = "xxxxx";
					}else{
						t_name = t_att.m_filename.substring(t_lastSplash + 1,t_att.m_filename.length());
					}				
					
					ImageUnit bitmap;
					
					if(IsAudioFile(t_name)){
						bitmap = m_audioFileBitmap;
					}else if(IsTxtFile(t_name)){
						bitmap = m_textFileBitmap;
					}else if(IsImageFile(t_name)){
						bitmap = m_pictureBitmap;
					}else if(IsMovieFile(t_name)){
						bitmap = m_movieBitmap;
					}else{
						bitmap = m_binFileBitmap;
					}
										
					m_fileList.add(new FileIcon(t_name,t_att.m_filename,bitmap,t_att.m_fileSize,false,0));
					
					t_index++;
				}
				
			}catch(Exception _e){
				m_mainApp.SetErrorString("DFL0", _e);
			}

		}else{

			m_mainApp.m_uploadFileFavorPath = _path;
			m_currDisplayPath = _path;
			
			setTitle(_path);

			try{
				
				synchronized (this) {
					m_delayLoadRunnable = new LoadFileThread(_path);
					m_delayLoadRunnable.start();
				}
				
			}catch(Exception _e){
				m_mainApp.SetErrorString("DFL1", _e);
			}
		}
	}
		 
	

	
	public void openAddAttachment(){
		m_addAttachment = true;
	}
	
	public void openDelAttachment(){
		m_addAttachment = false;
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(m_ok);
		menu.add(m_check);
	    
		menu.add(m_topMenu);
		menu.add(m_bottomMenu);
	    
	    super.makeMenu(menu,instance);
	}
		
	public boolean onClose(){
		if(m_delScreen){
			close();
			return true;
		}
		
		if(m_currDisplayPath.equals(m_rootPath)){
			
			close();
			return true;
			
		}else{
			final int t_slash_rear = m_currDisplayPath.lastIndexOf('/', m_currDisplayPath.length() - 2);
			m_currDisplayPath = m_currDisplayPath.substring(0, t_slash_rear + 1);
			
			DisplayFileList(m_currDisplayPath);
			
			return false;
		}
	}
	
	public void close(){
		super.close();
				
		if(m_delayLoadRunnable != null){
			synchronized (this){
				m_delayLoadRunnable.closeLoad();
				m_delayLoadRunnable = null;
			}
		}	
	}
	
	public boolean keyDown(int keycode,int time){
		final int key = Keypad.key(keycode);
		
		switch(key){
		case 'T':
			m_topMenu.run();
			return true;
		case 'B':
			m_bottomMenu.run();
			return true;
		case 10:
			return trackwheelClick(0,0);
		}
		
		return super.keyDown(keycode,time);
	}
	
	public boolean trackwheelClick(int status, int time){

		if(m_currFocusIconItem != null){
			if(m_currFocusIconItem.m_isFolder){
				DisplayFileList(m_currFocusIconItem.m_filename_full);
				return true;
			}else{
				m_check.run();
			}
		}
		
		return false;
	}
	
	public static boolean IsAudioFile(String _filename){
		String t_lower = _filename.toLowerCase();
		t_lower = t_lower.substring(Math.max(0, t_lower.length() - 4));
				
		return t_lower.equals(".mp3") 
				|| t_lower.equals(".wav")
				|| t_lower.equals(".ogg")
				|| t_lower.equals(".mid");
	}
	
	public static boolean IsTxtFile(String _filename){
		
		String t_lower = _filename.toLowerCase();
		t_lower = t_lower.substring(Math.max(0, t_lower.length() - 4));
				
		return t_lower.equals(".txt")
				|| t_lower.equals(".log")
				|| t_lower.equals(".dic");
	}
	
	public static boolean IsImageFile(String _filename){
		
		String t_lower = _filename.toLowerCase();
		t_lower = t_lower.substring(Math.max(0, t_lower.length() - 4));
				
		return t_lower.equals(".jpg")
				|| t_lower.equals(".png")
				|| t_lower.equals(".bmp")
				|| t_lower.equals(".gif");
	}
	
	public static boolean IsMovieFile(String _filename){

		String t_lower = _filename.toLowerCase();
		t_lower = t_lower.substring(Math.max(0, t_lower.length() - 4));
				
		return t_lower.equals(".3gp")
				|| t_lower.equals(".mp4")
				|| t_lower.equals(".avi");
	}
	
	/**
	 * file list manager to display file list
	 * @author yuch
	 *
	 */
	private class FileFieldManager extends VerticalFieldManager{
		
		public FileFieldManager(){
			super(VerticalFieldManager.VERTICAL_SCROLL | Field.FOCUSABLE);
		}
		
		public void sublayout(int width, int height){

			int t_num = getFieldCount();
			int t_y = 0;
			for(int i = 0;i < t_num;i++){
				Field f = getField(i);
				
				setPositionChild(f,0,t_y);
				layoutChild(f,f.getPreferredWidth(),f.getPreferredHeight());
				
				t_y += f.getPreferredHeight();
			}
			
			setExtent(getPreferredWidth(), getPreferredHeight());
		}
		
		public int getPreferredWidth(){			
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			
			int t_titleHeight;
			Manager mgr = uploadFileScreen.this.getMainManager().getManager();
			if(mgr != null && mgr.getFieldCount() > 0){
				t_titleHeight = mgr.getField(0).getHeight();
			}else{
				t_titleHeight = Font.getDefault().getHeight(); 
			}
			
			return recvMain.fsm_display_height - t_titleHeight;			
		}
	}
	
	class LoadFileThread extends Thread{
		
		final String m_path;
		
		boolean m_closed = false;
				
		public LoadFileThread(String _path)throws Exception {
			m_path = _path;
		}
		
		public void closeLoad(){
			m_closed = true;
		}
		
		public void run() {
			
			// load times counter
			int t_loadTimer = 0;
			Enumeration t_delayLoadFileEnum;
			
			try{
				
				FileConnection fc = (FileConnection)Connector.open(m_path,Connector.READ);
				try{
					t_delayLoadFileEnum = fc.list("*",true);
				}finally{
					fc.close();
				}
				
				while(!m_closed && t_delayLoadFileEnum.hasMoreElements()) {
					
					if(t_loadTimer > 2){
						
						t_loadTimer = 0;
						
						synchronized (UiApplication.getEventLock()) {
							m_fileList.sublayout(0, 0);
						}
						m_fileList.invalidate();
										
						try{
							Thread.sleep(100);
						}catch(Exception e){}
						
						continue;
					}
					
					t_loadTimer++;
					
					String t_name = t_delayLoadFileEnum.nextElement().toString();
					String t_fullname = m_path + t_name;
					
					FileConnection next = (FileConnection) Connector.open(t_fullname,Connector.READ);
					try{
						ImageUnit bitmap = null;
						
						if(next.isDirectory()){
							bitmap = m_folderBitmap;
						}else if(IsAudioFile(t_name)){
							bitmap = m_audioFileBitmap;
						}else if(IsTxtFile(t_name)){
							bitmap = m_textFileBitmap;
						}else if(IsImageFile(t_name)){
							bitmap = m_pictureBitmap;
						}else if(IsMovieFile(t_name)){
							bitmap = m_movieBitmap;
						}else{
							bitmap = m_binFileBitmap;
						}
						
						long t_time = next.lastModified();
						
						FileIcon t_icon ;
						if(next.isDirectory()){
							t_icon = new FileIcon(t_name,t_fullname,bitmap,0,true,t_time);
						}else{
							t_icon = new FileIcon(t_name,t_fullname,bitmap,(int)next.fileSize(),false,t_time);
						}									
							
						boolean t_add = false;
						int t_num = m_fileList.getFieldCount();
						
						for(int i = 0;i < t_num;i++){
							FileIcon icon = (FileIcon)m_fileList.getField(i);
							
							if(icon.m_lastModified <= t_icon.m_lastModified){	
								t_add = true;
								synchronized (UiApplication.getEventLock()) {
									m_fileList.insert(t_icon,i);
								}											
								break;
							}
						}
						
						if(!t_add){
							synchronized (UiApplication.getEventLock()) {
								m_fileList.add(t_icon);
							}
						}
						
					}finally{
						next.close();
					}	
				}
				
				
			}catch(Exception _e){
				m_mainApp.SetErrorString("DFL1R", _e);
			}
		}
	}

	/**
	 * file icon static data
	 * @author yuch
	 *
	 */
	private class FileIcon extends Field{
		
		int				m_fileSize;
		String			m_filename 	= null;
		String			m_filename_full 	= null;
		ImageUnit		m_bitmap		= null;
		boolean		m_isFolder	= false;
		long			m_lastModified = 0;
		
		public FileIcon(String _name,String _name_full,ImageUnit _image,
				int _fileSize,boolean _isFolder,long _lastModified){
			
			super(Field.FOCUSABLE);
						
			m_fileSize	= _fileSize;
			m_filename	= _name;
			m_filename_full = _name_full;
			m_bitmap		= _image;
			m_isFolder	= _isFolder;
			m_lastModified = _lastModified;
		}
		
		public String toString(){
			return m_filename;
		}
		
		protected void drawFocus(Graphics graphics,boolean _on){
			if(_on){
				m_currFocusIconItem = this;
				
				int t_color = graphics.getColor();
				try{
					graphics.setColor(0x005de7);
					graphics.fillRect(0, 0, getPreferredWidth(), getPreferredHeight());
				}finally{
					graphics.setColor(t_color);
				}
			}		
			
			m_imageSets.drawImage(graphics, m_bitmap, 0, 0);
			
			if(m_isFolder){
				graphics.drawText(m_filename, uploadFileScreen.fsm_bitmap_width, 0);
			}else{
				if(m_fileSize > 1024 * 1024){					 
					graphics.drawText("(" + (m_fileSize / 1024 / 1024) + "MB) " + m_filename, uploadFileScreen.fsm_bitmap_width, 0);
				}else if(m_fileSize > 1024){
					graphics.drawText("(" + (m_fileSize / 1024 ) + "KB) " + m_filename, uploadFileScreen.fsm_bitmap_width, 0);
				}else{
					graphics.drawText("(" + (m_fileSize) + "B) " + m_filename, uploadFileScreen.fsm_bitmap_width, 0);
				}
			}
		}
		
		protected void onUnfocus(){
			super.onUnfocus();
			invalidate();
		}
		
		public int getPreferredWidth(){
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			return m_bitmap.getHeight();
		}
		
		protected void layout(int width, int height){
			setExtent(getPreferredWidth(), getPreferredHeight());
		}

		protected void paint(Graphics graphics) {
			drawFocus(graphics, isFocus());			
		}

	}	
}
