package com.yuchting.yuchberry.server;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.ServerSocket;
import java.net.URL;
import java.net.URLConnection;
import java.security.KeyStore;
import java.util.Date;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Header;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.Session;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.mail.internet.MimeUtility;
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

import com.sun.mail.smtp.SMTPTransport;
import com.yuchting.yuchberry.server.fetchEmail.MailIndexAttachment;

public class fetchMgr{
		
	public final static String	fsm_configFilename 			= "config.xml";
				
	Logger	m_logger	= null;
	
	ServerSocket m_svr 	= null;
	
	String	m_prefix	= null;   
	String	m_userPassword	= null;
		
	int		m_listenPort = 9716;
	
	int		m_fetchInterval = 10;
	
	boolean m_convertToSimpleChar = false;

    boolean m_userSSL			= false;
    
    Vector			m_fetchAccount = new Vector();
        
    //! is connected?
    berrySvrDeamon	m_currConnect = null; 
    
    
        
	public void InitConnect(String _prefix,String _configFile,Logger _logger){
		
		m_prefix	= _prefix;
		m_logger	= _logger;
			
		try{
			
			SAXReader t_xmlReader = new SAXReader();
			Document t_doc = t_xmlReader.read(new FileInputStream(_configFile)); 
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
	    	
		}catch(Exception ex){
			m_logger.PrinterException(ex);
		}
			
	}
	
	public void StartListening(){
		
		try{
			
			for(int i = 0;i < m_fetchAccount.size();i++){
				fetchAccount accout =(fetchAccount)m_fetchAccount.elementAt(i);
				accout.ResetSession(false);
			}
			
			
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
			
		}		
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
	
	public synchronized void SetClientConnected(berrySvrDeamon _set){
		m_currConnect = _set;
	}
	
	
	
	public void CheckFolder()throws Exception{
		
		
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
	
	
}