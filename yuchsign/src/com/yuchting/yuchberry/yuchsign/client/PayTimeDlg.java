package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HasHorizontalAlignment;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PayTimeDlg extends DialogBox{

	Yuchsign			m_mainSign	= null;
	yuchbber			m_bber 		= null;
	
	String				m_buyURL	= null;
	public PayTimeDlg(final Yuchsign _mainSign,final yuchbber _bber){
		super(false,true);
		
		m_mainSign = _mainSign;
		m_bber = _bber;
		
		int t_weekMoney = 2;
		switch(_bber.GetLevel()){
		case 1: t_weekMoney = 3;
		case 2: t_weekMoney = 4;
		case 3: t_weekMoney = 5;
		}		

		final RadioButton		t_weekPay	= new RadioButton("pay","￥" + t_weekMoney + "/一个星期");
		final RadioButton		t_monthPay	= new RadioButton("pay","￥" + (t_weekMoney*4) + "/一个月");
		
		final VerticalPanel t_pane = new VerticalPanel();
		t_pane.add(new HTML("提示：请确认您在一个星期内的免费使用阶段里面，" +
				"能正常使用。因为某些原因，不是所有的移动设备都能" +
				"正常使用YuchBerry的服务。YuchBerry不会因为提升用户级别而改变服务质量。" +
				"<br /><br />普通用户(推送一个账户)：￥2/星期" +
				"<br />VIP1  (推送两个账户)：￥3/星期" +
				"<br />VIP2  (推送三个账户)：￥4/星期" +
				"<br />VIP3  (推送四个账户)：￥5/星期"));

		
		t_weekPay.setValue(true);
		
		t_pane.add(new HTML("<br />"));
		t_pane.add(t_weekPay);
		
		t_pane.add(new HTML("<br />"));
		t_pane.add(t_monthPay);
		
		t_weekPay.setValue(true);
		
		final Button t_confirm = new Button("确认充值");
		final Button t_cancel = new Button("取消");
		
		final int ft_payWeekFee = t_weekMoney;
		
		t_confirm.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(m_buyURL != null){
					
					hide();
					Window.open(m_buyURL,"_blank","");
					
				}else{
					
					int t_type = t_weekPay.getValue()?0:1;
					
					Yuchsign.PopupWaiting("正在提交订单...", t_pane);
					
					try{
						m_mainSign.greetingService.payTime(_bber.GetSigninName(),t_type,
														(t_type== 0)?ft_payWeekFee:ft_payWeekFee*4,
						new AsyncCallback<String>() {
							
							@Override
							public void onSuccess(String result) {
								if(result.startsWith("http")){
									m_buyURL = result;
									t_confirm.setText("去付费");
									Yuchsign.PopupPrompt("提交订单成功！\n点击 去付费 按钮使用支付宝充值。", t_pane);
									Yuchsign.HideWaiting();
									
									t_weekPay.setEnabled(false);
									t_monthPay.setEnabled(false);
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
		
		t_cancel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		final HorizontalPanel t_buttonPane = new HorizontalPanel();
		t_buttonPane.setHorizontalAlignment(HasHorizontalAlignment.ALIGN_CENTER);
		t_buttonPane.setSpacing(30);
		
		t_buttonPane.add(t_confirm);
		t_buttonPane.add(t_cancel);
		
		t_pane.add(t_buttonPane);
		
		setWidget(t_pane);
		
		setText("充值时间");
		setAnimationEnabled(true);
		setGlassEnabled(true);		
		setPopupPosition(30,100);
		setSize("400px","300px");
	}	
}
