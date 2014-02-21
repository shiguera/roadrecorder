package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.gps.GpsModel;
import com.mlab.roadrecorder.video.VideoModel;

public class GetRecordingTimeCommand extends GetValueCommand {

	public GetRecordingTimeCommand(VideoModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		long t = ((VideoModel)model).getRecordingTime();
		String value = Util.secondsToHMSString(t/1000);
		return value;
	}
	

}
