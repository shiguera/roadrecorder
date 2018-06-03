package com.mlab.roadrecorder.settings;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.PreferenceFragment;

import com.mlab.roadrecorder.App;
import com.mlab.roadrecorder.R;

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
				setSummary(preference, newValue.toString());
				return true;
			}
		});
		setSummary(listpref, App.getVideoResolution());
		
		EditTextPreference edit = (EditTextPreference) findPreference("mindiskspace");
		edit.setSummary("Mínimo espacio en disco para empezar a grabar " + App.getMinDiskSpaceToSave() + " Mb");
		
	}
	private void setSummary(Preference preference, String summary) {
		if(summary.equals("480")) {
			preference.setSummary("720x480 px");									
		} else if(summary.equals("720")) {
			preference.setSummary("1280x720 px");									
		} else if(summary.equals("1080")) {
			preference.setSummary("1920x1080 px");									
		} else {
			preference.setSummary("");									
		}
	}
}
