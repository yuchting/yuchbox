package com.yuchting.yuchdroid.client;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.io.Writer;
import java.lang.Thread.UncaughtExceptionHandler;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.content.Context;
import android.util.Log;

public class ExceptionHandler implements UncaughtExceptionHandler{
	
	public static final String fsm_stackstraceSuffix = ".stacktrace";
	
	private String STORE_FILES_PATH = ".";
	private UncaughtExceptionHandler defaultExceptionHandler;
    
    private static final String TAG = "UNHANDLED_EXCEPTION";

    // constructor
    public ExceptionHandler(UncaughtExceptionHandler _formerExceptionHandler,Context _ctx){
    	STORE_FILES_PATH = _ctx.getFilesDir().getAbsolutePath();
        defaultExceptionHandler = _formerExceptionHandler;
    }
     
    // Default exception handler
    public void uncaughtException(Thread t, Throwable e){
        
        try{
        	
        	// Here you should have a more robust, permanent record of problems
            final Writer result = new StringWriter();
            final PrintWriter printWriter = new PrintWriter(result);
            e.printStackTrace(printWriter);
            
            String t_resultStr = result.toString();
            System.err.println(TAG + t_resultStr);
            
            // Random number to avoid duplicate files
        	//
        	SimpleDateFormat t_timeformat = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss_");
            String filename = t_timeformat.format(new Date()) + YuchDroidApp.fsm_appVersion ;
            
            System.err.println(TAG+"Writing unhandled exception to: " + STORE_FILES_PATH + "/"+ filename + fsm_stackstraceSuffix);
            
            // Write the stacktrace to disk
            //
            BufferedWriter bos = new BufferedWriter(new FileWriter(STORE_FILES_PATH+"/" + filename + fsm_stackstraceSuffix));
            try{
            	bos.write(filename + ":\n");
	            bos.write(t_resultStr);
                bos.flush();
            }finally{
            	bos.close();
            }
            
        }catch(Exception ebos){
            // Nothing much we can do about this - the game is over
            ebos.printStackTrace();
        }
        
        //call original handler
        //
        defaultExceptionHandler.uncaughtException(t, e);        
    }
    
    public static void registerHandler(Context _ctx){
    	
    	UncaughtExceptionHandler currentHandler = Thread.getDefaultUncaughtExceptionHandler();
        if(currentHandler != null){
        	Log.d(TAG, "current handler class="+currentHandler.getClass().getName());
        }
        
        // don't register again if already registered
        //
        if(!(currentHandler instanceof ExceptionHandler)){
            // Register default exceptions handler
        	//
            Thread.setDefaultUncaughtExceptionHandler(new ExceptionHandler(currentHandler,_ctx));
        }
    }
}
