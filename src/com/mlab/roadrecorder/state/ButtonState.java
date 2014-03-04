package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.NewActivity;

public abstract class ButtonState extends ActivityState {

	public ButtonState(NewActivity activity) {
		super(activity);
	}
	
	public abstract void doAction();

}
