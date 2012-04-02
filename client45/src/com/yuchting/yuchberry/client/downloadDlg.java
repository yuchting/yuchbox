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
package com.yuchting.yuchberry.client;

import local.yblocalResource;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.GaugeField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.component.SeparatorField;
import net.rim.device.api.ui.container.HorizontalFieldManager;
import net.rim.device.api.ui.container.PopupScreen;
import net.rim.device.api.ui.container.VerticalFieldManager;

import com.yuchting.yuchberry.client.connectDeamon.FetchAttachment;

public class downloadDlg extends PopupScreen implements FieldChangeListener{

	GaugeField			m_progress		= null;
	
	ButtonField			m_backgroud		= new ButtonField(recvMain.sm_local.getString(yblocalResource.DOWNLOAD_BACKGROUND),
											Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	ButtonField			m_cancel		= new ButtonField(recvMain.sm_local.getString(yblocalResource.DOWNLOAD_CANCEL),
											Field.FIELD_HCENTER | ButtonField.CONSUME_CLICK | ButtonField.NEVER_DIRTY);
	
	recvMain			m_mainApp		= null;
	UiApplication		m_parent		= null;
	
	FetchAttachment		m_att			= null;
			
	public downloadDlg(recvMain _mainApp,FetchAttachment _att){
		super(new VerticalFieldManager(Manager.VERTICAL_SCROLL));		
		
		m_parent	= UiApplication.getUiApplication();
		m_mainApp	= _mainApp;
		m_att		= _att;

		LabelField	t_label = new LabelField(recvMain.sm_local.getString(yblocalResource.DOWNLOAD_DLG_LABEL) + _att.m_realName);
		add(t_label);
		
		m_progress	= new GaugeField("", 0, 100, 0, Field.NON_FOCUSABLE | Field.FIELD_HCENTER);
		add(m_progress);
		
		add(m_backgroud);
		add(m_cancel);
		
		m_backgroud.setChangeListener(this);
		m_cancel.setChangeListener(this);		
		
		RefreshProgress(_att);
	}
	
	public boolean	 onClose(){
		super.close();
		return true;
	}
	
	public void fieldChanged(Field _field,int _context){
		if(_context != FieldChangeListener.PROGRAMMATIC){
			if(_field == m_backgroud){
				close();
			}else if(_field == m_cancel){
				m_mainApp.m_connectDeamon.cancelDownloadAtt(m_att);
				close();
			}
		}
	}
	
	public void RefreshProgress(final connectDeamon.FetchAttachment _att){
				
		m_parent.invokeAndWait(new Runnable(){
			public void run() {
				m_progress.setValue(_att.m_completePercent);
			}
		});
	}
}
