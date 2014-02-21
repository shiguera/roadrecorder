package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.api.WayPoint;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.gps.GpsModel;

public class GetLatCommand extends GetValueCommand {

	public GetLatCommand(GpsModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		
		WayPoint wp = ((GpsModel)model).getLastWayPoint();
		
		String value =(wp != null && wp.getLatitude() != 0.0)?String.format("%11.6f", wp.getLatitude()):"  -  ";
		return value;
	}
	

}
