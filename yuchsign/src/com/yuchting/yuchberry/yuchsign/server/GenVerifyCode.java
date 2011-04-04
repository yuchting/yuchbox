package com.yuchting.yuchberry.yuchsign.server;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;

public class GenVerifyCode {

	final static int		fsm_imageSize = 48;
	
	final static String	fsm_numberCacheKeyPrefix = "VerfiyCode_";
	
	static public byte[] generate(HttpServletRequest _request){
		int[] t_randStr = new int[6];
		
		ArrayList<Composite> t_compositeImage = new ArrayList<Composite>();
		
		t_randStr[0] = (new Random()).nextInt(8) + 1;
		
		t_compositeImage.add(getNumberImage(t_randStr[0],_request));
		
		for(int i = 1;i < t_randStr.length;i++){
			t_randStr[i] = (new Random()).nextInt(9);
			
			
		}
		
		
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

		ImagesService imagesService = ImagesServiceFactory.getImagesService();
		Image oldImage = ImagesServiceFactory.makeImage(_imageData);
	}
	
	
}

