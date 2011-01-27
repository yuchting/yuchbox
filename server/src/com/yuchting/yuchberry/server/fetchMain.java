package com.yuchting.yuchberry.server;


public class fetchMain{
		
	public static void main(String[] _arg){

		fetchMgr t_manger = new fetchMgr();
		Logger t_logger = new Logger("");
		
		new fakeMDSSvr();
		
		while(true){

			try{
				t_manger.InitConnect("",t_logger);
				t_manger.StartListening();	
			}catch(Exception e){
				t_logger.PrinterException(e);
			}			
			
		    try{
		    	Thread.sleep(10000);
		    }catch(InterruptedException e){
		    	System.exit(0);
		    }
			
			try{
				t_manger.EndListening();
			}catch(Exception _e){
				System.exit(0);
			}
		}
	}
}