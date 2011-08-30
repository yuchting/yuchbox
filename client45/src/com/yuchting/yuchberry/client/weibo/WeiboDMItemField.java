package com.yuchting.yuchberry.client.weibo;

import java.util.Vector;

import local.localResource;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.component.TextField;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.ui.BubbleImage;
import com.yuchting.yuchberry.client.ui.HyperlinkButtonField;
import com.yuchting.yuchberry.client.ui.WeiboHeadImage;

final class WeiboDMData{
	
	fetchWeibo		m_weibo;
	String			m_renderText;
	int				m_dataItemHeight;
	WeiboHeadImage	m_headImage;
	
	public WeiboDMData(fetchWeibo _weibo,WeiboHeadImage _image){
		m_weibo	= _weibo;
		m_headImage = _image;
		
		StringBuffer t_string = new StringBuffer();
		t_string.append("@").append(_weibo.GetUserScreenName()).append(": ").
				append(_weibo.GetText()).append(" (").append(WeiboDMItemField.getTimeString(_weibo)).append(")");
		
		m_renderText = t_string.toString();
		t_string = null;
		
		WeiboDMItemField.sm_testTextArea.setText(m_renderText);
		m_dataItemHeight = WeiboDMItemField.sm_testTextArea.getHeight();
	
		int t_minHeight = WeiboItemField.fsm_weiboSignImageSize + WeiboItemField.fsm_headImageWidth;
		if(m_dataItemHeight < t_minHeight){
			m_dataItemHeight = t_minHeight;			
		}
	}
}

public class WeiboDMItemField extends WeiboItemField{
	
	public final static int		fsm_numberOfPage = 5;
	
	public final static int		fsm_msgTextInterval = 8;
	
	public final static int		fms_msgTextWidth	= fsm_weiboItemFieldWidth - fsm_headImageWidth - fsm_msgTextInterval;
	
	final static TextField[]		sm_renderTextArray = 
	{
		new WeiboTextField(WeiboItemField.fsm_weiboCommentFGColor,WeiboItemField.fsm_weiboCommentBGColor),
		new WeiboTextField(WeiboItemField.fsm_weiboCommentFGColor,WeiboItemField.fsm_weiboCommentBGColor),
		new WeiboTextField(WeiboItemField.fsm_weiboCommentFGColor,WeiboItemField.fsm_weiboCommentBGColor),
		new WeiboTextField(WeiboItemField.fsm_weiboCommentFGColor,WeiboItemField.fsm_weiboCommentBGColor),
		new WeiboTextField(WeiboItemField.fsm_weiboCommentFGColor,WeiboItemField.fsm_weiboCommentBGColor),
	};
	
	public static HyperlinkButtonField	 sm_nextPageBut = new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_DM_NEXT_PAGE_BUT_LABEL));
	public static HyperlinkButtonField	 sm_prePageBut  = new HyperlinkButtonField(recvMain.sm_local.getString(localResource.WEIBO_DM_PRE_PAGE_BUT_LABEL));
	
	final static int			fsm_nextPageBut_x = fsm_weiboItemFieldWidth - sm_defaultFont.getAdvance(sm_nextPageBut.getText());
	final static int			fsm_prePageBut_x = fsm_nextPageBut_x -  sm_defaultFont.getAdvance(sm_prePageBut.getText());
	
	
	static public final int fsm_controlField_DM_next_page		= 0;
	static public final int fsm_controlField_DM_pre_page 		= 1;
	static public final int fsm_controlField_edit_return		= 2;
	
	static public final int[] fsm_messageFieldIndex = {3,4,5,6,7};
	
	//! the list of direct message
	Vector m_DMList		= new Vector();
	
	//! current page index
	int					m_pageIndex = 0;
	int					m_pageNum = 1;
	
	
	
	public WeiboDMItemField(fetchWeibo _weibo,WeiboHeadImage _headImage,WeiboMainManager _manager){
		super(_weibo,_headImage,_manager);		
		m_DMList.addElement(new WeiboDMData(_weibo,_headImage));
	}
	
	public void AddDelControlField(boolean _add){
		if(_add){
			
			m_parentManager.setCurrExtendedItem(this);			
			m_parentManager.setCurrEditItem(this);
			
			if(!m_hasControlField[fsm_controlField_edit_return]){
				m_hasControlField[fsm_controlField_edit_return] = true;
				add(m_parentManager.m_editTextArea);
			}
			
			recalculateHeight(true, _add, false,null);
			
			if(!m_hasControlField[fsm_controlField_DM_pre_page]){
				m_hasControlField[fsm_controlField_DM_pre_page] = true;
				add(sm_prePageBut);
			}
			
			if(!m_hasControlField[fsm_controlField_DM_next_page]){
				m_hasControlField[fsm_controlField_DM_next_page] = true;
				add(sm_nextPageBut);
			}
			
		}else{
			
			m_parentManager.setCurrExtendedItem(null);
			m_parentManager.setCurrEditItem(null);
			
			
			if(m_hasControlField[fsm_controlField_edit_return]){
				m_hasControlField[fsm_controlField_edit_return] = false;
				delete(m_parentManager.m_editTextArea);
			}
			
			recalculateHeight(true, _add, false,null);
			
			if(m_hasControlField[fsm_controlField_DM_pre_page]){
				m_hasControlField[fsm_controlField_DM_pre_page] = false;
				delete(sm_prePageBut);
			}
			
			if(m_hasControlField[fsm_controlField_DM_next_page]){
				m_hasControlField[fsm_controlField_DM_next_page] = false;
				delete(sm_nextPageBut);
			}			
		}
	}
	
	public fetchWeibo getReplyWeibo(){
		
		for(int i = 0;i < m_DMList.size();i++){
			WeiboDMData t_data = (WeiboDMData)m_DMList.elementAt(i);
			if(!t_data.m_weibo.IsOwnWeibo()){				
				return t_data.m_weibo;
			}
		}
		
		return null; 
	}
	
	public void AddDelEditTextArea(boolean _add,String _text){
		//AddDelControlField(_add);	
	}
	
	public int getPreferredHeight() {
		if(m_parentManager.getCurrExtendedItem() == this){		
			return recalculateHeight(false, false, false, null);
		}else{
			return fsm_closeHeight;
		}
	}
		
	public void subpaint(Graphics _g){
		
		if(m_parentManager.getCurrExtendedItem() == this){
			paintChild(_g,m_parentManager.m_editTextArea);
									
			recalculateHeight(false, false, false,_g);
		}else{
			super.subpaint(_g);			
		}		
	}
	
	public void sublayout(int width, int height){
		
		if(m_parentManager.getCurrExtendedItem() == this){
			
			// edit text
			//
			setPositionChild(m_parentManager.m_editTextArea,0,0);
			layoutChild(m_parentManager.m_editTextArea,fsm_weiboItemFieldWidth,sm_editTextAreaHeight);
			
			height =  recalculateHeight(false,false,true,null);						
			
		}else{		
			height = fsm_closeHeight;
		}
		
		setExtent(fsm_weiboItemFieldWidth,height);
	}
	
	public boolean hasTheWeibo(fetchWeibo _weibo){
		for(int i = 0;i < m_DMList.size();i++){
			if(((WeiboDMData)m_DMList.elementAt(i)).m_weibo == _weibo){				
				return true;
			}
		}
		return false;
	}
	
	public boolean delWeibo(fetchWeibo _weibo){
		
		for(int i = 0;i < m_DMList.size();i++){
			if(((WeiboDMData)m_DMList.elementAt(i)).m_weibo == _weibo){
				m_DMList.removeElement(_weibo);
				
				m_pageNum = m_DMList.size() / fsm_numberOfPage;
				if(m_pageNum % fsm_numberOfPage != 0){
					m_pageNum++;
				}
				
				recalculateHeight(false,false,false,null);
				
				return true;
			}
		}
		return false;
	}
	
	public boolean isEmptyPost(){
		return m_DMList.size() == 0;
	}
	
	public boolean AddSameSender(fetchWeibo _weibo	,WeiboHeadImage _headImage){
		
		for(int i = 0;i < m_DMList.size();i++){
			
			fetchWeibo weibo = ((WeiboDMData)m_DMList.elementAt(i)).m_weibo;
			
			if(isInOneChat(_weibo,weibo)){
				
				m_DMList.insertElementAt(new WeiboDMData(_weibo,_headImage),0);
				m_weibo = _weibo;
				
				m_simpleAbstract = getSimpleAbstract(_weibo);
				
				m_pageNum = m_DMList.size() / fsm_numberOfPage;
				if(m_pageNum % fsm_numberOfPage != 0){
					m_pageNum++;
				}
				
				recalculateHeight(false,false,false,null);
				
				return true;				
			}
			
			
		}
		
		return false;
	}
	
	private static boolean isInOneChat(fetchWeibo a,fetchWeibo b){
		
		if(a.GetWeiboStyle() != b.GetWeiboStyle()){
			return false;
		}
		
		if(a.GetWeiboStyle() == fetchWeibo.QQ_WEIBO_STYLE){
			
			return (a.GetUserScreenName().equals(b.GetUserScreenName())
					&& a.getReplyName().equals(b.getReplyName()))
					
					|| (a.GetUserScreenName().equals(b.getReplyName())
						&& a.getReplyName().equals(b.GetUserScreenName()));
		}else{
			
			return (a.GetUserId() == b.GetUserId()
					&& a.GetReplyWeiboId() == b.GetReplyWeiboId())
					
					|| (a.GetUserId() == b.GetReplyWeiboId()
						&& a.GetReplyWeiboId() == b.GetUserId());
		}
	}
	
	
	private int recalculateHeight(boolean _addControl,boolean _add,boolean _sublayout,Graphics _g){
		
		if(m_pageIndex >= m_DMList.size()){
			m_pageIndex = 0;
		}
		
		int t_endNum = (m_pageIndex + 1)* fsm_numberOfPage;
		if(t_endNum > m_DMList.size()){
			t_endNum = m_DMList.size();
		}
		
		int t_messageFieldIndex = 0;
		
		m_parentManager.RefreshEditTextAreHeight();
		
		if(_g != null){
			fillWeiboFieldBG(_g,0,sm_editTextAreaHeight,
						fsm_weiboItemFieldWidth,m_textHeight + sm_editTextAreaHeight + fsm_headImageTextInterval,true);
		}		
		
		m_textHeight = sm_editTextAreaHeight + fsm_headImageTextInterval;
		
		for(int i = m_pageIndex * fsm_numberOfPage;i < t_endNum;i++,t_messageFieldIndex++){
			
			WeiboDMData data = (WeiboDMData)m_DMList.elementAt(i);			
			
			if(_addControl){

				if(_add){
					if(!m_hasControlField[fsm_messageFieldIndex[t_messageFieldIndex]]){
						m_hasControlField[fsm_messageFieldIndex[t_messageFieldIndex]] = true;
						sm_renderTextArray[t_messageFieldIndex].setText(data.m_renderText);
						add(sm_renderTextArray[t_messageFieldIndex]);
					}
				}else{
					if(m_hasControlField[fsm_messageFieldIndex[t_messageFieldIndex]]){
						m_hasControlField[fsm_messageFieldIndex[t_messageFieldIndex]] = false;
						delete(sm_renderTextArray[t_messageFieldIndex]);
					}
				}
			}
			
			if(_sublayout){
				int t_text_x = data.m_weibo.IsOwnWeibo()?0:(fsm_headImageWidth + fsm_msgTextInterval); 
				
				setPositionChild(sm_renderTextArray[t_messageFieldIndex],t_text_x + 3,m_textHeight + 1);
				layoutChild(sm_renderTextArray[t_messageFieldIndex],fms_msgTextWidth - 4,data.m_dataItemHeight - 4);
			}
			
			if(_g != null){
				
				int t_text_x = data.m_weibo.IsOwnWeibo()?0:(fsm_headImageWidth + fsm_msgTextInterval); 
				int t_sign_x = data.m_weibo.IsOwnWeibo()?(fms_msgTextWidth + fsm_msgTextInterval):0;
				
				weiboTimeLineScreen.sm_weiboUIImage.drawImage(
						_g,weiboTimeLineScreen.GetWeiboSign(data.m_weibo.GetWeiboStyle()),t_sign_x,m_textHeight);
			
				displayHeadImage(_g,t_sign_x,m_textHeight + fsm_weiboSignImageSize + fsm_headImageTextInterval,data.m_headImage);
				
				weiboTimeLineScreen.sm_bubbleImage.draw(
							_g,t_text_x,m_textHeight-2,fms_msgTextWidth,data.m_dataItemHeight + 6 ,
							data.m_weibo.IsOwnWeibo()?BubbleImage.RIGHT_POINT_STYLE:BubbleImage.LEFT_POINT_STYLE);
											
				
				paintChild(_g,sm_renderTextArray[t_messageFieldIndex]);
				
			}
			
			m_textHeight += data.m_dataItemHeight + fsm_headImageTextInterval * 2;
		}
		
		if(_sublayout){
			setPositionChild(sm_nextPageBut,fsm_nextPageBut_x,m_textHeight);
			layoutChild(sm_nextPageBut,sm_nextPageBut.getPreferredWidth(),sm_nextPageBut.getPreferredHeight());
			
			setPositionChild(sm_prePageBut,fsm_prePageBut_x,m_textHeight);
			layoutChild(sm_prePageBut,sm_prePageBut.getPreferredWidth(),sm_prePageBut.getPreferredHeight());
		}
		
		if(_g != null){
			int color		= _g.getColor();
			try{
				_g.setColor(0x8f8f8f);
				_g.drawText(Integer.toString(m_pageIndex + 1)+ "/" + m_pageNum,0,m_textHeight);
			}finally{
				_g.setColor(color);
			}
			
			paintChild(_g,sm_prePageBut);
			paintChild(_g,sm_nextPageBut);
		}
		
		m_textHeight += sm_fontHeight;
		
		if(_g != null){
			_g.drawLine(0,m_textHeight - 1,fsm_weiboItemFieldWidth,m_textHeight - 1);
		}
				
		return m_textHeight;
	}
}
