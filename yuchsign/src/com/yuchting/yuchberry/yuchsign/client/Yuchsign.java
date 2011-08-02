package com.yuchting.yuchberry.yuchsign.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.PopupPanel;
import com.google.gwt.user.client.ui.Widget;
import com.yuchting.yuchberry.yuchsign.client.account.BberPanel;
import com.yuchting.yuchberry.yuchsign.client.account.LoginPanel;
import com.yuchting.yuchberry.yuchsign.client.account.YuchPanel;
import com.yuchting.yuchberry.yuchsign.client.account.yuchbber;
import com.yuchting.yuchberry.yuchsign.client.welcome.ActiveImage;
import com.yuchting.yuchberry.yuchsign.shared.FieldVerifier;



/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Yuchsign implements EntryPoint {
		  
	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	public final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
	
	/**
	 * client logger
	 */
	private final static Logger fsm_logger = Logger.getLogger("ClientLogger");
	
	public  LoginPanel m_logonDlg = null;
	public  BberPanel	m_bberPane = null;
	public  YuchPanel	m_yuchPanel = null;
	
	private ActiveImage m_actImage = null;
	
	static private final DecoratedPopupPanel fsm_simplePopup = new DecoratedPopupPanel(true);
		
	static YesNoDialog		sm_yesNoDlg = null;
	
	private static class waitingLabel extends PopupPanel{

		final Label		m_label = new Label();
		
	    public waitingLabel() {
			// PopupPanel's constructor takes 'auto-hide' as its boolean parameter.
			// If this is set, the panel closes itself automatically when the user
			// clicks outside of it.
			super();
			
			final HorizontalPanel t_horz = new HorizontalPanel();
			Image t_waiting = new Image("logon.gif");
			t_horz.add(t_waiting);
			t_horz.add(m_label);
			
			m_label.setWordWrap(true);
			
			setWidget(t_horz);
			  
			// waiting label
			setStyleName("waitingLabel");	    
	    }
	    
	    public void ShowText(String _text,Widget _attach){
	    	setModal(true);
	    	show();
	    	
	    	int t_left = 120;
	    	int t_top = 60;
	    	
	    	try{
	    		
		    	if(_attach != null){
		    		
		    		t_left 	= _attach.getAbsoluteLeft() + (_attach.getOffsetWidth() - _text.length() * 12) / 2;
		    		t_top	= _attach.getAbsoluteTop() + 30; 
		    		
		    	}
	    	}catch(Exception e){}	    	
	    	
	    	// popup position
			setPopupPosition(t_left, t_top);
	    	
	    	
	    	m_label.setText(_text);	    	
	    }
	    
	    public void Hide(){
	    	setModal(false);
	    	hide();	    	
	    }
	}
	  
	// waiting label
	public static final waitingLabel			fsm_waitingLable		= new waitingLabel();
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		m_actImage = new ActiveImage();
		m_actImage.enableChange(true);
		
		m_logonDlg = new LoginPanel(this);
	}
	
	public void ShowYuchbberPanel(String _bberXMLData,boolean _signinState)throws Exception{
		
		yuchbber t_bber = new yuchbber();
		t_bber.InputXMLData(_bberXMLData);
		
		if(m_bberPane == null){		
			m_bberPane = new BberPanel(this);
		}
		
		m_logonDlg.setVisible(false);
		
		m_bberPane.ShowBberPanle();		
		m_bberPane.ShowYuchbberData(t_bber);
		
		if(t_bber.GetSigninName().equalsIgnoreCase(FieldVerifier.fsm_admin)){
			if(m_yuchPanel == null){
				m_yuchPanel = new YuchPanel(this);
			}
			
			m_yuchPanel.ShowYuchPanel();		
		}
		
		if(_signinState){
			Yuchsign.PopupPrompt("激活邮件已经发送到了 " + t_bber.GetSigninName() + " ，请及时激活。\n如果发现没有收到，请在 【垃圾邮件箱】里面查看一下。", m_bberPane);
		}
	}
	
	public void Signout(){
		m_logonDlg.setVisible(true);
		
		if(m_yuchPanel != null){
			m_yuchPanel.HideYuchPanel();
		}
		
		if(m_bberPane != null){		
			m_bberPane.HideBberPanel();
		}
	}
	
	public static void PopupPrompt(String _prompt,Widget _attachWidget){

		_prompt = _prompt.replaceAll("[\r\n]", "<br />");
		
		HTML label = new HTML(_prompt);
		label.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		fsm_simplePopup.setWidget(label);
		
		int t_maxLine = 0;
		String[] t_lines =_prompt.split("<br />");
		
		for(String line:t_lines){
			// get ride of HTML tag
			//
			if(line.indexOf("/>") == -1 && line.indexOf("</") == -1
					
			&& t_maxLine < line.length()){
				t_maxLine = line.length();
			}
		}
		
		int left;
		int top;
		
		if(_attachWidget == null){
			left = 100;
	        top = 300;
		}else{
			left = _attachWidget.getAbsoluteLeft() + (_attachWidget.getOffsetWidth() - t_maxLine * 13) / 2;
	        top = _attachWidget.getAbsoluteTop() + (_attachWidget.getOffsetHeight()) / 2;	
		}
		
		if(left < 0){
			left = 0;
		}
		
        fsm_simplePopup.setPopupPosition(left, top);
        
        // Show the popup
        fsm_simplePopup.show();      
	}
	
	public static void PopupYesNoDlg(String _prompt , YesNoHandler _yes,YesNoHandler _no){
		if(sm_yesNoDlg == null){
			sm_yesNoDlg = new YesNoDialog();
		}
		
		sm_yesNoDlg.Popup(_prompt, _yes, _no);
	}
	
	public static void PopupWaiting(String _prompt,Widget _attach){
		if(_prompt != null && _prompt.length() != 0){
			fsm_waitingLable.ShowText(_prompt, _attach);
		}
	}
	
	public static void HideWaiting(){
		fsm_waitingLable.Hide();
	}

}
