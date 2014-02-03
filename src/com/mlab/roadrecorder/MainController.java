package com.mlab.roadrecorder;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.mlab.android.utils.AndroidUtils;
import com.mlab.roadrecorder.api.Observable;
import com.mlab.roadrecorder.api.Observer;
import com.mlab.roadrecorder.video.VideoController;

public class MainController implements Observer {

	private static Logger LOG = Logger.getLogger(MainController.class);
	
	NewActivity activity;
	MainModel model;
	VideoController videoController;
	FrameLayout videoFrame;
	
	
	public MainController(MainModel model, NewActivity activity, FrameLayout videoframe) {
		this.activity = activity;
		this.model = model;
		this.videoFrame = videoframe;

		videoController = new VideoController(model.getVideoModel(), activity, videoFrame);
		
		initApplicationDirectory();
		
	}
	// Private methods
	private void initApplicationDirectory() {
		
		List<File> secdirs = AndroidUtils.getSecondaryStorageDirectories();
		if(secdirs.size()>0) {
			LOG.info("MainController.initApplicationDirectory() appdir: " + 
					secdirs.get(0).getPath());
			model.setOutputDirectory(secdirs.get(0));
		}		
		LOG.error("MainController.initApplicationDirectory() Can't init appdir ");
		activity.finish();
		return;
	}
	// Interface Observer
	@Override
	public Observable getObservable() {
		return model;
	}
	@Override
	public void update(Object sender, Bundle parameters) {
		
		
	}
	
	// getters
	public NewActivity getActivity() {
		return activity;
	}
	public MainModel getModel() {
		return model;
	}
	public VideoController getVideoController() {
		return videoController;
	}
	public FrameLayout getVideoFrame() {
		return videoFrame;
	}
	


}
