package com.yuchting.yuchberry.yuchsign.client.account;

import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.DialogBox;
import com.google.gwt.user.client.ui.HTML;
import com.google.gwt.user.client.ui.HorizontalPanel;
import com.google.gwt.user.client.ui.TextArea;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.yuchting.yuchberry.yuchsign.client.Yuchsign;
import com.yuchting.yuchberry.yuchsign.shared.FieldVerifier;

public class SendInviteDlg extends DialogBox{

	final static int		fsm_maxInviteContain = 500;
	
	yuchbber m_currbber = null;
	
	Yuchsign	m_yuchsign	= null;
		
	public SendInviteDlg(yuchbber _bber,Yuchsign _sign){
		super(false,true);
		m_currbber = _bber;
		m_yuchsign = _sign;
		
		VerticalPanel t_mainPane = new VerticalPanel();
		
		t_mainPane.add(new HTML("发送语盒账户的邀请码给你的好友，邀请好友注册语盒账户，" +
				"<br />在你的好友账户激活时，两人都会获得"+FieldVerifier.fsm_inviteDays+"天的使用时间哦，赶紧邀请吧！<br />" +
				"详情请参见<a href=\"http://code.google.com/p/yuchberry/wiki/Invite_Mechanism\" target=_blank>这里</a><br />" +
				"<br /> 好友邮箱(一行一个)：<br />"));
		
		final TextArea t_inviteContain		= new TextArea();
		t_inviteContain.setText("我是 "+ _bber.GetSigninName() +" ，正在使用语盒，你也来试试吧，推送邮件、微博，速度非常快呢。");
		t_inviteContain.setSize("300px", "80px");
		
		final TextArea t_emailList		= new TextArea();
		
		t_emailList.setSize("300px", "150px");
		t_mainPane.add(t_emailList);
		t_mainPane.add(new HTML("<br />说上两句话吧（最多"+fsm_maxInviteContain+"个字）：<br />"));
		t_mainPane.add(t_inviteContain);
		t_mainPane.add(new HTML("<br />也可以使用其他方式将自己的邀请码给你的好友。<br />"));
				
		HorizontalPanel t_buttonPane = new HorizontalPanel();
		t_buttonPane.setSpacing(10);
		Button t_confirm = new Button("发送");
		Button t_cancel = new Button("取消");
		
		t_confirm.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				String t_list = t_emailList.getText();
				if(t_list.isEmpty()){
					Yuchsign.PopupPrompt("请输入想要邀请的朋友的邮件地址，一行一个", t_emailList);
					return ;
				}
				String[] t_emList = t_list.split("\n");
				for(String email:t_emList){
					if(!LoginPanel.IsValidEmail(email)){
						Yuchsign.PopupPrompt("请输入合法邮件地址，一行一个", t_emailList);
						return ;
					}
				}
				try{

					Yuchsign.PopupWaiting("正在请求发送……", SendInviteDlg.this);
					m_yuchsign.greetingService.sendInviteMail(m_currbber.GetSigninName(),t_list,t_inviteContain.getText(),
					new AsyncCallback<String>() {
						
						@Override
						public void onSuccess(String result) {
			
							hide();							

							Yuchsign.HideWaiting();
							Yuchsign.PopupPrompt("发送邀请邮件成功！",SendInviteDlg.this);
						}
						
						@Override
						public void onFailure(Throwable caught) {
							Yuchsign.HideWaiting();
							Yuchsign.PopupPrompt("失败：" + caught.getMessage(),SendInviteDlg.this);
						}
					});	
				}catch(Exception e){
					Yuchsign.HideWaiting();
					Yuchsign.PopupPrompt("异常：" + e.getMessage(),SendInviteDlg.this);
				}				
			}
		});
		
		t_cancel.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				hide();				
			}
		});
		
		t_buttonPane.add(t_confirm);
		t_buttonPane.add(t_cancel);
		
		t_mainPane.add(t_buttonPane);
		
		setText("发送邀请邮件");
		
		setWidget(t_mainPane);
		
		setAnimationEnabled(true);
		setGlassEnabled(true);		
		setPopupPosition(380,160);
		
		show();
	}
}
