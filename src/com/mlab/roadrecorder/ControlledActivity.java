package com.mlab.roadrecorder;

import android.app.Activity;

public abstract class ControlledActivity extends Activity {

	ActivityController controller;
	
	public ControlledActivity(ActivityController controller) {
		this.controller = controller;
	}
	
	@Override
	protected void onStart() {
		controller.onStart();
		super.onStart();
	}
	@Override
	protected void onRestart() {
		controller.onRestart();
		super.onRestart();
	}
	@Override
	protected void onResume() {
		controller.onResume();
		super.onResume();
	}
	@Override
	protected void onStop() {
		controller.onStop();
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		controller.onDestroy();
		super.onDestroy();
	}

}
