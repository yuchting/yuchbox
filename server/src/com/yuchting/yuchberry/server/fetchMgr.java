package com.yuchting.yuchberry.server;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;

import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.SAXReader;
import org.htmlparser.Node;
import org.htmlparser.NodeFilter;
import org.htmlparser.Parser;
import org.htmlparser.nodes.TextNode;
import org.htmlparser.tags.LinkTag;
import org.htmlparser.util.NodeList;

public class fetchMgr{
		
	public final static String	fsm_configFilename 			= "config.xml";
				
	Logger	m_logger					= null;
	
	ServerSocket m_svr 					= null;
	
	String	m_prefix					= null;   
	String	m_userPassword				= null;
		
	int		m_listenPort 				= 9716;
	
	int		m_fetchInterval 			= 10;
	
	boolean m_convertToSimpleChar 		= false;

    boolean m_userSSL					= false;
    
    Vector			m_fetchAccount 		= new Vector();
        
    //! is connected?
    berrySvrDeamon	m_currConnect		= null;
    
    long 			m_confirmTimer 		= 0;
    
    int				m_clientLanguage	= 0;
    
	
	public int GetServerPort(){
		return m_listenPort;
	}
	
	public String GetUserPassword(){
		return m_userPassword;
	}
	
	public boolean IsUseSSL(){
		return m_userSSL;
	}
	
	public String GetPrefixString(){
		return m_prefix;
	}
	
	public int GetPushInterval(){
		return m_fetchInterval;
	}
	
	public berrySvrDeamon GetClientConnected(){
		return	m_currConnect;
	}
	
	public int GetClientLanguage(){
		return m_clientLanguage;
	}
	
	public synchronized void SetClientConnected(berrySvrDeamon _set){
		m_currConnect = _set;
		
		// set the confirm timer to prepare the unconfirm
		//
		if(_set != null){
			m_confirmTimer = 0;
		}
		
	}
	
	public void SendData(ByteArrayOutputStream _os,boolean _sendImm)throws Exception{
		
		if(m_currConnect == null || m_currConnect.m_sendReceive == null || !m_currConnect.m_sendReceive.isAlive()){
			throw new Exception("Client has been closed");
		}
		
		m_currConnect.m_sendReceive.SendBufferToSvr(_os.toByteArray(), _sendImm);		
	}
        
	public void InitConnect(String _prefix,Logger _logger)throws Exception{
		
		m_prefix	= _prefix;
		m_logger	= _logger;
		
		DestroyAllAcount();
		
		try{
			
			SAXReader t_xmlReader = new SAXReader();
			Document t_doc = t_xmlReader.read(new FileInputStream(m_prefix + fsm_configFilename)); 
			Element t_root = t_doc.getRootElement();
			
			m_userPassword					= fetchAccount.ReadStringAttr(t_root,"userPassword");
			m_listenPort					= fetchAccount.ReadIntegerAttr(t_root,"serverPort");
			m_fetchInterval					= fetchAccount.ReadIntegerAttr(t_root,"pushInterval");
			m_userSSL						= fetchAccount.ReadBooleanAttr(t_root,"userSSL");
			m_convertToSimpleChar			= fetchAccount.ReadBooleanAttr(t_root,"convertoSimpleChar");			
			
			for( Iterator i = t_root.elementIterator("EmailAccount"); i.hasNext();){
	            Element element = (Element) i.next();
	            fetchEmail t_email = new fetchEmail(this);
	            t_email.InitAccount(element);
	            
	            m_fetchAccount.addElement(t_email);
	        }
			
			for( Iterator i = t_root.elementIterator("SinaWeiboAccount"); i.hasNext();){
	            Element element = (Element) i.next();
	            fetchSinaWeibo t_email = new fetchSinaWeibo(this);
	            t_email.InitAccount(element);
	            
	            m_fetchAccount.addElement(t_email);
	        }
	    	
		}catch(Exception ex){
			m_logger.PrinterException(ex);
		}
			
	}
	
	public void StartListening(){
		
		try{
			
			ResetAllAccountSession(false);
			
			m_confirmTimer = (new Date()).getTime();
			
	    	m_svr = GetSocketServer(m_userPassword,m_userSSL);	    	
	    	
			while(true){
				try{
					if(m_svr == null){
						break;
					}
					m_currConnect = new berrySvrDeamon(this, m_svr.accept());
				}catch(Exception _e){
					m_logger.PrinterException(_e);
		    	}
	    	}
			
		}catch(Exception ex){
			m_logger.PrinterException(ex);
		}
		
	}
	
	public synchronized void EndListening(){
		
		try{
			
			for(int i = 0;i < m_fetchAccount.size();i++){
				fetchAccount accout =(fetchAccount)m_fetchAccount.elementAt(i);
				accout.DestroySession();
			}
			
			if(m_svr != null){
				m_svr.close();
				m_svr = null;
			}
		}catch(Exception e){
			if(m_logger != null){
				m_logger.PrinterException(e);
			}
		}		
	}
	
	public void DestroyAllAcount()throws Exception{
		for(int i = 0;i < m_fetchAccount.size();i++){
			fetchAccount accout =(fetchAccount)m_fetchAccount.elementAt(i);
			accout.DestroySession();
		}
		
		m_fetchAccount.removeAllElements();
	}
	public void ResetAllAccountSession(boolean _testAll)throws Exception{
		
		for(int i = 0;i < m_fetchAccount.size();i++){
			fetchAccount accout =(fetchAccount)m_fetchAccount.elementAt(i);
			accout.ResetSession(_testAll);
		}
	}
	
	public String GetAccountName(){
		if(!m_fetchAccount.isEmpty()){
			fetchAccount accout =(fetchAccount)m_fetchAccount.elementAt(0);
			return accout.GetAccountName();
		}
		return "No Account Name!";
	}
	
	public void ProcessPackage(byte[] _package)throws Exception{
		
		ByteArrayInputStream in = new ByteArrayInputStream(_package);
				
		final int t_msg_head = in.read();
		
		switch(t_msg_head){			
			case msg_head.msgKeepLive:
				break;
			case msg_head.msgSponsorList:
				ProcessSponsorList(in);
				break;
			default:
			{
				for(int i = 0 ;i < m_fetchAccount.size();i++){
					fetchAccount account = (fetchAccount)m_fetchAccount.elementAt(i);
					if(account.ProcessNetworkPackage(_package)){
						break;
					}
				}
			}
		}		
	}
	
	public void SendImmMail(final String _subject ,final String _contain,final String _from){
		for(int i = 0;i < m_fetchAccount.size();i++){
			fetchAccount account = (fetchAccount)m_fetchAccount.elementAt(i);
			
			if(account instanceof fetchEmail){
				((fetchEmail)account).SendImmMail(_subject, _contain, _from);
				break;
			}
		}
	}
	public void CheckAccountFolders(){
				
		for(int i = 0;i < m_fetchAccount.size();i++){
			fetchAccount account = (fetchAccount)m_fetchAccount.elementAt(i);
			
			try{
				
				account.CheckFolder();
				
			}catch(Exception e){
				
				m_logger.PrinterException(e);
				
				try{
					
					Thread.sleep(5000);
					account.ResetSession(false);
					
				}catch(Exception _e){
					m_logger.PrinterException(e);
					break;
				}
				
			}
		}
	}
		
	public void Push(sendReceive _send){
		
		boolean t_repush = false;
		
		final long t_currentTime = (new Date()).getTime();
		
		if(t_currentTime - m_confirmTimer > 2 * 60 * 1000){
			// send the mail without confirm
			//
			m_confirmTimer 	= t_currentTime;
			t_repush 		= true;			
		}
		
		for(int i = 0;i < m_fetchAccount.size();i++){
			fetchAccount account = (fetchAccount)m_fetchAccount.elementAt(i);
			if(t_repush){
				account.PrepareRepushUnconfirmMsg(t_currentTime);
			}
			
			try{
				
				account.PushMsg(_send);
				
			}catch(Exception e){
				m_logger.PrinterException(e);
			}			
		}
	}
		
	private void ProcessSponsorList(ByteArrayInputStream _in){
		try{
			
			// read the google code host page
			//
			final String ft_URL = new String("http://code.google.com/p/yuchberry/wiki/Thanks_sheet");
			String charSet = null;
			StringBuffer t_stringBuffer = new StringBuffer();
			
			URL url = new URL(ft_URL);
			URLConnection con = url.openConnection();
			con.setAllowUserInteraction(false);
			con.connect();
			   
			String type = URLConnection.guessContentTypeFromStream(con.getInputStream());
			
			if (type == null)
				type = con.getContentType();
			
			if (type == null || type.trim().length() == 0 || type.trim().indexOf("text/html") < 0){
				return ;
			}
			
			if(type.indexOf("charset=") > 0){
				charSet = type.substring(type.indexOf("charset=") + 8);
			}
						
			BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream(), charSet));
			
			String temp;
			while ((temp = in.readLine()) != null) {
				t_stringBuffer.append(temp+"\n");
			}
			in.close();
	        	        
	        String t_line = fetchMgr.ParseHTMLText(t_stringBuffer.toString(),false);
	        
	        final int t_start = t_line.indexOf("##@##");
	        final int t_end = t_line.indexOf("@@#@@");
	        if(t_start != -1 && t_end != -1){
	        	t_line = t_line.substring(t_start + 5 ,t_end);
	        }
	        t_line = t_line.replace("&para;","");
	        
	        ByteArrayOutputStream t_os = new ByteArrayOutputStream();
	        t_os.write(msg_head.msgSponsorList);
	        sendReceive.WriteString(t_os,t_line,m_convertToSimpleChar);
	        
	        SendData(t_os,true);
	        
		}catch(Exception _e){}
	}
	
		
	static public String ReadSimpleIniFile(String _file,String _decodeName,Vector _lines)throws Exception{
		
		File t_file = new File(_file);
		
		String t_ret = new String();
		
		if(_lines != null){
			_lines.removeAllElements();
		}
		
		if(t_file.exists()){
			BufferedReader in = new BufferedReader(
									new InputStreamReader(
											new FileInputStream(_file),_decodeName));
			
			StringBuffer t_stringBuffer = new StringBuffer();
			String t_line = null;
			
			boolean t_firstLine = true;
			while((t_line = in.readLine()) != null){
				
				if(t_firstLine && _decodeName.equals("UTF-8")){
					
					byte[] t_bytes = t_line.getBytes("UTF-8");
					
					// BOM process
					//
					if(t_bytes.length >= 3
						&& t_bytes[0] == -17 && t_bytes[1] == -69 && t_bytes[2] == -65){						
						
						if(t_bytes.length == 3){
							t_line = "";
						}else{
							t_line = new String(t_bytes,3,t_bytes.length - 3,"UTF-8");
						}
															
					}
				}
				
				t_firstLine = false;
								
				if(!t_line.startsWith("#")){
					t_stringBuffer.append(t_line + "\n");
					
					if(_lines != null){
						_lines.addElement(t_line);
					}
				}
			}
			
			t_ret = t_stringBuffer.toString();
			in.close();
		}
		
		return t_ret;
	}
	
		
	public ServerSocket GetSocketServer(String _userPassword,boolean _ssl)throws Exception{
		
		if(_ssl){
			String	key				= m_prefix + "YuchBerrySvr.key";  
			
			char[] keyStorePass		= _userPassword.toCharArray();
			char[] keyPassword		= _userPassword.toCharArray();
			
			KeyStore ks				= KeyStore.getInstance(KeyStore.getDefaultType());
			
			ks.load(new FileInputStream(key),keyStorePass);
			
			KeyManagerFactory kmf	= KeyManagerFactory.getInstance("SunX509");
			kmf.init(ks,keyPassword);
			
			SSLContext sslContext = SSLContext.getInstance("SSLv3");
			sslContext.init(kmf.getKeyManagers(),null,null);
			  
			SSLServerSocketFactory factory=sslContext.getServerSocketFactory();
			
			SSLServerSocket t_socket = (SSLServerSocket)factory.createServerSocket(m_listenPort); 
			//t_socket.setNeedClientAuth(true);
			
			return t_socket;
			
		}else{
			return new ServerSocket(m_listenPort);
		}		  
	}
	
	static public String ParseHTMLText(String _html,boolean _shortURL){
		StringBuffer t_text = new StringBuffer();	
		StringBuffer t_shorterText = new StringBuffer();
		
		boolean t_shorted = false;
		try{
			Parser parser = new Parser(_html,null);
			parser.setEncoding("UTF-8");
			
	        NodeList list = parser.parse(new  NodeFilter() {
	        								public boolean accept(Node node) {
	        									return node instanceof TextNode || node instanceof LinkTag ;
	        								}
	        							});
	        
	        Node[] nodes = list.toNodeArray();

            for (int i = 1; i < nodes.length; i++){
                Node nextNode = nodes[i];

                if (nextNode instanceof TextNode){
                    TextNode textnode = (TextNode) nextNode;
                    t_text.append(textnode.getText());
                    t_text.append("\n");
                }else{
                	
                	LinkTag link = (LinkTag)nextNode;
                	if(_shortURL){
                		t_text.append(GetShortURL(link.getLink()));
                		t_shorted = true;
                	}else{
                		t_text.append(link.getLink());
                	}
                	
                	t_text.append("\n");
                }              
            }            
            
            int t_emptyCharCounter = 0;
            
            for(int i = 0;i < t_text.length();i++){
            	final char t_char = t_text.charAt(i);
            	if(IsEmptyChar(t_char)){
            		if(t_emptyCharCounter++ < 2){
            			t_shorterText.append(t_char);
            		}
            	}else{
            		t_emptyCharCounter = 0;
            		t_shorterText.append(t_char);
            	}            	
            }
            
		}catch(Exception _e	){}
		
		String t_result = t_shorterText.toString();
		
		t_result = t_result.replaceAll("&lt;", "<");
		t_result = t_result.replaceAll("&gt;", ">");
		t_result = t_result.replaceAll("&amp;", "&");
		t_result = t_result.replaceAll("&apos;", "'");
		t_result = t_result.replaceAll("&quot;", "\"");
		t_result = t_result.replaceAll("&nbsp;", " ");	
		
		if(t_shorted){
			return "[yuchberry prompt:some URL would be shorted]\n" + t_result;
		}else{
			return t_result;
		}
		
	}
	
	static public boolean IsEmptyChar(final char _char){
		return _char == ' ' || _char == '\n' || _char == '\t' || _char == '\r';
	}
	
	static public boolean IsEmptyLine(final String _string){
		for(int i = 0;i < _string.length();i++){
			if(!IsEmptyChar(_string.charAt(i))){
				return false;
			}
		}
		
		return true;
	}
	
	static private String GetShortURL(String _longURL){
		
		try{
			URL is_gd = new URL("http://is.gd/api.php?longurl=" + _longURL);
			
	        URLConnection yc = is_gd.openConnection();
	        BufferedReader in = new BufferedReader(
	                                new InputStreamReader(yc.getInputStream()));
	        
	        String inputLine = in.readLine();	        
	        in.close();
	        
	        return (inputLine != null && inputLine.length() < _longURL.length()) ? inputLine:_longURL ;
	        
		}catch(Exception _e){}
		
		return _longURL;
		
        
	}
	
	
}