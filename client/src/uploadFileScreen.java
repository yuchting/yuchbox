import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.io.IOUtilities;
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
	String			m_filename_null 	= null;
	EncodedImage	m_image		= null;
	boolean		m_isFolder	= false;
	
	public fileIcon(String _name,String _name_full,EncodedImage _image,boolean _isFolder){
		
		m_filename	= _name;
		m_filename_null = _name_full;
		m_image		= _image;
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
		
		final int image_width	= t_file.m_image.getWidth();
		final int image_height = t_file.m_image.getHeight();
		
		graphics.drawBitmap(0, y,image_width,image_height,
							t_file.m_image.getBitmap(), 0, 0);
		
		graphics.drawText(t_file.m_filename, image_width, y);          
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
	
	public void clear(){
		m_iconList.removeAllElements();
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
	
	uploadFileScreenMenu	m_ok		= new uploadFileScreenMenu("OK",0,100,this);
	uploadFileScreenMenu	m_cancel	= new uploadFileScreenMenu("Cancel",1,100,this);
	
	EncodedImage		m_textFileImage	= null;
	EncodedImage		m_audioFileImage = null;
	EncodedImage		m_binFileImage	= null;
	EncodedImage		m_folderImage	= null;
	
	IconListCallback	m_listCallback	= new IconListCallback();
	
	connectDeamon		m_deamon	= null; 
	
	boolean			m_addAttachment = false;
	
	recvMain			m_mainApp		= null;
	
	final static String fsm_rootPath	= new String("file:///store/home/user/");
	
	String				m_currDisplayPath = null;
	
	uploadFileScreen(connectDeamon _deamon,recvMain _app) {
		
		try{
			
			Class classs = Class.forName("recvMain");

			byte[] bytes = IOUtilities.streamToBytes(classs.getResourceAsStream("/Crystal_Txt_resize.bmp"));
			m_textFileImage		= EncodedImage.createEncodedImage(bytes, 0, bytes.length);
			
			bytes = IOUtilities.streamToBytes(classs.getResourceAsStream("/Crystal_Audio_resize.bmp"));
			m_audioFileImage	= EncodedImage.createEncodedImage(bytes, 0, bytes.length);
			
			bytes = IOUtilities.streamToBytes(classs.getResourceAsStream("/Crystal_Generic_resize.bmp"));
			m_binFileImage		= EncodedImage.createEncodedImage(bytes, 0, bytes.length);
			
			bytes = IOUtilities.streamToBytes(classs.getResourceAsStream("/Folder_yellow_resize.bmp"));
			m_folderImage		= EncodedImage.createEncodedImage(bytes, 0, bytes.length);
						
		}catch(Exception e){}	
		
		m_fileList.setCallback(m_listCallback);		
		add(m_fileList);
		
		m_deamon = _deamon;
		m_mainApp = _app;
				
		DisplayFileList(fsm_rootPath);
	}
		
	public void DisplayFileList(String _path){
		try{
			
			m_currDisplayPath = _path;

			// clear all former file list
			//
			for(int i = 0;i < m_listCallback.m_iconList.size();i++){
				m_fileList.delete(0);			
			}
			m_listCallback.m_iconList.removeAllElements();
			
			setTitle(_path);
			
			FileConnection fc = (FileConnection) Connector.open(_path,Connector.READ);
			int t_index = 0;
			for(Enumeration e = fc.list("*",true); e.hasMoreElements() ;) {
				String t_name = (String)e.nextElement();
				String t_fullname = _path + t_name;
				
				FileConnection next = (FileConnection) Connector.open(t_fullname,Connector.READ);
			    
				EncodedImage image = null;
				
				if(next.isDirectory()){
					image = m_folderImage;
				}else if(IsAudioFile(next)){
					image = m_audioFileImage;
				}else if(IsTxtFile(next)){
					image = m_textFileImage;
				}else{
					image = m_binFileImage;
				}
				
				fileIcon t_icon = new fileIcon(t_name,t_fullname,image,next.isDirectory());
				
				m_fileList.insert(t_index++);
				m_listCallback.insert(t_icon);
			}
	
		}catch(Exception _e){
			System.out.print(_e.getMessage());
		}
	}
	
	public boolean IsAudioFile(FileConnection _file){
		String t_lower = _file.getName().toLowerCase();
		t_lower = t_lower.substring(Math.max(0, t_lower.length() - 4));
				
		return t_lower.equals(".mp3") 
				|| t_lower.equals(".wav")
				|| t_lower.equals(".wma")
				|| t_lower.equals(".ogg")
				|| t_lower.equals(".mid");
	}
	
	public boolean IsTxtFile(FileConnection _file){
		
		String t_lower = _file.getName().toLowerCase();
		t_lower = t_lower.substring(Math.max(0, t_lower.length() - 4));
				
		return t_lower.equals(".txt")
				|| t_lower.equals(".log")
				|| t_lower.equals(".dic");
	}

	
	public void openAddAttachment(){
		m_addAttachment = true;
	}
	
	public void openDelAttachment(){
		m_addAttachment = false;
	}

	protected void makeMenu(Menu menu, int instance) {
	    menu.add(m_ok);
	    menu.add(m_cancel);
	}
	
	public void menuClicked(uploadFileScreenMenu _menu){
		if(_menu == m_ok){
			
			final int t_index = m_fileList.getSelectedIndex(); 
			if(t_index != -1 ){
				final fileIcon t_file = (fileIcon)m_listCallback.m_iconList.elementAt(t_index);
				if(t_file.m_isFolder){
					DisplayFileList(t_file.m_filename_null);
				}else{
					
					// add a attachment file
					//
					m_deamon.AddAttachmentFile(t_file.m_filename_null);
					
					close();
				}
			}	
			
		}else if(_menu == m_cancel){
			close();
		}	
	}
	
	public boolean onClose(){
		
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
				DisplayFileList(t_file.m_filename_null);
				
				return true;
			}else{
				
				return false;
			}
		}
		
		return false;
	}

	
}
