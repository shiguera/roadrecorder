package com.mlab.roadrecorder;

import java.io.File;

import org.apache.log4j.Logger;

import android.app.Application;

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
	
	private static VERSION VERSION_NAME = VERSION.Basic;
	private static final String VERSION_NUMBER = "1.0";
	
	
	private static final String APP_DIRECTORY_NAME = "RoadRecorder";
	private static File applicationDirectory;

	private static MainModel mainModel;
	private static MainController mainController;
	
	private static boolean saveAsCsv = true;
	

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

	public static void setSaveAsCsv(boolean saveAsCsv) {
		App.saveAsCsv = saveAsCsv;
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
		App.applicationDirectory = applicationDirectory;
	}
}
