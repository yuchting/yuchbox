package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.VerticalPanel;
	
public class YesNoDialog extends DialogBox{
			
	
	YesNoHandler	m_yes		= null;
	YesNoHandler	m_no		= null;
	
	Label			m_text 		= new Label();
	
	public YesNoDialog(){
		super(false,false);
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
		setPopupPosition(300, 300);
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