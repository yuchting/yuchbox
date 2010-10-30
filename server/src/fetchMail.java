import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Date;
import java.util.Vector;

import javax.mail.Address;
import javax.mail.Flags;
import javax.mail.Message;
import javax.mail.Multipart;
import javax.mail.Part;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;


public class  fetchMail{
	
	final static int	VERSION = 1;
	    	
	final static int	ANSWERED 	= 1 << 0;
	final static int	DELETED 	= 1 << 1;
	final static int	DRAFT 		= 1 << 2;
	final static int	FLAGGED 	= 1 << 3;
	final static int	RECENT 		= 1 << 4;
	final static int	SEEN 		= 1 << 5;
	
	private int m_mailIndex = 0;
	
	private Vector<String>	m_vectFrom 		= new Vector<String>();
	private Vector<String>	m_vectReplyTo	= new Vector<String>();
	private Vector<String>	m_vectTo		= new Vector<String>();
	private Vector<String>	m_vectGroup		= new Vector<String>();
	
	private String			m_subject 		= new String();
	private Date			m_sendDate 		= new Date();
	private int			m_flags 		= 0;
	private String			m_XMailName 	= new String();
	
	private String			m_contain		= new String();
	
	private Vector<String>	m_vectAttachmentName = new Vector<String>();
	private Vector<byte[]>	m_vectAttachment= new Vector<byte[]>();
	
	
	
	public void SetMailIndex(int _index)throws Exception{
		if(_index <= 0){
			throw new Exception("SetMailIndex Negative");
		}
		m_mailIndex =_index;		
	}
	
	public int GetMailIndex(){
		return m_mailIndex;
	}
	
	public void ImportMail(Message m)throws Exception{
		
		Address[] a;
		
		// FROM 
		if ((a = m.getFrom()) != null) {
		    for (int j = 0; j < a.length; j++){
		    	m_vectFrom.addElement(a[j].toString());
		    }
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
		    for (int j = 0; j < a.length; j++){
		    	m_vectReplyTo.addElement(a[j].toString());
		    }
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
		    for (int j = 0; j < a.length; j++) {
		    	
		    	m_vectTo.addElement(a[j].toString());
			    
				InternetAddress ia = (InternetAddress)a[j];
				
				if (ia.isGroup()) {
				    InternetAddress[] aa = ia.getGroup(false);
				    for (int k = 0; k < aa.length; k++){
				    	m_vectGroup.addElement(aa[k].toString());
				    }
				}
		    }
		}
		
		m_subject 	= m.getSubject();
		m_sendDate	= m.getSentDate();
		
		Flags.Flag[] sf = m.getFlags().getSystemFlags(); // get the system flags

		for (int i = 0; i < sf.length; i++) {
		    Flags.Flag f = sf[i];
		    if (f == Flags.Flag.ANSWERED)
		    	m_flags |= ANSWERED;
		    else if (f == Flags.Flag.DELETED)
		    	m_flags |= DELETED;
		    else if (f == Flags.Flag.DRAFT)
		    	m_flags |= DRAFT;
		    else if (f == Flags.Flag.FLAGGED)
		    	m_flags |= FLAGGED;
		    else if (f == Flags.Flag.RECENT)
		    	m_flags |= RECENT;
		    else if (f == Flags.Flag.SEEN)
		    	m_flags |= SEEN;
		    else
		    	continue;	// skip it		
		}
		
		String[] hdrs = m.getHeader("X-Mailer");
		
		if (hdrs != null){
			m_XMailName = hdrs[0];
	    }
		
		ImportPart(m);	
	}
	
	private void ImportPart(Part p)throws Exception{
		
		String filename = p.getFileName();
		/*
		 * Using isMimeType to determine the content type avoids
		 * fetching the actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			
		    try{
		    	m_contain += (String)p.getContent();
		    }catch(Exception e){
		    	m_contain += "cant decode content " + e.getMessage();
		    }	    
		    
		} else if (p.isMimeType("multipart/*")) {
			
		    Multipart mp = (Multipart)p.getContent();
		    int count = mp.getCount();
		    
		    for (int i = 0; i < count; i++){
		    	ImportPart(mp.getBodyPart(i));
		    }
		    
		} else if (p.isMimeType("message/rfc822")) {

			ImportPart((Part)p.getContent());
		} else {
			/*
			 * If we actually want to see the data, and it's not a
			 * MIME type we know, fetch it and check its Java type.
			 */
			Object o = p.getContent();
			
			if (o instanceof String) {
			    
			    m_contain += (String)o;
			    
			} else if (o instanceof InputStream) {

			    InputStream is = (InputStream)o;
			    int c;
			    while ((c = is.read()) != -1){
			    	//System.out.write(c);
			    	m_contain += c;
			    }
			} else {
			    m_contain += o.toString();
			}			
		}

		/*
		 * If we're saving attachments, write out anything that
		 * looks like an attachment into an appropriately named
		 * file.  Don't overwrite existing files to prevent
		 * mistakes.
		 */
		if (!p.isMimeType("multipart") 
			&& p instanceof MimeBodyPart){
			
		    String disp = p.getDisposition();
		    
		    // many mailers don't include a Content-Disposition
		    if (disp != null && disp.equals("ATTACHMENT")) {
				if (filename == null){	
				    filename = "Attachment_" + m_vectAttachmentName.size();
				}
				
				m_vectAttachmentName.addElement(filename);
				
				ByteArrayOutputStream os = new ByteArrayOutputStream();
			    ((MimeBodyPart)p).writeTo(os);
			    
			    m_vectAttachment.add(os.toByteArray());				
		    }
		}
	}
	
	public void OutputMail(OutputStream _stream)throws Exception{
		
		_stream.write(VERSION);
		
		WriteInt(_stream,GetMailIndex());
		
		WriteStringVector(_stream,m_vectFrom);
		WriteStringVector(_stream,m_vectReplyTo);
		WriteStringVector(_stream,m_vectTo);
		WriteStringVector(_stream,m_vectGroup);
		
		WriteString(_stream,m_subject);
		WriteInt(_stream,(int)m_sendDate.getTime());
		WriteInt(_stream,(int)(m_sendDate.getTime() >>> 32));
				
		WriteInt(_stream,m_flags);
		
		WriteString(_stream,m_XMailName);
		WriteString(_stream,m_contain);
		WriteStringVector(_stream,m_vectAttachmentName);
	}
		
	public void InputMail(InputStream _stream)throws Exception{
		
		final int t_version = _stream.read();
		
		m_mailIndex = ReadInt(_stream);
		
		ReadStringVector(_stream,m_vectFrom);
		ReadStringVector(_stream,m_vectReplyTo);
		ReadStringVector(_stream,m_vectTo);
		ReadStringVector(_stream,m_vectGroup);
		
		m_subject = ReadString(_stream);
		long t_time = ReadInt(_stream);
		t_time |= ((long)ReadInt(_stream)) << 32;
		m_sendDate.setTime(t_time);
		
		m_flags = ReadInt(_stream);
		
		m_XMailName = ReadString(_stream);
		m_contain = ReadString(_stream);
		
		ReadStringVector(_stream, m_vectAttachmentName);
	}
	
	public String GetSubject(){	return m_subject;}
	public void SetSubject(String _subject){m_subject = _subject;}
	
	public String GetContain(){return m_contain;}
	public void SetContain(String _contain){m_contain = _contain;}
	
	static public void WriteStringVector(OutputStream _stream,Vector<String> _vect)throws Exception{
		
		final int t_size = _vect.size();
		_stream.write(t_size);
		
		for(int i = 0;i < t_size;i++){
			WriteString(_stream,_vect.get(i));
		}
	}
	
	static public void WriteString(OutputStream _stream,String _string)throws Exception{
		final byte[] t_strByte = _string.getBytes();
		WriteInt(_stream,t_strByte.length);
		if(t_strByte.length != 0){
			_stream.write(t_strByte);
		}		
	}
		
	static public void ReadStringVector(InputStream _stream,Vector<String> _vect)throws Exception{
		
		_vect.clear();
		
		int t_size = 0;
		t_size = _stream.read();
		
		for(int i = 0;i < t_size;i++){
			_vect.addElement(ReadString(_stream));
		}
	}
	
	static public String ReadString(InputStream _stream)throws Exception{
		
		final int len = ReadInt(_stream);
		if(len != 0){
			byte[] t_buffer = new byte[len];
			
			_stream.read(t_buffer);	
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
}