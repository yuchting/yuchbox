package com.yuchting.yuchberry.yuchsign.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.event.dom.client.ClickEvent;
import com.google.gwt.event.dom.client.ClickHandler;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.FlexTable;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
import com.google.gwt.user.client.ui.VerticalPanel;
import com.google.gwt.view.client.DefaultSelectionEventManager;
import com.google.gwt.view.client.ListDataProvider;
import com.google.gwt.view.client.ProvidesKey;
import com.google.gwt.view.client.SelectionModel;
import com.google.gwt.view.client.SingleSelectionModel;
import com.google.gwt.xml.client.Document;
import com.google.gwt.xml.client.Element;
import com.google.gwt.xml.client.XMLParser;

public class YuchPanel extends TabPanel{

	final TextBox	m_newHostName = new TextBox();
	final TextBox	m_newHostPort = new TextBox();
	final TextBox	m_newHostPass = new TextBox();
	final TextBox	m_newHostRecommend = new TextBox();
	
	// interface to get the value of host's attribute
	private static interface GetValue<C> {
		C getValue(yuchHost contact);
	}
	
	final ProvidesKey<yuchHost> m_privdesKey = new ProvidesKey<yuchHost>() {
	     public Object getKey(yuchHost item) {
	    	 return item == null ? null : item.m_hostName;
	     }
    };
	
	final CellTable<yuchHost> m_table		= new CellTable<yuchHost>(m_privdesKey);
	
	final List<AbstractEditableCell<?, ?>> m_editableCells = new ArrayList<AbstractEditableCell<?, ?>>();
		
	ListDataProvider<yuchHost> m_hostList 	= new ListDataProvider<yuchHost>();	
	
	Yuchsign		m_mainServer			= null;
		
	final AsyncCallback<String>	m_getListCallback	= new AsyncCallback<String>(){
		public void onFailure(Throwable caught) {
			CallOnFailed(caught);
		}

		public void onSuccess(String result){
			CallOnSuccess_list(result);
		}
	};
	
	final AsyncCallback<String> m_addCallback = new AsyncCallback<String>(){
		public void onFailure(Throwable caught){
			CallOnFailed(caught);
		}

		public void onSuccess(String result){
			CallOnSuccess_add(result);
		}
	};
	
	final AsyncCallback<String>	m_changeCallback	= new AsyncCallback<String>(){
		public void onFailure(Throwable caught) {
			CallOnFailed(caught);
		}

		public void onSuccess(String result) {}
	};
	
	public YuchPanel(Yuchsign _main){
		m_mainServer = _main;
		
		PrepareHostListPanel();
		final FlexTable t_addTable = PrepareAddHostPanel();
		final VerticalPanel t_panel = new VerticalPanel();
		t_panel.add(m_table);
		t_panel.add(t_addTable);		
		
		add(t_panel,"可用主机列表");
		
		setPixelSize(600,300);
		selectTab(0);		
		
		ShowYuchPanel();
	}
	
	public void ShowYuchPanel(){
		
		RootPanel.get("yuchTab").add(this);
		
		// query the host list
		try{
	    	m_mainServer.greetingService.getHostList(m_getListCallback);
	    }catch(Exception e){
	    	Yuchsign.PopupPrompt("get list of host exception:" + e.getMessage(),m_table);
	    }	
	}
	
	public void HideYuchPanel(){
		RootPanel.get("yuchTab").remove(this);
	}
	
	private void PrepareHostListPanel(){
		
		m_table.setWidth("100%", true);
		   
		    
	    ListHandler<yuchHost> sortHandler = new ListHandler<yuchHost>(m_hostList.getList());
	    m_table.addColumnSortHandler(sortHandler);
	    
	  
	    // simple pager
	    //SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
	    //m_pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
	    //m_pager.setDisplay(m_table);
	    
	    // Add a selection model so we can select cells.
	    final SelectionModel<yuchHost> selectionModel = new SingleSelectionModel<yuchHost>(m_privdesKey);
	    m_table.setSelectionModel(selectionModel,DefaultSelectionEventManager.<yuchHost> createDefaultManager());
	    
	   
	    
	    // 主机地址
	    //
	    addColumn(new TextCell(),"主机地址",new GetValue<String>(){
	    	public String getValue(yuchHost host){
	    		return host.m_hostName;
	    	}
	    },null);
	    
	    
	    // 主机端口
	    addColumn(new EditTextCell(),"主机端口",new GetValue<String>(){
	    	public String getValue(yuchHost host){
	    		return Integer.toString(host.m_httpPort);
	    	}
	    },new FieldUpdater<yuchHost,String>(){
	    	public void update(int index, yuchHost object, String value){
	    		object.m_httpPort = Integer.valueOf(value).intValue();
	    		
	    		final StringBuffer t_data = new StringBuffer();	    		
	    		
	    		try{
	    			object.OutputXMLData(t_data);
	    			m_mainServer.greetingService.modifyHost(object.m_hostName,t_data.toString(),m_changeCallback);
	    		}catch(Exception e){
	    			Yuchsign.PopupPrompt("change host'port exception:" + e.getMessage(),m_table);
	    		}
	    	}
	    });
	    
	    
	    // password
	    addColumn(new EditTextCell(),"访问密码",new GetValue<String>(){
	    	public String getValue(yuchHost host){
	    		return host.m_httpPassword;
	    	}
	    },new FieldUpdater<yuchHost,String>(){
	    	
	    	public void update(int index, yuchHost object, String value){
	    		
	    		object.m_httpPassword = value;
	    		
	    		final StringBuffer t_data = new StringBuffer();	    		
	    			    		
	    		try{
	    			object.OutputXMLData(t_data);
	    			m_mainServer.greetingService.modifyHost(object.m_hostName,t_data.toString(),m_changeCallback);
	    		}catch(Exception e){
	    			Yuchsign.PopupPrompt("change host'password exception:" + e.getMessage(),m_table);
	    		}
	    		
	    	}
	    });
	    
	    // recommend host sub
	    addColumn(new EditTextCell(),"建议主机",new GetValue<String>(){
	    	public String getValue(yuchHost host){
	    		return host.m_recommendHost;
	    	}
	    },new FieldUpdater<yuchHost,String>(){
	    	public void update(int index, yuchHost object, String value){
	    		
	    		object.m_recommendHost = value;
	    		
	    		final StringBuffer t_data = new StringBuffer();	    		
	    		
	    		try{
	    			object.OutputXMLData(t_data);
	    			m_mainServer.greetingService.modifyHost(object.m_hostName,t_data.toString(),m_changeCallback);
	    		}catch(Exception e){
	    			Yuchsign.PopupPrompt("change host'recommendHost exception:" + e.getMessage(),m_table);
	    		}
	    		
	    	}
	    });
	    
	    // add delete button column
	    //
	    addColumn(new ButtonCell(),"删除",new GetValue<String>(){
	    	public String getValue(yuchHost object){
	    		return "X";
	    	}
	    	
	    },new FieldUpdater<yuchHost, String>() {
	    	public void update(int index, yuchHost object, String value){

	    		final yuchHost t_finalObject = object;
	    		
	    		Yuchsign.PopupYesNoDlg("你真的要删除这个" + object.m_hostName + "主机么？",
	    				new YesNoHandler(){
	    					public void Process(){
	    						try{
	    			    			m_mainServer.greetingService.delHost(t_finalObject.m_hostName,m_changeCallback);
	    			    		}catch(Exception e){
	    			    			Yuchsign.PopupPrompt("delete host exception:" + e.getMessage(),m_table);
	    			    		}
	    			    		
	    			    		m_hostList.getList().remove(t_finalObject);	    		
	    			    		m_table.redraw();
	    					}
	    		},null);
	    		
	    	}
		});
	    
	    m_hostList.addDataDisplay(m_table);
	        
	}
	
	private final FlexTable PrepareAddHostPanel(){
		
		final FlexTable t_table = new FlexTable();
		final TextBox t_hostAddr = new TextBox();
		final TextBox t_hostPort = new TextBox();
		final TextBox t_hostPass = new TextBox();
		final TextBox t_hostRecommend = new TextBox();
		final Button  t_addHostBut	 = new Button("增加主机");

		t_hostPort.addKeyPressHandler(BberPanel.fsm_socketPortHandler);
		t_hostPort.setStyleName("gwt-TextBox-SocketPort");
		
		t_addHostBut.addClickHandler(new ClickHandler() {
			
			@Override
			public void onClick(ClickEvent event) {
				if(t_hostAddr.getText().length() == 0){
					Yuchsign.PopupPrompt("地址不能为空", t_hostAddr);
					return;
				}
				
				for(yuchHost host : m_hostList.getList()){
					if(host.m_hostName.equalsIgnoreCase(t_hostAddr.getText())){
						Yuchsign.PopupPrompt("主机地址重复", t_hostAddr);
						return;
					}
				}
				final int t_port = Integer.valueOf(t_hostPort.getText()).intValue();
				if(t_port <= 0 || t_port >= 65535 ){
					Yuchsign.PopupPrompt("端口不合法", t_hostPort);
					return ;
				}
				
				if(t_hostPass.getText().length() == 0){
					Yuchsign.PopupPrompt("通讯密码不能为空", t_hostPass);
					return ;
				}
				
				yuchHost t_host = new yuchHost();
				t_host.m_hostName = t_hostAddr.getText();
				t_host.m_httpPort = t_port;
				t_host.m_httpPassword = t_hostPass.getText();
				t_host.m_recommendHost = t_hostRecommend.getText();
				
				StringBuffer t_data = new StringBuffer();
				t_host.OutputXMLData(t_data);
				
				try{
					m_mainServer.greetingService.addHost(t_data.toString(),m_addCallback);
					
					t_hostAddr.setText("");
					t_hostPort.setText("");
					t_hostPass.setText("");
					t_hostRecommend.setText("");
					
				}catch(Exception e){
					Yuchsign.PopupPrompt("add host exception:" + e.getMessage(),m_table);
				}				
			}
		});
		
		t_hostAddr.setWidth("9em");
		t_hostPort.setWidth("5em");
		t_hostPass.setWidth("10em");
		t_hostRecommend.setWidth("10em");
		
		t_table.setWidget(0,0,t_hostAddr);
		t_table.setWidget(0,1,t_hostPort);
		t_table.setWidget(0,2,t_hostPass);
		t_table.setWidget(0,3,t_hostRecommend);
		
		t_addHostBut.setWidth("6em");
		t_table.setWidget(0,4,t_addHostBut);
		
		t_table.setPixelSize(600, 25);
	
		return t_table;
	}
	/**
	* copy from GWT Showcase
	* 
	* Add a column with a header.
	*
	* @param <C> the cell type
	* @param cell the cell used to render the column
	* @param headerText the header string
	* @param getter the value getter for the cell
	*/
	private <C> Column<yuchHost, C> addColumn(Cell<C> cell, String headerText,
			final GetValue<C> getter, FieldUpdater<yuchHost, C> fieldUpdater) {
		
		Column<yuchHost, C> column = new Column<yuchHost, C>(cell) {
			@Override
			public C getValue(yuchHost object) {
				if(getter != null){
					return getter.getValue(object);
				}else{
					return null;
				}				
			}
		};
		
		column.setFieldUpdater(fieldUpdater);
		
		if (cell instanceof AbstractEditableCell<?, ?>) {
			m_editableCells.add((AbstractEditableCell<?, ?>) cell);
		}
		
		m_table.addColumn(column, headerText);
	    return column;
	}
	  
	public void CallOnFailed(Throwable caught){
		Yuchsign.PopupPrompt("change host exception:" + caught.getMessage(),m_table);
	}
	
	public void CallOnSuccess_list(String _listXMLData){
		
		m_hostList.getList().clear();
		
		Document t_doc = XMLParser.parse(_listXMLData);
		Element t_elem = t_doc.getDocumentElement();
		
		t_elem = (Element)t_elem.getFirstChild();

		while(t_elem != null){
			
			yuchHost t_host = new yuchHost();
			try{
				t_host.InputXMLData(t_elem);
				t_elem = (Element)t_elem.getNextSibling();
				
				m_hostList.getList().add(t_host);
			}catch(Exception e){
				
			}			
		}
		
		m_hostList.refresh();
	}
	
	public void CallOnSuccess_add(String _hostXMLData){
		
				
		try{
			Document t_doc = XMLParser.parse(_hostXMLData);
			Element t_elem = t_doc.getDocumentElement();
			if(t_elem.getTagName().equals("Error")){
				Yuchsign.PopupPrompt("add host Exception:" + t_elem.getFirstChild().toString(), m_table);
			}else{
				
				yuchHost t_host = new yuchHost();
				t_host.InputXMLData(t_doc.getDocumentElement());
				m_hostList.getList().add(t_host);
				
				m_table.redraw();
			}		
			
		}catch(Exception e){
			Yuchsign.PopupPrompt("add host Exception:"+e.getMessage(), m_table);
		}		
	}
	
}


