package com.yuchting.yuchberry.yuchsign.client;


import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.Image;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;

interface InputVerfiyCode{
	void InputCode(String _code);
}

public class YuchVerifyCodeDlg extends DialogBox{
	
	public YuchVerifyCodeDlg(String _imageData,final InputVerfiyCode _input){
		super(false,true);
		
		assert _input != null;
		
		final VerticalPanel t_mainPane = new VerticalPanel();
		
		Image t_img = new Image(_imageData);
		t_mainPane.add(t_img);
		
		t_mainPane.add(new HTML("请输入对应的数字：(例如12345)"));
		
		final TextBox t_code = new TextBox();
		t_mainPane.add(t_code);
		t_mainPane.add(new HTML("<br />"));
		
		final Button t_confirmBut = new Button("确定");
		t_mainPane.add(t_confirmBut);
		
		t_confirmBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(t_code.getText().isEmpty()){
					Yuchsign.PopupPrompt("请输入验证码", t_code);
					return;
				}
				
				_input.InputCode(t_code.getText());
				
				hide();
			}
		});
		
		
		
		setWidget(t_mainPane);
		
		setText("验证码确认");
		
		setAnimationEnabled(true);
		setGlassEnabled(true);		
		setPopupPosition(20,100);
		
		show();
	}

}
