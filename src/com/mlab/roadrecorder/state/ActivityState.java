package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.NewActivity;

public abstract class ActivityState {

	protected NewActivity activity;
	
	public ActivityState(NewActivity activity) {
		this.activity = activity;
	}
	public abstract void doAction();
}
