package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.MainActivity;

public class BtnRecordingState extends ButtonState {

	public BtnRecordingState(MainActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		activity.setButtonBackground(MainActivity.BTNBACKGROUND.RECORDING);
		activity.setButtonEnabled(true);
		activity.setActionBarEnabled(false);

	}

}
