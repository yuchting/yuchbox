package com.yuchting.yuchberry.yuchsign.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.net.URLEncoder;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.images.CompositeTransform;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;

public class GenVerifyCode implements Serializable{

	final static int		fsm_imageSize = 48;
	final static String	fsm_numberCacheKeyPrefix = "VerfiyCode_";	
	
	private String m_currVerifyCode = "";
	
	public boolean compareCode(String _accountName,String _code){
		if(m_currVerifyCode.isEmpty() || _code == null || _code.isEmpty()){
			return false;
		}
		
		return m_currVerifyCode.equals(_code);
	}
	
	public String generate(HttpServletRequest _request)throws Exception{
//		int[] t_randStr = new int[6];
//		
//		ArrayList<Composite> t_compositeImage = new ArrayList<Composite>();
//		
//		
//		t_randStr[0] = (new Random()).nextInt(8) + 1;
//		
//		t_compositeImage.add();
//		
//		for(int i = 1;i < t_randStr.length;i++){
//			t_randStr[i] = (new Random()).nextInt(9);
//			
//			
//		}
		
		m_currVerifyCode = "0";
		
		byte[] t_result =  getNumberImage(0,_request).getImageData();
		
	    return "data:image/png;base64,"+ URLEncoder.encode(com.google.gwt.user.server.Base64Utils.toBase64(t_result),"UTF-8"); 
		
	}
	
	static private Image getNumberImage(int _number,HttpServletRequest _request)throws Exception{
		
		byte[] t_data = (byte[])YuchsignCache.queryCache().get(fsm_numberCacheKeyPrefix + _number);
		if(t_data == null){
			
			File t_file = new File(_request.getSession().getServletContext().getRealPath(Integer.toString(_number) + ".gif"));
			if(t_file.exists()){
				FileInputStream t_is = new FileInputStream(t_file);
				try{

					t_data = new byte[(int)t_file.length()];
					t_is.read(t_data);
					
					YuchsignCache.queryCache().put(fsm_numberCacheKeyPrefix + _number,t_data);
				}finally{
					t_is.close();
				}
				
				
			}else{
				throw new Exception("compose image is not exist!");
			}
		}
		
		return transfromImage(t_data);
	}
	
	static private Image transfromImage(byte[] _imageData){

		//ImagesService t_imagesService = ImagesServiceFactory.getImagesService();
		Image t_oldImage = ImagesServiceFactory.makeImage(_imageData);
		
		Random t_rand = new Random();
		Transform t_resize = ImagesServiceFactory.makeResize(t_rand.nextInt(10) + fsm_imageSize - 5, t_rand.nextInt(10) + fsm_imageSize - 5);
		//Transform t_rotate = ImagesServiceFactory.makeRotate(t_rand.nextInt(20) - 10);
		
		CompositeTransform t_comp = ImagesServiceFactory.makeCompositeTransform();
		t_comp.preConcatenate(t_resize);
		//t_comp.preConcatenate(t_rotate);
		
		return t_oldImage;//t_imagesService.applyTransform(t_comp, t_oldImage,OutputEncoding.PNG);		
	}
	
	
}

