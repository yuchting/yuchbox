import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

final class stateScreen extends MainScreen implements FieldChangeListener{
										
        
    EditField           m_hostName      = null;
    EditField			m_hostport		= null;
    EditField           m_userPassword  = null;
    
    ButtonField         m_connectBut    = null;
    LabelField          m_stateText     = null;
    LabelField          m_errorText     = null;
        
    recvMain			m_mainApp		= null;
    
    public stateScreen(recvMain _app) {
    	        
        super();
        
        m_mainApp	= _app;        
        
        m_hostName = new EditField("hostname:","",128, EditField.FILTER_DEFAULT);
        m_hostName.setChangeListener(this);
        add(m_hostName);
        
        m_hostport = new EditField("port:","",5, EditField.FILTER_INTEGER);
        m_hostport.setChangeListener(this);
        add(m_hostport);
        
        m_userPassword = new EditField("userpassword:","",128, EditField.FILTER_DEFAULT);
        add(m_userPassword);
        
        m_connectBut = new ButtonField(m_mainApp.m_connectDeamon.IsConnected()?"disconnect":"connect",
        								ButtonField.CONSUME_CLICK| ButtonField.NEVER_DIRTY);
        
        m_connectBut.setChangeListener(this);
        
        add(m_connectBut);              
        
        m_stateText = new LabelField(m_mainApp.GetStateString(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_stateText);
        
        m_errorText = new LabelField(m_mainApp.GetErrorString(), LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_errorText);
        
        
        
               
    }
    
    public boolean onClose(){
    	if(m_mainApp.m_connectDeamon.IsConnected()){
    		m_mainApp.requestBackground();
    		return false;
    	}
    	
    	System.exit(0);
    	
    	return true;
    }
        
    public void DialogAlert(final String _msg){
    	
    	UiApplication.getUiApplication().invokeLater(new Runnable() 
		{
		    public void run(){
		       Dialog.alert(_msg);
		    }
		});
    }
        
    public void fieldChanged(Field field, int context) {
        if(context != FieldChangeListener.PROGRAMMATIC){
			// Perform action if user changed field. 
			//
			if(field == m_connectBut){
				
				if(/*m_hostName.getText().length() == 0 
					|| m_userPassword.getText().length() == 0
					|| m_hostport.getText().length() == 0*/false){
					
					Dialog.alert("the host name or port or user password is null");
					
					return;
				}				
												
				if(m_mainApp.m_connectDeamon.IsConnected()){
					
					try{
						m_mainApp.m_connectDeamon.Disconnect();
					}catch(Exception _e){}
					
					m_connectBut.setLabel("connect");
					m_mainApp.SetStateString("disconnect");
					
				}else{
										
					
					try{
						//m_mainApp.m_connectDeamon.Connect(m_hostName.getText(),
						//									Integer.valueOf(m_hostport.getText()).intValue(),
						//									m_userPassword.getText());
						
						m_mainApp.m_connectDeamon.Connect("192.168.10.20",9716,"111111");
						
						m_mainApp.SetStateString("connecting...");
						m_connectBut.setLabel("disconnect");
						
					}catch(Exception e){
						DialogAlert(e.getMessage());
					}
				}				
			}
        }else{
        	// Perform action if application changed field.
        }
    }
    
}

public class recvMain extends UiApplication /*implements clientResource*/ {
	
	stateScreen 		m_stateScreen 		= null;
	uploadFileScreen 	m_uploadFileScreen	= null;
	connectDeamon 		m_connectDeamon		= new connectDeamon(this);
	
	String				m_stateString		= new String("disconnect");
	String				m_errorString		= new String();
	
	String				m_currentPath 	= new String("file:///store/");
	
//	private static ResourceBundle _resources = ResourceBundle.getBundle(
//	           		clientResource.BUNDLE_ID, clientResource.BUNDLE_NAME);
	
	public static void main(String[] args) {
		recvMain t_theApp = new recvMain();		
		t_theApp.enterEventDispatcher();		
	}
   
	public recvMain() {	
				
		// create the sdcard path 
		//
//        try{
//        	FileConnection fc = (FileConnection) Connector.open("file:///SDCard/YuchBerry",Connector.READ_WRITE);
//        	if(!fc.exists()){
//        		fc.mkdir();
//        	}
//        }catch(Exception _e){
//        	
//        	DialogAlert("can't use the SDCard to store attachment!");
//        	System.exit(0);
//        }
		
	}
	
	public void activate(){
//		if(m_stateScreen == null){
//			m_stateScreen = new stateScreen(this);
//			pushScreen(m_stateScreen);
//		}
		
		if(m_uploadFileScreen == null){
			m_uploadFileScreen = new uploadFileScreen(m_connectDeamon,this);
			pushScreen(m_uploadFileScreen);
		}
	}
	
	public void deactivate(){
//		if(m_stateScreen != null){
//			popScreen(m_stateScreen);
//			m_stateScreen = null;	
//		}
		
		if(m_uploadFileScreen != null){
			popScreen(m_uploadFileScreen);
			m_uploadFileScreen = null;	
		}
	}
	
	public void SetStateString(String _state){
		
		if(m_stateScreen != null){
			m_stateScreen.m_stateText.setText(_state);
		}
		
		m_stateString = _state;
	}

	public String GetStateString(){
		return m_stateString;
	}
	
	public void SetErrorString(String _error){
		if(m_stateScreen != null){
			m_stateScreen.m_errorText.setText(_error);
		}
		
		m_errorString = _error;
	}
	
	public String GetErrorString(){
		return m_errorString;
	}
	
	
}

