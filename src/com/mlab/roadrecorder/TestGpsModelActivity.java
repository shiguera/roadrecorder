package com.mlab.roadrecorder;

import com.mlab.roadrecorder.alvac.R;
import com.mlab.roadrecorder.gps.GpsModel;

import android.app.Activity;
import android.os.Bundle;

public class TestGpsModelActivity extends Activity {
	
	GpsModel model;
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		this.setContentView(R.layout.activity_main);
		
		this.model = new GpsModel(getApplicationContext());
		
	}
	public GpsModel getModel() {
		return this.model;
	}

}
