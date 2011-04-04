package com.yuchting.yuchberry.yuchsign.server;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import javax.cache.Cache;
import javax.cache.CacheException;
import javax.cache.CacheFactory;
import javax.cache.CacheManager;

public class YuchsignCache {
	
	private static Cache			sm_cacheInstance = null;
	
	static public Object getCacheYuchhostList(){
		
		List<Object> t_list = null;
		
		try {		    
		    t_list = (List<Object>)queryCache().get(yuchHost.class.getName());
		}catch (CacheException e) {
			System.err.println("fetch the YuchhostList Cache failed:"+e.getMessage());
		}
			
		return t_list;
	}
	
	static public yuchAlipay getCacheAlipay(){
		yuchAlipay t_pay = null;
		
		try {		    
			t_pay = (yuchAlipay)queryCache().get(yuchAlipay.class.getName());
		}catch (CacheException e) {
			System.err.println("fetch the Alipay Cache failed:"+e.getMessage());
		}
			
		return t_pay;
	}
	
	static public void makeCacheYuchhostList(List<yuchHost> _list){
		if(_list != null){
			try{
				ArrayList<yuchHost> t_cache = new ArrayList<yuchHost>(_list.size());
				for(yuchHost host:_list){
					t_cache.add((yuchHost)host.clone());
				}
				
				queryCache().put(yuchHost.class.getName(),t_cache);
				
			}catch(Exception e){
				System.err.println("makeCacheYuchhostList error:"+e.getMessage());
			}		
		}
	}
	
	
	
	static public void makeCacheYuchAlipay(yuchAlipay _pay){
		if(_pay != null){
			try{
				queryCache().put(yuchAlipay.class.getName(),_pay.clone());
			}catch(Exception e){
				System.err.println("makeCacheYuchhostList error:"+e.getMessage());
			}
		}		
	}
	
	static synchronized Cache queryCache()throws CacheException{
		if(sm_cacheInstance == null){
		  CacheFactory cacheFactory = CacheManager.getInstance().getCacheFactory();
		  sm_cacheInstance = cacheFactory.createCache(Collections.emptyMap());
		}
		
		return sm_cacheInstance;
	}
		
	static public void invalidCache(String _key){
		
		try{
		    queryCache().remove(_key);		    
		}catch (CacheException e) {
			System.err.println("invalid the Cache failed:"+e.getMessage());
		}
	}
}
