package com.mlab.roadrecorder;

import java.io.File;

import org.apache.log4j.Logger;

import android.app.Application;
import android.content.SharedPreferences;

/**
 * Base class to maintain global application state
 * 
 * @author shiguera
 *
 */
public class App extends Application {

	private static Logger LOG = Logger.getLogger(App.class);
	
	public static enum VERSION {Basic, Extended};
	private static final int BASIC_VERSION_MAX_VIDEO_DURATION = 600000;
	private static final int EXTENDED_VERSION_MAX_VIDEO_DURATION = 6000000;

	private static final String LOGFILE_NAME = "roadrecorder.log";
	private static final String PREFSFILE_NAME = "prefs";

	private static VERSION VERSION_NAME = VERSION.Basic;
	private static final String VERSION_NUMBER = "1.0";
	
	private static final String APP_DIRECTORY_NAME = "RoadRecorder";
	private static File applicationDirectory;

	// Mínimo espacio que se exige al disco para empezar a grabar.
	private static final int DEFAULT_MIN_DISK_SPACE_TO_SAVE = 250;
	private static int minDiskSpaceToSave = DEFAULT_MIN_DISK_SPACE_TO_SAVE;
	
	private static MainModel mainModel;
	private static MainController mainController;
	
	// Variables configurables en SharedPreferences a través de SettingsActivity
	private static boolean highResolutionVideoRecording = true;
	private static boolean saveAsCsv = true;
	
	//
	
	// Getters
	public static MainModel getMainModel() {
		return mainModel;
	}

	public static void setMainModel(MainModel mainModel) {
		LOG.debug("App.setMainModel()");
		App.mainModel = mainModel;
	}

	public static MainController getMainController() {
		return mainController;
	}

	public static void setMainController(MainController mainController) {
		LOG.debug("App.setMainController()");
		App.mainController = mainController;
	}

	public static boolean isSaveAsCsv() {
		return saveAsCsv;
	}

	public static void setSaveAsCsv(boolean saveascsv) {
		LOG.debug("App.setSaveAsCsv() "+saveascsv);
		App.saveAsCsv = saveascsv;
	}

	public static VERSION getVERSION_NAME() {
		return VERSION_NAME;
	}

	public static String getVERSION_NUMBER() {
		return VERSION_NUMBER;
	}

	public static String getAppDirectoryName() {
		return APP_DIRECTORY_NAME;
	}
	public static int getMaxVideoDuration() {
		int dur = BASIC_VERSION_MAX_VIDEO_DURATION;
		if(VERSION_NAME == VERSION.Extended) {
			dur = EXTENDED_VERSION_MAX_VIDEO_DURATION;
		}
		return dur;
	}

	public static File getApplicationDirectory() {
		return applicationDirectory;
	}

	public static void setApplicationDirectory(File applicationDirectory) {
		LOG.debug("App.setApplicationDirectory() "+applicationDirectory);
		App.applicationDirectory = applicationDirectory;
	}

	public static boolean isHighResolutionVideoRecording() {
		return highResolutionVideoRecording;
	}

	public static void setHighResolutionVideoRecording(
			boolean highResolutionVideoRecording) {
		LOG.debug("App.setHighResolutionVideoRecording() "+highResolutionVideoRecording);
		App.highResolutionVideoRecording = highResolutionVideoRecording;
	}

	public static String getLogfileName() {
		return LOGFILE_NAME;
	}

	public static String getPrefsfileName() {
		return PREFSFILE_NAME;
	}

	public static int getMinDiskSpaceToSave() {
		return minDiskSpaceToSave;
	}

	public static void setMinDiskSpaceToSave(int minDiskSpaceToSave) {
		LOG.debug("App.setMinDiskSpaceToSave(): "+minDiskSpaceToSave);
		App.minDiskSpaceToSave = minDiskSpaceToSave;
	}
}
