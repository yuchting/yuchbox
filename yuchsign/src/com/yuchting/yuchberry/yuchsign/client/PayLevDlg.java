package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PayLevDlg extends DialogBox{

	String				m_buyURL	= null;
	
	public PayLevDlg(final Yuchsign _mainSign,final yuchbber _bber){
		super(false,true);
		
		final Label 			t_expiredTime = new Label();
		
		final VerticalPanel t_pane = new VerticalPanel();
		t_pane.add(new HTML(PayTimeDlg.fsm_payPrompt));
		
		final RadioButton[] t_levBut = 
		{
			new RadioButton("payLev","￥" + yuchbber.fsm_levelMoney[1] + " VIP1"),
			new RadioButton("payLev","￥" + yuchbber.fsm_levelMoney[2] + " VIP2"),
			new RadioButton("payLev","￥" + yuchbber.fsm_levelMoney[3] + " VIP3"),
		};
		
		final Button t_confirm	= new Button("生成订单");
		final Button t_cancel	= new Button("取消");
		
		int t_levButIndex = 0;
		for(int i = _bber.GetLevel() + 1;i < yuchbber.fsm_levelMoney.length ;i++,t_levButIndex++){
			t_pane.add(t_levBut[t_levButIndex]);

			t_levBut[t_levButIndex].addClickHandler(new ClickHandler() {
				
				@Override
				public void onClick(ClickEvent event) {
					
					if(m_buyURL != null){
						
						hide();
						m_buyURL = URL.encode(m_buyURL);
						Window.open(m_buyURL,"_blank","");
						
					}else{
						int t_fee = 0;
						for(int i = 0;i < t_levBut.length;i++){
							if(t_levBut[i].getValue()){
								t_fee = yuchbber.fsm_levelMoney[i+1];
								break;
							}
						}
						
						Yuchsign.PopupWaiting("正在提交订单...", t_pane);
						
						try{
							_mainSign.greetingService.payTime(_bber.GetSigninName(),1,t_fee,
							new AsyncCallback<String>() {
								
								@Override
								public void onSuccess(String result) {
									if(result.startsWith("http")){
										m_buyURL = result;
										t_confirm.setText("去支付宝付费");
										Yuchsign.PopupPrompt("提交订单成功！\n点击 去付费 按钮使用支付宝充值。", t_pane);
										Yuchsign.HideWaiting();
										
										for(RadioButton btn :t_levBut){
											btn.setEnabled(false);
										}
										
									}else{
										Yuchsign.HideWaiting();
										Yuchsign.PopupPrompt("错误：" + result, t_pane);
									}							
								}
								
								@Override
								public void onFailure(Throwable caught) {
									Yuchsign.HideWaiting();
									Yuchsign.PopupPrompt("错误：" + caught.getMessage(), t_pane);
									
								}
							});
						}catch(Exception e){
							Yuchsign.HideWaiting();
							Yuchsign.PopupPrompt("错误：" + e.getMessage(), t_pane);
						}	
					}
				}
			});
			
			if(t_levButIndex == 0){
				t_levBut[t_levButIndex].fireEvent(new ClickEvent(){});
			}
		}		
		
		final HorizontalPanel t_butPane = new HorizontalPanel();
		
		t_cancel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		t_butPane.add(t_confirm);
		t_butPane.add(t_cancel);
		
		t_pane.add(t_expiredTime);
		t_pane.add(t_butPane);
		
		setWidget(t_pane);
		
		setText("升级用户等级");
		setAnimationEnabled(true);
		setGlassEnabled(true);		
		setPopupPosition(30,100);
	}
}