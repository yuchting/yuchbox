package com.yuchting.yuchberry.yuchsign.client;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;

public class PayTimeDlg extends DialogBox{

	public final static String	fsm_payPrompt = "温馨提示：<br />" +
													"请确认您在免费使用阶段里面，能正常使用。因为某些原因，不是所有的移动设备都能正常使用YuchBerry的服务。<br />" +
													"<br /><b>YuchBerry不会因为推送帐户增加，而改变服务质量。</b><br />" +
													"<br />如有疑问请访问<a href=\"http://code.google.com/p/yuchberry/wiki/Yuchsign_Using_Intro#账户充值时间、等级\" target=_blank>这里</a>，" +
													"或者发送邮件到<a href=\"mailto:yuchberry@gmail.com\">yuchberry@gmail.com</a>，或者联系<a href=\"http://t.sina.com.cn/1894359415\" target=_blank>YuchBerry新浪WeiBo</a><br />" +
													"<br />VIP0  (推送一个账户)：￥" + yuchbber.fsm_weekMoney[0] +"/星期" +
													"<br />VIP1  (推送两个账户)：￥" + yuchbber.fsm_weekMoney[1] +"/星期" +
													"<br />VIP2  (推送三个账户)：￥" + yuchbber.fsm_weekMoney[2] +"/星期" +
													"<br />VIP3  (推送四个账户)：￥" + yuchbber.fsm_weekMoney[3] +"/星期<br /><br />";
													
	String				m_buyURL	= null;
	
		
	public PayTimeDlg(final Yuchsign _mainSign,final yuchbber _bber){
		super(false,true);
			
		final int t_weekMoney = yuchbber.fsm_weekMoney[_bber.GetLevel()];				

		final VerticalPanel t_pane = new VerticalPanel();
		t_pane.add(new HTML(fsm_payPrompt));
		
		final Label 			t_expiredTime = new Label();
		
		final RadioButton		t_weekPay	= new RadioButton("pay","￥" + t_weekMoney + "/一个星期");
		final RadioButton		t_monthPay	= new RadioButton("pay","￥" + (t_weekMoney*4) + "/一个月");
		
		t_pane.add(t_weekPay);
		t_pane.add(t_monthPay);
		
		t_pane.add(new HTML("<br />"));
		
		t_pane.add(t_expiredTime);
		
		t_weekPay.setValue(true);
		
		ClickHandler t_radioClicked = new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				long t_payTime 				= (7 * 24 * 3600000);
				if(t_monthPay.getValue()){
					t_payTime *= 4;
					
					// give two days
					//
					t_payTime += (2 * 24 * 3600000);
				}
				
				long t_currTime 			= (new Date()).getTime();
				long t_formerExpiredTime	= _bber.GetCreateTime() + _bber.GetUsingHours() * 3600000;
				
				Date t_expireDate = new Date(Math.max(t_currTime,t_formerExpiredTime) + t_payTime);
				t_expiredTime.setText("充值后到期时间："+ DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(t_expireDate));				
			}
		};
		
		t_weekPay.addClickHandler(t_radioClicked);
		t_monthPay.addClickHandler(t_radioClicked);
		
		t_weekPay.fireEvent(new ClickEvent(){});
		
		final Button t_confirm = new Button("提交订单");
		final Button t_cancel = new Button("取消");
		
		final int ft_payWeekFee = t_weekMoney;
		
		t_confirm.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				if(m_buyURL != null){
					
					hide();
					m_buyURL = URL.encode(m_buyURL);
					Window.open(m_buyURL,"_blank","");
					
				}else{
					
					Yuchsign.PopupWaiting("正在提交订单...", t_pane);
					
					try{
						_mainSign.greetingService.payTime(_bber.GetSigninName(),0,
														(t_weekPay.getValue())?ft_payWeekFee:ft_payWeekFee*4,
						new AsyncCallback<String>() {
							
							@Override
							public void onSuccess(String result) {
								if(result.startsWith("http")){
									m_buyURL = result;
									t_confirm.setText("去支付宝付费");
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
		t_buttonPane.setSpacing(20);
		
		t_buttonPane.add(t_confirm);
		t_buttonPane.add(t_cancel);
		
		t_pane.add(t_buttonPane);
		
		setWidget(t_pane);
		
		setText("充值时间");
		setAnimationEnabled(true);
		setGlassEnabled(true);		
		setPopupPosition(30,100);
	}	
}
