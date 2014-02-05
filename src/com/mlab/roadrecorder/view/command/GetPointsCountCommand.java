package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;
import com.mlab.roadrecorder.api.Observable;

public class GetPointsCountCommand extends AbstractGetValueCommand {

	public GetPointsCountCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		int count = ((MainModel)model).getGpsModel().getPointsCount();
		String value = String.format("%d", count);
		return value;
	}
	

}
