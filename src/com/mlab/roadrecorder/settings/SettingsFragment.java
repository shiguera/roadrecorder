package com.mlab.roadrecorder.settings;

import android.os.Bundle;
import android.preference.PreferenceFragment;

import com.mlab.roadrecorder.alvac.R;

public class SettingsFragment extends PreferenceFragment {
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		this.addPreferencesFromResource(R.xml.prefs);
	}
}
