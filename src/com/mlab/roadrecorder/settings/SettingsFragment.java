package com.mlab.roadrecorder.settings;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import com.mlab.roadrecorder.alvac.R;

public class SettingsFragment extends PreferenceFragment {
	private final Logger LOG = Logger.getLogger(SettingsFragment.class);
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		addPreferencesFromResource(R.xml.prefs);
		ListPreference listpref = (ListPreference) findPreference("videoresolution");
		
		listpref.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
			@Override
			public boolean onPreferenceChange(Preference preference, Object newValue) {
				LOG.debug("onPreferenceChange() " + newValue);
				return true;
			}
		});
	}
	
}
