package com.yuchting.yuchberry.server.frame;

import java.awt.Dimension;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Vector;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;

import com.yuchting.yuchberry.server.fetchMgr;

class accountTableModel extends DefaultTableModel{
	
	final static String[] 	fsm_tableCol = {"账户","端口","用户密码","剩余时间（小时）","SSL 状态","当前状态"};
	final static int[] fsm_colWidth = {150,50,100,120,110,200};
	final static Object[][] fsm_tableData = {{}};
	
	
	public String getColumnName(int col) {
        return fsm_tableCol[col].toString();
    }
	    
    public int getColumnCount() { 
    	return fsm_tableCol.length; 
    }
        
    public boolean isCellEditable(int row, int col){
    	return false;
    }
    
}

public class accountTable extends JTable{
		
	mainFrame		m_mainFrame = null;
	
	final static accountTableModel m_defaultModel = new accountTableModel();
	
	Vector m_fetchMgrListRef	= new Vector();
	
	public accountTable(mainFrame _mainFrame){
		super(m_defaultModel);
				
		m_mainFrame = _mainFrame;
		
		setAutoscrolls(true);
		
		setPreferredScrollableViewportSize(new Dimension(500, 70));
		setFillsViewportHeight(true);
		
		for (int i = 0; i < accountTableModel.fsm_tableCol.length; i++) {
		    getColumnModel().getColumn(i).setPreferredWidth(accountTableModel.fsm_colWidth[i]); 		    
		}
	}
	
	public void AddAccount(final fetchThread _thread){
		
		fetchMgr _mgr = _thread.m_fetchMgr;
		
		Object[] t_row = {
				_mgr.GetAccountName(),
				new Integer(_mgr.GetServerPort()),
				_mgr.GetUserPassword(),
				new Long(_thread.m_usingHours),
				new Boolean(_mgr.IsUseSSL()),
				"",
		};
		
		m_defaultModel.addRow(t_row);
		m_fetchMgrListRef.addElement(_thread);
	}
	
	public synchronized void RefreshState(){
		
		final int t_rowNum = m_defaultModel.getRowCount();
		
		Date t_date = new Date();
		
		long t_currTime = t_date.getTime();
				
		for(int i = 0;i < t_rowNum;i++){
			fetchThread t_thread = (fetchThread)m_fetchMgrListRef.elementAt(i);
			
			m_defaultModel.setValueAt(new Long(t_thread.GetLastTime(t_currTime) / 3600000),i,3);			
			
			if(t_thread.m_pauseState){
				m_defaultModel.setValueAt("暂停",i, 5);
			}else if(t_thread.m_close){
				m_defaultModel.setValueAt("关闭",i, 5);
			}else if(t_thread.m_fetchMgr.GetClientConnected() != null){
				m_defaultModel.setValueAt("客户端连接中",i, 5);
			}else{
				String t_clientDate = "(未连接过)";
				if(t_thread.m_clientDisconnectTime != 0){	
					t_date.setTime(t_thread.m_clientDisconnectTime);
					t_clientDate = (new SimpleDateFormat("(上次链接时间 yyyy年MM月dd日 HH:mm)")).format(t_date);
				}
				
				m_defaultModel.setValueAt("监听中" + t_clientDate,i, 5);
			}
		}
	}
	
	public void DelAccount(final fetchThread _thread){
		
		final int t_rowNum = m_defaultModel.getRowCount();
		
		for(int i = 0;i < t_rowNum;i++){
			String name = (String)m_defaultModel.getValueAt(i, 0);
			if(_thread.m_fetchMgr.GetAccountName().equals(name)){
								
				m_fetchMgrListRef.remove(i);
				m_defaultModel.removeRow(i);
				
				break;
			}
		}
		
	}
}
