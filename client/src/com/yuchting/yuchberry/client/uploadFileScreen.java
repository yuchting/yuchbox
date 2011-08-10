package com.yuchting.yuchberry.client;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import local.localResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.ui.ImageSets;
import com.yuchting.yuchberry.client.ui.ImageUnit;

class fileIcon {

	static ImageSets	sm_imageSets = null;
	
	int				m_fileSize;
	String			m_filename 	= null;
	String			m_filename_full 	= null;
	ImageUnit		m_bitmap		= null;
	boolean		m_isFolder	= false;
	long			m_lastModified = 0;
	
	public fileIcon(String _name,String _name_full,ImageUnit _image,
			int _fileSize,boolean _isFolder,long _lastModified){
		
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

}


class IconListCallback implements ListFieldCallback {
	
	Vector 	m_iconList	= new Vector();
	
	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
		
		fileIcon t_file = (fileIcon)m_iconList.elementAt(index);
		
		fileIcon.sm_imageSets.drawImage(graphics, t_file.m_bitmap, 0, y);
		
		if(t_file.m_isFolder){
			graphics.drawText(t_file.m_filename, uploadFileScreen.fsm_bitmap_width, y);
		}else{
			if(t_file.m_fileSize > 1024 * 1024){
				 
				graphics.drawText("(" + (t_file.m_fileSize / 1024 / 1024) + "MB) " + t_file.m_filename, uploadFileScreen.fsm_bitmap_width, y);
			}else if(t_file.m_fileSize > 1024){
				graphics.drawText("(" + (t_file.m_fileSize / 1024 ) + "KB) " + t_file.m_filename, uploadFileScreen.fsm_bitmap_width, y);
			}else{
				graphics.drawText("(" + (t_file.m_fileSize) + "B) " + t_file.m_filename, uploadFileScreen.fsm_bitmap_width, y);
			}
			
		}
		          
	}
		 
	public Object get(ListField listField, int index){
		return m_iconList.elementAt(index);
	}
		 
	public int getPreferredWidth(ListField listField){
		return recvMain.fsm_display_width;
	}
		 
	public int indexOfList(ListField listField, String prefix, int start){
		for(;start < m_iconList.size();start++){
			
			fileIcon t_file = (fileIcon)m_iconList.elementAt(start);
			if(t_file.m_filename.indexOf(prefix) != -1){
				return start;
			}
		}
		
		return -1;
	}
	
	public void insert(fileIcon _icon,int _index){
		m_iconList.insertElementAt(_icon, _index);
	}
	
}

class fileIconList extends ObjectListField{
	
}

class uploadFileScreenMenu extends MenuItem{
	
	uploadFileScreen m_screen = null;
	
	public uploadFileScreenMenu(String _text,
								int _order,
								int priority,
								uploadFileScreen _screen){
		
		super(_text, _order, priority);
		m_screen = _screen;
	}
	
	public void run() {
		m_screen.menuClicked(this);
    }

}
public class uploadFileScreen extends MainScreen implements
										FieldChangeListener{
	
	fileIconList		m_fileList 		= new fileIconList();
	
	uploadFileScreenMenu	m_ok		= new uploadFileScreenMenu(" ",0,100,this);
	uploadFileScreenMenu	m_check		= new uploadFileScreenMenu(recvMain.sm_local.getString(localResource.CHECK_UPLOAD_FILE),1,100,this);	
	
	
	final static int fsm_bitmap_width	= 32;
	final static int fsm_bitmap_height = 32;
	
	ImageUnit				m_textFileBitmap= null;
	ImageUnit				m_audioFileBitmap = null;
	ImageUnit				m_binFileBitmap	= null;
	ImageUnit				m_folderBitmap	= null;
	ImageUnit				m_pictureBitmap	= null;
	ImageUnit				m_movieBitmap	= null;
	
	IconListCallback	m_listCallback	= new IconListCallback();
	
	connectDeamon		m_deamon	= null; 
	
	boolean			m_addAttachment = false;
	
	recvMain			m_mainApp		= null;
	
	final static String fsm_rootPath_back		= "file:///store/home/user/";
	final static String fsm_rootPath_default	= "file:///SDCard/";
	
	String				m_rootPath = null;
	
	String				m_currDisplayPath = null;
	
	boolean			m_delScreen		=false;
	
	uploadFileScreen(connectDeamon _deamon,recvMain _app,boolean _del) throws Exception {
		
		m_deamon = _deamon;
		m_mainApp = _app;
		m_delScreen = _del;
		
		fileIcon.sm_imageSets = _app.m_allImageSets;
		
		m_textFileBitmap 	= _app.m_allImageSets.getImageUnit("text_resize");
		m_audioFileBitmap 	= _app.m_allImageSets.getImageUnit("audio_resize");
		m_binFileBitmap 	= _app.m_allImageSets.getImageUnit("unknown_resize");
		m_folderBitmap 		= _app.m_allImageSets.getImageUnit("folder_resize");
		m_pictureBitmap		= _app.m_allImageSets.getImageUnit("picture_resize");
		m_movieBitmap		= _app.m_allImageSets.getImageUnit("movie_resize");
		
		m_fileList.setCallback(m_listCallback);	
		add(m_fileList);		
		
		if(_del){
			m_ok.setText(recvMain.sm_local.getString(localResource.DEL_UPLOAD_FILE));
		}else{
			m_ok.setText(recvMain.sm_local.getString(localResource.ADD_UPLOAD_FILE));
		}
		
		try{
			FileConnection fc = (FileConnection) Connector.open(fsm_rootPath_default,Connector.READ_WRITE);
			m_rootPath = fsm_rootPath_default;
			fc.close();
		}catch(Exception _e){
			m_rootPath = fsm_rootPath_back;
		}
		
		DisplayFileList(m_rootPath);
	}
		
	public void DisplayFileList(String _path){

		// clear all former file list
		//
		for(int i = 0;i < m_listCallback.m_iconList.size();i++){
			m_fileList.delete(0);			
		}
		m_listCallback.m_iconList.removeAllElements();
		
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
					
					fileIcon t_icon = new fileIcon(t_name,t_att.m_filename,bitmap,t_att.m_fileSize,false,0);
					
					m_fileList.insert(t_index);
					m_listCallback.insert(t_icon,t_index);
					
					t_index++;
				}
				
			}catch(Exception _e){
				//System.out.println(_e.getMessage());
			}

		}else{

			
			m_currDisplayPath = _path;
			
			setTitle(_path);
			
			try{			
				
				
				FileConnection fc = (FileConnection) Connector.open(_path,Connector.READ);

				for(Enumeration e = fc.list("*",true); e.hasMoreElements() ;) {
					String t_name = (String)e.nextElement();
					String t_fullname = _path + t_name;
					
					FileConnection next = (FileConnection) Connector.open(t_fullname,Connector.READ);
				    
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
					
					fileIcon t_icon ;
					if(next.isDirectory()){
						t_icon = new fileIcon(t_name,t_fullname,bitmap,0,true,t_time);
					}else{
						t_icon = new fileIcon(t_name,t_fullname,bitmap,(int)next.fileSize(),false,t_time);
					}					
					
					if(m_listCallback.m_iconList.size() == 0){
						
						m_fileList.insert(0);
						m_listCallback.insert(t_icon,0);
						
					}else{
						
						boolean t_add = false;
						
						for(int i = 0;i < m_listCallback.m_iconList.size();i++){
							fileIcon icon = (fileIcon)m_listCallback.m_iconList.elementAt(i);
							if(icon.m_lastModified <= t_icon.m_lastModified){
								
								t_add = true;
								
								m_fileList.insert(i);
								m_listCallback.insert(t_icon,i);
								
								break;
							}
						}
						
						if(!t_add){
							m_fileList.insert(m_listCallback.m_iconList.size() - 1);
							m_listCallback.insert(t_icon,m_listCallback.m_iconList.size() - 1);
						}
					}
				}

			}catch(Exception _e){
				//System.out.println(_e.getMessage());
			}
		}
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

	
	public void openAddAttachment(){
		m_addAttachment = true;
	}
	
	public void openDelAttachment(){
		m_addAttachment = false;
	}

	protected void makeMenu(Menu menu, int instance) {
		menu.add(m_check);
	    menu.add(m_ok);
	    
	    super.makeMenu(menu,instance);
	}
	
	public void menuClicked(uploadFileScreenMenu _menu){
		if(_menu == m_check){
			final int t_index = m_fileList.getSelectedIndex();
			if(t_index != -1){
				final fileIcon t_file = (fileIcon)m_listCallback.m_iconList.elementAt(t_index);
				m_mainApp.PushViewFileScreen(t_file.m_filename_full);
			}
			
		}else if(_menu == m_ok){
			
			final int t_index = m_fileList.getSelectedIndex();
			if(t_index != -1 ){
				final fileIcon t_file = (fileIcon)m_listCallback.m_iconList.elementAt(t_index);
				if(t_file.m_isFolder){
					DisplayFileList(t_file.m_filename_full);
				}else{
					
					// add a attachment file
					//
					if(m_delScreen){
						m_deamon.DelAttachmentFile(t_file.m_filename_full);
						m_mainApp.DialogAlert(m_mainApp.sm_local.getString(localResource.DEL_ATTACHMENT_SUCC));
					}else{
						m_deamon.AddAttachmentFile(t_file.m_filename_full,t_file.m_fileSize);
						m_mainApp.DialogAlert(m_mainApp.sm_local.getString(localResource.ADD_ATTACHMENT_SUCC));
					}				
					
					m_mainApp.ClearUploadingScreen();
					
					close();					
				}
			}else{
				m_mainApp.SetErrorString("menuClicked:-1");
			}
			
		}
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
	
	public void fieldChanged(Field field, int context) {
		
	}
	
	public boolean trackwheelClick(int status, int time) 
	{
		final int t_index = m_fileList.getSelectedIndex(); 
		if(t_index != -1 ){
			final fileIcon t_file = (fileIcon)m_listCallback.m_iconList.elementAt(t_index);
			if(t_file.m_isFolder){
				DisplayFileList(t_file.m_filename_full);
				
				return true;
			}else{
				
				return false;
			}
		}
		
		return false;
	}

	
}
