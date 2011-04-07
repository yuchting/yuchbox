package com.yuchting.yuchberry.yuchsign.server;

import java.io.File;
import java.io.FileInputStream;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Random;

import javax.servlet.http.HttpServletRequest;

import com.google.appengine.api.images.Composite;
import com.google.appengine.api.images.Image;
import com.google.appengine.api.images.ImagesService;
import com.google.appengine.api.images.ImagesServiceFactory;
import com.google.appengine.api.images.Transform;
import com.google.appengine.api.images.ImagesService.OutputEncoding;

public class GenVerifyCode implements Serializable{

	final static int		fsm_imageSize = 32;
	final static String	fsm_numberCacheKeyPrefix = "VerfiyCode_";	
	
	private String			m_currVerifyCode = "";
	private byte[]			m_imageVerifyCode = null;
	
	
	public byte[] getVerfiyCodeImageData(){
		return m_imageVerifyCode;
	}
	
	static public boolean compareCode(String _accountName,String _code){
		
		try{
			String t_cachePrefix = fsm_numberCacheKeyPrefix + _accountName;
			GenVerifyCode t_code = (GenVerifyCode)YuchsignCache.queryCache().get(t_cachePrefix);
			
			if(t_code == null || _code == null || _code.isEmpty()){
				return false;
			}
									
			return t_code.m_currVerifyCode.equals(_code);
			
		}catch(Exception ex){
			return false;
		}
		
	}	
	
	
	static public String generate(String _accountName,HttpServletRequest _request)throws Exception{
		
		final int t_stringNum = 5;
		
		int[] t_randStr = new int[t_stringNum];
		
		Random t_rand = new Random();
		t_randStr[t_stringNum - 1] = t_rand.nextInt(8) + 1;
		
		for(int i = t_stringNum - 2;i >= 0;i--){
			t_randStr[i] = t_rand.nextInt(9);
		}		
		
		ArrayList<Composite> t_compositeImage = new ArrayList<Composite>();
		
		GenVerifyCode t_code = new GenVerifyCode();
		
		int t_xOffset = 0;
		boolean t_zero = false;
		for(int i = t_stringNum - 1;i >= 0;i--){
			
			if(t_randStr[i] != 0){
				t_compositeImage.add(getNumberImage(Integer.toString(t_randStr[i]),t_xOffset,_request));
				t_xOffset += fsm_imageSize;
				
				if(i % 4 != 0){
					t_compositeImage.add(getNumberImage(getBitName(i),t_xOffset,_request));
					t_xOffset += fsm_imageSize;	
				}
				
				t_zero = false;
			}else{
				if(!t_zero){
					
					if(i % 4 != 0){
						boolean t_needZero = false;
						for(int j = i - 1;j >= 0;j--){
							if(t_randStr[j] !=0){
								t_needZero = true;
								break;
							}
						}
						
						if(t_needZero){
							t_compositeImage.add(getNumberImage("0",t_xOffset,_request));
							t_xOffset += fsm_imageSize;	
						}		
					}
					
					t_zero = true;
				}
			}
			
			if(i == 4){
				t_compositeImage.add(getNumberImage("wan",t_xOffset,_request));
				t_xOffset += fsm_imageSize;
			}
			
			t_code.m_currVerifyCode = t_code.m_currVerifyCode + Integer.toString(t_randStr[i]);
		}
		
		ImagesService t_imagesService = ImagesServiceFactory.getImagesService();
		Image t_wholeImage = t_imagesService.composite(t_compositeImage,t_xOffset,fsm_imageSize,0xffffffffl);
		t_code.m_imageVerifyCode = t_wholeImage.getImageData();
		
	    YuchsignCache.queryCache().put(fsm_numberCacheKeyPrefix + _accountName,t_code);
	    
	    // append a random parameter to avoid the client cached
	    //
	    return "/verifycode?acc="+_accountName+"&rand=" + t_rand.nextInt();	
	}
	
	static public String getBitName(int _bit){
		switch(_bit % 4){
		case 1: return "shi";
		case 2: return "bai";
		case 3: return "qian";
		default: return "0";
		}
	}
	
	static private Composite getNumberImage(String _number,int _xOffset,HttpServletRequest _request)throws Exception{
		
		byte[] t_data = (byte[])YuchsignCache.queryCache().get(fsm_numberCacheKeyPrefix + _number);
		if(t_data == null){
			
			File t_file = new File(_request.getSession().getServletContext().getRealPath(_number + ".png"));
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
		
		Random t_rand = new Random();
		float t_capacity = t_rand.nextFloat();
		if(t_capacity < 0.1f){
			t_capacity = 0.1f;
		}
		
		final int ft_offset = 12;
		
		return ImagesServiceFactory.makeComposite(transfromImage(t_data),
													_xOffset + t_rand.nextInt(ft_offset) - ft_offset / 2,
													t_rand.nextInt(ft_offset) - ft_offset / 2,
													t_capacity,
													Composite.Anchor.TOP_LEFT);
	}
	
	static private Image transfromImage(byte[] _imageData){

		ImagesService t_imagesService = ImagesServiceFactory.getImagesService();
		Image t_oldImage = ImagesServiceFactory.makeImage(_imageData);
		
		Random t_rand = new Random();
		final int ft_size = 16;
		Transform t_resize = ImagesServiceFactory.makeResize(t_rand.nextInt(ft_size) + fsm_imageSize - ft_size/2,
															t_rand.nextInt(ft_size) + fsm_imageSize - ft_size/2);
		
		return t_imagesService.applyTransform(t_resize, t_oldImage,OutputEncoding.PNG);	
	}
	
	
}

