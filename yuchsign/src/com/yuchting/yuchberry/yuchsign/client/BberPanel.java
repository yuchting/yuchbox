package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;

class ContentTab extends TabPanel{
	
	
	public ContentTab(){
		
	}
}

public class BberPanel extends SimplePanel{

	final Label m_signinName 		= new Label();
	final Label m_serverPort 		= new Label();	
	final Label m_pushInterval 		= new Label();
	
	final CheckBox m_usingSSL		= new CheckBox("使用SSL");
	final CheckBox m_convertToSimple = new CheckBox("转换繁体到简体");
	
	final ListBox	m_pushList	= new ListBox();
	
	final ContentTab m_pushContent	= new ContentTab();
	
	public BberPanel(){
		setStyleName("BberPanel");
		
		m_pushList.setPixelSize(100, 300);
		
		final FlowPanel t_subPane =new FlowPanel();
		
		t_subPane.add(new HTML("用户名:"));
		t_subPane.add(m_signinName);
		t_subPane.add(new HTML("<br />端口:"));
		t_subPane.add(m_serverPort);
		t_subPane.add(new HTML("<br />推送间隔:"));
		t_subPane.add(m_pushInterval);
		t_subPane.add(new HTML("<br />"));
		t_subPane.add(m_usingSSL);
		t_subPane.add(new HTML("<br />"));
		t_subPane.add(m_convertToSimple);
		t_subPane.add(new HTML("<br />"));
		t_subPane.add(m_pushList);
		
		RootPanel.get("mainTab").add(this);
	}

}
