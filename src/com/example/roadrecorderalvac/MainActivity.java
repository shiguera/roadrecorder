package com.example.roadrecorderalvac;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
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
