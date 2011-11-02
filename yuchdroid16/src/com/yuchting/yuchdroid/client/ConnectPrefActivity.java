package com.yuchting.yuchdroid.client;

import android.os.Bundle;
import android.preference.PreferenceActivity;

public class ConnectPrefActivity extends PreferenceActivity {

	@Override
    public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.login_preference);
	}
}
