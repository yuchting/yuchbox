/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
package com.yuchting.yuchberry.client.ui;

import java.util.Vector;

import net.rim.device.api.ui.DrawTextParam;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Font;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.ActiveRichTextField;
import net.rim.device.api.util.Arrays;

import com.yuchting.yuchberry.client.recvMain;

public class WeiboTextField extends ActiveRichTextField{
	
	private static final class ReadText{
		String m_originalText = null;
		int m_index = 0;
		public ReadText(String _text){
			m_originalText = _text;
		}
	}
	
	// getConvertString parameters using macro
	//
	public final static int			CONVERT_NORMAL				= 0;
	public final static int			CONVERT_DISABLE_AT_SIGN		= 0x1 << 1;
	public final static int			CONVERT_DISABLE_PHIZ		= 0x1 << 2;
	public final static int			CONVERT_DISABLE_TEXT		= 0x1 << 3;
	
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
		0x1355a6,
		0x1355a6,
	};
		
	static Font[]		sm_fontList = 
	{
		null,
		null,
		null,
	};
		
	Vector m_phizList	= new Vector();
	boolean	m_disableAtSign = false;
		
	public static String sm_replacePhizText = " ";
	public static int sm_replacePhiz_x_offset = 0;
	
	public WeiboTextField(int _foreground,int _background){
		super("",Field.READONLY | Field.FOCUSABLE | SCANFLAG_THREAD_ON_CREATE);
		init(_foreground,_background);
	}
	
	public WeiboTextField(int _foreground,int _background,boolean _disableAtSign){
		super("",Field.READONLY | Field.FOCUSABLE | SCANFLAG_THREAD_ON_CREATE);
		init(_foreground,_background);
		
		m_disableAtSign = _disableAtSign;
	}
	
	private void init(int _foreground,int _background){
		setColor(_foreground,_background);
		
		if(sm_fontList[0] == null){
			
			for(int i = 0;i < sm_fontList.length;i++){
				sm_fontList[i] = getFont();
			}
			
			// weibo name is bold
			//
			sm_fontList[1] = sm_fontList[0].derive(sm_fontList[0].getStyle()); //RRR:字体改普通 sm_fontList[1] = sm_fontList[0].derive(sm_fontList[0].getStyle() | Font.BOLD);
			
			int t_width = 0;
			while((t_width = sm_fontList[0].getAdvance(sm_replacePhizText)) < Phiz.fsm_phizSize){
				sm_replacePhizText = sm_replacePhizText + " ";
			}
			
			sm_replacePhiz_x_offset = (t_width - Phiz.fsm_phizSize) / 2;
		}
	}
	
	public void setColor(int _foreground,int _background){
		m_foreground[0] = _foreground;
		Arrays.fill(m_background,_background);
	}
		
	/**
	 * get the tag of weibo text or IM text
	 * @param _text
	 * @param _disable	check WeiboTextField.CONVERT_NORMAL for detail
	 * @return
	 */
	public static String getTag(ReadText _text,int _disable){
		
		if(_text.m_index >= _text.m_originalText.length()){
			return null;
		}
		
		StringBuffer t_tag = new StringBuffer();
		
		getTag_while:
		while(_text.m_index < _text.m_originalText.length()){
			
			char a = _text.m_originalText.charAt(_text.m_index);
			
			switch(a){

			case '[':
				
				if((_disable & CONVERT_DISABLE_PHIZ) == 0){
					if(t_tag.length() != 0){
						break getTag_while;
					}
					
					t_tag.append(a);
					_text.m_index++;
					
					while(_text.m_index < _text.m_originalText.length()){
						
						a = _text.m_originalText.charAt(_text.m_index);
						
						switch(a){
						case ']':
							t_tag.append(a);
							_text.m_index++;
							break getTag_while;
						case ' ':
						case '[':
							break getTag_while;
						}
						
						t_tag.append(a);
						_text.m_index++;					
					}
					break;
				}
				
			case '@':
				
				if((_disable & CONVERT_DISABLE_AT_SIGN) == 0 
				&& a == '@'){ // _disable & CONVERT_DISABLE_PHIZ can make this condition false
					
					if(t_tag.length() != 0){
						break getTag_while;
					}
					
					t_tag.append(a);
					_text.m_index++;
					
					while(_text.m_index < _text.m_originalText.length()){
						a = _text.m_originalText.charAt(_text.m_index);
											
						if(!isLeagalNameCharacter(a)){
							break getTag_while;
						}
						
						t_tag.append(a);
						_text.m_index++;
					}
					break;
				}
				
			default:
				
				if((_disable & CONVERT_DISABLE_TEXT) == 0){
					t_tag.append(a);
				}				
			}
			
			_text.m_index++;
		}
		
		return t_tag.toString();
	}
	
	public static boolean isLeagalNameCharacter(char a){
		
		if(a == '，' || a == '；' ||a == '：' ||a == '？' ||a == '‘'){
			return false;
		}
		
		if(Character.isDigit(a) || isChinese(a) || isAlpha(a)){
			return true;
		}
		
		if(a == '-' || a== '_' ){
			return true;
		}
		
		return false;
	}
	
	public static boolean isChinese(char a){ 
		int v = (int)a; 
		return (v>=19968 && v <=171941);	
	}
	
	public static boolean isAlpha(char a){
		return Character.isLowerCase(a) || Character.isUpperCase(a);
	}
	
	public static Phiz findPhizName(String _phizName){
		
		Vector t_list = recvMain.sm_phizImageList;
		
		for(int i = 0;i < t_list.size();i++){
			
			Phiz t_phiz = (Phiz)t_list.elementAt(i);
			
			if(_phizName.equals(t_phiz.getImage().getName()) ){
				return t_phiz;
			}
		}
		
		return null;
	}
	
	public void paint(Graphics _g){
		super.paint(_g);
	}
	
	/**
	 * convert the original weibo text to some final string
	 * @param _text		original Text
	 * @param _disable	check CONVERT_NORMAL for detail
	 * @return
	 */
	public static String getConvertString(String _text,int _disable,String _otherText){
		
		StringBuffer t_finalText = new StringBuffer();
		
		ReadText t_originalText = new ReadText(_text);
		
		boolean t_appendSpace = ((_disable & CONVERT_DISABLE_TEXT) != 0) && ((_disable & CONVERT_DISABLE_PHIZ) != 0);
		
		String t_read = null;
		while((t_read = getTag(t_originalText,_disable)) != null){
			
			if(t_read.length() == 0){
				continue;
			}
			
			char a = t_read.charAt(0);
			
			switch(a){
			
			case '[':
				
				if((_disable & CONVERT_DISABLE_PHIZ) == 0){
					Phiz t_phiz = findPhizName(t_read);
					if(t_phiz != null){
						t_finalText.append(sm_replacePhizText);
						
					}else{
						t_finalText.append(t_read);
					}
					
					break;
				}
				
			case '@':
				
				if((_disable & CONVERT_DISABLE_AT_SIGN) == 0
				&& a == '@'){ // _disable & CONVERT_DISABLE_PHIZ can make this condition false
					
					if(t_appendSpace){
						if(t_finalText.toString().indexOf(t_read) == -1
						&& (_otherText != null && _otherText.indexOf(t_read) == -1)){ // get rid of duplication
							
							t_finalText.append(t_read);	
							t_finalText.append(" ");
						}						
					}else{
						t_finalText.append(t_read);
					}					
					break;
				}
				
			default:
				
				if((_disable & CONVERT_DISABLE_TEXT) == 0){
					t_finalText.append(t_read);
				}

				break;
			}
		}
		
		return t_finalText.toString();
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
		m_bufferedOffset[0] = 0;
		
		Arrays.fill(m_bufferedAttr,(byte)0);
				
		// parse the offset and attribute and style
		//
		int t_offsetIndex = 1;
		int t_attrIndex = 0;
		
		m_phizList.removeAllElements();
		
		StringBuffer t_finalText = new StringBuffer();
		
		ReadText t_originalText = new ReadText(_text);
		
		String t_read = null;
		while((t_read = getTag(t_originalText,m_disableAtSign ? CONVERT_DISABLE_AT_SIGN : CONVERT_NORMAL)) != null){
			
			if(t_read.length() == 0){
				continue;
			}
			
			char a = t_read.charAt(0);
			
			switch(a){

			case '[':
				
				Phiz t_phiz = findPhizName(t_read);
				if(t_phiz != null){
					t_finalText.append(sm_replacePhizText);
					m_bufferedAttr[t_attrIndex]		= 2;
					m_phizList.addElement(t_phiz);
					
				}else{
					t_finalText.append(t_read);
					m_bufferedAttr[t_attrIndex]		= 0;
				}
				
				m_bufferedOffset[t_offsetIndex] = t_finalText.length();
				break;
			case '@':
				if(!m_disableAtSign){
					t_finalText.append(t_read);
					m_bufferedOffset[t_offsetIndex] = t_finalText.length();
					m_bufferedAttr[t_attrIndex]		= 1;			
					break;
				}
			default:
				t_finalText.append(t_read);
				
				m_bufferedOffset[t_offsetIndex] = t_finalText.length();
				m_bufferedAttr[t_attrIndex]		= 0;
				break;
			}
			
			t_offsetIndex++;
			t_attrIndex++;
			
			if(t_offsetIndex >= m_bufferedOffset.length - 1){
				// relocate that
				//
				if(!incBuffered(0,(byte)0)){
					break;
				}
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
				t_phiz.getImageSets().drawImage(_g, t_phiz.getImage(), 
						x + sm_replacePhiz_x_offset,y - sm_fontList[0].getBaseline());
				
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
