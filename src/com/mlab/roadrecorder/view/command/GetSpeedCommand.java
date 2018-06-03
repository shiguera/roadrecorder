package com.mlab.roadrecorder.view.command;

import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.gps.GpsModel;

public class GetSpeedCommand extends GetValueCommand {

	public GetSpeedCommand(GpsModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		String value = String.format("%5.1f", ((GpsModel)model).getSpeed());
		return value;
	}
	

}
