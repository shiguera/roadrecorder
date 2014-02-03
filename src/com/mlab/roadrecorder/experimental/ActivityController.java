package com.mlab.roadrecorder.experimental;

import android.app.Activity;

public interface ActivityController {

	Activity getActivity();
	void onStart();
	void onRestart();
	void onResume();
	void onStop();
	void onPause();
	void onDestroy();
	
}
