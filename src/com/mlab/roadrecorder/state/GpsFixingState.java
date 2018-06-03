package com.mlab.roadrecorder.state;


import com.mlab.roadrecorder.MainActivity;
import com.mlab.roadrecorder.MainActivity.GPSICON;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;

public class GpsFixingState extends GpsState {

	public GpsFixingState(MainActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		
        // GpsIcon
		//ColorFilter filter = new LightingColorFilter( Color.TRANSPARENT,Color.TRANSPARENT);
        //activity.getGpsIcon().setColorFilter(Color.RED);
        activity.setGpsIcon(MainActivity.GPSICON.FIXING, "FIXING");
        activity.startGpsIconBlinker();
        // Button
        // activity.setButtonState(new BtnStoppedState(activity));
        // InfoLabel
        activity.setLabelInfoColor(Color.YELLOW);
        activity.startLabelInfoBlinker("Fixing GPS position");
	}

}
