package com.yuchting.yuchberry.client;

import net.rim.device.api.ui.component.RichTextField;

public class textViewScreen extends fileViewScreen{

	RichTextField m_editText	= null;
	
	public textViewScreen(String _filename,recvMain _mainApp)throws Exception{
		
		super(_filename,_mainApp,true);
		
		final int t_maxTextLen = 5 * 1024;
		
		byte[] t_tmpContain = null;
		
		if(m_fileContain.length > t_maxTextLen){
			String t_appendPrompt = new String("\n\n\n remain contain... \n\n\n");
			
			t_tmpContain = new byte[t_maxTextLen];
			
			System.arraycopy(m_fileContain, 0, t_tmpContain, 0, t_maxTextLen - t_appendPrompt.length());
			System.arraycopy(t_appendPrompt.getBytes(),0,t_tmpContain,t_maxTextLen - t_appendPrompt.length(),t_appendPrompt.length());
			
		}else{
			t_tmpContain = m_fileContain;
		}
		
		m_editText = new RichTextField(new String(t_tmpContain,"GB2312"));
		add(m_editText);
		
	}

}
