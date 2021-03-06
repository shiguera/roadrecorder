package com.mlab.roadrecorder.state;


import org.apache.log4j.Logger;

import com.mlab.roadrecorder.MainActivity;
import com.mlab.roadrecorder.MainActivity.GPSICON;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;
import android.util.Log;

public class GpsFixedState extends GpsState {
	private final Logger LOG = Logger.getLogger(GpsFixedState.class);
	
	public GpsFixedState(MainActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		LOG.debug("GpsFixedState.doAction()");
		// GpsIcon
		ColorFilter filter = new LightingColorFilter( Color.GREEN,Color.GREEN);
        //activity.getGpsIcon().setColorFilter(Color.RED);
        activity.setGpsIcon(MainActivity.GPSICON.FIXED, "FIXED");
        activity.stopGpsIconBlinker();
        // Button
        // activity.setButtonState(new BtnStoppedState(activity));
        // InfoLabel
        activity.setLabelInfoColor(Color.GREEN);
        activity.stopLabelInfoBlinker("GPS fixed. You can start recording");
	}

}
