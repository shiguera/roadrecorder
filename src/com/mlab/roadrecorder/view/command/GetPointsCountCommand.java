package com.mlab.roadrecorder.view.command;

import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.gps.GpsModel;

public class GetPointsCountCommand extends GetValueCommand {

	public GetPointsCountCommand(GpsModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		int count = ((GpsModel)model).getPointsCount();
		String value = String.format("%5d", count);
		return value;
	}
	

}
