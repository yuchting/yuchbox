package com.yuchting.yuchberry.yuchsign.client;

import java.util.Vector;

import com.google.gwt.user.client.ui.CheckBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.FlowPanel;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.ListBox;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.FlexTable.FlexCellFormatter;

class ContentTab extends TabPanel{
	
	
	public ContentTab(){
		final FlowPanel t_subPane =new FlowPanel();
		
		
	}
}

public class BberPanel extends SimplePanel{

	final Label m_signinName 		= new Label();
	final Label m_serverPort 		= new Label();	
	final Label m_pushInterval 		= new Label();
	
	final CheckBox m_usingSSL		= new CheckBox("使用SSL");
	final CheckBox m_convertToSimple = new CheckBox("转换繁体到简体");
	
	final ListBox	m_pushList	= new ListBox(true);
	
	final ContentTab m_pushContent	= new ContentTab();
	
	yuchbber m_currentBber = null;
	
	public BberPanel(){
		setStyleName("BberPanel");
		
		m_pushList.setPixelSize(200, 300);
		
		final FlowPanel t_subPane =new FlowPanel();
		
		FlexTable layout = new FlexTable();
		layout.setCellSpacing(3);
		FlexCellFormatter cellFormatter = layout.getFlexCellFormatter();
		
		// Add a title to the form
		layout.setHTML(0, 0, "[用户属性]");
		cellFormatter.setColSpan(0, 0, 1);
		cellFormatter.setHorizontalAlignment(0, 0, HasHorizontalAlignment.ALIGN_CENTER);
		
		// Add some standard form options
		layout.setHTML(1, 0, "用户名:");
		layout.setWidget(1, 1,m_signinName);
		layout.setHTML(2, 0, "端口:");
		layout.setWidget(2, 1, m_serverPort);
		
		setWidget(layout);
		
//		t_subPane.add(new Label("用户名:"));
//		t_subPane.add(m_signinName);
//		t_subPane.add(new Label("端口:"));
//		t_subPane.add(m_serverPort);
//		t_subPane.add(new Label("推送间隔:"));
//		t_subPane.add(m_pushInterval);
//		t_subPane.add(m_usingSSL);
//		t_subPane.add(m_convertToSimple);
//		t_subPane.add(new HTML("<br />"));
//		t_subPane.add(m_pushList);
//		
//		setWidget(t_subPane);
		
		RootPanel.get("mainTab").add(this);
	}
	
	public void SetYuchbberData(final yuchbber _bber){
		m_currentBber = _bber;
		
		m_signinName.setText(_bber.GetSigninName());
		m_serverPort.setText("" + _bber.GetServerPort());
		
		m_pushInterval.setText("" + _bber.GetPushInterval());
		
		m_usingSSL.setValue(_bber.IsUsingSSL());
		m_convertToSimple.setValue(_bber.IsConvertSimpleChar());
		
		while(m_pushList.getItemCount() != 0){
			m_pushList.removeItem(0);
		}
		
		Vector<yuchEmail> t_emailList = _bber.GetEmailList();
		if(t_emailList != null){

			for(yuchEmail email : t_emailList){
				m_pushList.addItem(email.toString());
			}
		}		
		
	}

}
