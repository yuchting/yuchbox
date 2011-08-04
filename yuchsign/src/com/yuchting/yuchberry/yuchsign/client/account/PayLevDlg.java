package com.yuchting.yuchberry.yuchsign.client.account;

import java.util.Date;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.http.client.URL;
import com.google.gwt.i18n.client.DateTimeFormat;
import com.google.gwt.user.client.Window;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.Label;
import com.google.gwt.user.client.ui.RadioButton;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.yuchting.yuchberry.yuchsign.client.Yuchsign;

public class PayLevDlg extends DialogBox{

	public PayLevDlg(final Yuchsign _mainSign,final yuchbber _bber){
		super(false,true);
		
		final Label 			t_expiredTime = new Label();
		
		final VerticalPanel t_pane = new VerticalPanel();
		t_pane.add(new HTML(PayTimeDlg.fsm_payPrompt));
		
		final RadioButton[] t_levBut = 
		{
			null,
			null,
			null,
			null,
		};
		
		final int[] t_levMoney = 
		{
			0,0,0,0
		};
		
		final Button t_confirm	= new Button("提交订单");
		final Button t_cancel	= new Button("取消");
		
		int t_startMoney = 0;
				
		for(int i = _bber.GetLevel() + 1;i < yuchbber.fsm_levelMoney.length ;i++){
			t_levBut[i] = new RadioButton("payLev","￥" + (t_startMoney + yuchbber.fsm_levelMoney[i]) + " VIP" + i + " （一次性费用）");
			t_startMoney += yuchbber.fsm_levelMoney[i];
			
			t_levMoney[i] = t_startMoney;
			
			t_pane.add(t_levBut[i]);

			t_levBut[i].addClickHandler(new ClickHandler(){
				
				@Override
				public void onClick(ClickEvent event) {
					final long t_currTime = (new Date()).getTime();
					final long t_expire = _bber.GetCreateTime() + _bber.GetUsingHours() * 3600000;
					
					if(_bber.GetCreateTime() != 0 && t_expire > t_currTime){
						for(int i = 0;i < t_levBut.length;i++){
							if(t_levBut[i] == event.getSource()){
								
								long t_remainTime = t_expire - t_currTime;
								t_remainTime = yuchbber.fsm_weekMoney[_bber.GetLevel()] * t_remainTime / yuchbber.fsm_weekMoney[i];
								
								t_remainTime = t_currTime + t_remainTime;
								
								t_expiredTime.setText("升级后到期时间："+ 
													DateTimeFormat.getFormat("yyyy-MM-dd HH:mm").format(new Date(t_remainTime)) + 
													"（如果本机时间不准，显示就会有偏差，服务器会重新计算）");	
						
								break;
							}						
						}
					}
				}
			});
			
			if(i == _bber.GetLevel() + 1){
				t_levBut[i].fireEvent(new ClickEvent(){});
				t_levBut[i].setValue(true);
			}
			
		}
		t_pane.add(new HTML("<br />"));		

		t_confirm.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				
				int t_fee = 0;
				for(int j = 0;j < t_levBut.length;j++){
					if(t_levBut[j] != null && t_levBut[j].getValue()){
						t_fee = t_levMoney[j];
						break;
					}
				}

				try{
					Window.open("http://yuchberrysign.yuchberry.info/pay/?yname=" + 
								URL.encode(_bber.GetSigninName()) + "&type=1&fee=" + t_fee,"_blank","");
				}catch(Exception e){
					Yuchsign.PopupPrompt("错误：" + e.getMessage(), t_pane);
				}
			}
		});
		
		t_cancel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();
			}
		});
		
		final HorizontalPanel t_butPane = new HorizontalPanel();
		t_butPane.setSpacing(20);
		
		t_butPane.add(t_confirm);
		t_butPane.add(t_cancel);
		
		
		t_pane.add(t_expiredTime);
		t_pane.add(t_butPane);
		
		setWidget(t_pane);
		
		setText("升级用户等级");
		setAnimationEnabled(true);
		setGlassEnabled(true);		
		setPopupPosition(200,100);
	}
}