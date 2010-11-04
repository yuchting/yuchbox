import net.rim.device.api.system.Characters;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

final class stateScreen extends MainScreen implements FieldChangeListener{
										
        
    EditField                       m_hostName      = null;
    EditField						m_hostport		= null;
    EditField                       m_userPassword  = null;
    
    ButtonField                     m_connectBut    = null;
    LabelField                      m_stateText     = null;
    
    connectDeamon					m_connectDeamon	= null; 
    
    uploadFileScreen				m_uploadFileScreen = null;
            
        
    public stateScreen() {
        
        super();
        
        m_hostName = new EditField("hostname:","",128, EditField.FILTER_DEFAULT);
        m_hostName.setChangeListener(this);
        add(m_hostName);
        
        m_hostport = new EditField("port:","",5, EditField.FILTER_INTEGER);
        m_hostport.setChangeListener(this);
        add(m_hostport);
        
        m_userPassword = new EditField("userpassword:","",128, EditField.FILTER_DEFAULT);
        add(m_userPassword);
        
        m_connectBut = new ButtonField("connect",ButtonField.CONSUME_CLICK
                                                                | ButtonField.NEVER_DIRTY);
        m_connectBut.setChangeListener(this);
        
        add(m_connectBut);              
        
        m_stateText = new LabelField("disconnect", LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_stateText);

        
        
        try{
            // create the sdcard path 
            //
//            try{
//            	FileConnection fc = (FileConnection) Connector.open("file:///SDCard/YuchBerry",Connector.READ_WRITE);
//            	if(!fc.exists()){
//            		fc.mkdir();
//            	}
//            }catch(Exception _e){
//            	
//            	DialogAlert("can't use the SDCard!");
//            	System.exit(0);
//            }
        	
			m_connectDeamon = new connectDeamon(this);
			m_uploadFileScreen = new uploadFileScreen(m_connectDeamon);
			
        }catch(Exception _e){
        	DialogAlert("Error to listen INBOX");
        }
        
    }
    
    public boolean onClose(){
	   UiApplication.getUiApplication().requestBackground()
	   return false;
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
				
				
												
				if(m_connectDeamon.IsConnected()){
					
					try{
						m_connectDeamon.Disconnect();
					}catch(Exception _e){}
					
					m_connectBut.setLabel("connect");
					m_stateText.setText("disconnect");
					
				}else{
										
					//m_connectDeamon.Connect(m_hostName.getText(),Integer.valueOf(m_hostport.getText()).intValue(),m_userPassword.getText());
					m_connectDeamon.Connect("192.168.10.20",9716,"111111");
					
					m_stateText.setText("connect....");
					m_connectBut.setLabel("disconnect");				
				}				
			}
        }else{
        	// Perform action if application changed field.
        }
    }
    
}

public class recvMain extends UiApplication {
	
	static stateScreen m_stateScreen = null;
	
	public static void main(String[] args) {
			
		recvMain t_theApp = new recvMain();		
		t_theApp.enterEventDispatcher();
	}
   
	public recvMain() {	
		if(m_stateScreen == null){
			m_stateScreen = new stateScreen();
		}
		pushScreen(m_stateScreen);
	}
}

