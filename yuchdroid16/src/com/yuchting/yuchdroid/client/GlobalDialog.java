package com.yuchting.yuchdroid.client;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.method.ScrollingMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

public class GlobalDialog extends Activity implements DialogInterface.OnCancelListener{
	
	// information prompt dialog
	public final static int STYLE_INFO		= 0;
	
	
	//Now edit this function
    protected Dialog onCreateDialog(int id) {
		Dialog dialog;
		switch(id) {
		case STYLE_INFO:
			final String prompt = getIntent().getExtras().getString("info");

			AlertDialog.Builder builder;

			LayoutInflater inflater = (LayoutInflater)getSystemService(LAYOUT_INFLATER_SERVICE);
			View layout = inflater.inflate(R.layout.dlg_info,null);

			TextView text = (TextView) layout.findViewById(R.id.dlg_info_text);
			text.setMovementMethod(new ScrollingMovementMethod());
			text.setText(prompt);

			builder = new AlertDialog.Builder(this);
			builder.setView(layout);
			dialog = builder.create();
			
			Button t_but = (Button)layout.findViewById(R.id.dlg_info_confirm);
			t_but.setOnClickListener(new View.OnClickListener(){
				
				@Override
				public void onClick(View v) {
					GlobalDialog.this.onCancel(null);					
				}
			});
			
			t_but = (Button)layout.findViewById(R.id.dlg_info_copy);
			t_but.setOnClickListener(new View.OnClickListener() {
				
				@Override
				public void onClick(View v) {
					YuchDroidApp.copyTextToClipboard(GlobalDialog.this, prompt);
				}
			});
			
						
			break;
		default:
			//create a default dialog
			dialog = null;
		}
		
		if(dialog != null){
			dialog.setOnCancelListener(this);
		}		
		
		return dialog;
	}
	
	public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showDialog(this.getIntent().getExtras().getInt("dialog"));
    }

	@Override
	public void onCancel(DialogInterface arg0) {
		// THIS IS VERY IMPORTANT TO REMOVE THE ACTIVITY WHEN THE DIALOG IS DISMISSED
		// IF NOT ADDED USER SCREEN WILL NOT RECEIVE ANY EVENTS BEFORE USER PRESSES BACK
		finish();
	}
	
	// show info dialog
	//
	public static void showInfo(String _promptInfo,Context _ctx){
		Intent myIntent = new Intent(_ctx, GlobalDialog.class);
		
    	Bundle bundle = new Bundle();
    	bundle.putInt("dialog", STYLE_INFO);
    	bundle.putString("info", _promptInfo);
    	
    	myIntent.putExtras(bundle);
    	myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
    	
    	_ctx.startActivity(myIntent);
	}
	
	public static void showYesNoDialog(String _promptInfo,Context _ctx,DialogInterface.OnClickListener _yesNolistener){
		AlertDialog.Builder builder = new AlertDialog.Builder(_ctx);
		builder.setMessage(_promptInfo)
			.setPositiveButton(R.string.dlg_info_yesno_confirm, _yesNolistener)
		    .setNegativeButton(R.string.dlg_info_yesno_cancel	, _yesNolistener).show();
	}
}
