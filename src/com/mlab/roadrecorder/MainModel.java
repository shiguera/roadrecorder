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
	GpsModel gpsModel;
	VideoModel videoModel;

	List<Observer> videoObservers;
	List<Observer> gpsObservers;
	
	public MainModel(Context context) {
		LOG.info("MainModel.MainModel()");
		outputDirectory = null;
		
		videoObservers = new ArrayList<Observer>();
		videoModel = new VideoModel();
		videoModel.registerObserver(this);
		
		gpsObservers = new ArrayList<Observer>();
		gpsModel = new GpsModel(context);
		gpsModel.registerObserver(this);
	}

	// Interface Observer
	/**
	 * Deriva el update a los Observers registrados
	 * de cada tipo
	 */
	@Override
	public void update(Object sender, Bundle parameters) {
		if(sender.getClass().isAssignableFrom(GpsModel.class)) {
			this.notifyGpsObservers((GpsModel)sender, parameters);
		}
		if(sender.getClass().isAssignableFrom(VideoModel.class)) {
			this.notifyVideoObservers((VideoModel)sender, parameters);
		}
	}

	// Gestión de observers

	public void registerVideoObserver(Observer o) {
		videoObservers.add(o);
	}
	private void notifyVideoObservers(VideoModel videomodel, Bundle parameters) {
		for(Observer o: videoObservers) {
			o.update(videomodel, parameters);
		}
	}
	public void registerGpsObserver(Observer o) {
		gpsObservers.add(o);
	}
	private void notifyGpsObservers(GpsModel gpsmodel, Bundle parameters) {
		for(Observer o: gpsObservers) {
			o.update(gpsmodel, parameters);
		}
	}
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
	public GpsModel getGpsModel() {
		return gpsModel;
	}
	public VideoModel getVideoModel() {
		return videoModel;
	}

	
	

}
