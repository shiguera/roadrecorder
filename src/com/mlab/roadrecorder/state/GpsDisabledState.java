package com.mlab.roadrecorder.state;


import com.mlab.roadrecorder.MainActivity;
import com.mlab.roadrecorder.MainActivity.GPSICON;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;

public class GpsDisabledState extends GpsState {

	public GpsDisabledState(MainActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		
        // GpsIcon
		//ColorFilter filter = new LightingColorFilter( Color.TRANSPARENT,Color.TRANSPARENT);
        //activity.getGpsIcon().setColorFilter(Color.RED);
        activity.setGpsIcon(MainActivity.GPSICON.DISABLED, "DISABLED");
        activity.startGpsIconBlinker();
        // Button
        // activity.setButtonState(new BtnDisabledState(activity));
        // InfoLabel
        activity.setLabelInfoColor(Color.RED);
        activity.startLabelInfoBlinker("GPS No disponible. Active el GPS");
	}

}
