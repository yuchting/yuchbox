package com.yuchting.yuchberry.yuchsign.client.welcome;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.ToggleButton;

public class ActiveImage {
	
	HTML	m_actImage = new HTML();
	
	boolean	m_changed = false;
	Timer		m_changeTimer = null;
	
	static final String[] fsm_changeImageURL = 
	{
		"./images/actimage0.jpg",
		"./images/actimage1.jpg",
	};
	
	int			m_changeIndex = 0;
	
	public ActiveImage(){
		RootPanel.get("change").add(m_actImage);
							
		HorizontalPanel t_butpane = new HorizontalPanel();		
		
		final ToggleButton[] t_controlPutton = 
		{
			new ToggleButton("欢迎"),
			new ToggleButton("登录|注册")
		};
		
		

		ClickHandler t_handler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				for(int i = 0;i < t_controlPutton.length;i++){
					ToggleButton but = t_controlPutton[i];
					
					if(but != event.getSource()){
						but.setValue(false,false);
					}else{
						if(i == 1){
							RootPanel.get("welcome").setVisible(false);
							RootPanel.get("account").setVisible(true);						
						}else{
							RootPanel.get("welcome").setVisible(true);
							RootPanel.get("account").setVisible(false);
						}						
					}					
				}				
			}
		};
		
		for(ToggleButton but:t_controlPutton){
			but.setStyleName("yb-ToggleButton");
			but.addClickHandler(t_handler);
			t_butpane.add(but);
		}
		
		t_controlPutton[0].setValue(true,false);
		
		RootPanel.get("navBut").add(t_butpane);
		
	}
	
	public void enableChange(boolean _change){
		
		if(m_changed != _change){
			m_changed = _change;
			
			if(m_changed){
				
				if(m_changeTimer != null){
					m_changeTimer.cancel();
				}
				
				m_changeTimer = new Timer() {
			    	@Override
			    	public void run() {
			    		m_actImage.setHTML("<img src=\"" + fsm_changeImageURL[m_changeIndex] + "\" />");
			    		m_changeIndex++;
			    		if(m_changeIndex >= fsm_changeImageURL.length){
			    			m_changeIndex = 0;
			    		}
			    	}
			    };
			    
			    m_changeTimer.scheduleRepeating(5*1000);
			    
			}else{
				
				if(m_changeTimer != null){
					m_changeTimer.cancel();
					m_changeTimer = null;
				}
				
			}
		}
	}
}
