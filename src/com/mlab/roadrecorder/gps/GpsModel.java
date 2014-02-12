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
import com.mlab.gpx.impl.SimpleWayPoint;
import com.mlab.gpx.impl.Track;
import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.api.AbstractObservable;

/**
 * Dispone de un GpsManager para acceder al GPS. Registra las posiciones
 * en un Track que se puede grabar en formato GPX y CSV
 * 
 * @author shiguera
 *
 */
public class GpsModel extends AbstractObservable implements GpsListener {

	//private final String TAG = "ROADRECORDER";

	private final Logger LOG = Logger.getLogger(GpsModel.class);
	
	private Context context;
	private GpxFactory gpxFactory;

	protected GpsManager gpsManager;
	protected Track track;

	// Datos de status guardados para el caso isRecording=true
	protected boolean isRecording;
	// Primer punto guardado: lon, lat, alt, t
	protected WayPoint firstWayPoint;
	// Ultimo punto guardado: lon, lat, alt, t
	protected WayPoint lastWayPoint;
	// Ultimo tramo recorrido (Del último punto al anetrior): distance, speed, bearing, incT, incAltitude 
	protected long lastIncT;
	protected double lastDistance;
	protected double lastSpeed;
	protected double lastBearing;
	protected double lastIncAltitude;
	// Datos a origen
	protected long accT;
	protected double accDistance;
	protected double accDistanceUp;
	protected double accDistanceDown;
	protected double accIncAltitude;
	protected double accIncAltitudeUp;
	protected double accIncAltitudeDown;
	// Medias
	protected double avgSpeed;
	
	// Constructor
	public GpsModel(Context context) {
		super();
		this.context = context;
		gpsManager = new GpsManager(context);
		gpxFactory = GpxFactory.getFactory(GpxFactory.Type.AndroidGpxFactory);
		track = new Track();
		
		initStatusValues();
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
				track = new Track();
				initStatusValues();
			}
			isRecording = true;
			notifyObservers(null);
			return true;
		}
		return false;
	}
	private void initStatusValues() {
		firstWayPoint = null;
		lastWayPoint = null;
		//
		lastIncT = 0l;
		lastDistance = -1.0;
		lastSpeed = -1.0;
		lastBearing = -1.0;
		lastIncAltitude = 0.0;
		//
		accT = 0l;
		accDistance = 0.0;
		accDistanceUp = 0.0;
		accDistanceDown = 0.0;
		accIncAltitude = 0.0;
		accIncAltitudeUp = 0.0;
		accIncAltitudeDown = 0.0;
		// 
		avgSpeed = 0.0;
		
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
		// TODO Pasar a AsyncTask con Synchronized
		if(wp != null) {
			if(firstWayPoint == null) {
				firstWayPoint = wp.clone();
				lastWayPoint = wp.clone();
				return;
			}
			// Last point
			lastIncT = (long)((wp.getTime()-lastWayPoint.getTime())/1000l);
			lastDistance = Util.dist3D(lastWayPoint, wp);
			lastSpeed = lastDistance / (double)lastIncT;
			lastBearing = Util.bearing(lastWayPoint, wp);
			lastIncAltitude = wp.getAltitude() - lastWayPoint.getAltitude();
			// Accumulates
			accT += lastIncT;
			accDistance += lastDistance;
			if(lastIncAltitude>0.0) {
				accDistanceUp += lastIncAltitude;
				accIncAltitudeUp += lastIncAltitude;
			} else {
				accDistanceDown += lastIncAltitude;
				accIncAltitudeDown += lastIncAltitude;
			}
			// Averages
			avgSpeed = accDistance / (double)accT;
			//
			track.addWayPoint(wp, false);	
			lastWayPoint = wp.clone();
		}
	}

	/**
	 * Graba el Track en un fichero en formato GPX.
	 * Utiliza un proceso asíncrono, pero espera hasta la respuesta
	 * 
	 * @param outputfile Fichero de salida
	 * 
	 * @return true si ok, false en caso de errores
	 */
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
	/**
	 * AsyncTask para grabar el Track en un fichero en formato GPX.
	 * Notifica a través de un Toast si hay error
	 * 
	 */
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
	/**
	 * Graba el Track en un fichero en formato CSV.
	 * Utiliza un proceso asíncrono, pero espera hasta la respuesta
	 * 
	 * @param outputfile Fichero de salida
	 * 
	 * @return true si ok, false en caso de errores
	 */
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
	/**
	 * AsyncTask para grabar el Track en un fichero en formato CSV
	 * Notifica a través de un Toast si hay error
	 * 
	 */
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
		return lastSpeed;
	}
	public double getBearing() {
		return lastBearing;
	}
	public double getDistance() {
		return lastDistance;
	}

	// Status
	public boolean isGpsEnabled() {
		return this.gpsManager.isGpsEnabled();
	}
	public boolean isReceiving() {
		return this.gpsManager.isGpsEventFirstFix();
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

	public WayPoint getFirstWayPoint() {
		return firstWayPoint;
	}

	public long getLastIncT() {
		return lastIncT;
	}

	public double getLastDistance() {
		return lastDistance;
	}

	public double getLastSpeed() {
		return lastSpeed;
	}

	public double getLastBearing() {
		return lastBearing;
	}

	public double getLastIncAltitude() {
		return lastIncAltitude;
	}

	public long getAccT() {
		return accT;
	}

	public double getAccDistance() {
		return accDistance;
	}

	public double getAccDistanceUp() {
		return accDistanceUp;
	}

	public double getAccDistanceDown() {
		return accDistanceDown;
	}

	public double getAccIncAltitude() {
		return accIncAltitude;
	}

	public double getAccIncAltitudeUp() {
		return accIncAltitudeUp;
	}

	public double getAccIncAltitudeDown() {
		return accIncAltitudeDown;
	}

	public double getAvgSpeed() {
		return avgSpeed;
	}
}
