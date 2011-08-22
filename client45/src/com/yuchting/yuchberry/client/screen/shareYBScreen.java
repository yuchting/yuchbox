package com.yuchting.yuchberry.client.screen;

import java.util.Enumeration;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.pim.ContactList;
import javax.microedition.pim.PIM;
import javax.wireless.messaging.Message;
import javax.wireless.messaging.MessageConnection;
import javax.wireless.messaging.TextMessage;

import com.yuchting.yuchberry.client.fetchMail;
import com.yuchting.yuchberry.client.recvMain;

import local.localResource;
import net.rim.blackberry.api.pdap.BlackBerryContact;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.MenuItem;
import net.rim.device.api.ui.component.CheckboxField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.Menu;
import net.rim.device.api.ui.component.RadioButtonField;
import net.rim.device.api.ui.component.RadioButtonGroup;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.DialogFieldManager;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.MainScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

final class sendingSMSDlg extends Dialog{

	LabelField       	m_stateText  		= new LabelField();
	
	recvMain			m_mainApp			= null;
	shareYBScreen		m_parentScreen		= null;
			
	public sendingSMSDlg(recvMain _mainApp,shareYBScreen _parent){
		super("Sending...",new Object[]{recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_CONTENT_SMS_STOP)},new int[]{0},
				Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
		
		m_mainApp		= _mainApp;
		m_parentScreen	= _parent;
		
		Manager delegate = getDelegate();
		if( delegate instanceof DialogFieldManager ){
			
            DialogFieldManager dfm = (DialogFieldManager)delegate;
            
            Manager manager = dfm.getCustomManager();
            
            if(manager != null){
                manager.insert(m_stateText,0);
            }
        }
		
		setDialogClosedListener(new DialogClosedListener(){
			
			public void dialogClosed(Dialog dialog, int choice) {
				
				switch (choice) {
					case Dialog.OK:
						m_parentScreen.m_sendThread.m_cancel = true;
						onClose();
						break;
					
					default:
						break;
				}
			}
		});
		
	}
	
	public void RefreshProgress(final int _currIdx,final int _num){
		m_mainApp.invokeAndWait(new Runnable(){
			public void run() {
				m_stateText.setText("" + (_currIdx + 1) + "/" + _num);
			}
		});
	}
	
	public boolean onClose(){
		return false;
	}

}

final class shareConcatData{
	String m_name = null;
	String m_email = null;
	String m_phoneNumber = null;
}

final class sendSMSThread extends Thread{
	
	private Vector m_sendConcatData 	= null;
	private String m_sendContent		= null;
	
	private shareYBScreen m_parentScreen = null;
	private recvMain m_mainApp			= null;
	
	public boolean 	m_cancel		= false;
	
	public sendSMSThread(recvMain _mainApp,shareYBScreen _parent,Vector _sendConcatData,String _content){
		m_sendConcatData 	= _sendConcatData;
		m_sendContent		= _content;
		
		m_parentScreen		= _parent;		
		m_mainApp			= _mainApp;
		
	}
	public void run(){
		
		for(int i = 0;i < m_sendConcatData.size();i++){
			
			shareConcatData t_data = (shareConcatData)m_sendConcatData.elementAt(i);
			
			try{
				
				if(m_cancel){
					break;
				}
				
				m_parentScreen.m_sendingSMSDlg.RefreshProgress(i,m_sendConcatData.size());
				
				MessageConnection msgConn = (MessageConnection)Connector.open("sms://" + t_data.m_phoneNumber);
				Message msg = msgConn.newMessage(MessageConnection.TEXT_MESSAGE);
				TextMessage txtMsg = (TextMessage)msg;
				txtMsg.setPayloadText(m_sendContent);
				msgConn.send(txtMsg);
				
			}catch(Exception e){
				m_mainApp.SetErrorString("SH:" + e.getMessage() + " " + e.getClass().getName());
			}
		}
	
		m_mainApp.invokeLater(new Runnable() {
			
			public void run() {
				m_mainApp.popScreen(m_parentScreen.m_sendingSMSDlg);
				m_parentScreen.m_sendingSMSDlg = null;
				
				m_mainApp.popScreen(m_parentScreen);
				m_mainApp.m_shareScreen = null;
			}
		});
		
		m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_THANKS));
	}
};

public class shareYBScreen extends MainScreen implements FieldChangeListener{
	
	private ContactList 	m_mainConcat;
	private recvMain		m_mainApp;
	
	private Vector			m_concatList = new Vector();
	
	private RadioButtonGroup m_shareTypeGroup = new RadioButtonGroup();
	private RadioButtonField m_useEmail= new RadioButtonField(recvMain.sm_local.getString(localResource.SHARE_USE_EMAIL),m_shareTypeGroup,true);
	private RadioButtonField m_useSMS	= new RadioButtonField(recvMain.sm_local.getString(localResource.SHARE_USE_SMS),m_shareTypeGroup,false);
	
	private HorizontalFieldManager	m_selectShareType = new  HorizontalFieldManager(){
		
		static final int fsm_height = 25;
		
		public int getPreferredWidth(){
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			return 25;
		}
		protected void sublayout(int width, int height){	
			super.sublayout(recvMain.fsm_display_width,fsm_height);
		}

	};
	
	// construct the select share type Manager
	//
	{
		m_selectShareType.add(m_useEmail);
		m_selectShareType.add(m_useSMS);
		
		m_useEmail.setChangeListener(this);
		m_useSMS.setChangeListener(this);	
	}
	
	private VerticalFieldManager m_concatListMgr	= new VerticalFieldManager();
	
	public shareYBScreen(recvMain _mainApp)throws Exception{
		m_mainApp	= _mainApp;
		
		m_mainConcat =(ContactList)PIM.getInstance().openPIMList(PIM.CONTACT_LIST, PIM.READ_ONLY);
		
		Enumeration allContacts = m_mainConcat.items();
		
	    if(allContacts != null){
		    while(allContacts.hasMoreElements()) {
		    	m_concatList.addElement(allContacts.nextElement());
		    }
	    }
	    
	    add(m_selectShareType);
	    add(new SeparatorField());
	    add(m_concatListMgr);
	    
	    setTitle(new LabelField(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_MENU)));
	    
	    reloadConcatList();
	}
		
	private Vector m_shareConcatDataList = new Vector();
	
	public void reloadConcatList(){
		
		m_concatListMgr.deleteAll();
		m_shareConcatDataList.removeAllElements();
		
		for(int i = 0;i < m_concatList.size();i++){
			BlackBerryContact bbContact = (BlackBerryContact)m_concatList.elementAt(i);
			
			int fieldsWithData[] = bbContact.getFields();
			
			String t_name = null;
			String t_email = null;
			String t_phoneNumber = null;
			
			for(int j = 0;j< fieldsWithData.length;j++){
				
				if(fieldsWithData[j] == BlackBerryContact.NAME && bbContact.countValues(BlackBerryContact.NAME) > 0){
					
					String[] name = bbContact.getStringArray(BlackBerryContact.NAME, 0);
					
					if(name[BlackBerryContact.NAME_FAMILY] != null){
						t_name = name[BlackBerryContact.NAME_FAMILY]; 
					}
					
					if(name[BlackBerryContact.NAME_GIVEN] != null){
						if(t_name == null){
							t_name = name[BlackBerryContact.NAME_GIVEN];
						}else{
							t_name += name[BlackBerryContact.NAME_GIVEN]; 
						}
					}
					
				}else if(fieldsWithData[j] == BlackBerryContact.EMAIL){
					
					t_email = bbContact.getString(BlackBerryContact.EMAIL, 0);
					
				}else if(fieldsWithData[j] == BlackBerryContact.TEL){
					
					for(int atrCount = 0;atrCount < bbContact.countValues(BlackBerryContact.TEL); ++atrCount){
						
			            switch(bbContact.getAttributes(BlackBerryContact.TEL, atrCount)){
			            case BlackBerryContact.ATTR_MOBILE:
			            		t_phoneNumber = bbContact.getString(BlackBerryContact.TEL,atrCount);
			            		break;
			            }
					}
				}
			}
				
			
			if(t_name == null){
				continue;
			}
			
			if(t_email == null && t_phoneNumber == null){
				continue;
			}
			
			if((m_useEmail.isSelected() && t_email != null) 
			|| (m_useSMS.isSelected() && t_phoneNumber != null)){
				
				shareConcatData t_data = new shareConcatData();
				
				t_data.m_name 			= t_name;
				t_data.m_email			= t_email;
				t_data.m_phoneNumber	= t_phoneNumber;
				
				m_shareConcatDataList.addElement(t_data);	
			}
			
		}
		
		for(int i = 0;i < m_shareConcatDataList.size();i++){
			shareConcatData t_data = (shareConcatData)m_shareConcatDataList.elementAt(i);
			
			String t_label = null;
			
			if(m_useEmail.isSelected()){
				t_label = t_data.m_name + "(" + t_data.m_email + ")";
			}else{
				t_label = t_data.m_name + "(" + t_data.m_phoneNumber + ")";
			}
			
			CheckboxField t_field = new CheckboxField(t_label,false);
			m_concatListMgr.add(t_field);
		}
		
	}
	
	public void fieldChanged(Field field, int context) {
		if(((RadioButtonField)field).isSelected()){
			reloadConcatList();
		}
		
	}
	
	MainScreen	m_checkContentScreen = null;
	sendingSMSDlg m_sendingSMSDlg = null;
	
	sendSMSThread	m_sendThread = null;
	
	MenuItem 	m_checkContentMenu = new MenuItem(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_CHECK_MENU), 100, 10) {
		public void run() {
			PopupCheckContentScreen();
		}
	};
	
	protected void makeMenu(Menu _menu,int instance){
		_menu.add(m_checkContentMenu);
		super.makeMenu(_menu, instance);
    }
	
	// send the content to all select friend
	//
	final Vector m_sendConcatData = new Vector();
	
	public void PopupCheckContentScreen(){
		
		m_sendConcatData.removeAllElements();
		
		for(int i = 0 ;i < m_shareConcatDataList.size();i++){
			if(((CheckboxField)m_concatListMgr.getField(i)).getChecked()){
				m_sendConcatData.addElement(m_shareConcatDataList.elementAt(i));
			}				
		}
		
		if(m_sendConcatData.isEmpty()){
			m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_EMPTY));
			return;
		}
		
		final EditField t_text = new EditField("",m_useEmail.isSelected()?recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_CONTENT_EMAIL).replace('#','\n')
																:recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_CONTENT_SMS).replace('#','\n'),
												m_useEmail.isSelected()?256:70,
												EditField.FILTER_DEFAULT);
		
		m_checkContentScreen = new MainScreen(){
			

			MenuItem 	m_sendMenu = new MenuItem(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_SEND_MENU), 101, 10) {
				public void run() {
					Send(t_text.getText());
				}
			};
		
			public boolean onClose(){
				m_checkContentScreen = null;				
				close();
				return true;
			}
			
			protected void makeMenu(Menu _menu,int instance){
				_menu.add(m_sendMenu);
				super.makeMenu(_menu, instance);
		    }
		};
	
		
		m_checkContentScreen.add(t_text);
		m_checkContentScreen.setTitle(new LabelField(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_CONTENT_TITLE)));
		m_mainApp.pushScreen(m_checkContentScreen);
	}
	
	public void Send(final String _content){
		if(_content.length() != 0){
						
			if(m_useEmail.isSelected()){
				
				fetchMail t_email = new fetchMail();
				
				t_email.SetSubject(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_CONTENT_EMAIL_SUBJECT));
				t_email.SetContain(_content);
				
				for(int i = 0;i<m_sendConcatData.size();i++){
					shareConcatData t_data = (shareConcatData)m_sendConcatData.elementAt(i);
					
					t_email.GetSendToVect().addElement(t_data.m_email);
				}
				
				try{
					m_mainApp.m_connectDeamon.AddSendingMail(t_email,null,null,fetchMail.NOTHING_STYLE);

					m_mainApp.popScreen(m_checkContentScreen);
					m_checkContentScreen = null;
					
					m_mainApp.popScreen(this);
					m_mainApp.m_shareScreen = null;
					
					m_mainApp.DialogAlert(recvMain.sm_local.getString(localResource.SHARE_TO_FRIEND_THANKS));
					
				}catch(Exception e){
					m_mainApp.SetErrorString("SH:" + e.getMessage() + " " + e.getClass().getName());
				}			
				
			}else{
				
				m_mainApp.popScreen(m_checkContentScreen);
				m_checkContentScreen = null;
				
				m_sendThread = new sendSMSThread(m_mainApp,this,m_sendConcatData, _content);
				m_sendThread.start();	
				
				m_sendingSMSDlg = new sendingSMSDlg(m_mainApp, this);
				m_mainApp.pushScreen(m_sendingSMSDlg);
				
							
			}
			
		}
	}
	
	public boolean onClose(){
		m_mainApp.m_shareScreen = null;
		close();
		return true;
	}
}
