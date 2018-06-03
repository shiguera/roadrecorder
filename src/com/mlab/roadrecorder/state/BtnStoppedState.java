package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.MainActivity;
import com.mlab.roadrecorder.alvac.R;

public class BtnStoppedState extends ButtonState {

	public BtnStoppedState(MainActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		activity.setButtonBackground(MainActivity.BTNBACKGROUND.STOPPED);
		activity.setButtonEnabled(true);
		activity.setActionBarEnabled(true);

	}

}
