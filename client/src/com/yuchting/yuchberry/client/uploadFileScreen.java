package com.yuchting.yuchberry.client;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.EncodedImage;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.ListField;
import net.rim.device.api.ui.component.ListFieldCallback;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.container.MainScreen;

class fileIcon {

	String			m_filename 	= null;
	String			m_filename_full 	= null;
	Bitmap			m_bitmap		= null;
	boolean		m_isFolder	= false;
	
	public fileIcon(String _name,String _name_full,Bitmap _image,boolean _isFolder){
		
		m_filename	= _name;
		m_filename_full = _name_full;
		m_bitmap		= _image;
		m_isFolder	= _isFolder;
	}
	
	public String toString(){
		return m_filename;
	}

}


class IconListCallback implements ListFieldCallback {
	
	Vector 	m_iconList	= new Vector();
	
	public void drawListRow(ListField listField, Graphics graphics, int index, int y, int width) {
		
		fileIcon t_file = (fileIcon)m_iconList.elementAt(index);
			
		graphics.drawBitmap(0, y,uploadFileScreen.fsm_bitmap_width,
							uploadFileScreen.fsm_bitmap_width,
							t_file.m_bitmap, 0, 0);
		
		graphics.drawText(t_file.m_filename, uploadFileScreen.fsm_bitmap_width, y);          
	}
		 
	public Object get(ListField listField, int index){
		return m_iconList.elementAt(index);
	}
		 
	public int getPreferredWidth(ListField listField){
		return Graphics.getScreenWidth();
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
	
	public void insert(fileIcon _icon){
		m_iconList.addElement(_icon);
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
	
	uploadFileScreenMenu	m_check		= new uploadFileScreenMenu("Check",0,100,this);
	uploadFileScreenMenu	m_ok		= new uploadFileScreenMenu("OK",1,100,this);
	uploadFileScreenMenu	m_cancel	= new uploadFileScreenMenu("Cancel",2,100,this);
	
	
	final static int fsm_bitmap_width	= 32;
	final static int fsm_bitmap_height = 32;
	
	Bitmap				m_textFileBitmap	= null;
	Bitmap				m_audioFileBitmap = null;
	Bitmap				m_binFileBitmap	= null;
	Bitmap				m_folderBitmap	= null;
	Bitmap				m_pictureBitmap	= null;
	Bitmap				m_movieBitmap	= null;
	
	IconListCallback	m_listCallback	= new IconListCallback();
	
	connectDeamon		m_deamon	= null; 
	
	boolean			m_addAttachment = false;
	
	recvMain			m_mainApp		= null;
	
	final static String fsm_rootPath	= new String("file:///store/home/user/");
	
	String				m_currDisplayPath = null;
	
	boolean			m_delScreen		=false;
	
	uploadFileScreen(connectDeamon _deamon,recvMain _app,boolean _del) {
		
		try{
			m_textFileBitmap 	= GetConstFileBitmap("/Crystal_Txt_resize.bmp");
			m_audioFileBitmap 	= GetConstFileBitmap("/Crystal_Audio_resize.bmp");
			m_binFileBitmap 	= GetConstFileBitmap("/Crystal_Generic_resize.bmp");
			m_folderBitmap 		= GetConstFileBitmap("/Folder_yellow_resize.bmp");
			m_pictureBitmap		= GetConstFileBitmap("/Picture.bmp");
			m_movieBitmap		= GetConstFileBitmap("/Movie.bmp");
			
		}catch(Exception e){}	
		
		m_fileList.setCallback(m_listCallback);		
		add(m_fileList);
		
		m_deamon = _deamon;
		m_mainApp = _app;
		m_delScreen = _del;
				
		DisplayFileList(fsm_rootPath);
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
					String t_fullname = (String)t_files.elementAt(i);
					String t_name = null;
					
					final int t_lastSplash = t_fullname.lastIndexOf('/');
					if(t_lastSplash == -1){
						t_name = "xxxxx";
					}else{
						t_name = t_fullname.substring(t_lastSplash + 1,t_fullname.length());
					}				
					
					Bitmap bitmap;
					
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
					
					fileIcon t_icon = new fileIcon(t_name,t_fullname,bitmap,false);
					
					m_fileList.insert(t_index++);
					m_listCallback.insert(t_icon);
				}
				
			}catch(Exception _e){
				System.out.print(_e.getMessage());
			}

		}else{

			
			m_currDisplayPath = _path;
			
			setTitle(_path);
			
			try{			
				
				
				FileConnection fc = (FileConnection) Connector.open(_path,Connector.READ);
				int t_index = 0;
				for(Enumeration e = fc.list("*",true); e.hasMoreElements() ;) {
					String t_name = (String)e.nextElement();
					String t_fullname = _path + t_name;
					
					FileConnection next = (FileConnection) Connector.open(t_fullname,Connector.READ);
				    
					Bitmap bitmap = null;
					
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
					
					fileIcon t_icon = new fileIcon(t_name,t_fullname,bitmap,next.isDirectory());
					
					m_fileList.insert(t_index++);
					m_listCallback.insert(t_icon);
				}

			}catch(Exception _e){
				System.out.print(_e.getMessage());
			}
		}
	}
	
	
	public Bitmap GetConstFileBitmap(String res)throws Exception{
		Class classs = Class.forName("com.yuchting.yuchberry.client.recvMain");
		byte[] bytes = IOUtilities.streamToBytes(classs.getResourceAsStream(res));
		
		Bitmap b = new Bitmap(fsm_bitmap_width,fsm_bitmap_width);
		b.createAlpha(Bitmap.ALPHA_BITDEPTH_8BPP);
		
		int[] t_data = new int[fsm_bitmap_width * fsm_bitmap_height];
		
		for(int i = 0;i < fsm_bitmap_height;i++){
			
			for(int j = 0;j < fsm_bitmap_width; j++){
				
				final int int_index = fsm_bitmap_width * i + j;
				final int byte_index = (fsm_bitmap_width * ( fsm_bitmap_height  - i - 1) + j ) * 4 + 0x32 ;
				t_data[int_index] = ((int)bytes[byte_index] ) | ((int)bytes[byte_index + 1] << 8)
									| ((int)bytes[byte_index + 2] << 16)  | ((int)bytes[byte_index + 3] << 24);
			}
			
		}
		
		b.setARGB(t_data, 0, fsm_bitmap_width, 0, 0, fsm_bitmap_width, fsm_bitmap_height);
		
		return b;
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
	    menu.add(m_cancel);
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
					}else{
						m_deamon.AddAttachmentFile(t_file.m_filename_full);
					}				
					
					m_mainApp.ClearUploadingScreen();
					close();
				}
			}	
			
		}else if(_menu == m_cancel){
			m_mainApp.ClearUploadingScreen();
			close();
		}	
	}
	
	public boolean onClose(){
		if(m_delScreen){
			close();
			return true;
		}
		
		if(m_currDisplayPath.equals(fsm_rootPath)){
			
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
