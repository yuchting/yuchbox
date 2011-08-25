package com.yuchting.yuchberry.client.weibo;

import java.util.Vector;

import net.rim.device.api.ui.DrawTextParam;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ActiveRichTextField;
import net.rim.device.api.util.Arrays;

public class WeiboTextField extends ActiveRichTextField{
	
	int[] m_bufferedOffset	 = new int[20];
	byte[] m_bufferedAttr	= new byte[20];
	
	int[]		m_background = 
	{
		0xffffff,
		0xffffff,
		0xffffff,
	};
	
	int[]		m_foreground = 
	{
		0,
		0x012a93,
		0x012a93,
	};
		
	static Font[]		sm_fontList = 
	{
		WeiboItemField.sm_defaultFont,
		WeiboItemField.sm_defaultFont,
		null,
	};
	
	Vector m_phizList	= new Vector();
		
	static String sm_replacePhizText = " ";
	static {
		while(sm_fontList[0].getAdvance(sm_replacePhizText) < Phiz.fsm_phizSize){
			sm_replacePhizText = sm_replacePhizText + " ";
		}
	}
	
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
		m_bufferedOffset[0] = 0;
		
		Arrays.fill(m_bufferedAttr,(byte)0);
				
		// parse the offset and attribute and style
		//
		int t_index = 0;
		int t_assignIndex = 0;
		int t_offsetIndex = 1;
		int t_attrIndex = 0;
		
		m_phizList.removeAllElements();
		
		StringBuffer t_finalText = new StringBuffer();
		
		setText_while:
		while(t_index < t_textLength){
			
			char a = _text.charAt(t_index);
			
			boolean t_userName = (a == '@');
			boolean t_phiz = (a == '[');
			
			int t_beginIndex = t_index;
			int t_beginAssignIndex = t_assignIndex;
			
			if(t_userName || t_phiz){
				
				if(t_offsetIndex != 1){
					
					if(t_offsetIndex >= m_bufferedOffset.length - 1){
						// relocate that
						//
						if(!incBuffered(t_textLength,(byte)0)){
							break;
						}
					}
					
					m_bufferedAttr[t_attrIndex] = 0;
					m_bufferedOffset[t_offsetIndex] = t_assignIndex;
					
					t_attrIndex++;
					t_offsetIndex++;
				}			
				
				for(int i = t_index + 1;i < t_textLength;i++,t_index++,t_assignIndex++){
					
					a = _text.charAt(i);
					
					if(t_phiz){
						
						if(a == ']'){
							
							String t_phizStr = _text.substring(t_beginIndex,t_index + 2);
							
							Phiz t_phizImage = WeiboUserFind.findPhizName(t_phizStr);
							if(t_phizImage != null){
								
								m_phizList.addElement(t_phizImage);
								t_finalText.append(sm_replacePhizText);
								
								t_index += 1;
								
								t_assignIndex += sm_replacePhizText.length() - t_phizStr.length() - 1;
								
								break;
								
							}else{
								
								t_finalText.append(_text.charAt(t_beginIndex));
								
								t_index = t_beginIndex + 1;
								t_assignIndex = t_beginAssignIndex + 1;
								
								continue setText_while;
							}
						}
						
					}else{
						
						if(!WeiboUserFind.isLeagalNameCharacter(a)){
							break;
						}
						
						t_finalText.append(a);
					}
					
				}
				
				if(t_offsetIndex >= m_bufferedOffset.length - 1){
					// relocate that
					//
					if(!incBuffered(t_textLength,(byte)0)){
						break;
					}
				}
				
				if(t_phiz){
					m_bufferedAttr[t_attrIndex] = 2;
				}else{
					m_bufferedAttr[t_attrIndex] = 1;	
				}
				
				t_index++;
				t_assignIndex++;
				m_bufferedOffset[t_offsetIndex] = t_assignIndex;
				
				t_attrIndex++;
				t_offsetIndex++;
				
			}else{
				t_finalText.append(a);
				
				t_index++;
				t_assignIndex++;
			}			
		}
		
		String t_finalT = t_finalText.toString();
		Arrays.fill(m_bufferedOffset,t_finalT.length(),t_offsetIndex,m_bufferedOffset.length - t_offsetIndex);
		
		super.setText(t_finalT,m_bufferedOffset,m_bufferedAttr,sm_fontList,m_foreground,m_background);
	}
	
	public int drawText(Graphics _g,int offset,int length,int x,int y,DrawTextParam drawTextParam){
		
		int t_phizIndex = 0;
		int t_offset = drawTextParam != null ? (offset + drawTextParam.getStartOffset()):offset;
		
		for(int i = 0 ;i < m_bufferedOffset.length - 1;i++){
			
			if(m_bufferedOffset[i] == m_bufferedOffset[i + 1]){
				// reach the rear
				//
				break;
			}
			
			if(t_offset == m_bufferedOffset[i] && m_bufferedAttr[i] == 2){
				
				Phiz t_phiz = (Phiz)m_phizList.elementAt(t_phizIndex);
				t_phiz.getImageSets().drawImage(_g, t_phiz.getImage(), x, y - sm_fontList[0].getBaseline());
				
				return 0;
			}
			
			if(m_bufferedAttr[i] == 2){
				t_phizIndex++;
			}
		}
		
		return super.drawText(_g,offset,length,x,y,drawTextParam);
		
	}
	
	private boolean incBuffered(int _offsetDefaultVal,byte _attrDefaultVal){
		
		if(m_bufferedOffset.length + 5 > 256){
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
