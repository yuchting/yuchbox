package com.yuchting.yuchdroid.client;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

public class Yuchdroid16Activity extends Activity {
    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.main);
        
        Intent intent = new Intent(ConnectDeamon.CONNECTIVITY_SERVICE);
        intent.setClass(Yuchdroid16Activity.this, ConnectDeamon.class);
        
        startService(intent);
    }
}