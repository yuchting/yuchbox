/**
 *  Dear developer:
 *  
 *   If you want to modify this file of project and re-publish this please visit:
 *  
 *     http://code.google.com/p/yuchberry/wiki/Project_files_header
 *     
 *   to check your responsibility and my humble proposal. Thanks!
 *   
 *  -- 
 *  Yuchs' Developer    
 *  
 *  
 *  
 *  
 *  尊敬的开发者：
 *   
 *    如果你想要修改这个项目中的文件，同时重新发布项目程序，请访问一下：
 *    
 *      http://code.google.com/p/yuchberry/wiki/Project_files_header
 *      
 *    了解你的责任，还有我卑微的建议。 谢谢！
 *   
 *  -- 
 *  语盒开发者
 *  
 */
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
