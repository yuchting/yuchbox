import java.util.*;
import java.io.*;
import javax.mail.*;
import javax.mail.internet.*;
import javax.mail.search.*;
import javax.activation.*;


public class fetchMain{
	
	static String sm_protocol 	= "imaps";
    static String sm_host 		= "imap.gmail.com";
    static int		sm_port		= 993;
    static String sm_inBox 		= "INBOX";
    
    static boolean sm_debug 		= false;
    
	static String sm_strUserName = "yuchting@gmail.com";
	static String sm_strPassword = "superman@@@";
	
	public static void main(String _arg[]){
		
		try{
			 // Get a Properties object
		    Properties props = System.getProperties();

		    // Get a Session object
		    Session session = Session.getInstance(props, null);
		    session.setDebug(sm_debug);

		    
		    if(sm_protocol == null
		    || (sm_protocol != "imap" && sm_protocol != "pop3" && sm_protocol != "pop3s" && sm_protocol != "imaps")){
		    	
		    	sm_protocol = "pop3";
		    }
		    Store store = session.getStore(sm_protocol);
		    
		    store.connect(sm_host,sm_port,sm_strUserName,sm_strPassword);
		    
		    Folder folder = store.getDefaultFolder();
		    if (folder == null) {
		    	System.out.println("Cant find default namespace");
		    	System.exit(1);
		    }
		    
		    folder = folder.getFolder(sm_inBox);
		    if (folder == null) {
				System.out.println("Invalid folder");
				System.exit(1);
		    }
		    
		    folder.open(Folder.READ_ONLY);
		   
		    int t_messageNum = folder.getMessageCount();
		    int t_availbNum = Math.min(50,t_messageNum);
		    
		    Message[] t_msgs = folder.getMessages(t_messageNum - t_availbNum + 1, t_messageNum);
		    
		    for(int i = 0;i < t_msgs.length;i++){
		    	
		    	Message t_msg = t_msgs[i];
		    	
		    	Flags flags = t_msg.getFlags();
	        	Flags.Flag[] flag = flags.getSystemFlags();  
	        	
	        	boolean t_isNew = true;
	        	for (int j = 0; j < flag.length; j++) {   
	                if (flag[j] == Flags.Flag.SEEN 
	                	&& flag[j] != Flags.Flag.DELETED
	                	&& flag[j] != Flags.Flag.DRAFT) {
	                	
	                    t_isNew = false;
	                    break;      
	                }
	            }      
	        	
		    	if(t_isNew){
		    		ShowMsg(t_msg);
		    	}else{
		    		System.out.println("Idx " + i + " message is Readed!");
		    	}
		    }
		    
			store.close();
		    
		}catch(Exception ex){
			
			System.out.println("Oops, got exception! " + ex.getMessage());
		    ex.printStackTrace();
		    
		    if(ex.getMessage().indexOf("Invalid credentials") != -1){
				// the password or user name is invalid..
				//
				System.out.println("the password or user name is invalid");
			}
		}
		
	}
	
	
	public static void ShowMsg(Message m) throws Exception{
		
		Address[] a;
		// FROM 
		if ((a = m.getFrom()) != null) {
		    for (int j = 0; j < a.length; j++)
		    System.out.println("FROM: " + a[j].toString());
		}

		// REPLY TO
		if ((a = m.getReplyTo()) != null) {
		    for (int j = 0; j < a.length; j++)
		    System.out.println("REPLY TO: " + a[j].toString());
		}

		// TO
		if ((a = m.getRecipients(Message.RecipientType.TO)) != null) {
		    for (int j = 0; j < a.length; j++) {
		    	
			    System.out.println("TO: " + a[j].toString());
			    
				InternetAddress ia = (InternetAddress)a[j];
				if (ia.isGroup()) {
				    InternetAddress[] aa = ia.getGroup(false);
				    for (int k = 0; k < aa.length; k++){
				    	System.out.println("  GROUP: " + aa[k].toString());
				    }
				}
		    }
		}
		
		ShowPart(m);		
	}
	
	public static void ShowPart(Part p) throws Exception{
		String ct = p.getContentType();
		try {
		    pr("CONTENT-TYPE: " + (new ContentType(ct)).toString());
		} catch (ParseException pex) {
		    pr("BAD CONTENT-TYPE: " + ct);
		}
		String filename = p.getFileName();
		if (filename != null)
		    pr("FILENAME: " + filename);

		/*
		 * Using isMimeType to determine the content type avoids
		 * fetching the actual content data until we need it.
		 */
		if (p.isMimeType("text/plain")) {
			
		    pr("This is plain text");
		    pr("---------------------------");
		    pr((String)p.getContent());
		    
		} else if (p.isMimeType("multipart/*")) {
			
		    pr("This is a Multipart");
		    pr("---------------------------");
		    Multipart mp = (Multipart)p.getContent();
		    int count = mp.getCount();
		    
		    for (int i = 0; i < count; i++){
		    	ShowPart(mp.getBodyPart(i));
		    }
		    
		} else if (p.isMimeType("message/rfc822")) {
		    pr("This is a Nested Message");
		    pr("---------------------------");

		    ShowPart((Part)p.getContent());
		} else {
			/*
			 * If we actually want to see the data, and it's not a
			 * MIME type we know, fetch it and check its Java type.
			 */
			Object o = p.getContent();
			if (o instanceof String) {
			    pr("This is a string");
			    pr("---------------------------");
			    System.out.println((String)o);
			} else if (o instanceof InputStream) {
			    pr("This is just an input stream");
			    pr("---------------------------");
			    InputStream is = (InputStream)o;
			    int c;
			    while ((c = is.read()) != -1)
				System.out.write(c);
			} else {
			    pr("This is an unknown type");
			    pr("---------------------------");
			    pr(o.toString());
			}			
		}

		/*
		 * If we're saving attachments, write out anything that
		 * looks like an attachment into an appropriately named
		 * file.  Don't overwrite existing files to prevent
		 * mistakes.
		 */
		if (!p.isMimeType("multipart/*")){
		    String disp = p.getDisposition();
		    int attnum = 0;
		    
		    // many mailers don't include a Content-Disposition
		    if (disp == null || disp.equalsIgnoreCase(Part.ATTACHMENT)) {
				if (filename == null)
				    filename = "Attachment_" + attnum++;
				
				pr("Saving attachment to file " + filename);
				
				try {
				    File f = new File(filename);
				    if (f.exists()){
						// XXX - could try a series of names
						throw new IOException("file exists");
				    }
				
				    ((MimeBodyPart)p).saveFile(f);
				    
				} catch (IOException ex) {
				    pr("Failed to save attachment: " + ex);
				}
				
				pr("---------------------------");
		    }
		}
	}
	
	public static void pr(String s) {		
		System.out.println(s);
	}
}