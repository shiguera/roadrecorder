package com.mlab.roadrecorder.state;


import com.mlab.roadrecorder.NewActivity;
import com.mlab.roadrecorder.NewActivity.GPSICON;

import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.LightingColorFilter;

public class GpsDisabledState extends GpsState {

	public GpsDisabledState(NewActivity activity) {
		super(activity);
	}

	@Override
	public void doAction() {
		
        // GpsIcon
		//ColorFilter filter = new LightingColorFilter( Color.TRANSPARENT,Color.TRANSPARENT);
        //activity.getGpsIcon().setColorFilter(Color.RED);
        activity.setGpsIcon(NewActivity.GPSICON.DISABLED, "DISABLED");
        activity.startGpsIconBlinker();
        // Button
        activity.setButtonState(new BtnDisabledState(activity));
        // InfoLabel
        activity.setLabelInfoColor(Color.RED);
        activity.startLabelInfoBlinker("GPS No disponible. Active el GPS");
	}

}
