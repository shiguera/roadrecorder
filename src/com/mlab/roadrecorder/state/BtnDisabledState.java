package com.mlab.roadrecorder.state;

import com.mlab.roadrecorder.NewActivity;
import com.mlab.roadrecorder.alvac.R;

public class BtnDisabledState extends ButtonState {

	public BtnDisabledState(NewActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		activity.setButtonBackground(NewActivity.BTNBACKGROUND.DISABLED);
		activity.setButtonEnabled(false);
	}

}
