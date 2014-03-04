package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.NewActivity;
import com.mlab.roadrecorder.alvac.R;

public class BtnStoppedState extends ButtonState {

	public BtnStoppedState(NewActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		activity.setButtonBackground(NewActivity.BTNBACKGROUND.STOPPED);
		activity.setButtonEnabled(true);
	}

}
