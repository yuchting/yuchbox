package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;

public class downloadDlg extends Dialog{

	LabelField          m_stateText     = new LabelField("0%");
	
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
	
	public void RefreshProgress(final int _percent){
		
		m_mainApp.invokeLater(new Runnable() {
			public void run() {
				m_stateText.setText("" + _percent + "%");
			}
		});
	}
	
	public boolean onClose(){
		close();
		m_mainApp.m_downloadDlg = null;
		return true;
	}
}
