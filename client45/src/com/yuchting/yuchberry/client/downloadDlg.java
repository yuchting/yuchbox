package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;

public class downloadDlg extends Dialog{

	LabelField       	m_stateText  	= new LabelField();
	
	recvMain			m_mainApp		= null;
	UiApplication		m_parent		= null;
			
	public downloadDlg(recvMain _mainApp,UiApplication _parent,String _filename){
		super("Download " + _filename,new Object[]{recvMain.sm_local.getString(localResource.DOWNLOAD_BACKGROUND)},new int[]{0},
				Dialog.OK, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION), Dialog.GLOBAL_STATUS);
		
		m_parent	= _parent;
		m_mainApp	= _mainApp;
		
		Manager delegate = getDelegate();
		if( delegate instanceof DialogFieldManager ){
			
            DialogFieldManager dfm = (DialogFieldManager)delegate;
            
            Manager manager = dfm.getCustomManager();
            
            if(manager != null){
                manager.insert(m_stateText,0);
            }
        }
		
		setDialogClosedListener(new DialogClosedListener(){
			
			public void dialogClosed(Dialog dialog, int choice) {
				
				switch (choice) {
					case Dialog.OK:
						onClose();
						break;
					
					default:
						break;
				}
			}
		});
		
	}
	
	public void RefreshProgress(final connectDeamon.FetchAttachment _att){
		m_parent.invokeAndWait(new Runnable(){
			public void run() {
				m_stateText.setText("" + _att.m_completePercent + "%");
			}
		});
	}
	
	public boolean onClose(){
		close();
		m_mainApp.m_downloadDlg = null;
		return true;
	}

}
