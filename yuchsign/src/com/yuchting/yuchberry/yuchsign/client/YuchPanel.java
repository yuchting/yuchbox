package com.yuchting.yuchberry.yuchsign.client;

import java.util.ArrayList;
import java.util.List;

import com.google.gwt.cell.client.AbstractEditableCell;
import com.google.gwt.cell.client.ButtonCell;
import com.google.gwt.cell.client.Cell;
import com.google.gwt.cell.client.EditTextCell;
import com.google.gwt.cell.client.FieldUpdater;
import com.google.gwt.cell.client.TextCell;
import com.google.gwt.core.client.GWT;
import com.google.gwt.uibinder.client.UiField;
import com.google.gwt.user.cellview.client.CellTable;
import com.google.gwt.user.cellview.client.Column;
import com.google.gwt.user.cellview.client.SimplePager;
import com.google.gwt.user.cellview.client.ColumnSortEvent.ListHandler;
import com.google.gwt.user.cellview.client.SimplePager.TextLocation;
import com.google.gwt.user.client.rpc.AsyncCallback;
import com.google.gwt.user.client.ui.Button;
import com.google.gwt.user.client.ui.RootPanel;
import com.google.gwt.user.client.ui.TabPanel;
import com.google.gwt.user.client.ui.TextBox;
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
	
	final Button	m_addHostBut			= new Button();
	
	ListDataProvider<yuchHost> m_hostList 	= new ListDataProvider<yuchHost>();	

	@UiField(provided = true)
	SimplePager m_pager;
		
	
	final AsyncCallback<String>	m_getListCallback	= new AsyncCallback<String>(){
		public void onFailure(Throwable caught) {
			CallOnFailed(caught);
		}

		public void onSuccess(String result) {
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
	
	final AsyncCallback<String> m_delCallback = new AsyncCallback<String>(){
		public void onFailure(Throwable caught) {
			CallOnFailed(caught);
		}

		public void onSuccess(String result){}
	};
	
	final AsyncCallback<String> m_modifyCallback = new AsyncCallback<String>(){
		public void onFailure(Throwable caught) {
			CallOnFailed(caught);
		}

		public void onSuccess(String result){}
	};
	
	public YuchPanel(){
		AddHostListPanel();
		
		setPixelSize(400,300);
		selectTab(0);
		
		RootPanel.get("yuchTab").add(this);
	}
	
	private void AddHostListPanel(){
		
		m_table.setWidth("100%", true);
		
	    ListHandler<yuchHost> sortHandler = new ListHandler<yuchHost>(m_hostList.getList());
	    m_table.addColumnSortHandler(sortHandler);
	    
	    // simple pager
	    SimplePager.Resources pagerResources = GWT.create(SimplePager.Resources.class);
	    m_pager = new SimplePager(TextLocation.CENTER, pagerResources, false, 0, true);
	    m_pager.setDisplay(m_table);
	    
	    // Add a selection model so we can select cells.
	    final SelectionModel<yuchHost> selectionModel = new SingleSelectionModel<yuchHost>(m_privdesKey);
	    m_table.setSelectionModel(selectionModel,DefaultSelectionEventManager.<yuchHost> createCheckboxManager());
	    
	    // add delete button column
	    //
	    addColumn(new ButtonCell(),"删除",new GetValue<String>(){
	    	public String getValue(yuchHost object){
	    		return "X";
	    	}
	    	
	    },new FieldUpdater<yuchHost, String>() {
	    	public void update(int index, yuchHost object, String value){
	    		//TODO delete the host object
	    		//
	    		m_table.redraw();
	    	}
		});
	    
	    // add 主机地址
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
	    		//TODO send the change message
	    		//
	    		m_table.redraw();
	    	}
	    });
	    
	    
	    // password
	    addColumn(new EditTextCell(),"访问密码",new GetValue<String>(){
	    	public String getValue(yuchHost host){
	    		return host.m_httpPassword;
	    	}
	    },new FieldUpdater<yuchHost,String>(){
	    	
	    	public void update(int index, yuchHost object, String value){
	    		//TODO send the change message
	    		//
	    		m_table.redraw();
	    	}
	    });
	    
	    // recommend host sub
	    
		add(m_table,"Push主机");	
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
		
		m_table.redraw();
	}
	
	public void CallOnSuccess_add(String _hostXMLData){
		
	}
}


