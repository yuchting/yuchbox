import java.io.FileInputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.security.KeyStore;
import java.util.Properties;
import java.util.Vector;

import javax.mail.Flags;
import javax.mail.Folder;
import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Store;
import javax.net.ssl.KeyManagerFactory;
import javax.net.ssl.SSLContext;
import javax.net.ssl.SSLServerSocket;
import javax.net.ssl.SSLServerSocketFactory;


class berrySvrDeamon extends Thread{
	
	fetchMgr	m_fetchMgr = null;
	Socket		m_socket = null;
		
	
	public berrySvrDeamon(Socket _s,fetchMgr _mgr){
		m_fetchMgr 	= _mgr;
		m_socket	= _s;
	}
	
	public void run(){
		
		// loop
		//
		while(true){
			if(!m_fetchMgr.IsConnected()){
				break;
			}
		
			// process....
			//
			try{
				sendReceive t_receive = new sendReceive();
				byte[] t_package = t_receive.RecvBufferFromSvr(m_socket);
				
				ProcessPackage(t_package);
				
			}catch(Exception _e){
				try{
					m_socket.close();
				}catch(Exception e){}
				
				break;
			}
			
		}

	}
	
	public void ProcessPackage(byte[] _package)throws Exception{
		
	}
	
}

class fetchMgr{
	
	final static int	ACCEPT_PORT = 9716;
	
	String 	m_protocol 	= null;
    String 	m_host 		= null;
    int		m_port		= 0;
    
    String 	m_inBox 	= "INBOX";
       
	String 	m_userName 	= null;
	String 	m_password 	= null;
	String	m_userPassword	= null;
	
	// Get a Properties object
    Properties m_sysProps = System.getProperties();

    // Get a Session object
    Session m_session 	= null;
    Store 	m_store		= null;
    	
    Vector<fetchMail> m_unreadMailVector = new Vector<fetchMail>();
    
    Vector<berrySvrDeamon>	m_vectConnect = new Vector<berrySvrDeamon>();
    
    // pushed mail index vector 
    Vector m_vectPushedMailIndex = new Vector();
    
    int		m_beginFetchIndex 	= 0;
    int		m_totalMailCount	= 0;
    
    int		m_unreadFetchIndex	= 0;
    
        
	public void InitConnect(String _protocol,
							String _host,
							int _port,
							String _username,
							String _password,
							String _userPassword) throws Exception{

    	DestroyConnect();
    	
		if(m_session != null){
			throw new Exception("has been initialize the session");
		}
		
    	m_session = Session.getInstance(m_sysProps, null);
    	m_session.setDebug(false);
		
    	m_protocol	= _protocol;
    	m_host		= _host;
    	m_port		= _port;
    	m_userName	= _username;
    	m_password	= _password;
    	m_userPassword = _userPassword;
    	
    	if(m_protocol == null){
    		m_protocol = "pop3";
    	}else{
    		
    		if(!m_protocol.equals("imap") 
    		&& !m_protocol.equals("pop3") 
    		&& !m_protocol.equals("pop3s") 
    		&& !m_protocol.equals("imaps")){
    			
    			m_protocol = "pop3";
    		}   		
	    }
    	
		
    	m_store = m_session.getStore(m_protocol);
    	m_store.connect(m_host,m_port,m_userName,m_password);
    	   	
    	//
    	//
    	ServerSocket t_svr = GetSocketServer();
    	
    	try{
    		while(true){
    			m_vectConnect.addElement(new berrySvrDeamon(t_svr.accept(),this));
        	}
    	}finally{
    		t_svr.close();
    	}    	
	}
	
	public int GetMailCountWhenFetched(){
		return m_totalMailCount;
	}
	
	public void SetBeginFetchIndex(int _index){
		m_beginFetchIndex = _index;
	}
	
	public int GetBeginFetchIndex(){
		return m_beginFetchIndex;
	}
	
	public void SetUnreadFetchIndex(int _index){
		m_unreadFetchIndex = _index;
	}
	
	public int GetUnreadFetchIndex(){
		return m_unreadFetchIndex;
	}
	
	public void CheckFolder()throws Exception{
		
		Folder folder = m_store.getDefaultFolder();
	    if(folder == null) {
	    	throw new Exception("Cant find default namespace");
	    }
	    
	    folder = folder.getFolder("INBOX");
	    if (folder == null) {
	    	throw new Exception("Invalid INBOX folder");
	    }
	    
	    m_unreadMailVector.clear();
	    
	    folder.open(Folder.READ_ONLY);
	   
	    if(m_totalMailCount != folder.getMessageCount()){
	    	m_totalMailCount = folder.getMessageCount();	    
		    final int t_startIndex = Math.max(m_totalMailCount - Math.min(50,m_totalMailCount) + 1,m_unreadFetchIndex);
		    
		    Message[] t_msgs = folder.getMessages(t_startIndex, m_totalMailCount);
		    
		    for(int i = 0;i < t_msgs.length;i++){
		    	
		    	Message t_msg = t_msgs[i];
		    	
		    	Flags flags = t_msg.getFlags();
	        	Flags.Flag[] flag = flags.getSystemFlags();  
	        	
	        	boolean t_isNew = true;
	        	for(int j = 0; j < flag.length; j++){
	                if (flag[j] == Flags.Flag.SEEN 
	                	&& flag[j] != Flags.Flag.DELETED
	                	&& flag[j] != Flags.Flag.DRAFT) {
	                	
	                    t_isNew = false;
	                    break;      
	                }
	            }      
	        	
		    	if(t_isNew){
		    		
		    		fetchMail t_mail = new fetchMail();
		    		t_mail.SetMailIndex(i + t_startIndex);
		    		t_mail.ImportMail(t_msg);
		    		
		    		m_unreadMailVector.addElement(t_mail);
		    	}
		    }
		    		    
		    m_beginFetchIndex = t_startIndex;
		    
	    }	       
	    
	    folder.close(false);
	}
	
	public void DestroyConnect()throws Exception{
		m_session = null;
		
		if(m_store != null){
			
		    m_unreadMailVector.clear();
		    m_vectConnect.clear();
		    
		    // pushed mail index vector 
		    m_vectPushedMailIndex.clear();
		    
			m_store.close();
			m_store = null;
		}
	}
	
	public boolean IsConnected(){
		return m_session != null;
	}
	
	public SSLServerSocket GetSocketServer()throws Exception{
		
		String	key				= "YuchBerryKey";  
		
		char[] keyStorePass		= m_userPassword.toCharArray();
		char[] keyPassword		= m_userPassword.toCharArray();
		
		KeyStore ks				= KeyStore.getInstance(KeyStore.getDefaultType());
		
		ks.load(new FileInputStream(key),keyStorePass);
		
		KeyManagerFactory kmf	= KeyManagerFactory.getInstance("SunX509");
		kmf.init(ks,keyPassword);
		
		SSLContext sslContext = SSLContext.getInstance("SSLv3");
		sslContext.init(kmf.getKeyManagers(),null,null);
		  
		SSLServerSocketFactory factory=sslContext.getServerSocketFactory();
		
		return (SSLServerSocket)factory.createServerSocket(ACCEPT_PORT);
		  
	}
	
}

public class fetchMain{
	
	static String sm_protocol;
    static String sm_host;
    static int		sm_port	;
    static String sm_inBox;
    
    static boolean sm_debug 		= false;
    
	static String sm_strUserName ;
	static String sm_strPassword ;
	static String sm_strUserPassword;
	
	public static void main(String[] _arg){
		
		
		
		Properties p = new Properties(); 
		fetchMgr t_manger = new fetchMgr();
		
		while(true){

			try{
				
				FileInputStream fs = new FileInputStream("config.ini");
				
				p.load(fs);
				
				sm_protocol			= p.getProperty("protocol");
				sm_host				= p.getProperty("host");
				sm_port				= Integer.valueOf(p.getProperty("port")).intValue();
				sm_strUserName		= p.getProperty("account");
				sm_strPassword		= p.getProperty("password");
				sm_strUserPassword	= p.getProperty("userPassword");
					
				
				fs.close();
				p = null;
				
				t_manger.InitConnect(sm_protocol, sm_host, sm_port, 
									sm_strUserName, sm_strPassword,sm_strUserPassword);
				
				
			}catch(Exception ex){
								
				System.out.println("Oops, got exception! " + ex.getMessage());
			    ex.printStackTrace();
			    
			    if(ex.getMessage().indexOf("Invalid credentials") != -1){
					// the password or user name is invalid..
					//
					System.out.println("the password or user name is invalid");
				}
			    
			    try{
			    	Thread.sleep(10000);
			    }catch(InterruptedException e){
			    	System.exit(0);
			    }
			}
			
			try{
				t_manger.DestroyConnect();
			}catch(Exception _e){
				System.exit(0);
			}
		}
	}
}