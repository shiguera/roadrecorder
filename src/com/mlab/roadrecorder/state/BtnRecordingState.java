package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.MainActivity;
import com.mlab.roadrecorder.alvac.R;

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
