package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.ActiveRichTextField;
import net.rim.device.api.util.Arrays;

public class WeiboTextField extends ActiveRichTextField{
	
	int[] m_bufferedOffset	 = new int[20];
	byte[] m_bufferedAttr	= new byte[20];
	
	static Font[]		sm_fontList = 
	{
		WeiboItemField.sm_defaultFont,
		WeiboItemField.sm_defaultFont
	};
	
		
	public WeiboTextField(){
		super("",Field.READONLY | Field.FOCUSABLE);
	}
	
	public void setText(String _text){
		
		// if the superclass is call the override function
		// the subclass is not constructed
		//
		if(m_bufferedOffset == null){
			super.setText(_text);
			return;
		}
		
		// clear the initialized buffered state
		//
		int t_textLength = _text.length();
		Arrays.fill(m_bufferedOffset,t_textLength);
		m_bufferedOffset[0] = 0;
		
		Arrays.fill(m_bufferedAttr,(byte)0);
		
		// parse the offset and attribute and style
		//
		int t_index = 0;
		int t_offsetIndex = 1;
		int t_attrIndex = 0;
		
		while(t_index < t_textLength){
			
			char a = _text.charAt(t_index);
			
			if(a == '@'){
				
				if(t_offsetIndex != 1){
					
					m_bufferedAttr[t_attrIndex] = 0;
					
					m_bufferedOffset[t_offsetIndex] = t_index;
					
					t_attrIndex++;
					t_offsetIndex++;
				}
				
				if(t_offsetIndex >= m_bufferedOffset.length ){
					// relocate that
					//
					if(!incBuffered()){
						break;
					}
				}
				
				for(int i = t_index + 1;i < t_textLength;i++,t_index++){
					
					a = _text.charAt(i);
					
					if(!WeiboUserFind.isLeagalNameCharacter(a)){
						break;
					}
				}
				
				m_bufferedAttr[t_attrIndex] = 1;
				
				t_index++;
				m_bufferedOffset[t_offsetIndex] = t_index;
				
				t_attrIndex++;
				t_offsetIndex++;
				
			}else{
				t_index++;
			}
		}
		
		super.setText(_text,m_bufferedOffset,m_bufferedAttr,sm_fontList);
	}
	
	
	private boolean incBuffered(){
		
		if(m_bufferedOffset.length + 5 > 128){
			return false;
		}
		
		int[] t_newOffset =  new int[m_bufferedOffset.length + 5];
		m_bufferedOffset = t_newOffset;
		
		byte[] t_newAttr = new byte[m_bufferedAttr.length + 5]; 
		m_bufferedAttr = t_newAttr;
		
		return true;
	}
	
	
}
