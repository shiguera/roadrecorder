package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.api.WayPoint;
import com.mlab.gpx.impl.AndroidWayPoint;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;

public class GetDistanceCommand extends AbstractGetValueCommand {

	public GetDistanceCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		String value = String.format("%8.1f", ((MainModel)model).getGpsModel().getDistance());
		return value;
	}
	

}
