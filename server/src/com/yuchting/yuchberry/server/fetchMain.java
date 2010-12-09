package com.yuchting.yuchberry.server;


public class fetchMain{
		
	public static void main(String[] _arg){

		fetchMgr t_manger = new fetchMgr();
		Logger t_logger = new Logger("");
		
		while(true){

			t_manger.InitConnect("","config.ini",t_logger);
			t_manger.StartListening();
			
		    try{
		    	Thread.sleep(10000);
		    }catch(InterruptedException e){
		    	System.exit(0);
		    }
			
			try{
				t_manger.DestroyConnect();
			}catch(Exception _e){
				System.exit(0);
			}
			
		}
	}
}