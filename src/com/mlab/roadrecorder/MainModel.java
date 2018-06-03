package com.mlab.roadrecorder;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.os.Bundle;

import com.mlab.roadrecorder.api.AbstractObservable;
import com.mlab.roadrecorder.api.Observable;
import com.mlab.roadrecorder.api.Observer;
import com.mlab.roadrecorder.gps.GpsModel;
import com.mlab.roadrecorder.video.VideoModel;


public class MainModel extends AbstractObservable implements Observer {
	
	private final Logger LOG = Logger.getLogger(MainModel.class);
	
	File outputDirectory;

	
	public MainModel(Context context) {
		LOG.debug("MainModel.MainModel()");
		outputDirectory = null;
		
	}

	// Interface Observer
	/**
	 * Deriva el update a los Observers registrados
	 * de cada tipo
	 */
	@Override
	public void update() {
	}

	// Gestión de observers
	
	// Getters
	public File getOutputDirectory() {
		return outputDirectory;
	}
	public void setOutputDirectory(File outputDirectory) {
		this.outputDirectory = outputDirectory;
	}
	@Override
	public Observable getObservable() {
		return null;
	}

	@Override
	public boolean addComponent(Observer o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public boolean removeComponent(Observer o) {
		// TODO Auto-generated method stub
		return false;
	}

	@Override
	public Observer getComponent(int index) {
		// TODO Auto-generated method stub
		return null;
	}

	
	

}
