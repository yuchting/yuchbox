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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataInputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import javax.microedition.io.Connector;
import javax.microedition.io.HttpConnection;

import net.rim.device.api.browser.field.BrowserContent;
import net.rim.device.api.browser.field.BrowserContentChangedEvent;
import net.rim.device.api.browser.field.Event;
import net.rim.device.api.browser.field.ExecutingScriptEvent;
import net.rim.device.api.browser.field.RedirectEvent;
import net.rim.device.api.browser.field.RenderingApplication;
import net.rim.device.api.browser.field.RenderingException;
import net.rim.device.api.browser.field.RenderingOptions;
import net.rim.device.api.browser.field.RenderingSession;
import net.rim.device.api.browser.field.RequestedResource;
import net.rim.device.api.browser.field.SetHeaderEvent;
import net.rim.device.api.browser.field.UrlRequestedEvent;
import net.rim.device.api.io.http.HttpProtocolConstants;
import net.rim.device.api.system.Application;
import net.rim.device.api.system.Display;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.Manager;
import net.rim.device.api.ui.container.HorizontalFieldManager;

import com.yuchting.yuchberry.client.recvMain;

/**
 * Browser field implements for compatible with 5.0 and pre-5.0
 * @author tzz
 *
 */
public class BrowserFieldImpl {
	
	private Field m_browserField = null;
	private recvMain	m_mainApp;
	
	private RenderingApplicationImpl m_readApplication = null;
	
	private Manager		m_browserManager = null;
	private int		m_browserFieldIdx;
	
	public BrowserFieldImpl(recvMain _mainApp,byte[] _htmlContent,final String _type){
		m_mainApp = _mainApp;
		
		if(!recvMain.fsm_OS_version.startsWith("4.")){
			m_browserField = new net.rim.device.api.browser.field2.BrowserField();
			((net.rim.device.api.browser.field2.BrowserField)m_browserField).displayContent(_htmlContent, _type,"http://localhost");
		}else{
			try{
				//m_readApplication = new RenderingApplicationImpl();
				//m_browserField = m_readApplication.getBrowserContent(_htmlContent).getDisplayableContent();
				
				m_readApplication = new RenderingApplicationImpl();
				m_browserField  = m_readApplication.getBrowserContent(new String(_htmlContent)).getDisplayableContent();
				
			}catch(Exception e){
				m_mainApp.SetErrorString("BFI",e);
			}
		}
	}
	
	public void insertBrowserField(Manager _manager,int _idx){
		m_browserManager	= _manager;
		m_browserFieldIdx	= _idx;
		
		if(m_browserField != null){
			HorizontalFieldManager tHorzMgr = new HorizontalFieldManager(HorizontalFieldManager.HORIZONTAL_SCROLL);
			tHorzMgr.add(m_browserField);
			
			_manager.insert(tHorzMgr, _idx);
		}
	}
	
	private void display(HttpConnection connetion)throws Exception{
		
		BrowserContent browserContent = m_readApplication.getBrowserContent(connetion);
		
		if (browserContent != null) {
			Field field = browserContent.getDisplayableContent();
			
			if (field != null) {
				synchronized (Application.getEventLock()) {
					if(m_browserManager != null && m_browserField != null){
						m_browserManager.delete(m_browserField);
						m_browserManager.insert(m_browserField,m_browserFieldIdx);
					}
				}
			}
			
			try{
				browserContent.finishLoading();
			}catch (RenderingException e) {
				m_mainApp.SetErrorString("BFID", e);
			}
		}
	}
	
	private class RenderingApplicationImpl implements RenderingApplication {
		
		private RenderingSession renderingSession = RenderingSession.getNewInstance();

		public RenderingApplicationImpl() {
			
			renderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.JAVASCRIPT_ENABLED, false);
			renderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.JAVASCRIPT_LOCATION_ENABLED, false);
			renderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.WAP_MODE, true);
			renderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.ENABLE_WML, true);
			renderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.ENABLE_EMBEDDED_RICH_CONTENT, true);
			renderingSession.getRenderingOptions().setProperty(RenderingOptions.CORE_OPTIONS_GUID, RenderingOptions.ENABLE_CSS, true);
		}
		
		public BrowserContent getBrowserContent(String _htmlContent)throws Exception {
			return renderingSession.getBrowserContent(new HttpConnectionImpl(_htmlContent),"", this, null);
		}
		
		public BrowserContent getBrowserContent(HttpConnection connection)throws Exception {			
			return renderingSession.getBrowserContent(connection,this, null);
		}
		
		public Object eventOccurred(Event event) {
			int eventId = event.getUID();

	        switch (eventId) {

	            case Event.EVENT_URL_REQUESTED: {
	                UrlRequestedEvent urlRequestedEvent = (UrlRequestedEvent)event;
	                byte[] postData = urlRequestedEvent.getPostData();

	                if (postData != null) {
	                	(new FetchThread(urlRequestedEvent.getURL(), urlRequestedEvent.getPostData())).start();
	                } else {
	                	(new FetchThread(urlRequestedEvent.getURL())).start();
	                }
	                
	                break;
	            }
	            
	            case Event.EVENT_BROWSER_CONTENT_CHANGED: {
	                BrowserContentChangedEvent browserContentChangedEvent = (BrowserContentChangedEvent)event;
	                
	                if (browserContentChangedEvent.getSource() instanceof BrowserContent) {
	                    BrowserContent browserContent = (BrowserContent)browserContentChangedEvent.getSource();
	                    final String newUrl = browserContent.getURL();
	                    
	                    if (newUrl == null){
	                    	break;
	                    }
	                    
//	                    final int index = newUrl.indexOf("auth_token");
//	                    
//	                    if (index > -1) {
//	                    	Application.getApplication().invokeLater(new Runnable() {
//	                    		
//	                    		public void run() {
//	                    			String authToken = newUrl.substring(newUrl.indexOf('=', index)+1);
//	                    			
//	                    			try {
//	                    				facebookFacade.getSession(authToken);
//		                    			notifyActionListener("success");
//	                    			} catch (FacebookException e) {
//	                    				notifyActionListener("error");
//	                    			}
//	                    		}
//	                    	});
//	                    }
	                }

	                break;
	            }
	            
	            case Event.EVENT_REDIRECT: {
	            	RedirectEvent redirectEvent = (RedirectEvent)event;
	            	(new FetchThread(redirectEvent.getLocation())).start();
	            	break;
	            }
	            
	            case Event.EVENT_CLOSE:              // Close the appication
	                break;
	           
	            case Event.EVENT_SET_HEADER: {       // no cache support
	            	SetHeaderEvent setHeaderEvent = (SetHeaderEvent)event;
	            	break;
	            }
	            
	            case Event.EVENT_SET_HTTP_COOKIE: {
	            	//SetHttpCookieEvent setHttpCookieEvent = (SetHttpCookieEvent)event;
	            	//addCookies(setHttpCookieEvent.getCookie());
	            	break;
	            }
	            
	            case Event.EVENT_EXECUTING_SCRIPT: { // no progress bar is supported
	            	ExecutingScriptEvent executingScriptEvent = (ExecutingScriptEvent)event;
	            	break;
	            }
	            	
	            case Event.EVENT_HISTORY :           // no history support           
		        case Event.EVENT_FULL_WINDOW :       // no full window support
	            case Event.EVENT_STOP :              // no stop loading support
	            default :
	            	break;
	        }

	        return null;
		}

		public int getAvailableHeight(BrowserContent browserContent) {
			return Display.getHeight();
		}

		public int getAvailableWidth(BrowserContent browserContent) {
			return Display.getWidth();
		}

		public String getHTTPCookie(String url) {
			return null;
		}

		public int getHistoryPosition(BrowserContent browserContent) {
			return 0;
		}

		public HttpConnection getResource(RequestedResource resource,BrowserContent referrer) {
			if (resource == null) {
	            return null;
	        }

	        if (resource.isCacheOnly()) {
	            return null; // no cache support
	        }

	        final String url = resource.getUrl();

	        if (url == null)
	            return null;

	        if (referrer == null) {
	        	try { 
	        		return (HttpConnection)Connector.open(url); 
	        	}catch (IOException e) {
	        		m_mainApp.SetErrorString("RAIGR", e);
	        	}
	        } else {
	        	(new ResourceFetchThread(resource, referrer)).start();
	        }

	        return null;
		}

		public void invokeRunnable(Runnable runnable) {
			(new Thread(runnable)).run();
		}
	}
	
	
	
	/**
	 * Do GET or POST
	 * 
	 */
	private class FetchThread extends Thread {
		
		private String absoluteUrl = null;
		private String method = HttpConnection.GET;
		private byte[] data = null;
		
		public FetchThread(String absoluteUrl) {
			this.absoluteUrl = absoluteUrl;
		}
		
		public FetchThread(String absoluteUrl, String method) {
			this.absoluteUrl = absoluteUrl;
			this.method = method;
		}
		
		public FetchThread(String absoluteUrl, byte[] data) {
			this.absoluteUrl = absoluteUrl;
			this.method = HttpConnection.POST;
			this.data = data;
		}
		
		public void run() {
			HttpConnection connection = null;
			
			try {
				connection = (HttpConnection)Connector.open(absoluteUrl);
				connection.setRequestProperty("x-rim-gw-properties", "16.10");
				connection.setRequestProperty("x-rim-transcode-content", "*/*");
				connection.setRequestProperty("x-rim-accept-encoding", "yk;v=3;m=384");
				connection.setRequestProperty("x-wap-profile", "\"http://www.blackberry.net/go/mobile/profiles/uaprof/8320/4.5.0.rdf\"");
				connection.setRequestProperty("profile", "http://www.blackberry.net/go/mobile/profiles/uaprof/8320/4.5.0.rdf");
				connection.setRequestProperty("User-Agent", "BlackBerry8320/4.5.0.44 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/-1");
				connection.setRequestProperty("Accept", "application/vnd.rim.html,text/html,application/xhtml+xml,application/vnd.wap.xhtml+xml,application/vnd.wap.wmlc;q=0.9,application/vnd.wap.wmlscriptc;q=0.7,text/vnd.wap.wml;q=0.7,text/vnd.sun.j2me.app-descriptor,image/vnd.rim.png,image/jpeg,application/x-vnd.rim.pme.b,application/vnd.rim.ucs,image/gif;anim=1,application/vnd.rim.css;v=1,text/css;media=screen,*/*;q=0.5");
				connection.setRequestProperty("x-rim-original-accept", "application/vnd.rim.html,text/html,application/xhtml+xml,application/vnd.wap.xhtml+xml,application/vnd.wap.wmlc;q=0.9,application/vnd.wap.wmlscriptc;q=0.7,text/vnd.wap.wml;q=0.7,text/vnd.sun.j2me.app-descriptor,image/vnd.rim.png,image/jpeg,application/x-vnd.rim.pme.b,application/vnd.rim.ucs,image/gif;anim=1,application/vnd.rim.css;v=1,text/css;media=screen,*/*;q=0.");
				
				if (method == HttpConnection.POST) {
					connection.setRequestMethod(HttpConnection.POST);
					connection.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_TYPE, HttpProtocolConstants.CONTENT_TYPE_APPLICATION_X_WWW_FORM_URLENCODED);
					connection.setRequestProperty(HttpProtocolConstants.HEADER_CONTENT_LENGTH, String.valueOf(data.length));
					OutputStream os = connection.openOutputStream();
					os.write(data);
				} else if (method == HttpConnection.GET) {
					connection.setRequestMethod(HttpConnection.GET);
				}
				
				display(connection);
				
			} catch (Exception e) {
				m_mainApp.SetErrorString("FTR",e);
			} finally {
				if (connection != null) {
					try { connection.close(); }
					catch (IOException e) {}
				}
			}
		}
	}
	
	/**
	 * Fetch requested resources.
	 * 
	 * @author Eki Baskoro
	 *
	 */
	private class ResourceFetchThread extends Thread {
		
		private RequestedResource requestedResource = null;
		private BrowserContent browserContent = null;
		
		public ResourceFetchThread(RequestedResource requestedResource, BrowserContent browserContent) {
			this.requestedResource = requestedResource;
			this.browserContent = browserContent;
		}
		
		public void run() {
			HttpConnection connection = null;
			
			try {
				connection = (HttpConnection)Connector.open(requestedResource.getUrl());
				
				connection.setRequestProperty("x-rim-gw-properties", "16.10");
				connection.setRequestProperty("x-rim-transcode-content", "*/*");
				connection.setRequestProperty("x-rim-accept-encoding", "yk;v=3;m=384");
				connection.setRequestProperty("x-wap-profile", "\"http://www.blackberry.net/go/mobile/profiles/uaprof/8320/4.5.0.rdf\"");
				connection.setRequestProperty("profile", "http://www.blackberry.net/go/mobile/profiles/uaprof/8320/4.5.0.rdf");
				connection.setRequestProperty("User-Agent", "BlackBerry8320/4.5.0.44 Profile/MIDP-2.0 Configuration/CLDC-1.1 VendorID/-1");
				connection.setRequestProperty("Accept", "application/vnd.rim.html,text/html,application/xhtml+xml,application/vnd.wap.xhtml+xml,application/vnd.wap.wmlc;q=0.9,application/vnd.wap.wmlscriptc;q=0.7,text/vnd.wap.wml;q=0.7,text/vnd.sun.j2me.app-descriptor,image/vnd.rim.png,image/jpeg,application/x-vnd.rim.pme.b,application/vnd.rim.ucs,image/gif;anim=1,application/vnd.rim.css;v=1,text/css;media=screen,*/*;q=0.5");
				connection.setRequestProperty("x-rim-original-accept", "application/vnd.rim.html,text/html,application/xhtml+xml,application/vnd.wap.xhtml+xml,application/vnd.wap.wmlc;q=0.9,application/vnd.wap.wmlscriptc;q=0.7,text/vnd.wap.wml;q=0.7,text/vnd.sun.j2me.app-descriptor,image/vnd.rim.png,image/jpeg,application/x-vnd.rim.pme.b,application/vnd.rim.ucs,image/gif;anim=1,application/vnd.rim.css;v=1,text/css;media=screen,*/*;q=0.");
				requestedResource.setHttpConnection(connection);
				browserContent.resourceReady(requestedResource);
				
			} catch (IOException e) {
				m_mainApp.SetErrorString("RFTR", e);
			} finally {
				if (connection != null) {
					try { connection.close(); }
					catch (IOException e) {}
				}
			}
		}
	}

	
	public class HttpConnectionImpl implements HttpConnection {
	    private long streamLength = 7000;
	    private DataInputStream dataInput;
	    private InputStream in;
	    private String encoding = "text/html";

	    public HttpConnectionImpl(String data) {
	        try {
	        	byte[] bytes = data.getBytes("UTF-8");
	        	streamLength = bytes.length; 
	            in = new ByteArrayInputStream(bytes);
	            dataInput = new DataInputStream(in);
	        } catch (Exception e) {
	            System.out.println("HttpConnectionImpl : Exception : " + e);
	        }

	    }

	    public String getURL() {
	        return "";
	    }

	    public String getProtocol() {
	        return "";
	    }

	    public String getHost() {
	        return "";
	    }

	    public String getFile() {
	        return "";
	    }

	    public String getRef() {
	        return "";
	    }

	    public String getQuery() {
	        return "";
	    }

	    public int getPort() {
	        return 0;
	    }

	    public String getRequestMethod() {
	        return "";
	    }

	    public void setRequestMethod(String s) throws IOException {

	    }

	    public String getRequestProperty(String s) {
	        return "";
	    }

	    public void setRequestProperty(String s, String s1) throws IOException {

	    }

	    public int getResponseCode() throws IOException {
	        return 200;
	    }

	    public String getResponseMessage() throws IOException {
	        return "";
	    }

	    public long getExpiration() throws IOException {
	        return 0;
	    }

	    public long getDate() throws IOException {
	        return 0;
	    }

	    public long getLastModified() throws IOException {
	        return 0;
	    }

	    public String getHeaderField(String s) throws IOException {
	        return "";
	    }

	    public int getHeaderFieldInt(String s, int i) throws IOException {
	        return 0;
	    }

	    public long getHeaderFieldDate(String s, long l) throws IOException {
	        return 0;
	    }

	    public String getHeaderField(int i) throws IOException {
	        return "";
	    }

	    public String getHeaderFieldKey(int i) throws IOException {
	        return "";
	    }

	    public String getType() {
	        return "text/html";
	    }

	    public String getEncoding() {
	        return encoding;
	    }

	    public long getLength() {
	        return streamLength;
	    }

	    public InputStream openInputStream() throws IOException {
	        return in;
	    }

	    public DataInputStream openDataInputStream() throws IOException {
	        return dataInput;
	    }

	    public void close() throws IOException {
	    	dataInput.close();
	    	in.close();
	    }

	    public OutputStream openOutputStream() throws IOException {
	        return new ByteArrayOutputStream();
	    }

	    public DataOutputStream openDataOutputStream() throws IOException {
	        return new DataOutputStream(new ByteArrayOutputStream());
	    }
	}
}
