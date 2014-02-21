package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.api.WayPoint;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.gps.GpsModel;

public class GetLonCommand extends GetValueCommand {

	public GetLonCommand(GpsModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		
		WayPoint wp = ((GpsModel)model).getLastWayPoint();
		String value = (wp != null && wp.getLongitude() != 0.0)?String.format("%11.6f", wp.getLongitude()):"  -  ";
		return value;
	}
	

}
