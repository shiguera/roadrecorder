package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.NewActivity;
import com.mlab.roadrecorder.alvac.R;

public class BtnRecordingState extends ButtonState {

	public BtnRecordingState(NewActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		activity.setButtonBackground(NewActivity.BTNBACKGROUND.RECORDING);
		activity.setButtonEnabled(true);
		activity.setActionBarEnabled(false);

	}

}
