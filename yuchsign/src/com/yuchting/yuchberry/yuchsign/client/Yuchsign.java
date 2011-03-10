package com.yuchting.yuchberry.yuchsign.client;

import java.util.logging.Logger;

import com.google.gwt.core.client.EntryPoint;
import com.google.gwt.core.client.GWT;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DecoratedPopupPanel;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.user.client.ui.Widget;

interface YesNoHandler{
	public void Process();
}
	
final class YesNoDialog extends DialogBox{
			
	
	YesNoHandler	m_yes		= null;
	YesNoHandler	m_no		= null;
	
	Label			m_text 		= new Label();
	
	public YesNoDialog(){
		final VerticalPanel t_vert = new VerticalPanel();
		t_vert.add(m_text);
		t_vert.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		
		final FlexTable t_butPane = new FlexTable();
		t_butPane.setCellSpacing(20);
		
		final Button 	t_yesBut 	= new Button("确定");
		final Button 	t_noBut 	= new Button("取消");
		
		t_yesBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(m_yes != null){
					m_yes.Process();
				}
				
				setModal(false);
				hide();
			}
		});
		
		t_noBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(m_no != null){
					m_no.Process();
					
				}	
				setModal(false);
				hide();
			}
		});
		
		t_butPane.setWidget(0, 0, t_yesBut);
		t_butPane.setWidget(0, 1, t_noBut);
		
		t_vert.add(t_butPane);
		
		setWidget(t_vert);
		setAnimationEnabled(true);
		setGlassEnabled(true);
		setPopupPosition(100, 300);
	}
	
	public void Popup(String _prompt,YesNoHandler _yes,YesNoHandler _no){
		setText("确定？");
		m_text.setText(_prompt);
		m_yes 	= _yes;
		m_no	= _no;
		
		show();
		setModal(true);
	}		
	
}


/**
 * Entry point classes define <code>onModuleLoad()</code>.
 */
public class Yuchsign implements EntryPoint {
		  
	/**
	 * The message displayed to the user when the server cannot be reached or
	 * returns an error.
	 */
	private static final String SERVER_ERROR = "An error occurred while "
			+ "attempting to contact the server. Please check your network "
			+ "connection and try again.";

	/**
	 * Create a remote service proxy to talk to the server-side Greeting service.
	 */
	public final GreetingServiceAsync greetingService = GWT.create(GreetingService.class);
	
	/**
	 * client logger
	 */
	private final static Logger fsm_logger = Logger.getLogger("ClientLogger");
	
	private LogonDialog m_logonDlg = null;
	
	private BberPanel	m_bberPane = null;
	
	static private final DecoratedPopupPanel fsm_simplePopup = new DecoratedPopupPanel(true);
	
	static 
	{
		fsm_simplePopup.setWidth("180px");
	}
	
	
	
	static YesNoDialog		sm_yesNoDlg = null;
	
	/**
	 * This is the entry point method.
	 */
	public void onModuleLoad() {
		
		
		m_logonDlg = new LogonDialog(this);
		m_logonDlg.show();
		m_logonDlg.setModal(true);
		
//		yuchbber t_bber = new yuchbber();
//		t_bber.SetSigninName("yuchting@gmail.com");
//		String t_string = (t_bber).OuputXMLData();
//		try{
//			ShowYuchbberPanel(t_string);
//		}catch(Exception e){
//			
//		}
		
		
		
//		final Button sendButton = new Button("Send");
//		final TextBox nameField = new TextBox();
//		nameField.setText("GWT User");
//		final Label errorLabel = new Label();
//
//		// We can add style names to widgets
//		sendButton.addStyleName("sendButton");
//
//		// Add the nameField and sendButton to the RootPanel
//		// Use RootPanel.get() to get the entire body element
//		RootPanel.get("nameFieldContainer").add(nameField);
//		RootPanel.get("sendButtonContainer").add(sendButton);
//		RootPanel.get("errorLabelContainer").add(errorLabel);
//
//		// Focus the cursor on the name field when the app loads
//		nameField.setFocus(true);
//		nameField.selectAll();
//
//		// Create the popup dialog box
//		final DialogBox dialogBox = new DialogBox();
//		dialogBox.setText("Remote Procedure Call");
//		dialogBox.setAnimationEnabled(true);
//		final Button closeButton = new Button("Close");
//		// We can set the id of a widget by accessing its Element
//		closeButton.getElement().setId("closeButton");
//		final Label textToServerLabel = new Label();
//		final HTML serverResponseLabel = new HTML();
//		VerticalPanel dialogVPanel = new VerticalPanel();
//		dialogVPanel.addStyleName("dialogVPanel");
//		dialogVPanel.add(new HTML("<b>Sending name to the server:</b>"));
//		dialogVPanel.add(textToServerLabel);
//		dialogVPanel.add(new HTML("<br><b>Server replies:</b>"));
//		dialogVPanel.add(serverResponseLabel);
//		dialogVPanel.setHorizontalAlignment(VerticalPanel.ALIGN_RIGHT);
//		dialogVPanel.add(closeButton);
//		dialogBox.setWidget(dialogVPanel);
//
//		// Add a handler to close the DialogBox
//		closeButton.addClickHandler(new ClickHandler() {
//			public void onClick(ClickEvent event) {
//				dialogBox.hide();
//				sendButton.setEnabled(true);
//				sendButton.setFocus(true);
//			}
//		});
//
//		// Create a handler for the sendButton and nameField
//		class MyHandler implements ClickHandler, KeyUpHandler {
//			/**
//			 * Fired when the user clicks on the sendButton.
//			 */
//			public void onClick(ClickEvent event) {
//				sendNameToServer();
//			}
//
//			/**
//			 * Fired when the user types in the nameField.
//			 */
//			public void onKeyUp(KeyUpEvent event) {
//				if (event.getNativeKeyCode() == KeyCodes.KEY_ENTER) {
//					sendNameToServer();
//				}
//			}
//
//			/**
//			 * Send the name from the nameField to the server and wait for a response.
//			 */
//			private void sendNameToServer() {
//				// First, we validate the input.
//				errorLabel.setText("");
//				String textToServer = nameField.getText();
//				if (!FieldVerifier.isValidName(textToServer)) {
//					errorLabel.setText("Please enter at least four characters");
//					return;
//				}
//
//				// Then, we send the input to the server.
//				sendButton.setEnabled(false);
//				textToServerLabel.setText(textToServer);
//				serverResponseLabel.setText("");
//				greetingService.greetServer(textToServer,
//						new AsyncCallback<String>() {
//							public void onFailure(Throwable caught) {
//								// Show the RPC error message to the user
//								dialogBox
//										.setText("Remote Procedure Call - Failure");
//								serverResponseLabel
//										.addStyleName("serverResponseLabelError");
//								serverResponseLabel.setHTML(SERVER_ERROR);
//								dialogBox.center();
//								closeButton.setFocus(true);
//							}
//
//							public void onSuccess(String result) {
//								dialogBox.setText("Remote Procedure Call");
//								serverResponseLabel
//										.removeStyleName("serverResponseLabelError");
//								serverResponseLabel.setHTML(result);
//								dialogBox.center();
//								closeButton.setFocus(true);
//							}
//						});
//			}
//		}
//
//		// Add a handler to send the name to the server
//		MyHandler handler = new MyHandler();
//		sendButton.addClickHandler(handler);
//		nameField.addKeyUpHandler(handler);
		
		
		
//		// Create a tab panel with three tabs, each of which displays a different
//	    // piece of text.
//	    TabPanel tp = new TabPanel();
//	    tp.add(new HTML("Foo"), "foo");
//	    tp.add(new HTML("Bar"), "bar");
//	    tp.add(new HTML("Baz"), "baz");
//
//	    // Show the 'bar' tab initially.
//	    tp.selectTab(1);
//
//	    // Add it to the root panel.
//	    RootPanel.get("mainTab").add(tp);
				
	}
	
	public void ShowYuchbberPanel(String _bberXMLData)throws Exception{
		yuchbber t_bber = new yuchbber();
		t_bber.InputXMLData(_bberXMLData);		
		
		m_bberPane = new BberPanel();
		m_bberPane.SetYuchbberData(t_bber);
	}
	
	static void PopupPrompt(String _prompt,Widget _attachWidget){

		fsm_simplePopup.setWidget(new HTML(_prompt));
		fsm_simplePopup.setWidth("" + (_prompt.length() + 2) + "em");
		fsm_simplePopup.setHeight("2em");
		
		int left;
		int top;
		
		if(_attachWidget == null){
			left = 100;
	        top = 300;
		}else{
			left = _attachWidget.getAbsoluteLeft() + (_attachWidget.getOffsetWidth() - fsm_simplePopup.getOffsetWidth()) / 2;
	        top = _attachWidget.getAbsoluteTop() + (_attachWidget.getOffsetHeight() - fsm_simplePopup.getOffsetHeight()) / 2;	
		}
		
        fsm_simplePopup.setPopupPosition(left, top);

        // Show the popup
        fsm_simplePopup.show();
	}
	
	static void PopupYesNoDlg(String _prompt , YesNoHandler _yes,YesNoHandler _no){
		if(sm_yesNoDlg == null){
			sm_yesNoDlg = new YesNoDialog();
		}
		
		sm_yesNoDlg.Popup(_prompt, _yes, _no);
	}
	
	
	
	
}
