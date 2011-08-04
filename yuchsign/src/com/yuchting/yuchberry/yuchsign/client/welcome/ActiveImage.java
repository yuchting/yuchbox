package com.yuchting.yuchberry.yuchsign.client.welcome;


import java.util.Vector;

import com.google.gwt.dom.client.Element;
import com.google.gwt.dom.client.NodeList;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Timer;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.PushButton;
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
			new ToggleButton("登录|注册"),
			new ToggleButton("语盒FAQ"),
			new ToggleButton("玩转语盒"),
		};
		
		final RootPanel[] t_rootPanel = 
		{
			RootPanel.get("welcome"),
			RootPanel.get("account"),
			RootPanel.get("faq"),
			RootPanel.get("play"),
		};		

		ClickHandler t_handler = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				for(int i = 0;i < t_controlPutton.length;i++){
					ToggleButton but = t_controlPutton[i];
					
					if(but != event.getSource()){
						but.setValue(false,false);
						t_rootPanel[i].setVisible(false);
					}else{
						t_rootPanel[i].setVisible(true);
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
		
		initPlaybook();				
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
	
	int						m_pageIndex = 0;
	Vector<Element>			m_playPages = new Vector<Element>();
	
	private void initPlaybook(){
		
		RootPanel t_book = RootPanel.get("play");
		
		// find the page number
		//
		Element t_elm = t_book.getElement();
		NodeList<Element> t_nodeList = t_elm.getElementsByTagName("div");
		
		for(int i = 0;i <t_nodeList.getLength();i++){
			Element elm = t_nodeList.getItem(i);
			if(elm.getClassName().equals("page")){
				m_playPages.add(elm);
			}
		}
		
		
		HorizontalPanel t_pageButPane = new HorizontalPanel();
		t_pageButPane.setStyleName("pageButton");
		t_pageButPane.setSpacing(12);
		
		PushButton t_pageDown = new PushButton(new Image("./images/pagebut.gif",0,0,34,33),
												new Image("./images/pagebut.gif",0,33,34,33));
		
		PushButton t_pageUp = new PushButton(new Image("./images/pagebut.gif",0,66,34,33),
											new Image("./images/pagebut.gif",0,99,34,33));
				
		t_pageButPane.add(t_pageUp);
		t_pageButPane.add(t_pageDown);
		
		t_book.add(t_pageButPane);
		
		t_pageDown.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(m_pageIndex + 1 < m_playPages.size()){
					m_pageIndex++;
					changePlayPages();
				}
			}
		});
		
		t_pageUp.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(m_pageIndex - 1 >= 0){
					m_pageIndex--;
					changePlayPages();
				}				
			}
		});
	}
	
	Timer		m_pageTimer = null;
	float		m_pageOpacityCounter = 0;
	private synchronized void changePlayPages(){
		
		for(Element elm:m_playPages){
			elm.getStyle().setProperty("display", "none");
			elm.getStyle().setProperty("opacity", "0");
		}
		
		
		m_playPages.get(m_pageIndex).getStyle().setProperty("display", "");
		
		if(m_pageTimer != null){
			return;
		}
			
		m_pageOpacityCounter = 0;
		
		m_pageTimer = new Timer(){
			@Override
	    	public void run() {
				
				synchronized (this) {
					
					m_pageOpacityCounter += 0.1f;
								
					if(m_pageOpacityCounter > 1.0f){
						m_pageOpacityCounter = 1.0f;
										
						m_pageTimer.cancel();
						m_pageTimer = null;
					}			
					
					m_playPages.get(m_pageIndex).getStyle().setProperty("opacity", Float.toString(m_pageOpacityCounter));
				}
	    	}
		};
		
		m_pageTimer.scheduleRepeating(30);
		
		
	}
}
