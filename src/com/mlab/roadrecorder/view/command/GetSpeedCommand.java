package com.mlab.roadrecorder.view.command;

import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;

public class GetSpeedCommand extends AbstractGetValueCommand {

	public GetSpeedCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		String value = String.format("%5.1f", ((MainModel)model).getGpsModel().getSpeed());
		return value;
	}
	

}
