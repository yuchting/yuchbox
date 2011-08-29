package com.yuchting.yuchberry.server;

import java.util.Collection;
import java.util.Vector;

import org.dom4j.Element;
import org.jivesoftware.smack.Connection;
import org.jivesoftware.smack.ConnectionConfiguration;
import org.jivesoftware.smack.Roster;
import org.jivesoftware.smack.RosterEntry;
import org.jivesoftware.smack.RosterListener;
import org.jivesoftware.smack.XMPPConnection;
import org.jivesoftware.smack.packet.Presence;
import org.jivesoftware.smackx.packet.VCard;

public class fetchGTalk extends fetchAccount implements RosterListener{
	
	String	m_accountName 		= null;
	String	m_prefix			= null;
	
	String m_password			= null;
	String m_cryptPassword		= null;
	
	Connection	m_mainConnection = null;
	
	Roster		m_roster		= null;
	
	Vector<fetchChatRoster>		m_chatRosterList = new Vector<fetchChatRoster>();
	
	public fetchGTalk(fetchMgr _mainMgr){
		super(_mainMgr);
	}
	
	public void InitAccount(Element _elem)throws Exception{
		
		m_accountName	= fetchAccount.ReadStringAttr(_elem,"account");
		m_password		= fetchAccount.ReadStringAttr(_elem,"password");
		
		m_cryptPassword	= fetchAccount.ReadStringAttr(_elem,"cryptPassword");
	}
	
	public String GetAccountName(){
		return m_accountName;
	}
	
	public void CheckFolder()throws Exception{
		
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
		
		m_mainConnection.login(t_account,m_password,"YuchBerry.info");
		
		m_roster = m_mainConnection.getRoster();
		m_roster.addRosterListener(this);
		
		synchronized (m_chatRosterList) {

			m_chatRosterList.removeAllElements();
			
			Collection<RosterEntry> t_rosterList = m_roster.getEntries();
			
			for(RosterEntry entry:t_rosterList){
				m_chatRosterList.add(convertRoster(entry));
			}	
		}
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
    		account = account.substring(0,t_slashIndex);
    	}
    	
    	synchronized (m_chatRosterList) {
    		
    		for(fetchChatRoster roster : m_chatRosterList){
    			if(roster.getAccount().toLowerCase().equals(account)){
    				setPresence(roster,presence);
    				
    				m_mainMgr.m_logger.LogOut(GetAccountName() + " presenceChanged:" + account + " Presence:" + presence);
    				
    				VCard vCard = new VCard();
    				try{
    					vCard.load(m_mainConnection, roster.getSource());
    				}catch(Exception e){
    					m_mainMgr.m_logger.PrinterException(e);
    				}
    				
    				System.out.println(vCard);
    				
    				break;
    			}
    		}
    	}
    }
	
	private fetchChatRoster convertRoster(RosterEntry _entry){
		
		fetchChatRoster roster = new fetchChatRoster();
		
		roster.setStyle(fetchChatMsg.STYLE_GTALK);
		roster.setName(_entry.getName());
		roster.setAccount(_entry.getUser());
				
		Presence t_presence = m_roster.getPresence(roster.getAccount());
		setPresence(roster, t_presence);
		
		
		
		
		return roster;
	}
	
	private void setPresence(fetchChatRoster _roster,Presence _presence){
		
		_roster.setSource(_presence.getFrom());
		_roster.setStatus(_presence.getStatus());
		
		if(_presence.isAvailable()){

			if(_presence.getMode() == Presence.Mode.available){
				_roster.setPresence(fetchChatRoster.PRESENCE_AVAIL);
			}else if(_presence.getMode() == Presence.Mode.away){
				_roster.setPresence(fetchChatRoster.PRESENCE_AWAY);
			}else if(_presence.getMode() == Presence.Mode.chat){
				_roster.setPresence(fetchChatRoster.PRESENCE_CHATTING);
			}else if(_presence.getMode() == Presence.Mode.dnd){
				_roster.setPresence(fetchChatRoster.PRESENCE_BUSY);
			}else if(_presence.getMode() == Presence.Mode.xa){
				_roster.setPresence(fetchChatRoster.PRESENCE_FAR_AWAY);
			}
			
		}else{
			_roster.setPresence(fetchChatRoster.PRESENCE_UNAVAIL);
		}
	}
	
	public void DestroySession(){
		if(m_mainConnection != null && m_mainConnection.isConnected()){
			m_mainConnection.disconnect();
		}
	}
	
	public boolean ProcessNetworkPackage(byte[] _package)throws Exception{
		return false;
	}
	
	public void PushMsg(sendReceive _sendReceive)throws Exception{
		
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
