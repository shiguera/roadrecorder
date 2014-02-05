package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;
import com.mlab.roadrecorder.api.Observable;

public class GetRecordingTimeCommand extends AbstractGetValueCommand {

	public GetRecordingTimeCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		long t = ((MainModel)model).getVideoModel().getRecordingTime();
		String value = Util.secondsToHMSString(t/1000);
		return value;
	}
	

}
