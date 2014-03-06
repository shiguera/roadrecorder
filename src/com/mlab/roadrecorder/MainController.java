package com.mlab.roadrecorder;

import java.io.File;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.location.GpsStatus;
import android.location.Location;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.mlab.android.gpsmanager.GpsListener;
import com.mlab.android.utils.AndroidUtils;
import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.NewActivity.NotificationLevel;
import com.mlab.roadrecorder.api.Controller;
import com.mlab.roadrecorder.gps.GpsModel;
import com.mlab.roadrecorder.state.BtnDisabledState;
import com.mlab.roadrecorder.state.BtnRecordingState;
import com.mlab.roadrecorder.state.BtnStoppedState;
import com.mlab.roadrecorder.state.GpsDisabledState;
import com.mlab.roadrecorder.state.GpsFixedState;
import com.mlab.roadrecorder.state.GpsFixingState;
import com.mlab.roadrecorder.video.VideoController;

public class MainController extends Activity  implements Controller, GpsListener, GpsStatus.Listener {

	private static Logger LOG = Logger.getLogger(MainController.class);
	
	MainModel model;
	NewActivity activity;
	VideoController videoController;
	GpsModel gpsModel;
	
	FrameLayout videoFrame;
	
	
	public MainController(NewActivity activity) {
		this.activity = activity;
		this.model = new MainModel(this.activity);

		this.videoFrame = activity.getVideoFrame();

		App.setMainModel(model);
		App.setMainController(this);
		
		// Init application directory
		boolean result = initApplicationDirectory();
		if(!result) {
			activity.showNotification("Can't init application directory", 
				NotificationLevel.ERROR, true);
			activity.finish();
			return;
		}

		// Init VideoController
		videoController = new VideoController(activity, videoFrame);
		result = videoController.initMediaRecorder();
		if(!result) {
			activity.showNotification("Errores al inicializar el vídeo", 
					NotificationLevel.ERROR, true);
			activity.setButtonState(new BtnDisabledState(activity));
			
		}
		
		// GpsModel
		gpsModel = new GpsModel(activity);
		gpsModel.getGpsManager().registerStatusListener(this);
		gpsModel.getGpsManager().registerGpsListener(this);
		result = gpsModel.startGpsUpdates();
		if(!result) {
			activity.showNotification("Active GPS. GPS Desactivado", 
					NotificationLevel.ERROR, true);
			activity.setGpsState(new GpsDisabledState(activity));
		} else {
			activity.setGpsState(new GpsFixingState(activity));
		}
	}
	// Private methods
	public void onRestart() {
		LOG.debug("MainController.onRestart()");
	}
	public void onPause() {
		LOG.debug("MainController.onPause()");
		if(videoController.isRecording()) {
			stopRecording();			
		}
		videoController.release();
				
		if(gpsModel.isRecording()) {
			gpsModel.stopRecording();
		}
		gpsModel.stopGpsUpdates();
	}
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
	
	// getters
	public NewActivity getActivity() {
		return activity;
	}
	public VideoController getVideoController() {
		return videoController;
	}
	public FrameLayout getVideoFrame() {
		return videoFrame;
	}
	
	// public methods
	public boolean isRecording() {
		return videoController.isRecording() || gpsModel.isRecording();
	}
	public void startRecording() {
		activity.setButtonState(new BtnDisabledState(activity));
		boolean result = videoController.startRecording();
		if(!result) {
			activity.showNotification("MainController.startRecording(): Error,  can't start recording", 
					NotificationLevel.ERROR, true);
			activity.setButtonState(new BtnStoppedState(activity));
			return;
		}
		result = gpsModel.startRecording(true);
		if(!gpsModel.isGpsEnabled()) {
			activity.showNotification("MainController.startRecording(): Error gps is not receiving."+
				"Switching mode video-only", 
				NotificationLevel.ERROR, true);
		}
		activity.setButtonState(new BtnRecordingState(activity));
		return;
	}
	public void stopRecording() {
		activity.setButtonState(new BtnDisabledState(activity));
		if(videoController.isRecording()) {
			boolean result = videoController.stopRecording();		
			if(!result) {
				activity.showNotification("stopRecording(): Errors saving video recording", 
						NotificationLevel.ERROR, true);
			}
		}
		if(gpsModel.isRecording()) {
			gpsModel.stopRecording();
			if(gpsModel.getPointsCount()>0) {
				saveTrack();
			} else {
				activity.showNotification("MainController.stopRecording(): Can't save track, points=0", 
						NotificationLevel.ERROR, true);
			}
		}
		activity.setButtonState(new BtnStoppedState(activity));
		return;
	}
	private void saveTrack() {
		File outputVideoFile = videoController.getModel().getOutputFile();
		String namewithoutext = Util.fileNameWithoutExtension(outputVideoFile);
		this.saveGpxFile(namewithoutext);				
		if(App.isSaveAsCsv()) {
			this.saveCsvFile(namewithoutext);	
		}					
	}
	private void saveCsvFile(String namewithoutext) {
		String csvfilename = namewithoutext + ".csv";
		boolean result = gpsModel.saveTrackAsCsv(
				new File(model.getOutputDirectory(), csvfilename), true);
		if(!result) {
			activity.showNotification("Error saving CSV file", 
				NotificationLevel.ERROR, true);
		} else {
			LOG.debug("MainController.saveCsvFile(): file" + csvfilename + " saved");
		}
		
	}
	private void saveGpxFile(String namewithoutext) {
		String gpxfilename = namewithoutext+".gpx";
		boolean result = gpsModel.saveTrackAsGpx(new File(model.getOutputDirectory(), gpxfilename));
		if(!result) {
			activity.showNotification("Error saving GPX file", 
				NotificationLevel.ERROR, true);
		} else {
			LOG.debug("MainController.saveGpxFile(): file" + gpxfilename + " saved");
		}
	}

	//
	private void onGpsStopped() {
		LOG.error("ERROR : GPS stopped");
		stopRecording();
	}
	// Interface Controller
	@Override
	public MainModel getModel() {
		return model;
	}
	@Override
	public View getView() {
		// TODO Auto-generated method stub
		return null;
	}
	@Override
	public void release() {
		// TODO Auto-generated method stub
		
	}
	public GpsModel getGpsModel() {
		return gpsModel;
	}
	
	// Interface GpsListener
	@Override
	public void firstFixEvent() {
		LOG.debug("MainController.firstFixEvent()");
		activity.setGpsState(new GpsFixedState(activity));
	}
	@Override
	public void updateLocation(Location loc) {
		//LOG.debug("MainController.updateLocation() "+String.format("%f %f", loc.getLongitude(), loc.getLatitude()));
	}
		
	// Interface GpsStatus.Listener
	@Override
	public void onGpsStatusChanged(int event) {
		String text="MainController.";
		switch(event) {
		case(GpsStatus.GPS_EVENT_SATELLITE_STATUS):
			text = "GPS_EVENT_SATELLITE_STATUS";
			break;
		case(GpsStatus.GPS_EVENT_STARTED):
			Log.d("HAL", "MainController.onGpsStatusChanged(): GPS_EVENT_STARTED");				
			break;
		case(GpsStatus.GPS_EVENT_STOPPED):
			Log.d("HAL", "MainController.onGpsStatusChanged(): GPS_EVENT_STOPED");	
			activity.setGpsState(new GpsDisabledState(activity));
			onGpsStopped();
			break;
		case(GpsStatus.GPS_EVENT_FIRST_FIX):
			Log.d("HAL", "MainController.onGpsStatusChanged(): GPS_EVENT_FIRST_FIX");	
			firstFixEvent();
			break;
		}
		
	}
	
	
}












