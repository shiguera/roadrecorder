package com.mlab.roadrecorder.settings;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

import com.mlab.roadrecorder.App;
import com.mlab.roadrecorder.alvac.R;

public class SettingsActivity extends Activity {

	private final Logger LOG = Logger.getLogger(SettingsActivity.class);

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		LOG.debug("ConfigActivity.onCreate()");
		super.onCreate(savedInstanceState);
		//setContentView(R.layout.activity_config);
		getFragmentManager().beginTransaction()
			.replace(android.R.id.content, new SettingsFragment()).commit();
	}
	@Override
	protected void onRestart() {
		LOG.debug("ConfigActivity.onRestart()");
		super.onRestart();
	}
	@Override
	protected void onStart() {
		LOG.debug("ConfigActivity.onStart()");
		super.onStart();
	}

	@Override
	protected void onResume() {
		LOG.debug("ConfigActivity.onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		LOG.debug("ConfigActivity.onPause()");
		super.onPause();
		updateAppConstantsWithPreferences();
	}
	@Override
	protected void onStop() {
		LOG.debug("ConfigActivity.onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		LOG.debug("ConfigActivity.onDestroy()");
		super.onDestroy();
	}

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.config, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menuitem_back:
			finish();
			break;
		}
		return true;
	}
	private void updateAppConstantsWithPreferences() {
		LOG.debug("SettingsActivity.updateAppConstantsWithPreferences()");
		SharedPreferences prefs = getSharedPreferences("preferences",MODE_PRIVATE);
		App.setHighResolutionVideoRecording(prefs.getBoolean("highres", false));
		App.setSaveAsCsv(prefs.getBoolean("saveascsv", false));
	}
}
