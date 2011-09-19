package com.yuchting.yuchberry.client;

import java.util.Vector;

public class ObjectAllocator {

	Vector m_releaseList = new Vector();
	String m_objectName	= null;
	
	public ObjectAllocator(String _objectName){
		m_objectName = _objectName;
	}
	
	public Object alloc()throws Exception{
		if(m_releaseList.isEmpty()){
			return newInstance();
		}
				
		Object t_ret = m_releaseList.elementAt(0);
		m_releaseList.removeElementAt(0);
		
		return t_ret;
	}
	
	public void release(Object _obj){
		m_releaseList.addElement(_obj);
	}
	
	protected Object newInstance()throws Exception{
		Class t_class = Class.forName(m_objectName);
		return t_class.newInstance();
	}
}
