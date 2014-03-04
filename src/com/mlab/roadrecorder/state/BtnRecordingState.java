package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.NewActivity;
import com.mlab.roadrecorder.alvac.R;

public class BtnRecordingState extends ButtonState {

	public BtnRecordingState(NewActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		activity.getBtnStartStop().setBackgroundResource(R.drawable.button_stop);
		activity.getBtnStartStop().setEnabled(true);

	}

}
