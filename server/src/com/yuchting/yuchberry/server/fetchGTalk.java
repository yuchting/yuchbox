package com.yuchting.yuchberry.server;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.InputStream;
import java.util.Collection;
import java.util.Date;
import java.util.Random;
import java.util.Vector;

import javax.imageio.ImageIO;

import org.dom4j.Element;
import org.jivesoftware.smack.Chat;
import org.jivesoftware.smack.ChatManager;
import org.jivesoftware.smack.ChatManagerListener;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.MessageListener;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Message;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;


public class fetchGTalk extends fetchAccount implements RosterListener,
															ChatManagerListener,
															MessageListener{
		

	public final static	String fsm_ybClientSource = "YuchBerry.info";
	
	String	m_accountName 		= null;
	String	m_prefix			= null;
	
	String m_password			= null;
	String m_cryptPassword		= null;
	
	Connection	m_mainConnection = null;
	
	Roster		m_roster		= null;
	ChatManager	m_chatManager	= null;
	
	Vector<fetchChatRoster>		m_chatRosterList = new Vector<fetchChatRoster>();
		
	final class ChatData{		
		Chat			m_chatData;
		long			m_lastActiveTime;
		
		boolean		m_isYBClient;
		
		int				m_chatState 		= fetchChatMsg.CHAT_STATE_COMMON;
		int				m_chatState_sent	= fetchChatMsg.CHAT_STATE_COMMON;
		
		public ChatData(Chat _data,long _time){
			m_chatData = _data;
			m_lastActiveTime = _time;
			
			m_isYBClient = _data.getParticipant().indexOf(fsm_ybClientSource) != -1;
		}
	}
	
	Vector<ChatData>			m_chatList = new Vector<ChatData>();
	
	Vector<fetchChatMsg>		m_pushedChatMsgList = new Vector<fetchChatMsg>();
	
	public fetchGTalk(fetchMgr _mainMgr){
		super(_mainMgr);
	}
	
	public void InitAccount(Element _elem)throws Exception{
		
		m_accountName	= fetchAccount.ReadStringAttr(_elem,"account");
		m_password		= fetchAccount.ReadStringAttr(_elem,"password");
		
		m_cryptPassword	= fetchAccount.ReadStringAttr(_elem,"cryptPassword");
		
		File t_file  = new File(GetAccountPrefix());
		if(!t_file.exists() || !t_file.isDirectory()){
			t_file.mkdir();
		}
	}
	
	public String GetAccountName(){
		return m_accountName;
	}
	
	
	public String GetAccountPrefix(){
		return m_accountName + "[GTalk]/";
	}
	
	
	public void ResetSession(boolean _fullTest)throws Exception{
		
		if(m_mainConnection == null){
			// Create a connection to the jabber.org server on a specific port.
			//
			ConnectionConfiguration t_config = new ConnectionConfiguration("talk.google.com", 5222,"gmail.com");
			t_config.setSASLAuthenticationEnabled(false);
			
			m_mainConnection = new XMPPConnection(t_config);
		}
		
		m_mainConnection.connect();
				
		String t_account = null;
		int t_index;
		if((t_index = m_accountName.toLowerCase().indexOf("@gmail.com")) != -1){
			t_account = m_accountName.substring(0,t_index);
		}else{
			t_account = m_accountName;
		}
		
		m_mainConnection.login(t_account,m_password,fsm_ybClientSource + "-" + (new Random()).nextInt(1000));
		
		m_chatManager = m_mainConnection.getChatManager();
		m_chatManager.addChatListener(this);
		
		m_roster = m_mainConnection.getRoster();
		m_roster.addRosterListener(this);
		
		synchronized (m_chatRosterList) {

			m_chatRosterList.removeAllElements();
			
			Collection<RosterEntry> t_rosterList = m_roster.getEntries();
			
			for(RosterEntry entry:t_rosterList){
				m_chatRosterList.add(convertRoster(entry));
			}	
		}
		
		m_mainMgr.m_logger.LogOut(GetAccountPrefix() + " prepare OK!");
		
		ClientDisconnected();
	}
	

	/**
	 * client is connected to server
	 */
	public void ClientConnected(){
		Presence t_presence = new Presence(Presence.Type.available);
		t_presence.setMode(Presence.Mode.available);
		
		m_mainConnection.sendPacket(t_presence);
	}
	
	/**
	 * client is disconnected from the server
	 */
	public void ClientDisconnected(){
		Presence t_presence = new Presence(Presence.Type.available);
		t_presence.setMode(Presence.Mode.xa);
		
		m_mainConnection.sendPacket(t_presence);
	}
	
	public void entriesAdded(Collection<String> addresses){
    	synchronized (m_chatRosterList) {
    		
    		addRosterListener_flag:
    		for(String acc:addresses){
    			
    			for(fetchChatRoster roster : m_chatRosterList){
    				if(roster.getAccount().toLowerCase().equals(acc)){
    					
	    				continue addRosterListener_flag;
	    			}
	    		}
    			
    			RosterEntry t_entry = m_roster.getEntry(acc);
    			
    			m_chatRosterList.add(convertRoster(t_entry));
    			
    			m_mainMgr.m_logger.LogOut(GetAccountName() + " entriesAdded:" + acc);
    		}
    		
    		
    	}
    }
    public void entriesDeleted(Collection<String> addresses){
    	
    	synchronized (m_chatRosterList) {
    		for(String acc:addresses){
    			
    			for(fetchChatRoster roster : m_chatRosterList){
    				if(roster.getAccount().toLowerCase().equals(acc)){
    					
	    				m_chatRosterList.remove(roster);
	    				
	    				m_mainMgr.m_logger.LogOut(GetAccountName() + " entriesDeleted:" + acc);
	    				
	    				break;
	    			}
	    		}
    		}
    		
    	}
    }
    
    public void entriesUpdated(Collection<String> addresses){
    	
    }
    
    public void presenceChanged(Presence presence) {
    	
    	//That will return the presence value for the user with the highest priority and availability.
    	//
    	presence = m_roster.getPresence(presence.getFrom());
    	
    	String account = presence.getFrom();
    	int t_slashIndex = account.indexOf('/');
    	if(t_slashIndex != -1){
    		// parse the JID to the account name;
    		//
    		account = account.substring(0,t_slashIndex);
    	}
    	
    	synchronized (m_chatRosterList) {
    		
    		for(fetchChatRoster roster : m_chatRosterList){
    			if(roster.getAccount().toLowerCase().equals(account)){
    				setPresence(roster,presence);
    				
    				m_mainMgr.m_logger.LogOut(GetAccountName() + " presenceChanged:" + account + " Presence:" + presence);
   
    				storeHeadImage(roster);
    				
    				break;
    			}
    		}
    	}
    }
    
    final static String fsm_chatStateNamespace = "http://jabber.org/protocol/chatstates";
    
    public void chatCreated(Chat chat, boolean createdLocally){
    	
        if(!createdLocally){
        	        	
        	chat.addMessageListener(this);
            
            synchronized(m_chatList){
            	
				for(ChatData data:m_chatList){
					
					if(data.m_chatData.getParticipant().equals(chat.getParticipant())){
						
						data.m_lastActiveTime = (new Date()).getTime();
						
						return;
					}
				}
				
				m_chatList.add(new ChatData(chat, (new Date()).getTime()));
			}            
        }
    }
    
    public void processMessage(Chat chat, Message message){
        
    	int t_state = -1;
    	
		if(message.getBody() == null){

			if(message.getExtension("composing", fsm_chatStateNamespace) != null){
				
				// <message type='chat' id='purple296e7714' to='yuchberry@gmail.com'>
				//   <composing xmlns='http://jabber.org/protocol/chatstates'/>
				// </message>
				//			
				System.out.println(chat.getParticipant() + " composing");
				
				t_state = fetchChatMsg.CHAT_STATE_COMPOSING;
				
			}else if(message.getExtension("paused", fsm_chatStateNamespace) != null){
				
				// <message type='chat' id='purple296e7715' to='yuchberry@gmail.com'>
				//   <paused xmlns='http://jabber.org/protocol/chatstates'/>
				// </message>
				//
				System.out.println(chat.getParticipant() + " paused");
				
				t_state = fetchChatMsg.CHAT_STATE_COMMON;
			}
			
		}else{
			//TODO send to client...
    		//
			fetchChatMsg msg = convertChat(chat, message);
			sendClientChatMsg(msg,true);
			
			synchronized (m_pushedChatMsgList) {
				m_pushedChatMsgList.add(msg);				
			}		
		}
		
		synchronized(m_chatList){
			
			for(ChatData data:m_chatList){
				
				if(data.m_chatData.getParticipant().equals(chat.getParticipant())){
					
					data.m_lastActiveTime = (new Date()).getTime();
					
					if(t_state != -1){
						data.m_chatState = t_state;
					}
					
					break;
				}
			}
		}
    }
    
    private fetchChatMsg convertChat(Chat _chat, Message message){
    	
    	fetchChatMsg t_msg = new fetchChatMsg();
    	
    	t_msg.setStyle(fetchChatMsg.STYLE_GTALK);
    	t_msg.setMsg(message.getBody());
    	
    	String t_owner = _chat.getParticipant().toLowerCase();
    	int t_slash = t_owner.indexOf('/');
    	if(t_slash != -1){
    		t_owner = t_owner.substring(0,t_slash);
    	}
    	
    	t_msg.setOwner(t_owner);
    	
    	t_msg.setSendTime((new Date()).getTime());
    	
    	return t_msg;
    }
    
    private boolean sendClientChatMsg(fetchChatMsg _msg,boolean _imm){
    	
    	if(!m_mainMgr.isClientConnected()){
    		return false;
    	}
    	
    	try{
    		
    		ByteArrayOutputStream os = new ByteArrayOutputStream();
    		os.write(msg_head.msgChat);
    		
    		_msg.Output(os);
    		
    		m_mainMgr.SendData(os, _imm);
    		
    		return true;
    		
    	}catch(Exception e){
    		m_mainMgr.m_logger.PrinterException(e);
    	}
    	
    	return false;
    	
    }
    
	private fetchChatRoster convertRoster(RosterEntry _entry){
		
		fetchChatRoster roster = new fetchChatRoster();
		
		roster.setStyle(fetchChatMsg.STYLE_GTALK);
		roster.setAccount(_entry.getUser().toLowerCase());
		
		String t_name = _entry.getName();
		roster.setName(t_name == null?roster.getAccount():t_name);
						
		Presence t_presence = m_roster.getPresence(roster.getAccount());
		setPresence(roster, t_presence);
				
		return roster;
	}
	
	private void setPresence(fetchChatRoster _roster,Presence _presence){
		
		_roster.setSource(_presence.getFrom());
		_roster.setStatus(_presence.getStatus());				
		
		if(_presence.isAvailable()){

			_roster.setPresence(fetchChatRoster.PRESENCE_AVAIL);
			
			if(_presence.getMode() == Presence.Mode.away){
				_roster.setPresence(fetchChatRoster.PRESENCE_AWAY);
			}else if(_presence.getMode() == Presence.Mode.dnd){
				_roster.setPresence(fetchChatRoster.PRESENCE_BUSY);
			}else if(_presence.getMode() == Presence.Mode.xa){
				_roster.setPresence(fetchChatRoster.PRESENCE_FAR_AWAY);
			}
			
		}else{
			_roster.setPresence(fetchChatRoster.PRESENCE_UNAVAIL);
		}
	}
	
	private void storeHeadImage(fetchChatRoster _roster){
		
		File t_headImageFile_l = new File(GetAccountPrefix() + _roster.getAccount() + "_l.jpg");
		File t_headImageFile = new File(GetAccountPrefix() + _roster.getAccount() + ".jpg");
		
		long t_currentTime = (new Date()).getTime();
		
		if(!t_headImageFile_l.exists() 
			|| !t_headImageFile.exists()
			|| Math.abs(t_currentTime - t_headImageFile_l.lastModified()) > 5 * 24 * 3600000){
			
			VCard vCard = new VCard();
			try{
				
				vCard.load(m_mainConnection, _roster.getSource());
				
				if(vCard.getAvatar() != null){
					
					writeHeadImage(ImageIO.read(new ByteArrayInputStream(vCard.getAvatar())),
									getImageType(vCard.getAvatar()),
									t_headImageFile_l,t_headImageFile);
				}		
				
			}catch(Exception e){
				m_mainMgr.m_logger.PrinterException(e);	
			}
		}
		
		if(m_mainMgr.m_clientDisplayWidth <= 320){
			_roster.setHeadImageHashCode((int)t_headImageFile.length());
		}else{
			_roster.setHeadImageHashCode((int)t_headImageFile_l.length());
		}
	}
	
	public void DestroySession(){
		if(m_mainConnection != null && m_mainConnection.isConnected()){
			m_mainConnection.disconnect();
		}
	}
	
	public boolean ProcessNetworkPackage(byte[] _package)throws Exception{
		
		ByteArrayInputStream in = new ByteArrayInputStream(_package);
		
		boolean t_processed = false;
		
		final int t_head = in.read();
		switch(t_head){
			case msg_head.msgChat:
				t_processed = ProcessMsgChat(in);
				break;
			case msg_head.msgChatRosterList:
				t_processed = true;
				// fetch all IM fetchAccount roster to return
				//
				ProcessMsgRosterList();
				break;
			case msg_head.msgChatConfirm:
				t_processed = ProcessMsgChatConfirm(in);
				break;
		}
		
		return t_processed;
	}
	
	private void ProcessMsgRosterList()throws Exception{
		
		Vector<fetchChatRoster> t_rosterList = new Vector<fetchChatRoster>();
		
		for(fetchAccount t_acc: m_mainMgr.m_fetchAccount){
			if(t_acc instanceof fetchGTalk){
				((fetchGTalk)t_acc).loadRosterList(t_rosterList);
			}
		}
		
		ByteArrayOutputStream os = new ByteArrayOutputStream();
		os.write(msg_head.msgChatRosterList);
		os.write(0);
		
		sendReceive.WriteInt(os,t_rosterList.size());
		
		for(fetchChatRoster roster:t_rosterList){
			roster.Outport(os);
		}
		
		m_mainMgr.SendData(os, true);		
	}
	
	private void loadRosterList(Vector<fetchChatRoster> _rosterList){
		synchronized (m_chatRosterList) {
			for(fetchChatRoster roster: m_chatRosterList){
				_rosterList.add(roster);
			}
		}
	}
	
	private boolean ProcessMsgChatConfirm(InputStream in)throws Exception{
		int t_style = in.read();
				
		if(t_style == fetchChatMsg.STYLE_GTALK){
			int t_hashCode = sendReceive.ReadInt(in);
			
			synchronized (m_pushedChatMsgList) {
				for(fetchChatMsg msg:m_pushedChatMsgList){
					if(msg.hashCode() == t_hashCode){
						
						m_pushedChatMsgList.remove(msg);
						
						m_mainMgr.m_logger.LogOut(GetAccountPrefix() + " confirm " + t_hashCode);
						
						return true;
					}
				}
			}
		}
		
		return false;
	}
	
	private boolean ProcessMsgChat(InputStream in)throws Exception{
		int t_style = in.read();
		
		if(t_style == fetchChatMsg.STYLE_GTALK){
			
			String from = sendReceive.ReadString(in);
			
			if(from.equals(m_accountName)){

				String to = sendReceive.ReadString(in);
				String message = sendReceive.ReadString(in);
				
				int t_filelen = sendReceive.ReadInt(in);
				if(t_filelen != 0){
					int t_type = in.read();
					
					//TODO read from local to uploading file
					//
				}
				
				synchronized (m_chatList){
					
					for(ChatData data:m_chatList){
						if(data.m_chatData.getParticipant().equals(to)){
							
							data.m_chatData.sendMessage(message);
							data.m_lastActiveTime = (new Date()).getTime();
							
							return true;
						}
					}
				}
				
				return true;
			}
		}
		
		return false;
	}
	
	public void CheckFolder()throws Exception{
		if(!m_mainConnection.isConnected()){
			
			m_mainMgr.m_logger.LogOut(GetAccountPrefix() + " disconnected reset it.");
			
			ResetSession(true);
		}
	}
	
	public void PushMsg(sendReceive _sendReceive)throws Exception{

		long t_currTime = (new Date()).getTime();
		
		synchronized(m_chatList){
         	
			for(ChatData data : m_chatList){
				
				if(data.m_chatState != data.m_chatState_sent){
					
					// TODO send to client this chat state
					//
				}
			}
		}
		
		synchronized (m_pushedChatMsgList) {
			
			for(int i = 0 ;i < m_pushedChatMsgList.size();i++){
				fetchChatMsg msg = m_pushedChatMsgList.elementAt(i);
				
				if(msg.m_sentTimes >= 5){
					
					m_mainMgr.m_logger.LogOut(GetAccountPrefix() + " sent msg 5 times give up!" + msg.hashCode());
					m_pushedChatMsgList.remove(i);
					
					i--;					
					continue;
				}
				
				if(Math.abs(msg.m_sendTime - t_currTime) > 2 * 60000) {
					
					if(sendClientChatMsg(msg,false)){
						
						m_mainMgr.m_logger.LogOut(GetAccountPrefix() + " sent msg again..." + msg.hashCode());
						
						msg.m_sentTimes++;
					}
				}
			}
		}
	}
	
	public static void main(String[] _arg)throws Exception{
		try{

			Connection.DEBUG_ENABLED = true;
			
			fetchMgr t_manger = new fetchMgr();
			Logger t_logger = new Logger("");
			
			t_logger.EnabelSystemOut(true);
			t_manger.InitConnect("",t_logger);
			
			fetchGTalk t_talk = new fetchGTalk(t_manger);
			
			t_talk.m_accountName = "yuchdroid@gmail.com";
			t_talk.m_password = "hF8IBrCmBDQsKaWa";
			
			t_talk.ResetSession(true);
			
			System.out.println("OK");
			
			Thread.sleep(500000);
			
		}catch (Exception e) {
			e.printStackTrace();
		}
	}
}
