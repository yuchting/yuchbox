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
	
	static byte[]	sm_fileContain	= null;
	static int		sm_fileLength	= 0;
	
	public fileViewScreen(String _filename,boolean _readFile) throws Exception{
		m_viewFileName = _filename;
		
		// prepare the image
		//
		m_pathText = new LabelField(_filename,LabelField.ELLIPSIS | LabelField.USE_ALL_WIDTH);
		add(m_pathText);
		
		if(_readFile){
			FileConnection t_fileRead = (FileConnection)Connector.open(_filename,Connector.READ);
			try{

				if(!t_fileRead.exists()){
					t_fileRead.close();
					throw new Exception(_filename + " file is not exist!");
				}
				
				sm_fileLength = (int)t_fileRead.fileSize();
				
				if(sm_fileContain == null || sm_fileContain.length < sm_fileLength){					
					sm_fileContain = new byte[sm_fileLength];
				}
				
				sendReceive.ForceReadByte(t_fileRead.openInputStream(), sm_fileContain, sm_fileLength);
			}finally{
				t_fileRead.close();
			}			
		}

	}
	
	public fileViewScreen(byte[] _buffer) throws Exception{
		m_viewFileName = "contain";
		sm_fileContain = _buffer;
		sm_fileLength = _buffer.length;
	}

	public boolean onClose(){
		close();
		return true;
	}
	
}
