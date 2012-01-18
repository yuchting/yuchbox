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
package com.yuchting.yuchberry.client.screen;

import javax.microedition.io.Connector;
import javax.microedition.io.file.FileConnection;

import net.rim.device.api.ui.component.LabelField;
import net.rim.device.api.ui.container.MainScreen;

import com.yuchting.yuchberry.client.recvMain;
import com.yuchting.yuchberry.client.sendReceive;

public class fileViewScreen extends MainScreen{
	
	String 	m_viewFileName		= null;
	
	recvMain	m_mainApp		= null;
	LabelField	m_pathText		= null;
	
	byte[]		m_fileContain	= null;
	
	public fileViewScreen(String _filename,recvMain _mainApp,boolean _readFile) throws Exception{
		m_viewFileName = _filename;
		m_mainApp = _mainApp;
		
		// prepare the image
		//
		m_pathText = new LabelField(_filename,LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
		add(m_pathText);
		
		if(_readFile){
			FileConnection t_fileRead = (FileConnection)Connector.open(_filename,Connector.READ);
			if(!t_fileRead.exists()){
				t_fileRead.close();
				throw new Exception(_filename + " file is not exist!");
			}
			m_fileContain = new byte[(int)t_fileRead.fileSize()];
			sendReceive.ForceReadByte(t_fileRead.openInputStream(), m_fileContain, m_fileContain.length);
			t_fileRead.close();
		}

	}
	
	public fileViewScreen(byte[] _buffer,recvMain _mainApp) throws Exception{
		m_viewFileName = "contain";
		m_mainApp = _mainApp;
		
		m_fileContain = _buffer;
	}

	public boolean onClose(){
		close();
		return true;
	}
	
}
