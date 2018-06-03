package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.MainActivity;

public abstract class ButtonState extends ActivityState {

	public ButtonState(MainActivity activity) {
		super(activity);
	}
	
	public abstract void doAction();

}
