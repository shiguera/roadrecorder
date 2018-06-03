package com.mlab.roadrecorder;

import java.io.File;

import android.os.Environment;
import android.os.StatFs;
import org.apache.log4j.Logger;

import android.app.Activity;
import android.content.Intent;
import android.location.GpsStatus;
import android.location.Location;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.FrameLayout;

import com.mlab.android.gpsmanager.GpsListener;
import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.App.VERSION;
import com.mlab.roadrecorder.MainActivity.NotificationLevel;
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

    final String LOGTAG = "ROADRECORDER";
	private static Logger LOG = Logger.getLogger(MainController.class);
		
	MainActivity activity;
	FrameLayout videoFrame;

	MainModel model;
	VideoController videoController;
	GpsModel gpsModel;
		
	
	public MainController(MainActivity activity) {
		this.activity = activity;
		this.videoFrame = activity.getVideoFrame();

		model = new MainModel(this.activity);
		model.setOutputDirectory(App.getApplicationDirectory());	

		App.setMainController(this);
		App.setMainModel(model);
		
		// Init VideoController
		videoController = new VideoController(activity, videoFrame);
		initMediaRecorder(model.getOutputDirectory());
		
		// Set version
		// Invalidado el 3/6/2018 para entregar la versión a Claudio Rodriguez (Argentina)
		//setVersionLimits(App.getVERSION_NAME(), App.getVERSION_NUMBER());
		
		// GpsModel
		gpsModel = new GpsModel(activity);
		gpsModel.getGpsManager().registerStatusListener(this);
		gpsModel.getGpsManager().registerGpsListener(this);
		initGpsUpdates();
	}
	// Private methods
	private void initGpsUpdates() {
		boolean result = gpsModel.startGpsUpdates();
		if(!result) {
			activity.showNotification("Active GPS. GPS Desactivado", 
					NotificationLevel.ERROR, true);
			activity.setGpsState(new GpsDisabledState(activity));
		} else {
			activity.setGpsState(new GpsFixingState(activity));
		}
	}
	private void initMediaRecorder(File outdirectory) {
		boolean result = videoController.initMediaRecorder(outdirectory);
		if(!result) {
			activity.showNotification("Errores al inicializar el vídeo", 
					NotificationLevel.ERROR, true);
			activity.setButtonState(new BtnDisabledState(activity));
			
		}		
	}

	public void onRestart() {
		LOG.debug("MainController.onRestart()");
		videoController.postInitMediaRecorder();
		initGpsUpdates();
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
	private void setVersionLimits(VERSION versionName, String versionNumber) {
		LOG.info("MainController.setVersionLimits(): "+versionName+" "+versionNumber);
		videoController.setMaxVideoDuration(App.getMaxVideoDuration());
	}
	// getters
	public MainActivity getActivity() {
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
		
		boolean result = checkDiskSpace();
		if(!result) {
			activity.showNotification("MainController.startRecording(): Error, disk full", 
					NotificationLevel.ERROR, true);
			activity.speak("Error, no se puede grabar, el disco está lleno");
			activity.setButtonState(new BtnStoppedState(activity));
			return;
		}
		
		result = videoController.startRecording();
		if(!result) {
			activity.showNotification("MainController.startRecording(): Error,  can't start recording", 
					NotificationLevel.ERROR, true);
			activity.speak("Error, no se puede empezar a grabar");
			activity.setButtonState(new BtnStoppedState(activity));
			return;
		}
		
		result = gpsModel.startRecording(true);
		if(!gpsModel.isGpsEnabled()) {
			activity.showNotification("MainController.startRecording(): Error gps is not receiving."+
				"Switching mode video-only", 
				NotificationLevel.ERROR, true);
			activity.speak("Error, el GPS no esta habilitado, se grabará solo el vídeo");
		}
		
		
		activity.setButtonState(new BtnRecordingState(activity));
		return;
	}
	private boolean checkDiskSpace() {
        Log.d(LOGTAG, "MainController.checkDiskSpace():: ");
        //doIntegerOperationsTest();
		long maxVideoFileSize =  getExternalAvailableSpaceInBytes();
		// LOG.info("Available space: "+maxVideoFileSize);
        Log.d(LOGTAG, "MainController.checkDiskSpace():: " + maxVideoFileSize);
		if(maxVideoFileSize < App.getMinDiskSpaceToSave()) {
			return false;
		}
		maxVideoFileSize = (long) (0.8 * maxVideoFileSize);
		Log.d(LOGTAG, "MainController.checkDiskSpace():: " + maxVideoFileSize);
		videoController.setMaxVideoFileSize(maxVideoFileSize);		
		return true;
	}
    public long getExternalAvailableSpaceInBytes() {
        long availableSpace = -1L;
        try {
            StatFs stat = new StatFs(Environment.getExternalStorageDirectory().getPath());
            availableSpace = (long)stat.getAvailableBlocksLong() * (long)stat.getBlockSizeLong();
        } catch (Exception e) {
            Log.d(LOGTAG, "MainController.getExternalAvailableDiskSpaceInBytes():: ERROR " + e.getMessage());
            e.printStackTrace();
        }

        return availableSpace;
    }
    private void doIntegerOperationsTest() {
        long maxVideoFileSize = getExternalAvailableSpaceInBytes();
        Log.d(LOGTAG, "MainController.doIntegerOperattionsTest():: externalAvailableSpace= " + maxVideoFileSize);
        maxVideoFileSize = (long) (0.8 * maxVideoFileSize);
        Log.d(LOGTAG, "MainController.doIntegerOperattionsTest():: 0.8*externalAvailableSpace= " + maxVideoFileSize);
    }


    public void stopRecording() {
		activity.setButtonState(new BtnDisabledState(activity));
		
		if(videoController.isRecording()) {
			boolean result = videoController.stopRecording();
			if(result) {
				//activity.speak("Se grabó el archivo de vídeo");
				LOG.debug("stopRecording(): videoController.stopRecording()="+result);
			} else {
				activity.showNotification("stopRecording(): Errors saving video recording", 
						NotificationLevel.ERROR, true);
				activity.speak("Error, no se pudo grabar el archivo de vídeo");
			}
			activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
					Uri.fromFile(videoController.getModel().getOutputFile())));
		}
		
		if(gpsModel.isRecording()) {
			gpsModel.stopRecording();
			if(gpsModel.getPointsCount()>0) {
				saveTrack();
				//activity.speak("Se grabó el track");
			} else {
				activity.showNotification("MainController.stopRecording(): Can't save track, points=0", 
						NotificationLevel.ERROR, true);
				activity.speak("Error, no se pudo grabar el track, no hay puntos");
			}
		}
		activity.setButtonState(new BtnStoppedState(activity));
		return;
	}
	private void saveTrack() {
		LOG.debug("MainController.saveTrack()");
		File outputVideoFile = videoController.getModel().getOutputFile();
		String namewithoutext = Util.fileNameWithoutExtension(outputVideoFile);
		saveGpxFile(namewithoutext);				
		if(App.isSaveAsCsv()) {
			saveCsvFile(namewithoutext);	
		} else {
			LOG.debug("Doesn't save as CSV");
		}
	}
	private void saveCsvFile(String namewithoutext) {
		LOG.debug("MainController.saveCsvFile()");
		String csvfilename = namewithoutext + ".csv";
		File file = new File(model.getOutputDirectory(), csvfilename);
		boolean result = gpsModel.saveTrackAsCsv(
				file, true);
		if(!result) {
			activity.showNotification("Error saving CSV file", 
				NotificationLevel.ERROR, true);
		} else {
			LOG.debug("MainController.saveCsvFile(): file" + csvfilename + " saved");
		}
		activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
				Uri.fromFile(file)));
	}
	private void saveGpxFile(String namewithoutext) {
		LOG.debug("MainController.saveGpxFile()");
		String gpxfilename = namewithoutext+".gpx";
		File file = new File(model.getOutputDirectory(), gpxfilename);
		boolean result = gpsModel.saveTrackAsGpx(file);
		if(!result) {
			activity.showNotification("Error saving GPX file", 
				NotificationLevel.ERROR, true);
		} else {
			LOG.debug("MainController.saveGpxFile(): file" + gpxfilename + " saved");
		}
		activity.sendBroadcast(new Intent(Intent.ACTION_MEDIA_SCANNER_SCAN_FILE, 
				Uri.fromFile(file)));
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
		activity.speak("El GPS ha fijado la posición");
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












