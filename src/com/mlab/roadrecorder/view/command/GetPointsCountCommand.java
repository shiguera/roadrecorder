package com.mlab.roadrecorder.view.command;

import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;

public class GetPointsCountCommand extends AbstractGetValueCommand {

	public GetPointsCountCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		int count = ((MainModel)model).getGpsModel().getPointsCount();
		String value = String.format("%5d", count);
		return value;
	}
	

}
