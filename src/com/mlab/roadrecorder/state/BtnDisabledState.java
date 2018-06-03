package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.MainActivity;

public class BtnDisabledState extends ButtonState {

	public BtnDisabledState(MainActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		activity.setButtonBackground(MainActivity.BTNBACKGROUND.DISABLED);
		activity.setButtonEnabled(false);
		activity.setActionBarEnabled(true);
	}

}
