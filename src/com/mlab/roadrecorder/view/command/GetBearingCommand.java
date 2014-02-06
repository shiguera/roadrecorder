package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.api.WayPoint;
import com.mlab.gpx.impl.AndroidWayPoint;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;

public class GetBearingCommand extends AbstractGetValueCommand {

	public GetBearingCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		String value = String.format("%5.1f", ((MainModel)model).getGpsModel().getBearing());
		return value;
	}
	

}
