package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.api.WayPoint;
import com.mlab.gpx.impl.AndroidWayPoint;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.gps.GpsModel;

public class GetBearingCommand extends GetValueCommand {

	public GetBearingCommand(GpsModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		String value = String.format("%5.1f", ((GpsModel)model).getBearing());
		return value;
	}
	

}
