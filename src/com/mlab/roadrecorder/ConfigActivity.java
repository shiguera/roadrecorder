package com.mlab.roadrecorder;

import com.example.roadrecorderalvac.R;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

public class ConfigActivity extends Activity {

	private final String TAG = "RoadRecorder";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"ConfigActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_config);
	}
	@Override
	protected void onRestart() {
		Log.i(TAG,"ConfigActivity.onRestart()");
		super.onRestart();
	}
	@Override
	protected void onStart() {
		Log.i(TAG,"ConfigActivity.onStart()");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i(TAG,"ConfigActivity.onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(TAG,"ConfigActivity.onPause()");
		super.onPause();
	}
	@Override
	protected void onStop() {
		Log.i(TAG,"ConfigActivity.onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG,"ConfigActivity.onDestroy()");
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
}
