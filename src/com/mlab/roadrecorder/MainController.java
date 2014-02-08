package com.mlab.roadrecorder;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import android.os.Bundle;
import android.widget.FrameLayout;

import com.mlab.android.utils.AndroidUtils;
import com.mlab.roadrecorder.NewActivity.NotificationLevel;
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

		// Init VideoController
		videoController = new VideoController(model.getVideoModel(), activity, videoFrame);
		
		// Init application directory
		boolean result = initApplicationDirectory();
		if(!result) {
			activity.showNotification("Can't init application directory", 
				NotificationLevel.ERROR, true);
			activity.finish();
			return;
		}
		
		// startGpsUpdates
		result = model.getGpsModel().startGpsUpdates();
		if(!result) {
			activity.showNotification("GPS Desactivado", NotificationLevel.ERROR, true);
		}
		
	}
	// Private methods
	private boolean initApplicationDirectory() {		
		// Try secondary card
		List<File> secdirs = AndroidUtils.getSecondaryStorageDirectories();
		File outdir = null;
		if(secdirs.size()>0) {
			LOG.info("MainController.initApplicationDirectory() appdir: " + 
					secdirs.get(0).getPath());
			outdir = new File(secdirs.get(0), App.getAPP_DIRECTORY_NAME());
			return setApplicationDirectory(outdir);
		}	
		// Try normal external storage
		if(!AndroidUtils.isExternalStorageEnabled()) {
			LOG.info("MainController.initApplicationDirectory() "+ 
					"ERROR, can't init external storage"); 
			return false;
		}
		outdir = new File(AndroidUtils.getExternalStorageDirectory(), App.getAPP_DIRECTORY_NAME());
		return setApplicationDirectory(outdir);
	}
	private boolean setApplicationDirectory(File outdir) {
		if(!outdir.exists()) {
			if(!outdir.mkdir()) {
				LOG.info("MainController.setApplicationDirectory() "+ 
						"ERROR, can't create application directory"); 
				activity.finish();
				return false;				
			}
		}
		LOG.info("MainController.setApplicationDirectory() "+ 
				"appicationDirectory = "+outdir.getPath()); 
		model.setOutputDirectory(outdir);	
		return true;
		
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
	
	// public methods
	public void startRecording() {
		boolean result = videoController.startRecording();
		if(!result) {
			activity.showNotification("MainController.stopRecording(): Error,  can't start recording", 
					NotificationLevel.ERROR, true);
		}
		return;
	}
	public void stopRecording() {
		boolean result = videoController.stopRecording();		
		if(!result) {
			activity.showNotification("MainController.stopRecording(): Error,  can't stop recording", 
					NotificationLevel.ERROR, true);
		}
		return;
	}

}
