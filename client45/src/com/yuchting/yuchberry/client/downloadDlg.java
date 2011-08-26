package com.yuchting.yuchberry.client;

import local.localResource;
import net.rim.device.api.system.Bitmap;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.DialogClosedListener;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.DialogFieldManager;

import com.yuchting.yuchberry.client.connectDeamon.FetchAttachment;

public class downloadDlg extends Dialog{

	LabelField       	m_stateText  	= new LabelField();
	
	recvMain			m_mainApp		= null;
	UiApplication		m_parent		= null;
	
	FetchAttachment		m_att			= null;
			
	public downloadDlg(recvMain _mainApp,FetchAttachment _att){
		super("Download " + _att.m_realName,
			new Object[]
			{
				recvMain.sm_local.getString(localResource.DOWNLOAD_BACKGROUND),
				recvMain.sm_local.getString(localResource.DOWNLOAD_CANCEL),
			},new int[]{0,1},
			0, Bitmap.getPredefinedBitmap(Bitmap.INFORMATION),Dialog.GLOBAL_STATUS);
		
		
		m_parent	= UiApplication.getUiApplication();
		m_mainApp	= _mainApp;
		m_att		= _att;
		
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
					case 1:
						m_mainApp.m_connectDeamon.cancelDownloadAtt(m_att);
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
}
