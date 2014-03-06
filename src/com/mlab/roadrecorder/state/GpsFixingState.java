package com.mlab.roadrecorder.state;


import com.mlab.roadrecorder.NewActivity;
import com.mlab.roadrecorder.NewActivity.GPSICON;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;

public class GpsFixingState extends GpsState {

	public GpsFixingState(NewActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		
        // GpsIcon
		//ColorFilter filter = new LightingColorFilter( Color.TRANSPARENT,Color.TRANSPARENT);
        //activity.getGpsIcon().setColorFilter(Color.RED);
        activity.setGpsIcon(NewActivity.GPSICON.FIXING, "FIXING");
        activity.startGpsIconBlinker();
        // Button
        // activity.setButtonState(new BtnStoppedState(activity));
        // InfoLabel
        activity.setLabelInfoColor(Color.GREEN);
        activity.startLabelInfoBlinker("Fixing GPS position");
	}

}
