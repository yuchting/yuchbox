package com.yuchting.yuchberry.yuchsign.client;

import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.SimplePanel;

public class BberPanel extends SimplePanel{

	public BberPanel(){
		setStyleName("BberPanel");
		
		RootPanel.get("mainTab").add(this);
	}

}
