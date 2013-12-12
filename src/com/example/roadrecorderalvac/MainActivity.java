package com.example.roadrecorderalvac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

public class MainActivity extends Activity {

	private final String TAG = "RoadRecorder";

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"MainActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Comprobar si es la primera ejecución
		checkFirstExecution();
		
		
	}
	@Override
	protected void onRestart() {
		Log.i(TAG,"MainActivity.onRestart()");
		super.onRestart();
	}
	@Override
	protected void onStart() {
		Log.i(TAG,"MainActivity.onStart()");
		super.onStart();
	}

	@Override
	protected void onResume() {
		Log.i(TAG,"MainActivity.onResume()");
		super.onResume();
	}

	@Override
	protected void onPause() {
		Log.i(TAG,"MainActivity.onPause()");
		super.onPause();
	}
	@Override
	protected void onStop() {
		Log.i(TAG,"MainActivity.onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		Log.i(TAG,"MainActivity.onDestroy()");
		super.onDestroy();
	}

	private void checkFirstExecution() {
		if(isFirstExecution()) {
			Log.d(TAG, "First execution");
			if(!createAppDirectory()) {
				Log.e(TAG, "Can't create application directory");
				showMessage("Error Creating Application Directory");
				finish();
				return;
			}
			Log.d(TAG, "Created application directory");
			if(!createSettingsFile()) {
				Log.e(TAG, "Error Creating settings file");
				showMessage("Error Creating File");
				finish();
				return;
			}
			Log.d(TAG, "Created settings file");
		}
	}
	private boolean isFirstExecution() {
		// Se comprueba si es la primera ejecución del programa
		// por la existencia o no del directorio de la aplicación
		// TODO Complete method
		return true;
	}
	private boolean createAppDirectory() {
		return true;
	}
	private boolean createSettingsFile() {
		return true;
	}
	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menuitem_config:
			startActivityConfig();
			break;
		case R.id.menuitem_help:
			startActivityHelp();			
			break;
		case R.id.menuitem_about:
			startActivityAbout();
			break;
		case R.id.menuitem_back:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private void startActivityConfig() {
		Intent i = new Intent(this, ConfigActivity.class);
		startActivity(i);
	}
	private void startActivityHelp() {
		Intent i = new Intent(this, HelpActivity.class);
		startActivity(i);	
	}
	private void startActivityAbout() {
		Intent i = new Intent(this, AboutActivity.class);
		startActivity(i);
	}
	

}
