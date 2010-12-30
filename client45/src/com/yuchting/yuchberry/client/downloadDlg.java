package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;


class progressLabel extends LabelField{
	connectDeamon.FetchAttachment 	m_att	= null;
	
	Dialog		m_parentDlg					= null;			
	int			m_fontHeight				= 0;
	int			m_parentWidth				= 0;
	
	public progressLabel(Dialog _parent){
		m_fontHeight = getFont().getHeight();
		m_parentDlg = _parent;
		
		m_parentWidth = m_parentDlg.getPreferredWidth();
	}
	
	public void layout(int _width,int _height){		
		setExtent(m_parentWidth, m_fontHeight);
	}
	
	public void paint(Graphics _g){
		String t_str = null;
		if(m_att == null){
			t_str = "0%";
		}else{
			t_str = "" + m_att.m_completePercent + "%";
		}
		
		_g.drawText(t_str,0,0,Graphics.ELLIPSIS);
						
	}
	
	public boolean isFocusable(){
		return false;
	}
}
public class downloadDlg extends Dialog{

	progressLabel       m_stateText  	= new progressLabel(this);
	
	recvMain			m_mainApp		= null;
			
	public downloadDlg(recvMain _mainApp,String _filename){
		super("Download " + _filename,new Object[]{recvMain.sm_local.getString(localResource.DOWNLOAD_BACKGROUND)},new int[]{0},
				Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
		
		m_mainApp = _mainApp;
		
		Manager delegate = getDelegate();
		if( delegate instanceof DialogFieldManager ){
			
            DialogFieldManager dfm = (DialogFieldManager)delegate;
            
            Manager manager = dfm.getCustomManager();
            
            if(manager != null){
                manager.insert(m_stateText,0);
            }
        }
		
	}
	
	public void RefreshProgress(connectDeamon.FetchAttachment _att){
		m_stateText.m_att = _att;
		invalidate();
	}
	
	public boolean onClose(){
		close();
		m_mainApp.m_downloadDlg = null;
		return true;
	}

}
