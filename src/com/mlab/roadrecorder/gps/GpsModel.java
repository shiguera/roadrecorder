package com.mlab.roadrecorder.gps;

import java.io.File;
import java.util.Arrays;
import java.util.List;

import org.apache.log4j.Logger;

import android.content.Context;
import android.location.Location;
import android.os.AsyncTask;
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
	protected AndroidWayPoint lastWayPoint;
	protected double speed;
	protected double bearing;
	protected double distance;

	protected boolean isRecording;
	
	// Constructor
	public GpsModel(Context context) {
		super();
		this.context = context;
		gpsManager = new GpsManager(context);
		gpxFactory = GpxFactory.getFactory(GpxFactory.Type.AndroidGpxFactory);
		track = new Track();
		
		lastWayPoint = null;
		speed = -1.0;
		bearing = -1.0;
		distance = 0.0;
	}
	
	// GpsManager management
	public boolean startGpsUpdates() {
		boolean result = gpsManager.startGpsUpdates();
		if (result) { 
			notifyObservers(null);
		}
		return result;
	}
	public void stopGpsUpdates() {
		gpsManager.stopGpsUpdates();
		notifyObservers(null);
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
				lastWayPoint = null;
				speed = 0.0;
				bearing = -1.0;
				distance = 0.0;
				track = new Track();
			}
			isRecording = true;
			notifyObservers(null);
			return true;
		}
		return false;
	}
	/**
	 * Deja de añadir puntos al track en memoria
	 */
	public void stopRecording() {
		isRecording = false;
		notifyObservers(null);
	}
	
	// Interface GpsListener
	@Override
	public void firstFixEvent() {
		LOG.debug("GpsModel.firstFixEvent()");
		notifyObservers(null);
	}
	@Override
	public void updateLocation(Location loc) {
		LOG.debug("GpsModel.updateLocation(): "+loc.toString());		
		if(isRecording) {
			addPointToTrack(locToWayPoint(loc));
		}
		this.notifyObservers(null);
	}
	// Track management
	public int wayPointCount() {
		return track.wayPointCount();
	}
	private void addPointToTrack(WayPoint wp) {
		if(wp != null) {
			if(lastWayPoint !=  null) {
				double d = Util.dist3D(lastWayPoint, wp);
				double t = (double)((wp.getTime()-lastWayPoint.getTime())/1000l);
				distance = distance + d;
				bearing = Util.bearing(lastWayPoint, wp);
				speed = d / t;
			}			
			track.addWayPoint(wp, false);						
		}
	}

	public boolean saveTrackAsGpx(File outputfile) {
		GpxSaver saver = new GpxSaver(outputfile);
		saver.execute();
		boolean result = false;
		try { 
			saver.get();
			result = true;
		} catch (Exception e) {
			LOG.error("GpsModel.saveTrackAsGpx(); ERROR : can't save gpx track");
			result = false;
		}
		return result;
	}
	class GpxSaver extends AsyncTask<Void, Void, Boolean> {
		File outFile;
		GpxSaver(File outfile) {
			this.outFile = outfile;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result=false;
	        try {
	        	GpxDocument doc = gpxFactory.createGpxDocument();
	        	doc.addTrack(track);
	        	Util.write(outFile.getPath(), doc.asGpx());
	        	result = true;
	        } catch (Exception e) {
	        	result = false;
	        }	
			return result;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if(!result) {
				String msg = "Error can't save Gpx Document";
	        	LOG.error("GpsModel.saveTrackAsGpx(): " + msg);
	        	GpsModel.this.showNotification(msg);
			}
			super.onPostExecute(result);
		}
	}
	public boolean saveTrackAsCsv(File outputfile, boolean withutmcoords) {
		CsvSaver saver = new CsvSaver(outputfile, withutmcoords);
		saver.execute();
		boolean result = false;
		try {
			result = saver.get();
		} catch (Exception e ) {
			LOG.error("GpsModel.saveTrackAsCsv() ERROR: Can't save csv track");
			result = false;
		}
		return result;
	}
	public class CsvSaver extends AsyncTask<Void, Void, Boolean> {
		File outFile;
		boolean withUtmCoords;
		CsvSaver(File outfile, boolean withUtmCoords) {
			this.outFile = outfile;
			this.withUtmCoords = withUtmCoords;
		}
		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result=false;
	        try {
	        	GpxDocument doc = gpxFactory.createGpxDocument();
	        	doc.addTrack(track);
	        	Util.write(outFile.getPath(), track.asCsv(withUtmCoords));
	        	result = true;
	        } catch (Exception e) {
	        	result = false;
	        }
			return result;
		}
		@Override
		protected void onPostExecute(Boolean result) {
			if(!result) {
				String msg = "Error can't save CSV Document";
	        	LOG.error("GpsModel.saveTrackAsCsv(): " + msg);
	        	GpsModel.this.showNotification(msg);
			}
        	super.onPostExecute(result);
		}
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
	public int getPointsCount() {
		if(this.track != null) {
			return this.track.wayPointCount();
		}
		return 0;
	}
	public double getSpeed() {
		return speed;
	}
	public double getBearing() {
		return bearing;
	}
	public double getDistance() {
		return distance;
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
