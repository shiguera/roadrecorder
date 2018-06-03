package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.MainActivity;

public abstract class ActivityState {

	protected MainActivity activity;
	
	public ActivityState(MainActivity activity) {
		this.activity = activity;
	}
	public abstract void doAction();
}
