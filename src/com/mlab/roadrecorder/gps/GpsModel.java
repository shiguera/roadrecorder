package com.mlab.roadrecorder.gps;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.location.Location;
//import android.util.Log;
import android.widget.Toast;

import com.mlab.android.gpsmanager.GpsListener;
import com.mlab.android.gpsmanager.GpsManager;
import com.mlab.gpx.api.GpxDocument;
import com.mlab.gpx.api.GpxFactory;
import com.mlab.gpx.api.WayPoint;
import com.mlab.gpx.impl.AndroidWayPoint;
import com.mlab.gpx.impl.Track;
import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.api.AbstractObservable;

public class GpsModel extends AbstractObservable implements GpsListener {

	//private final String TAG = "ROADRECORDER";

	private final Logger LOG = Logger.getLogger(GpsModel.class);
	
	private Context context;

	protected GpsManager gpsManager;
	protected GpxFactory gpxFactory;
	protected Track track;
	//protected AndroidWayPoint lastWayPoint;

	protected boolean isRecording;
	
	// Constructor
	public GpsModel(Context context) {
		super();
		this.context = context;
		gpsManager = new GpsManager(context);
		gpxFactory = GpxFactory.getFactory(GpxFactory.Type.AndroidGpxFactory);
		track = new Track();
		//lastWayPoint = new AndroidWayPoint();
	}
	
	// GpsManager management
	public boolean startGpsUpdates() {
		return this.gpsManager.startGpsUpdates();
	}
	public void stopGpsUpdates() {
		this.gpsManager.stopGpsUpdates();
		return;
	}
	
	// Recording management (Recording = add points to track)
	
	// TODO Aquí se puede gestionar un nuevo segmento
	/**
	 * Comienza a añadir puntos al track en memoria.
	 * 
	 * @param newtrack Si newtrack=true se inicializa un nuevo track,
	 * si newtrack=false los puntos se añaden al track ya existente
	 * 
	 * @return true si todo va bien, false si el GPS no está habilitado
	 */
	public boolean startRecording(boolean newtrack) {
		if(this.gpsManager.isGpsEnabled()) {
			if(newtrack) {
				track = new Track();
			}
			isRecording = true;
			return true;
		}
		return false;
	}
	/**
	 * Deja de añadir puntos al track en memoria
	 */
	public void stopRecording() {
		isRecording = false;
	}
	
	// Interface GpsListener
	@Override
	public void firstFixEvent() {
		LOG.debug("GpsModel.firstFixEvent()");
	}
	@Override
	public void updateLocation(Location loc) {
		LOG.debug("GpsModel.updateLocation(): "+loc.toString());
		
		if(isRecording) {
			addPointToTrack(locToWayPoint(loc));
		}
		this.notifyObservers();
	}

	// Track management
	public int wayPointCount() {
		return track.wayPointCount();
	}
	private void addPointToTrack(WayPoint wp) {
		if(wp != null) {
			track.addWayPoint(wp, false);						
		}
	}
	public boolean saveTrackAsGpx(File outputfile) {
		// FIXME Hacerlo en segundo plano? -> Mejor en la librería
		boolean result=false;
        try {
        	GpxDocument doc = gpxFactory.createGpxDocument();
        	doc.addTrack(track);
        	Util.write(outputfile.getPath(), doc.asGpx());
        	result = true;
        } catch (Exception e) {
        	String msg = "Error can't save Gpx Document";
        	LOG.error("GpsModel.saveTrackAsGpx(): " + msg);
        	this.showNotification(msg);
        }
		return result;
	}
	public boolean saveTrackAsCsv(File outputfile, boolean withutmcoords) {
		// FIXME Hacerlo en segundo plano? -> Mejor en la librería
		boolean result=false;
        try {
        	GpxDocument doc = gpxFactory.createGpxDocument();
        	doc.addTrack(track);
        	Util.write(outputfile.getPath(), track.asCsv(withutmcoords));
        	result = true;
        } catch (Exception e) {
        	String msg = "Error can't save CSV Document";
        	LOG.error("GpsModel.saveTrackAsCsv(): " + msg);
        	this.showNotification(msg);
        }
		return result;
	}
	
	// Getters
	public Location getLastLocReceived() {
		return gpsManager.getLastLocation();
	}
	public GpsManager getGpsManager() {
		return gpsManager;
	}
	public Track getTrack() {
		return this.track;
	}
	public AndroidWayPoint getLastWayPoint() {
		if(getLastLocReceived() != null) {
			return locToWayPoint(getLastLocReceived());
		}
		return null;
	}

	// Status
	public boolean isGpsEnabled() {
		return this.gpsManager.isGpsEnabled();
	}
	public boolean isRecording() {
		return this.isRecording;
	}


	// Utilities
	private AndroidWayPoint locToWayPoint(Location loc) {
		List<Double> listvalues = Arrays.asList(new Double[]{loc.getLongitude(),
				loc.getLatitude(), loc.getAltitude(), (double) loc.getSpeed(), 
				(double) loc.getBearing(), (double) loc.getAccuracy()});
		WayPoint point = gpxFactory.createWayPoint("", "", loc.getTime(), listvalues); 
		return (AndroidWayPoint)point;
	}
	private void showNotification(String msg) {
		Toast.makeText(context, msg, Toast.LENGTH_LONG).show();
	}
	
}
