import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.microedition.io.Connector;
import javax.microedition.io.SocketConnection;
import javax.microedition.io.file.FileConnection;

import net.rim.blackberry.api.mail.Address;
import net.rim.blackberry.api.mail.BodyPart;
import net.rim.blackberry.api.mail.Folder;
import net.rim.blackberry.api.mail.Message;
import net.rim.blackberry.api.mail.MimeBodyPart;
import net.rim.blackberry.api.mail.Multipart;
import net.rim.blackberry.api.mail.Part;
import net.rim.blackberry.api.mail.SendListener;
import net.rim.blackberry.api.mail.Session;
import net.rim.blackberry.api.mail.Store;
import net.rim.blackberry.api.mail.SupportedAttachmentPart;
import net.rim.blackberry.api.mail.TextBodyPart;
import net.rim.blackberry.api.mail.Transport;
import net.rim.blackberry.api.mail.UnsupportedAttachmentPart;
import net.rim.blackberry.api.mail.BodyPart.ContentType;
import net.rim.blackberry.api.mail.event.MessageEvent;
import net.rim.blackberry.api.mail.event.MessageListener;
import net.rim.device.api.compress.GZIPInputStream;
import net.rim.device.api.compress.GZIPOutputStream;
import net.rim.device.api.ui.Field;
import net.rim.device.api.ui.FieldChangeListener;
import net.rim.device.api.ui.UiApplication;
import net.rim.device.api.ui.component.ButtonField;
import net.rim.device.api.ui.component.Dialog;
import net.rim.device.api.ui.component.EditField;
import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

class msg_head{
	
	final public static byte msgMail = 0;
	final public static byte msgSendMail = 1;

	final public static byte msgConfirm = 2;
	final public static byte msgNote = 3;
	
	final public static byte msgBeenRead = 4;

}

class sendReceive extends Thread{
	
	OutputStream		m_socketOutputStream = null;
	InputStream			m_socketInputStream = null;
	
	private Vector		m_unsendedPackage 		= new Vector();
	private Vector		m_unprocessedPackage 	= new Vector();
	
	boolean			m_closed				= false;
		
	public sendReceive(OutputStream _socketOut,InputStream _socketIn){
		m_socketOutputStream = _socketOut;
		m_socketInputStream = _socketIn;
		
		start();
	}
		
	//! send buffer
	public synchronized void SendBufferToSvr(byte[] _write,boolean _sendImm)throws Exception{	
		m_unsendedPackage.addElement(_write);
		
		if(_sendImm){
			SendBufferToSvr_imple(PrepareOutputData());
		}
	}
	
	public void CloseSendReceive(){
		
		if(m_closed = false){
			m_closed = true;
	
			while(isAlive()){
				try{
					sleep(10);
				}catch(Exception _e){};			
			}	
		}		
	}
	
	private synchronized byte[] PrepareOutputData()throws Exception{
		
		if(m_unsendedPackage.isEmpty()){
			return null;
		}
		
		ByteArrayOutputStream t_stream = new ByteArrayOutputStream();
				
		for(int i = 0;i < m_unsendedPackage.size();i++){
			byte[] t_package = (byte[])m_unsendedPackage.elementAt(i);	
			
			WriteInt(t_stream, t_package.length);
						
			t_stream.write(t_package);
		}
		
		m_unsendedPackage.removeAllElements();
		
		return t_stream.toByteArray();
	}
	
	//! send buffer implement
	private void SendBufferToSvr_imple(byte[] _write)throws Exception{
		
		if(_write == null){
			return;
		}		
		
		OutputStream os = m_socketOutputStream;
		
		ByteArrayOutputStream zos = new ByteArrayOutputStream();
		GZIPOutputStream zo = new GZIPOutputStream(zos);
		zo.write(_write);
		zo.close();	
		
		byte[] t_zipData = zos.toByteArray();
		
		if(t_zipData.length > _write.length){
			// if the ZIP data is large than original length
			// NOT convert
			//
			WriteInt(os,_write.length << 16);
			os.write(_write);
			
		}else{
			WriteInt(os,(_write.length << 16) | t_zipData.length);
			os.write(t_zipData);
			
		}
				
	}
	
	public void run(){
		
		try{
			
			while(!m_closed){
				SendBufferToSvr_imple(PrepareOutputData());
				sleep(500);
			}
			
		}catch(Exception _e){}
		
		try{
			m_socketOutputStream.close();
			m_socketInputStream.close();	
		}catch(Exception _e){}
	}

	//! recv buffer
	public byte[] RecvBufferFromSvr()throws Exception{
		
		if(!m_unprocessedPackage.isEmpty()){
			byte[] t_ret = (byte[])m_unprocessedPackage.elementAt(0);
			m_unprocessedPackage.removeElementAt(0);
			
			return t_ret;
		}
		
		InputStream in = m_socketInputStream;

		int t_len = ReadInt(in);
		
		final int t_ziplen = t_len & 0x0000ffff;
		final int t_orglen = t_len >>> 16;
				
		byte[] t_orgdata = new byte[t_orglen];
				
		if(t_ziplen == 0){
			
			ForceReadByte(in, t_orgdata, t_orglen);
			
		}else{
			
			byte[] t_zipdata = new byte[t_ziplen];
			
			ForceReadByte(in, t_zipdata, t_ziplen);
			
			GZIPInputStream zi	= new GZIPInputStream(
										new ByteArrayInputStream(t_zipdata));

			ForceReadByte(zi,t_orgdata,t_orglen);
			
			zi.close();
		}
		
		byte[] t_ret = ParsePackage(t_orgdata);
		t_orgdata = null;
		
		return t_ret;
	}
	
	private byte[] ParsePackage(byte[] _wholePackage)throws Exception{
		
		ByteArrayInputStream t_packagein = new ByteArrayInputStream(_wholePackage);
		int t_len = ReadInt(t_packagein);
					
		byte[] t_ret = new byte[t_len];
		t_packagein.read(t_ret,0,t_len);
		
		t_len += 4;
		
		while(t_len < _wholePackage.length){
			
			final int t_packageLen = ReadInt(t_packagein); 
			
			byte[] t_package = new byte[t_packageLen];
			
			t_packagein.read(t_package,0,t_packageLen);
			t_len += t_packageLen + 4;
			
			m_unprocessedPackage.addElement(t_package);			
		}		
		
		return t_ret;		
	}
	// static function to input and output integer
	//
	static public void WriteStringVector(OutputStream _stream,Vector _vect)throws Exception{
		
		final int t_size = _vect.size();
		WriteInt(_stream,t_size);
		
		for(int i = 0;i < t_size;i++){
			WriteString(_stream,(String)_vect.elementAt(i));
		}
	}
	
	static public void WriteString(OutputStream _stream,String _string)throws Exception{
		final byte[] t_strByte = _string.getBytes();
		WriteInt(_stream,t_strByte.length);
		if(t_strByte.length != 0){
			_stream.write(t_strByte);
		}
	}
	
		
	static public void ReadStringVector(InputStream _stream,Vector _vect)throws Exception{
		
		_vect.removeAllElements();
		
		final int t_size = ReadInt(_stream);
				
		for(int i = 0;i < t_size;i++){
			_vect.addElement(ReadString(_stream));
		}
	}
	
	static public String ReadString(InputStream _stream)throws Exception{
		
		final int len = ReadInt(_stream);
		
		if(len != 0){
			byte[] t_buffer = new byte[len];
			
			ForceReadByte(_stream,t_buffer,len);

			return new String(t_buffer);
		}
		
		return new String("");
		
	}
	
	static public int ReadInt(InputStream _stream)throws Exception{
		return _stream.read() | (_stream.read() << 8) | (_stream.read() << 16) | (_stream.read() << 24);
	}

	static public void WriteInt(OutputStream _stream,int _val)throws Exception{
		_stream.write(_val);
		_stream.write(_val >>> 8 );
		_stream.write(_val >>> 16);
		_stream.write(_val >>> 24);
	}
	
	static public void ForceReadByte(InputStream _stream,byte[] _buffer,int _readLen)throws Exception{
		int t_readIndex = 0;
		while(_readLen > t_readIndex){
			final int t_c = _stream.read(_buffer,t_readIndex,_readLen - t_readIndex);
			if(t_c > 0){
				t_readIndex += t_c;
			}			 
		}
	}
	
}



class  fetchMail{
	
	final static int	VERSION = 1;
	    	
	final static int	ANSWERED 	= 1 << 0;
	final static int	DELETED 	= 1 << 1;
	final static int	DRAFT 		= 1 << 2;
	final static int	FLAGGED 	= 1 << 3;
	final static int	RECENT 		= 1 << 4;
	final static int	SEEN 		= 1 << 5;
	
	private int m_mailIndex = 0;
	
	private Vector		m_vectFrom 		= new Vector();
	private Vector		m_vectReplyTo	= new Vector();
	private Vector		m_vectTo		= new Vector();
	private Vector		m_vectGroup		= new Vector();
	
	private String			m_subject 		= new String();
	private Date			m_sendDate 		= new Date();
	private int			m_flags 		= 0;
	private String			m_XMailName 	= new String();
	
	private String			m_contain		= new String();
	private String			m_contain_html		= new String();
	
	private Vector	m_vectAttachmentName = new Vector();
	private Vector	m_vectAttachment= new Vector();
	
	private Message m_attachMessage	= null; 
	
	
	public void SetMailIndex(int _index)throws Exception{
		if(_index <= 0){
			throw new Exception("SetMailIndex Negative");
		}
		m_mailIndex =_index;		
	}
	
	public int GetMailIndex(){
		return m_mailIndex;
	}
	
	public void SetAttchMessage(Message m){ m_attachMessage = m;}
	public Message GetAttchMessage(){return m_attachMessage;}
		
	public static Address[] parseAddressList(Vector _list)throws Exception{
		Address[] 	t_addressList = new Address[_list.size()];
		
		for(int i = 0;i < _list.size();i++){
			String add = (String)_list.elementAt(i);
			String t_name = null;
			
			final int t_start =add.indexOf('<');
			final int t_end = add.indexOf('>');
			
			final int t_start_quotation = add.indexOf('"');
			final int t_end_quotation = add.indexOf('"',t_start_quotation + 1);
			
			if(t_start_quotation != -1 && t_end_quotation != -1 ){			
				t_name = add.substring(t_start_quotation + 1, t_end_quotation);
			}else{
				t_name = "";
			}
			
			if(t_start != -1 && t_end != -1 ){			
				add = add.substring(t_start + 1, t_end);
			}else{
				t_name = "";
			}
			
			t_addressList[i] = new Address(add,t_name);
		}
		
		return t_addressList;
	}
	
	
	public void OutputMail(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		
		sendReceive.WriteInt(_stream,GetMailIndex());
		
		sendReceive.WriteStringVector(_stream,m_vectFrom);
		sendReceive.WriteStringVector(_stream,m_vectReplyTo);
		sendReceive.WriteStringVector(_stream,m_vectTo);
		sendReceive.WriteStringVector(_stream,m_vectGroup);
		
		sendReceive.WriteString(_stream,m_subject);
		sendReceive.WriteInt(_stream,(int)m_sendDate.getTime());
		sendReceive.WriteInt(_stream,(int)(m_sendDate.getTime() >>> 32));
				
		sendReceive.WriteInt(_stream,m_flags);
		
		sendReceive.WriteString(_stream,m_XMailName);
		sendReceive.WriteString(_stream,m_contain);
		sendReceive.WriteString(_stream,m_contain_html);
		sendReceive.WriteStringVector(_stream,m_vectAttachmentName);
	}
		
	public void InputMail(InputStream _stream)throws Exception{
		
		final int t_version = _stream.read();
		
		m_mailIndex = sendReceive.ReadInt(_stream);
		
		sendReceive.ReadStringVector(_stream,m_vectFrom);
		sendReceive.ReadStringVector(_stream,m_vectReplyTo);
		sendReceive.ReadStringVector(_stream,m_vectTo);
		sendReceive.ReadStringVector(_stream,m_vectGroup);
		
		m_subject = sendReceive.ReadString(_stream);
		long t_time = sendReceive.ReadInt(_stream);
		t_time |= ((long)sendReceive.ReadInt(_stream)) << 32;
		m_sendDate.setTime(t_time);
		
		m_flags = sendReceive.ReadInt(_stream);
		
		m_XMailName = sendReceive.ReadString(_stream);
		m_contain = sendReceive.ReadString(_stream);
		m_contain_html = sendReceive.ReadString(_stream);
		
		sendReceive.ReadStringVector(_stream, m_vectAttachmentName);
	}
	
	//set and gets function
	//
	public String GetSubject(){	return m_subject;}
	public void SetSubject(String _subject){m_subject = _subject;}
	
	public String GetContain(){return m_contain;}
	public void SetContain(String _contain){m_contain = _contain;}
	
	public String GetContain_html(){return m_contain_html;}
	public void SetContain_html(String _contain_html){m_contain_html = _contain_html;}
	
	public String GetXMailer(){return m_XMailName;}
	public void SetXMailer(String _str){m_XMailName = _str;}
	
	public Date GetSendDate(){return m_sendDate;}
	public void SetSendDate(Date _d){m_sendDate = _d;}
	
	public int GetFlags(){return m_flags;}
	public void SetFlags(int _flags){m_flags = _flags;}
	
	public void SetSendToVect(String[] _to){
		m_vectTo.removeAllElements();
		for(int i = 0;i < _to.length;i++){
			m_vectTo.addElement(_to[i]);
		}		
	}
	public Vector GetSendToVect(){return m_vectTo;}
	
	public void SetReplyToVect(String[] _replyTo){
		m_vectReplyTo.removeAllElements();
		for(int i = 0;i < _replyTo.length;i++){
			m_vectReplyTo.addElement(_replyTo[i]);
		}		
	}
	public Vector GetReplyToVect(){return m_vectReplyTo;}
	
	public Vector GetFromVect(){return m_vectFrom;}
	public void SetFromVect(String[] _from){
		m_vectFrom.removeAllElements();
		for(int i = 0;i < _from.length;i++){
			m_vectFrom.addElement(_from[i]);
		}		
	}
	
	public Vector GetGroupVect(){return m_vectGroup;}
	public void SetGroupVect(String[] _group){
		m_vectGroup.removeAllElements();
		for(int i = 0;i < _group.length;i++){
			m_vectGroup.addElement(_group[i]);
		}
	}
	
	public void AddAttachment(String _name,byte[] _buffer)throws Exception{
		if(_name == null || _name.length() <= 0){
			throw new Exception("Error Attachment format!");
		}
		
		m_vectAttachment.addElement(_buffer);
		m_vectAttachmentName.addElement(_name);		
	}
	public void ClearAttachment(){
		m_vectAttachment.removeAllElements();
		m_vectAttachmentName.removeAllElements();
	}
	public Vector GetAttachment(){
		return m_vectAttachment;
	}
	public Vector GetAttachmentFilename(){
		return m_vectAttachmentName;
	}
}
 
class connectDeamon extends Thread{
	
	class AppendMessage{
		int		m_mailIndex;
		Date	m_date;
		String	m_from;
	}
	
	 sendReceive		m_connect = null;
	 	 
	 static HelloWorldScreen	m_screen = null;
	 
	 String				m_hostname;
	 int				m_hostport;
	 String				m_userPassword;
	 
	 FileConnection		m_keyfile;
	 
	 SocketConnection	m_conn = null;
	 
	 Vector				m_sendingMail = new Vector();
	 
	 boolean			m_disconnect = true;
	
	 public Vector 		m_markReadVector = new Vector();
	 
	 // read the email temporary variables
	 //
	 private boolean			m_hasSupportedAttachment = false;
	 private boolean			m_hasUnsupportedAttachment = false;
	 private String			m_plainTextContain = new String();
	 private String			m_htmlTextContain = new String();
	 
	 
	 public connectDeamon(HelloWorldScreen _screen){
		 m_screen = _screen;	 
		 start();
	 }
	 	 
	 
	 public void run(){
		 		 
		while(true){

			while(m_disconnect == true){
				try{
					sleep(100);
				}catch(Exception _e){}
			}
			
			try{

				m_conn = GetConnection(false);
				m_connect = new sendReceive(m_conn.openOutputStream(),m_conn.openInputStream());
							
				// send the Auth info
				//
				ByteArrayOutputStream t_os = new ByteArrayOutputStream();
				t_os.write(msg_head.msgConfirm);
				sendReceive.WriteString(t_os, m_userPassword);
				
				m_connect.SendBufferToSvr(t_os.toByteArray(), true);
				
				// set the text connect
				//
				m_screen.m_stateText.setText("connected.");
				
				while(true){
					ProcessMsg(m_connect.RecvBufferFromSvr());
				}
				
			}catch(Exception _e){
				
				try{
					m_screen.DialogAlert("yuchBerry Exception: \n"+ _e.getMessage());
					m_screen.m_stateText.setText("disconnected retry later...");
					
				}catch(Exception e){}				
			}		
						
			synchronized (this) {
				try{
					if(m_conn != null){
						m_conn.close();
						m_conn = null;
					}
					
					if(m_connect == null){
						m_connect.CloseSendReceive();
						m_connect = null;
					}				
					
				}catch(Exception _e){}
			}		
			
			try{
				sleep(10000);
			}catch(Exception _e){}
			
		}
		
	 }
	 
	 public void Connect(String _host,int _port,String _userPassword){

		 m_hostname		= _host;
		 m_hostport		= _port;
		 m_userPassword = _userPassword;

		synchronized (this) {
			m_disconnect = false;
		}
	 }
	 
	 public boolean IsConnected(){
		 return !m_disconnect;
	 }
	 
	 public void Disconnect()throws Exception{
		 
		 m_disconnect = true;
		 
		 synchronized (this) {

			 if(m_conn != null){			 
				 m_conn.close();
				 m_conn = null; 
			 }
			 
			 if(m_connect != null){
				 m_connect.CloseSendReceive();
				 m_connect = null;
			 }	 
		 }
	 }
	 
	 private SocketConnection GetConnection(boolean _ssl)throws Exception{
		 
		 String URL ;
		 
		 if(_ssl){
			 URL =  "ssl://" + m_hostname + ":" + m_hostport + ";deviceside=true";
		 }else{
			 URL =  "socket://" + m_hostname + ":" + m_hostport + ";deviceside=true";
		 }
		 
		 SocketConnection socket = (SocketConnection)Connector.open(URL,Connector.READ_WRITE,false);
		 
		 socket.setSocketOption(SocketConnection.DELAY, 0);
		 socket.setSocketOption(SocketConnection.LINGER, 0);
		 socket.setSocketOption(SocketConnection.KEEPALIVE, 2);
		 socket.setSocketOption(SocketConnection.RCVBUF, 128);
		 socket.setSocketOption(SocketConnection.SNDBUF, 128);
		 
		 return socket;
	 }
	 
	 private synchronized void ProcessMsg(byte[] _package)throws Exception{
		 ByteArrayInputStream in  = new ByteArrayInputStream(_package);
		 
		 final int t_msg_head = in.read();
		 
		 switch(t_msg_head){
		 	case msg_head.msgMail:
		 		final Message m = new Message();
		 		
		 		fetchMail t_mail = new fetchMail();
		 		t_mail.InputMail(in);
				
				try{
					
					ComposeMessage(m,t_mail);
					
					Store store = Session.waitForDefaultSession().getStore();
					Folder folder = store.getFolder(Folder.INBOX);
					m.setInbound(true);
					m.setStatus(Message.Status.RX_RECEIVED,1);
					folder.appendMessage(m);
					
					AppendMessage t_app = new AppendMessage();
					t_app.m_date = m.getSentDate();
					t_app.m_from = m.getFrom().getAddr();
					t_app.m_mailIndex = t_mail.GetMailIndex();
					
					// add the message listener to send message to server
					// to remark the message is read
					//
					m.addMessageListener(m_screen);
					m_markReadVector.addElement(t_app);
							
				}catch(Exception _e){
					m_screen.DialogAlert("ComposeMessage error :\n" + _e.getMessage());
				}			
						 		
		 		break;
		 	case msg_head.msgSendMail:
		 		ProcessSentMail(in);
		 		break;
		 	case msg_head.msgNote:
		 		String t_string = sendReceive.ReadString(in);
		 		m_screen.DialogAlert(t_string);
		 		break;
		 }
	 }
	 
	public synchronized void ProcessSentMail(ByteArrayInputStream in)throws Exception{
		
		boolean t_succ = (in.read() == 1);
	
		long t_time = sendReceive.ReadInt(in);
		t_time |= ((long)sendReceive.ReadInt(in) << 32);
			
		
		for(int i = 0;i< m_sendingMail.size();i++){
			fetchMail t_sending = (fetchMail)m_sendingMail.elementAt(i);
			if(t_sending.GetSendDate().getTime() == t_time){
				if(t_succ){
					t_sending.GetAttchMessage().setStatus(Message.Status.TX_DELIVERED, 1);
				}else{
					t_sending.GetAttchMessage().setStatus(Message.Status.TX_ERROR, 1);
				}
				
				m_sendingMail.removeElementAt(i);
				
				break;
			}
		}
	}
	 
	public synchronized void AddSendingMail(fetchMail _mail)throws Exception{
			
		
		for(int i = 0;i < m_sendingMail.size();i++){
			fetchMail t_sending = (fetchMail)m_sendingMail.elementAt(i);
			if(t_sending.GetSendDate().equals(_mail.GetSendDate())){
				return;				 
			}
		}
		 
		m_sendingMail.addElement(_mail);
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgMail);
		_mail.OutputMail(os);
		
		m_connect.SendBufferToSvr(os.toByteArray(), false);
		
	}
	
	public synchronized void AddMarkReadMail(Message m){
		
		for(int i = 0;i < m_markReadVector.size();i++){
		
			try{
				
				AppendMessage t_mail = (AppendMessage)m_markReadVector.elementAt(i);
				
				if(t_mail.m_date.equals(m.getSentDate())
					&& t_mail.m_from.equals(m.getFrom().getAddr())){
					
					ByteArrayOutputStream t_os = new ByteArrayOutputStream();
					t_os.write(msg_head.msgBeenRead);
					sendReceive.WriteInt(t_os, t_mail.m_mailIndex);
					
					m_connect.SendBufferToSvr(t_os.toByteArray(), false);
					
					m_markReadVector.removeElementAt(i);
				}
									
			}catch(Exception _e){
				break;
			}				
		}
	}
	
	

	
	public void ImportMail(Message m,fetchMail _mail)throws Exception{
		
		_mail.SetAttchMessage(m);
		
		Address[] a;
		
		// FROM 
		if (m.getFrom() != null) {
			_mail.GetFromVect().removeAllElements();
			_mail.GetFromVect().addElement(m.getFrom().getAddr());
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
			_mail.GetFromVect().removeAllElements();
		    for (int j = 0; j < a.length; j++){
		    	_mail.GetFromVect().addElement(composeAddress(a[j]));
		    }
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
			_mail.GetSendToVect().removeAllElements();
			_mail.GetGroupVect().removeAllElements();
			
		    for (int j = 0; j < a.length; j++) {
		    	_mail.GetSendToVect().addElement(composeAddress(a[j]));
		    }
		}		
		
		_mail.SetSubject(m.getSubject());
		_mail.SetSendDate(m.getSentDate());
		
		final int t_flags = m.getFlags(); // get the system flags

		int t_setFlags = 0;
		if((t_flags & Message.Flag.DELETED) != 0){
			t_setFlags |= fetchMail.DELETED;
		}
		
		if((t_flags & Message.Flag.SAVED) != 0){
			t_setFlags |= fetchMail.SEEN;
		}
		
		String[] hdrs = m.getHeader("X-Mailer");
		
		if (hdrs != null){
			_mail.SetXMailer(hdrs[0]);
	    }
		
		_mail.GetAttachmentFilename().removeAllElements();
		_mail.GetAttachment().removeAllElements();
		
		m_plainTextContain = "";
		m_htmlTextContain	= "";
		
		findEmailBody(m.getContent(),_mail);
		
		_mail.SetContain(m_plainTextContain);
		_mail.SetContain_html(m_htmlTextContain); 
	}
	
	private String composeAddress(Address a){
		if(a.getName() != null){
    		return "\"" + a.getName() +"\" <" + a.getAddr() + ">";
    	}else{
    		return a.getAddr();
    	}
	}
	
	
	void findEmailBody(Object obj,fetchMail _mail)throws Exception{

	   //Reset the attachment flags.
		m_hasSupportedAttachment = false;
	   m_hasUnsupportedAttachment = false;
	   	   
	   if(obj instanceof Multipart)
	   {
	      Multipart mp = (Multipart)obj;
	    
	      for(int count=0; count < mp.getCount(); ++count)
	      {
	        findEmailBody(mp.getBodyPart(count),_mail);
	      }
	   }
	   else if (obj instanceof TextBodyPart)
	   {
	      TextBodyPart tbp = (TextBodyPart) obj;
	      readEmailBody(tbp,_mail);
	   }
	   else if (obj instanceof MimeBodyPart)
	   {
		   MimeBodyPart mbp = (MimeBodyPart)obj;
		   
	      if (mbp.getContentType().indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1)
	      {
	        readEmailBody(mbp,_mail);
	      }
	      else if (mbp.getContentType().equals(ContentType.TYPE_MULTIPART_MIXED_STRING) ||
				   mbp.getContentType().equals(ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING))
		   {    
		      //The message has attachments or we are at the top level of the message.
			   //Extract all of the parts within the MimeBodyPart message.
		      findEmailBody((MimeBodyPart)(mbp.getContent()),_mail);
		   }
	   }	   
	   else if (obj instanceof SupportedAttachmentPart)  
	   {
	      m_hasSupportedAttachment = true;
	   }
	
	   else if (obj instanceof UnsupportedAttachmentPart) 
	   {
	      m_hasUnsupportedAttachment = true;
	   }
		
	}
	
	private void readEmailBody(MimeBodyPart mbp,fetchMail _mail)throws Exception
	{
	   //Extract the content of the message.
	   Object obj = mbp.getContent();
	   String mimeType = mbp.getContentType();
	   String body = null;
   
	   if (obj instanceof String)
	   {
	      body = (String)obj;
	   }
	   else if (obj instanceof byte[])
	   {
	      body = new String((byte[])obj);
	   }else{
		   
		   throw new Exception("error MimeBodyPart Contain type");
	   }

	   if (mimeType.indexOf(ContentType.TYPE_TEXT_PLAIN_STRING) != -1)
	   {
		   
		  m_plainTextContain = m_plainTextContain.concat(body);

	      //Determine if all of the text body part is present.
	      if (mbp.hasMore() && !mbp.moreRequestSent())
	      {
	         try
	         {
	            Transport.more((BodyPart)mbp, true);
	         }
	         catch (Exception ex)
	         {
	        	 m_screen.DialogAlert("Exception: " + ex.toString());
	         }
	      }
	   }
	  
	   else if (mimeType.indexOf(ContentType.TYPE_TEXT_HTML_STRING) != -1)
	   {
		   m_plainTextContain = m_htmlTextContain.concat(body);

	      //Determine if all of the HTML body part is present.
	      if (mbp.hasMore() && !mbp.moreRequestSent())
	      {
	         try
	         {
	            Transport.more((BodyPart)mbp, true);
	         }
	         catch (Exception ex)
	         { 
	        	 m_screen.DialogAlert("Exception: " + ex.toString());
	         }
	      }
	   }
	}
	
	private void readEmailBody(TextBodyPart tbp,fetchMail _mail)
	{
		m_plainTextContain = m_plainTextContain.concat((String)tbp.getContent());

	   if (tbp.hasMore() && !tbp.moreRequestSent())
	   {
	      try
	      {
	         Transport.more((BodyPart)tbp, true);
	      }
	      catch (Exception ex)
	      {
	    	  m_screen.DialogAlert("Exception: " + ex.toString());
	      }
	   }
	}
	
	static public void ComposeMessage(Message msg,fetchMail _mail)throws Exception{
		
		_mail.SetAttchMessage(msg);
		
		msg.setFrom(fetchMail.parseAddressList(_mail.GetFromVect())[0]);
				
	    msg.addRecipients(Message.RecipientType.TO,
	    				fetchMail.parseAddressList(_mail.GetSendToVect()));
	    
	    if (!_mail.GetReplyToVect().isEmpty()){
	    	  msg.addRecipients(Message.RecipientType.CC,
	    				fetchMail.parseAddressList(_mail.GetReplyToVect()));
	    }
	    
	    if(!_mail.GetGroupVect().isEmpty()){
	    	 msg.addRecipients(Message.RecipientType.BCC,
	    				fetchMail.parseAddressList(_mail.GetGroupVect()));
	    }
		

	    msg.setSubject(_mail.GetSubject());

	    
	    if(_mail.GetContain_html().length() != 0
	    	|| !_mail.GetAttachment().isEmpty()) {

	    	Multipart multipart = new Multipart(ContentType.TYPE_MULTIPART_ALTERNATIVE_STRING);
	    	
	    	TextBodyPart t_text = new TextBodyPart(multipart,_mail.GetContain());
	    	multipart.addBodyPart(t_text);
	    	
	    	if(_mail.GetContain_html().length() != 0){

	    		TextBodyPart t_text1 = new TextBodyPart(multipart,_mail.GetContain_html());
		    	t_text1.setContentType(ContentType.TYPE_TEXT_HTML_STRING);
		    	
	    	}
	    	
	    	if(!_mail.GetAttachment().isEmpty()){
	    		
	    		Vector t_filename = _mail.GetAttachmentFilename();
				Vector t_contain = _mail.GetAttachment();
				
		    	for(int i = 0;i< t_contain.size();i++){
		    		SupportedAttachmentPart attach = new SupportedAttachmentPart( multipart,
							"application/x-example", (String)t_filename.elementAt(i), (byte[])t_contain.elementAt(i));
		    		
		    		multipart.addBodyPart(attach);
		    	}
	    	}
	    	
	    	msg.setContent(multipart);
				
	    } else {
	    	
			msg.setContent(_mail.GetContain());
	    }

	    msg.setHeader("X-Mailer",_mail.GetXMailer());
	    msg.setSentDate(_mail.GetSendDate());	      
	    
	}
	 
}
 

public class recvMain extends UiApplication {
        public static void main(String[] args) {
                recvMain theApp = new recvMain();
                theApp.enterEventDispatcher();
        }
   
        public recvMain() {
                pushScreen(new HelloWorldScreen());
        }
}


final class HelloWorldScreen extends MainScreen implements FieldChangeListener,
																SendListener,
																MessageListener{
										
        
    EditField                       m_hostName      = null;
    EditField						m_hostport		= null;
    EditField                       m_userPassword  = null;
    
    ButtonField                     m_connectBut    = null;
    LabelField                      m_stateText     = null;
    
    connectDeamon					m_connectDeamon	= null; 
            
        
    public HelloWorldScreen() {
        
        super();
        
        // create the sdcard path 
        //
//        try{
//        	FileConnection fc = (FileConnection) Connector.open("file:///SDCard/YuchBerry",Connector.READ_WRITE);
//        	if(!fc.exists()){
//        		fc.mkdir();
//        	}
//        }catch(Exception _e){
//        	
//        	Dialog.alert("can't use the SDCard!");
//        	System.exit(0);
//        }
        
        m_hostName = new EditField("hostname:","",128, EditField.FILTER_DEFAULT);
        m_hostName.setChangeListener(this);
        add(m_hostName);
        
        m_hostport = new EditField("port:","",5, EditField.FILTER_INTEGER);
        m_hostport.setChangeListener(this);
        add(m_hostport);
        
        m_userPassword = new EditField("userpassword:","",128, EditField.FILTER_DEFAULT);
        add(m_userPassword);
        
        m_connectBut = new ButtonField("connect",ButtonField.CONSUME_CLICK
                                                                | ButtonField.NEVER_DIRTY);
        m_connectBut.setChangeListener(this);
        
        add(m_connectBut);              
        
        m_stateText = new LabelField("disconnect", LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
        add(m_stateText);

        
        try{
        	// add the send listener
            //
            Store store = Session.getDefaultInstance().getStore();
            store.addSendListener(this);
            
        }catch(Exception _e){
        	DialogAlert("Error to listen INBOX");
        }
        
    }
    
    public void DialogAlert(final String _msg){
    	
    	UiApplication.getUiApplication().invokeLater(new Runnable() 
		{
		    public void run() {
		       Dialog.alert(_msg);
		    }
		});
    	
    }
    
    public boolean sendMessage(Message message){
    	if(m_connectDeamon != null){

        	fetchMail t_mail = new fetchMail();
        	try{
        		m_connectDeamon.ImportMail(message, t_mail);
        		t_mail.SetSendDate(new Date());
        		
        		message.setStatus(Message.Status.TX_SENDING,1);
        		m_connectDeamon.AddSendingMail(t_mail);
        		
        	}catch(Exception _e){

        		return false;
        	}	
    	}
    	
    	return false;
    }
    
    public void changed(MessageEvent e){
    	
    	if(e.getMessageChangeType() == MessageEvent.UPDATED
    	|| e.getMessageChangeType() == MessageEvent.OPENED){
    		
    		try{
    			if(m_connectDeamon != null){
    				m_connectDeamon.AddMarkReadMail(e.getMessage());
        			e.getMessage().removeMessageListener(this);
    			}    			
    		}catch(Exception _e){}
    	}
    }
          
    public boolean onClose() {
    	
        return true;
    }
        
    public void fieldChanged(Field field, int context) {
        if(context != FieldChangeListener.PROGRAMMATIC){
			// Perform action if user changed field. 
			//
			if(field == m_connectBut){
				
				if(/*m_hostName.getText().length() == 0 
					|| m_userPassword.getText().length() == 0
					|| m_hostport.getText().length() == 0*/false){
					
					Dialog.alert("the host name or port or user password is null");
					
					return;
				}
				
				if(m_connectDeamon == null){
					m_connectDeamon = new connectDeamon(this);	
				}
												
				if(m_connectDeamon.IsConnected()){
					
					try{
						m_connectDeamon.Disconnect();
					}catch(Exception _e){}
					
					m_connectBut.setLabel("connect");
					m_stateText.setText("disconnect");
					
				}else{
										
					m_connectDeamon.Connect(m_hostName.getText(),Integer.valueOf(m_hostport.getText()).intValue(),m_userPassword.getText());
					//m_connectDeamon.Connect("192.168.10.20",9716,"111111");
					
					m_stateText.setText("connect....");
					m_connectBut.setLabel("disconnect");				
				}				
			}
        }else{
        	// Perform action if application changed field.
        }
    }
    
}
