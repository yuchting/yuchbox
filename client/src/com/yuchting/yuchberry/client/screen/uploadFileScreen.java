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
import javax.microedition.lcdui.Image;

import local.yblocalResource;
import net.rim.device.api.math.Fixed32;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
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
import com.yuchting.yuchberry.client.sendReceive;
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
	
	MenuItem	m_systemROMMenu	= new MenuItem(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_SYS_ROM_MENU),m_menuOrder++,0){
		public void run() {
			browsePath(fsm_rootPath_back);
		}
	};
	
	MenuItem	m_sdCardRomMenu	= new MenuItem(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_SDCARD_ROM_MENU),m_menuOrder++,0){
		public void run() {
			browsePath(fsm_rootPath_default);
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
			
	boolean			m_addAttachment = false;
	
	recvMain			m_mainApp		= null;
		
	public final static String fsm_rootPath_back		= "file:///store/home/user/";
	public final static String fsm_rootPath_default	= "file:///SDCard/";
	
	String				m_currDisplayPath = null;
	
	boolean			m_delScreen		=false;
	
	IUploadFileScreenCallback	m_clickCallback = null;
	
	ImageSets			m_imageSets = null;
	
	// current selection file icon
	FileIcon			m_currFocusIconItem = null;
	
	// delay load runnable id
	LoadFileThread		m_delayLoadRunnable	= null;
			

	public uploadFileScreen(recvMain _app,boolean _del,IUploadFileScreenCallback _callback)throws Exception {
		super(Manager.VERTICAL_SCROLL);
		
		String t_initPath = fsm_rootPath_default;
		
		if(!_app.isSDCardAvailable(false)){
			
			t_initPath = fsm_rootPath_back;
			try{
				FileConnection fc = (FileConnection) Connector.open(t_initPath,Connector.READ_WRITE);
				fc.close();
			}catch(Exception e){
				// two both path are NOT avaiable 
				//
				_app.DialogAlert(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_CANT_SELECT_PROMPT));
				return;
			}
		}
		
		
		if(_callback == null){
			throw new IllegalArgumentException("uploadFileScreen _callback == null");
		}
		
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
				
		// there is a upload file favor path (former path)
		// but current path is NOT available (maybe plugin USB)
		//
		if(m_mainApp.m_uploadFileFavorPath != null){
			try{
				FileConnection fc = (FileConnection) Connector.open(m_mainApp.m_uploadFileFavorPath,Connector.READ_WRITE);
				fc.close();
				
				t_initPath = m_mainApp.m_uploadFileFavorPath;
			}catch(Exception e){}
		}		
		
		DisplayFileList(t_initPath);
	}
	
	/**
	 * browse path called by menu item
	 * @param _path
	 */
	private void browsePath(String _path){
		try{
			FileConnection fc = (FileConnection) Connector.open(_path,Connector.READ_WRITE);
			fc.close();
		}catch(Exception e){
			m_mainApp.DialogAlert(recvMain.sm_local.getString(yblocalResource.UPLOAD_FILE_CANT_SELECT_PROMPT));
			return;
		}
		
		m_mainApp.m_uploadFileFavorPath = _path;
		DisplayFileList(_path);
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
				Vector t_files = m_mainApp.m_connectDeamon.GetAttachmentFile();
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
	
	protected void makeMenu(Menu menu, int instance) {
		menu.add(m_ok);
		menu.add(m_check);
	    
		menu.add(m_topMenu);
		menu.add(m_bottomMenu);
		
		menu.add(m_systemROMMenu);
		menu.add(m_sdCardRomMenu);
	    
	    super.makeMenu(menu,instance);
	}
		
	public boolean onClose(){
		if(m_delScreen){
			close();
			return true;
		}
		
		if(m_currDisplayPath.equals(fsm_rootPath_back)
			|| m_currDisplayPath.equals(fsm_rootPath_default)){
			
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
			return trackwheelClick(-1,-1);
		}
		
		return super.keyDown(keycode,time);
	}
	
	public boolean trackwheelClick(int status, int time){

		if(m_currFocusIconItem != null){
			if(m_currFocusIconItem.m_isFolder){
				DisplayFileList(m_currFocusIconItem.m_filename_full);
				return true;
			}else{
				if(status == -1 && time == -1){ // called in keyDown function
					m_check.run();
					return true;
				}				
			}
		}
		
		return super.trackwheelClick(status, time);
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
			super(VerticalFieldManager.VERTICAL_SCROLL | Manager.VERTICAL_SCROLLBAR | Field.FOCUSABLE );
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
			
			setExtent(getPreferredWidth(), t_y);
		}
		
		public int getPreferredWidth(){			
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			
			int t_num = getFieldCount();
			int t_y = 0;
			for(int i = 0;i < t_num;i++){
				Field f = getField(i);			
				t_y += f.getPreferredHeight();
			}
			
			return t_y;
		}
	}
	
	static class SortFile{
		String m_name;
		String m_full_name;
		long m_modifiedTime;
		boolean m_isFolder;
		int m_size;
	}
	
	private class LoadFileThread extends Thread{
		
		final String m_path;
		
		boolean m_closed = false;
				
		public LoadFileThread(String _path)throws Exception {
			m_path = _path;
			
			m_loadImageThread.start();
		}
		
		public void closeLoad(){
			m_closed = true;
			
			synchronized (m_loadImageThread) {
				m_loadImageThread.notify();
			}
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

				Vector t_fileList 		= new Vector();
								
				SortFile t_sortFile;
				SortFile t_cmpSortFile;
				
				while(!m_closed && t_delayLoadFileEnum.hasMoreElements()){
					
					t_sortFile = new SortFile();
					
					t_sortFile.m_name 		= t_delayLoadFileEnum.nextElement().toString(); 
					t_sortFile.m_full_name	= m_path + t_sortFile.m_name;					
					
					FileConnection next = (FileConnection) Connector.open(t_sortFile.m_full_name,Connector.READ);
					try{
						t_sortFile.m_modifiedTime	= next.lastModified();
						t_sortFile.m_isFolder		= next.isDirectory();
						
						if(!t_sortFile.m_isFolder){
							t_sortFile.m_size			= (int)next.fileSize();
						}
						
					}finally{
						next.close();
					}
					
					boolean t_add = false;
					int t_num = t_fileList.size();
					for(int i = 0;i < t_num;i++){
						t_cmpSortFile = (SortFile)t_fileList.elementAt(i);
						
						if(t_cmpSortFile.m_modifiedTime <= t_sortFile.m_modifiedTime){
							
							t_add = true;
							t_fileList.insertElementAt(t_sortFile, i);
																		
							break;
						}
					}
					
					if(!t_add){
						t_fileList.addElement(t_sortFile);
					}
				}
				
				int t_fileIdx = 0;
				
				while(!m_closed && t_fileIdx < t_fileList.size()) {
					
					if(t_loadTimer > 3){
						
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
					
					t_sortFile = (SortFile)t_fileList.elementAt(t_fileIdx);
					
					String t_name 	  = t_sortFile.m_name;
					String t_fullname = t_sortFile.m_full_name;
					
					t_fileIdx ++;
					t_loadTimer++;					
					
					Object bitmap = null;
					
					if(t_sortFile.m_isFolder){
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
										
					FileIcon t_icon ;
					
					if(t_sortFile.m_isFolder){
						t_icon = new FileIcon(t_name,t_fullname,bitmap,0,true,t_sortFile.m_modifiedTime,this);
					}else{
						t_icon = new FileIcon(t_name,t_fullname,bitmap,t_sortFile.m_size,false,t_sortFile.m_modifiedTime,this);
					}
					
					if(t_fileIdx <= 5){
						readImageFile(t_icon);	
					}					
					
					synchronized (UiApplication.getEventLock()) {
						m_fileList.add(t_icon);
					}
				}				
				
			}catch(Exception _e){
				m_mainApp.SetErrorString("DFL1R", _e);
			}
		}
		
		final int fm_scaleSize = recvMain.fsm_display_width >= 320?80:50;
		byte[]	m_readImageBuffer = null;
		int		m_readImageLength = 0;
		
		Vector	m_imageLoadList = new Vector();
		
		Thread	m_loadImageThread = new Thread(){
			public void run(){
				readImageProcess();
			}
		};
		
		private void readImageProcess(){
			
			while(!m_closed){
				
				synchronized (m_loadImageThread) {
					try{
						m_loadImageThread.wait();
					}catch(Exception e){
						break;
					}
				}
								
				if(m_imageLoadList.size() != 0){
					FileIcon t_icon = (FileIcon)m_imageLoadList.elementAt(0);
					t_icon.m_bitmapSnap = (Bitmap)readImageFile_imple(t_icon.m_filename_full);
					t_icon.invalidate();
					
					m_imageLoadList.removeElementAt(0);
				}
			}
		}
		
		public void readImageFile(FileIcon _icon){
			
			synchronized (m_imageLoadList) {
				for(int i = 0;i < m_imageLoadList.size();i++){
					if(m_imageLoadList.elementAt(i) == _icon){
						return ;
					}
				}
			}
			
			m_imageLoadList.addElement(_icon);
			
			synchronized (m_loadImageThread) {
				m_loadImageThread.notify();
			}
		}
		
		private Object readImageFile_imple(String _name_full){
				
			try{
				
				FileConnection t_fileRead = (FileConnection)Connector.open(_name_full,Connector.READ);
				try{

					if(!t_fileRead.exists() || t_fileRead.isDirectory()){
						return null;
					}
					
					m_readImageLength = (int)t_fileRead.fileSize();
					
					if(m_readImageBuffer == null || m_readImageBuffer.length < m_readImageLength){					
						m_readImageBuffer = new byte[m_readImageLength];
					}
					
					sendReceive.ForceReadByte(t_fileRead.openInputStream(), m_readImageBuffer, m_readImageLength);
				}finally{
					t_fileRead.close();
				}
			
				EncodedImage t_origImage = EncodedImage.createEncodedImage(m_readImageBuffer, 0, m_readImageLength);
				
				int t_origWidth = t_origImage.getWidth();
				int t_origHeight = t_origImage.getHeight();
				
				int scaleX = Fixed32.div(Fixed32.toFP(t_origWidth), Fixed32.toFP(fm_scaleSize));
				int scaleY = Fixed32.div(Fixed32.toFP(t_origHeight), Fixed32.toFP(fm_scaleSize));
													
				return t_origImage.scaleImage32(scaleX, scaleY).getBitmap();
				
			}catch(Exception e){
				m_mainApp.SetErrorString("RIF:",e);
			}				
		
			return null;
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
		Bitmap			m_bitmapSnap	= null;
		boolean		m_isFolder	= false;
		long			m_lastModified = 0;
		
		String			m_display_name = null;
		
		LoadFileThread	m_loadThread;
				
		public FileIcon(String _name,String _name_full,Object _image,
				int _fileSize,boolean _isFolder,long _lastModified,LoadFileThread _thread){
			
			super(Field.FOCUSABLE);
						
			m_fileSize			= _fileSize;
			m_filename			= _name;
			m_filename_full 	= _name_full;
			m_isFolder			= _isFolder;
			m_lastModified 		= _lastModified;
			m_loadThread		= _thread;
			
			if(_image instanceof ImageUnit){
				m_bitmap			= (ImageUnit)_image;
			}else{
				m_bitmapSnap		= (Bitmap)_image;
			}
			
			if(m_isFolder){
				m_display_name = m_filename;
			}else{
				if(m_fileSize > 1024 * 1024){					 
					m_display_name = "(" + (m_fileSize / 1024 / 1024) + "MB) " + m_filename;
				}else if(m_fileSize > 1024){
					m_display_name = "(" + (m_fileSize / 1024 ) + "KB) " + m_filename;
				}else{
					m_display_name = "(" + (m_fileSize) + "B) " + m_filename;
				}	
			}
		}
		
		public void invalidate(){
			super.invalidate();
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
				
				if(IsImageFile(m_filename_full) && m_bitmapSnap == null){
					m_loadThread.readImageFile(this);
				}
			}		
			
			int t_x = uploadFileScreen.fsm_bitmap_width;
			int t_y = (getPreferredHeight() - graphics.getFont().getHeight()) / 2;
			
			if(m_bitmapSnap != null){
				graphics.drawBitmap(2, 2, m_bitmapSnap.getWidth(), m_bitmapSnap.getHeight(), m_bitmapSnap, 0, 0);
				t_x = m_bitmapSnap.getWidth() + 5;
			}else{
				m_imageSets.drawImage(graphics, m_bitmap, 0, 0);
			}
			
			graphics.drawText(m_display_name, t_x, t_y);			
		}
		
		protected void onUnfocus(){
			super.onUnfocus();
			invalidate();
		}
		
		public int getPreferredWidth(){
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			return m_bitmapSnap != null ? (m_bitmapSnap.getHeight() + 5):m_bitmap.getHeight();
		}
		
		protected void layout(int width, int height){
			setExtent(getPreferredWidth(), getPreferredHeight());
		}

		protected void paint(Graphics graphics) {
			drawFocus(graphics, isFocus());			
		}

	}
	
	/**
	 * reference:
	 * 
	 * http://webdesign.about.com/od/multimedia/a/mime-types-by-content-type.htm
	 */
	public final static String[][] MIME_TYPE_TO_EXT_NAME = 
	{
		{"application/envoy",".evy"},
		{"application/fractals",".fif"},
		{"application/futuresplash",".spl"},
		{"application/hta",".hta"},
		{"application/internet-property-stream",".acx"},
		{"application/mac-binhex40",".hqx"},
		{"application/msword",".doc"},
		{"application/msword",".dot"},
		{"application/octet-stream",".*"},
		{"application/octet-stream",".bin"},
		{"application/octet-stream",".class"},
		{"application/octet-stream",".dms"},
		{"application/octet-stream",".exe"},
		{"application/octet-stream",".lha"},
		{"application/octet-stream",".lzh"},
		{"application/oda",".oda"},
		{"application/olescript",".axs"},
		{"application/pdf",".pdf"},
		{"application/pics-rules",".prf"},
		{"application/pkcs10",".p10"},
		{"application/pkix-crl",".crl"},
		{"application/postscript",".ai"},
		{"application/postscript",".eps"},
		{"application/postscript",".ps"},
		{"application/rtf",".rtf"},
		{"application/set-payment-initiation",".setpay"},
		{"application/set-registration-initiation",".setreg"},
		{"application/vnd.ms-excel",".xla"},
		{"application/vnd.ms-excel",".xlc"},
		{"application/vnd.ms-excel",".xlm"},
		{"application/vnd.ms-excel",".xls"},
		{"application/vnd.ms-excel",".xlt"},
		{"application/vnd.ms-excel",".xlw"},
		{"application/vnd.ms-outlook",".msg"},
		{"application/vnd.ms-pkicertstore",".sst"},
		{"application/vnd.ms-pkiseccat",".cat"},
		{"application/vnd.ms-pkistl",".stl"},
		{"application/vnd.ms-powerpoint",".pot"},
		{"application/vnd.ms-powerpoint",".pps"},
		{"application/vnd.ms-powerpoint",".ppt"},
		{"application/vnd.ms-project",".mpp"},
		{"application/vnd.ms-works",".wcm"},
		{"application/vnd.ms-works",".wdb"},
		{"application/vnd.ms-works",".wks"},
		{"application/vnd.ms-works",".wps"},
		{"application/winhlp",".hlp"},
		{"application/x-bcpio",".bcpio"},
		{"application/x-cdf",".cdf"},
		{"application/x-compress",".z"},
		{"application/x-compressed",".tgz"},
		{"application/x-cpio",".cpio"},
		{"application/x-csh",".csh"},
		{"application/x-director",".dcr"},
		{"application/x-director",".dir"},
		{"application/x-director",".dxr"},
		{"application/x-dvi",".dvi"},
		{"application/x-gtar",".gtar"},
		{"application/x-gzip",".gz"},
		{"application/x-hdf",".hdf"},
		{"application/x-internet-signup",".ins"},
		{"application/x-internet-signup",".isp"},
		{"application/x-iphone",".iii"},
		{"application/x-javascript",".js"},
		{"application/x-latex",".latex"},
		{"application/x-msaccess",".mdb"},
		{"application/x-mscardfile",".crd"},
		{"application/x-msclip",".clp"},
		{"application/x-msdownload",".dll"},
		{"application/x-msmediaview",".m13"},
		{"application/x-msmediaview",".m14"},
		{"application/x-msmediaview",".mvb"},
		{"application/x-msmetafile",".wmf"},
		{"application/x-msmoney",".mny"},
		{"application/x-mspublisher",".pub"},
		{"application/x-msschedule",".scd"},
		{"application/x-msterminal",".trm"},
		{"application/x-mswrite",".wri"},
		{"application/x-netcdf",".cdf"},
		{"application/x-netcdf",".nc"},
		{"application/x-perfmon",".pma"},
		{"application/x-perfmon",".pmc"},
		{"application/x-perfmon",".pml"},
		{"application/x-perfmon",".pmr"},
		{"application/x-perfmon",".pmw"},
		{"application/x-pkcs12",".p12"},
		{"application/x-pkcs12",".pfx"},
		{"application/x-pkcs7-certificates",".p7b"},
		{"application/x-pkcs7-certificates",".spc"},
		{"application/x-pkcs7-certreqresp",".p7r"},
		{"application/x-pkcs7-mime",".p7c"},
		{"application/x-pkcs7-mime",".p7m"},
		{"application/x-pkcs7-signature",".p7s"},
		{"application/x-sh",".sh"},
		{"application/x-shar",".shar"},
		{"application/x-shockwave-flash",".swf"},
		{"application/x-stuffit",".sit"},
		{"application/x-sv4cpio",".sv4cpio"},
		{"application/x-sv4crc",".sv4crc"},
		{"application/x-tar",".tar"},
		{"application/x-tcl",".tcl"},
		{"application/x-tex",".tex"},
		{"application/x-texinfo",".texi"},
		{"application/x-texinfo",".texinfo"},
		{"application/x-troff",".roff"},
		{"application/x-troff",".t"},
		{"application/x-troff",".tr"},
		{"application/x-troff-man",".man"},
		{"application/x-troff-me",".me"},
		{"application/x-troff-ms",".ms"},
		{"application/x-ustar",".ustar"},
		{"application/x-wais-source",".src"},
		{"application/x-x509-ca-cert",".cer"},
		{"application/x-x509-ca-cert",".crt"},
		{"application/x-x509-ca-cert",".der"},
		{"application/ynd.ms-pkipko",".pko"},
		{"application/zip",".zip"},
		{"audio/basic",".au"},
		{"audio/basic",".snd"},
		{"audio/mid",".mid"},
		{"audio/mid",".rmi"},
		{"audio/mpeg",".mp3"},
		{"audio/x-aiff",".aif"},
		{"audio/x-aiff",".aifc"},
		{"audio/x-aiff",".aiff"},
		{"audio/x-mpegurl",".m3u"},
		{"audio/x-pn-realaudio",".ra"},
		{"audio/x-pn-realaudio",".ram"},
		{"audio/x-wav",".wav"},
		{"image/bmp",".bmp"},
		{"image/cis-cod",".cod"},
		{"image/gif",".gif"},
		{"image/ief",".ief"},
		{"image/jpeg",".jpe"},
		{"image/jpeg",".jpeg"},
		{"image/jpeg",".jpg"},
		{"image/pipeg",".jfif"},
		{"image/svg+xml",".svg"},
		{"image/tiff",".tif"},
		{"image/tiff",".tiff"},
		{"image/x-cmu-raster",".ras"},
		{"image/x-cmx",".cmx"},
		{"image/x-icon",".ico"},
		{"image/x-portable-anymap",".pnm"},
		{"image/x-portable-bitmap",".pbm"},
		{"image/x-portable-graymap",".pgm"},
		{"image/x-portable-pixmap",".ppm"},
		{"image/x-rgb",".rgb"},
		{"image/x-xbitmap",".xbm"},
		{"image/x-xpixmap",".xpm"},
		{"image/x-xwindowdump",".xwd"},
		{"message/rfc822",".mht"},
		{"message/rfc822",".mhtml"},
		{"message/rfc822",".nws"},
		{"text/css",".css"},
		{"text/h323",".323"},
		{"text/html",".htm"},
		{"text/html",".html"},
		{"text/html",".stm"},
		{"text/iuls",".uls"},
		{"text/plain",".bas"},
		{"text/plain",".c"},
		{"text/plain",".h"},
		{"text/plain",".txt"},
		{"text/richtext",".rtx"},
		{"text/scriptlet",".sct"},
		{"text/tab-separated-values",".tsv"},
		{"text/webviewhtml",".htt"},
		{"text/x-component",".htc"},
		{"text/x-setext",".etx"},
		{"text/x-vcard",".vcf"},
		{"video/mpeg",".mp2"},
		{"video/mpeg",".mpa"},
		{"video/mpeg",".mpe"},
		{"video/mpeg",".mpeg"},
		{"video/mpeg",".mpg"},
		{"video/mpeg",".mpv2"},
		{"video/quicktime",".mov"},
		{"video/quicktime",".qt"},
		{"video/x-la-asf",".lsf"},
		{"video/x-la-asf",".lsx"},
		{"video/x-ms-asf",".asf"},
		{"video/x-ms-asf",".asr"},
		{"video/x-ms-asf",".asx"},
		{"video/x-msvideo",".avi"},
		{"video/x-sgi-movie",".movie"},
		{"x-world/x-vrml",".flr"},
		{"x-world/x-vrml",".vrml"},
		{"x-world/x-vrml",".wrl"},
		{"x-world/x-vrml",".wrz"},
		{"x-world/x-vrml",".xaf"},
		{"x-world/x-vrml",".xof"},
		{"flv-application/octet-stream",".flv"},
	};
	
	/**
	 * get the MIME type string 
	 * @param _name
	 * @return
	 */
	public static String getMIMETypeString(String _filename){
		for(int i = 0;i < MIME_TYPE_TO_EXT_NAME.length;i++){
			String[] item = MIME_TYPE_TO_EXT_NAME[i];
		
			if(_filename.endsWith(item[1])){
				return item[0];
			}
		}
		
		return "application/octet-stream";
	}
}
