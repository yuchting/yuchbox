import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.BitmapField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.ObjectListField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.MainScreen;

class fileIcon extends BitmapField{
	
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
										/*TrackwheelListener,*/
										FieldChangeListener{
	
	
	
	
	LabelField			m_pathLabel 	= new LabelField("");
	SeparatorField		m_split 		= new SeparatorField(SeparatorField.LINE_HORIZONTAL);
	fileIconList		m_fileList 		= new fileIconList();
	
	uploadFileScreenMenu	m_ok		= new uploadFileScreenMenu("OK",0,100,this);
	uploadFileScreenMenu	m_cancel	= new uploadFileScreenMenu("Cancel",1,100,this);
	
	connectDeamon		m_deamon	= null; 
	
	boolean			m_addAttachment = false;
	
	recvMain			m_mainApp		= null;
	
	uploadFileScreen(connectDeamon _deamon,recvMain _app) {
		
		m_pathLabel.setText(_app.m_currentPath);
		
		add(m_pathLabel);
		add(m_split);
		add(m_fileList);
		
		m_deamon = _deamon;
		m_mainApp = _app;
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
			
			
			// add a attachment file
			//
			m_deamon.AddAttachmentFile("");
			
		}else if(_menu == m_cancel){
			onClose();
		}	
	}
	
	public void fieldChanged(Field field, int context) {
		
	}

//	public boolean trackwheelClick(int status, int time) {
//		 
//	}
//	
//	public boolean trackwheelRoll(int status, int time) {
//		 
//	}
//	
//	public boolean trackwheelUnclick(int status, int time) {
//		 
//	}
}
