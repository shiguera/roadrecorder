package com.mlab.roadrecorder;

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
	private static final String APP_DIRECTORY_NAME = "RoadRecorder";
	private static MainModel mainModel;
	private static MainController mainController;
	
	

	// Getters
	public static MainModel getMainModel() {
		return mainModel;
	}

	public static void setMainModel(MainModel mainModel) {
		LOG.info("App.setMainModel()");
		App.mainModel = mainModel;
	}

	public static MainController getMainController() {
		return mainController;
	}

	public static void setMainController(MainController mainController) {
		LOG.info("App.setMainController()");
		App.mainController = mainController;
	}

	public static String getAPP_DIRECTORY_NAME() {
		return APP_DIRECTORY_NAME;
	}
	
}
