package com.yuchting.yuchberry.client.screen;

import local.yblocalResource;
import net.rim.device.api.browser.field.ContentReadEvent;
import net.rim.device.api.browser.field2.BrowserField;
import net.rim.device.api.browser.field2.BrowserFieldConfig;
import net.rim.device.api.browser.field2.BrowserFieldListener;
import net.rim.device.api.script.ScriptableFunction;
import net.rim.device.api.ui.Graphics;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.container.MainScreen;

import org.w3c.dom.Document;

import com.yuchting.yuchberry.client.recvMain;

public class LoginWebView extends MainScreen {

	final stateScreen		m_stateScreen;
		
	BannerManager	m_banner = new BannerManager();
	BrowserField	m_webView = null;
	
	boolean 		m_loaded = false;
	
	final String 			m_loadURL;
	
	public LoginWebView(stateScreen _stateScreen,String _url)throws Exception{
		m_stateScreen	= _stateScreen;
		m_loadURL		= _url;
		
		BrowserFieldConfig myBrowserFieldConfig = new BrowserFieldConfig();
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.NAVIGATION_MODE,BrowserFieldConfig.NAVIGATION_MODE_POINTER);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.MDS_TRANSCODING_ENABLED,Boolean.FALSE);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.ALLOW_CS_XHR, Boolean.TRUE);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.JAVASCRIPT_ENABLED, Boolean.TRUE);
		myBrowserFieldConfig.setProperty(BrowserFieldConfig.JAVASCRIPT_TIMEOUT, new Integer(10000));
		
		m_webView = new BrowserField(myBrowserFieldConfig);
		add(m_webView);
		     
		m_webView.addListener(new BrowserFieldListener() {
			public void documentLoaded(BrowserField browserField, Document document) throws Exception {
				m_banner.m_text = browserField.getDocumentTitle();
				m_banner.invalidate();
				
				m_loaded = true;
				
				super.documentLoaded(browserField, document);
			}
			
			public void downloadProgress(BrowserField browserField, ContentReadEvent event) throws Exception {
				
				m_banner.setProgress(event.getItemsRead() * 100/ event.getItemsToRead());
				
				super.downloadProgress(browserField, event);
			}
			public void documentError(BrowserField browserField,Document document)throws Exception{
				super.documentError(browserField, document);
			}
		});
		
		m_webView.extendScriptEngine("YuchDroid.syncSucc", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args){
				if(args.length < 3){
					return null;
				}
				
				m_stateScreen.m_mainApp.m_hostname 		= args[0].toString();
				m_stateScreen.m_mainApp.m_port 			= Integer.parseInt(args[1].toString());
				m_stateScreen.m_mainApp.m_userPassword	= args[2].toString();
				
				m_stateScreen.m_mainApp.invokeLater(new Runnable() {
					public void run() {
						m_stateScreen.showAccMainManager(true);
						LoginWebView.this.onClose();
					}
				});
				
				return null;
			}
		});
		
		m_webView.extendScriptEngine("YuchDroid.escape", new ScriptableFunction() {
			public Object invoke(Object thiz, Object[] args){
				LoginWebView.this.close();
				
				return null;
			}
		});


		setBanner(m_banner);
		
		refresh();
	}
	
	public void refresh(){
		m_webView.requestContent(m_loadURL);
        m_loaded = false;
        m_banner.setProgress(0);
	}
	
	public boolean onClose(){
				
		if(m_loaded){
			try{
				m_webView.executeScript("escapeGWT();");
				return false;
			}catch(Exception e){
				m_stateScreen.m_mainApp.SetErrorString("LWVOC", e);
			}			
		}
		
		super.close();
		return true;
	}
	
	
	
	/**
	 * the webview field
	 * @author tzz
	 *
	 */
	private class BannerManager extends Manager{
		
		int		m_currProgress	= 5;
		
		String	m_text = recvMain.sm_local.getString(yblocalResource.STATE_LOGIN_LOADING);
		
		public BannerManager(){
			super(Manager.NO_VERTICAL_SCROLL);
		}
		
		/**
		 * set the progress of web loading
		 * @param _value
		 */
		public void setProgress(int _value){
			if(_value < 5){
				_value = 5;
			}
			m_currProgress = _value;
			invalidate();
		}
		
		public int getPreferredWidth(){
			return recvMain.fsm_display_width;
		}
		
		public int getPreferredHeight(){
			return getFont().getHeight() + 5;
		}
		
		protected void sublayout(int width, int height) {			
			setExtent(getPreferredWidth(),getPreferredHeight());
		}
		
		protected void subpaint(Graphics _g){
			
			int color = _g.getColor();
			try{
				_g.setColor(0);
				_g.fillRect(0,0,getPreferredWidth(),getPreferredWidth());
				
			}finally{
				_g.setColor(color);
			}
			
			super.subpaint(_g);
			
			color = _g.getColor();
			try{
				if(!m_loaded){
					_g.setColor(0x3459f6);
					_g.fillRoundRect(0, 2, getPreferredWidth() * m_currProgress / 100, getPreferredHeight() - 4, 2, 2);
				}
				
				_g.setColor(0xffffff);
				_g.drawText(m_text,0,2);
			}finally{
				_g.setColor(color);
			}
		}
	}
}
