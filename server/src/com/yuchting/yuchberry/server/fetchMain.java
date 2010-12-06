package com.yuchting.yuchberry.server;


public class fetchMain{
		
	public static void main(String[] _arg){

		fetchMgr t_manger = new fetchMgr();
	
		while(true){

			try{		
				t_manger.InitConnect("config.ini");
			}catch(Exception ex){
								
				Logger.LogOut("Oops, got exception! " + ex.getMessage());
			    ex.printStackTrace(Logger.GetPrintStream());
			    
			    if(ex.getMessage().indexOf("Invalid credentials") != -1){
					// the password or user name is invalid..
					//
			    	Logger.LogOut("the password or user name is invalid");
				}
			    
			    try{
			    	Thread.sleep(10000);
			    }catch(InterruptedException e){
			    	System.exit(0);
			    }
			}
			
			try{
				t_manger.DestroyConnect();
			}catch(Exception _e){
				System.exit(0);
			}
		}
	}
}