package com.mlab.roadrecorder.view.command;

import org.apache.log4j.Logger;

import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.gps.GpsModel;
import com.mlab.roadrecorder.video.VideoModel;

public class GetRecordingTimeCommand extends GetValueCommand {

	private final Logger LOG = Logger.getLogger(GetRecordingTimeCommand.class);
	public GetRecordingTimeCommand(VideoModel model) {
		super(model);
	}

	// No funciona, el modelo de vídeo no envía updates
	@Override
	public String getValue() {
		//LOG.debug("GetRecordingTimeCommand.getValue()");
		long t = ((VideoModel)model).getRecordingTime();
		String value = Util.secondsToHMSString(t/1000);
		return value;
	}
	

}
