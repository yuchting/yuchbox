package com.yuchting.yuchberry.client.weibo;

import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.component.ActiveRichTextField;
import net.rim.device.api.util.Arrays;

public class WeiboTextField extends ActiveRichTextField{
	
	int[] m_bufferedOffset	 = new int[20];
	byte[] m_bufferedAttr	= new byte[20];
	
	int[]		m_background = 
	{
		0xffffff,
		0xffffff,
	};
	
	int[]		m_foreground = 
	{
		0,
		0x21bee7,
	};
		
	static Font[]		sm_fontList = 
	{
		WeiboItemField.sm_defaultFont,
		WeiboItemField.sm_defaultFont
	};
		
		
	public WeiboTextField(int _foreground,int _background){
		super("",Field.READONLY | Field.FOCUSABLE | SCANFLAG_THREAD_ON_CREATE);
		
		m_foreground[0] = _foreground;
		Arrays.fill(m_background,_background);
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
					
					if(t_offsetIndex >= m_bufferedOffset.length - 1){
						// relocate that
						//
						if(!incBuffered(t_textLength,(byte)0)){
							break;
						}
					}
					
					m_bufferedAttr[t_attrIndex] = 0;
					m_bufferedOffset[t_offsetIndex] = t_index;
					
					t_attrIndex++;
					t_offsetIndex++;
				}			
				
				for(int i = t_index + 1;i < t_textLength;i++,t_index++){
					
					a = _text.charAt(i);
					
					if(!WeiboUserFind.isLeagalNameCharacter(a)){
						break;
					}
				}
				
				if(t_offsetIndex >= m_bufferedOffset.length - 1){
					// relocate that
					//
					if(!incBuffered(t_textLength,(byte)0)){
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
		
		super.setText(_text,m_bufferedOffset,m_bufferedAttr,sm_fontList,m_foreground,m_background);
	}
	
	
	private boolean incBuffered(int _offsetDefaultVal,byte _attrDefaultVal){
		
		if(m_bufferedOffset.length + 5 > 128){
			return false;
		}
		
		int[] t_newOffset =  new int[m_bufferedOffset.length + 5];
		for(int i = 0;i < t_newOffset.length;i++){
			if(i < m_bufferedOffset.length){
				t_newOffset[i] = m_bufferedOffset[i];
			}else{
				t_newOffset[i] = _offsetDefaultVal;
			}
			
		}
		m_bufferedOffset = t_newOffset;
		
		byte[] t_newAttr = new byte[m_bufferedAttr.length + 5];
		for(int i = 0;i < t_newAttr.length;i++){
			if(i < m_bufferedAttr.length){
				t_newAttr[i] = m_bufferedAttr[i]; 
			}else{
				t_newAttr[i] = _attrDefaultVal;
			}
		}
		m_bufferedAttr = t_newAttr;
		
		return true;
	}
	
}
