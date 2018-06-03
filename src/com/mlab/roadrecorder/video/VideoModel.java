package com.mlab.roadrecorder.video;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

import android.os.Environment;
import android.util.Log;
import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.AsyncTask;
import android.view.SurfaceHolder;

import com.mlab.gpx.impl.util.Util;
import com.mlab.roadrecorder.App;
import com.mlab.roadrecorder.api.AbstractObservable;

public class VideoModel extends AbstractObservable implements
		SurfaceHolder.Callback {

    final String LOGTAG = "ROADRECORDER";
	private final Logger LOG = Logger.getLogger(VideoModel.class);

	// public static final int LEVEL_INFO = 0;
	// public static final int LEVEL_DEBUG = 1;
	// public static final int LEVEL_WARNING = 2;
	// public static final int LEVEL_ERROR = 3;

	public static final int DEFAULT_VIDEO_MAX_DURATION = 18000000; // 18000 sg
	public static final long DEFAULT_VIDEO_MAX_FILE_SIZE = 2000000000; // 1500 Mb
	private static final String DEFAULT_DIRECTORY_NAME = "RoadRecorder";
	// private final int DEFAULT_CAMCORDER_PROFILE =
	// CamcorderProfile.QUALITY_1080P;
	private final int DEFAULT_CAMCORDER_PROFILE = CamcorderProfile.QUALITY_HIGH;

	private MediaRecorder mediaRecorder;
	private Camera camera;

	private File outputDirectory;
	private File outputFile;
	private int maxVideoDuration;
	private long maxVideoFileSize;

	// private Date startDate;
	private boolean isRecording;
	private boolean isEnabled;

	/**
	 * Fecha y hora del comienzo de la grabación
	 */
	private long startRecordingTime;
	/**
	 * Duración de la grabación en curso, milisegundos desde que se comenzó a
	 * grabar
	 */
	private long recordingDuration;
	
	ExecutorService executor;
	VideoTimer videoTimer;

	// SurfaceHolder holder;

	// Constructor
	/**
	 * Crea una instancia de VideoModel. <br/>
	 * Tras crear la instancia hay que comprobar la disponibilidad con el método
	 * isEnabled(); <br/>
	 * El proceso de inicialización que hay que seguir es :<br/>
	 * - 1.- setDefaultDirectory();<br/>
	 * - 2.- initCamera();<br/>
	 * - 3.- initMediaRecorder(SurfaceHolder);<br/>
	 * El SurfaceHolder no lo crea el VideoModel, hay que pasárselo como
	 * argumento en el método initMediaRecorder(). Además, la clase que crea el
	 * SurfaceHolder, tendrá que indicarle que VideoModel es quién tiene el
	 * SurfaceHolder.Callback:<br/>
	 * surfaceHolder.addCallback(model)<br/>
	 * 
	 */
	public VideoModel() {
		LOG.info("VideoModel.VideoModel()");

		isRecording = false;
		isEnabled = false;

		maxVideoDuration = DEFAULT_VIDEO_MAX_DURATION;
		maxVideoFileSize = DEFAULT_VIDEO_MAX_FILE_SIZE;

		executor = Executors.newFixedThreadPool(2);
	}

	/**
	 * El método initMediaRecorder() debe ser llamado antes de empezar a operar
	 * con el MediaRecorder. En concreto, la MainActivity lo llama en su método
	 * onStart() a través del controlador.
	 * 
	 * @return
	 */
	public boolean initMediaRecorder() {
		// String cad = "VideoModel.initMediaRecorder() ";
		mediaRecorder = new MediaRecorder();
		mediaRecorder.setCamera(camera);
		// TODO No es necesario devolver boolean
		return true;
	}

	private boolean prepare() {
		mediaRecorder = null;
		mediaRecorder = new MediaRecorder();
		if (camera == null) {
			initCamera();
		}
		camera.unlock();
		mediaRecorder.setCamera(camera);
		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			
			int profile = getCamcorderProfile();
			if (profile == -1) {
				LOG.error("VideoModel.prepare() error: profile ==-1");
				releaseMediaRecorder();
				return false;
			} 
			mediaRecorder.setProfile(CamcorderProfile.get(profile));
			LOG.info("VideoModel.prepare() CamcorderProfile = " + profile);

			// TODO Sincronizar bien con el método startRecording() y
			// startRecordingTime
			outputFile = createOutputFile();
			if (outputFile == null) {
				releaseMediaRecorder();
				return false;
			}
			mediaRecorder.setOutputFile(outputFile.getPath());

			// Anulo los límites el 3/6/2018 para entregar la versión a Claudio Rodriguez, de Argentina
			//mediaRecorder.setMaxDuration(maxVideoDuration);
			//mediaRecorder.setMaxFileSize(maxVideoFileSize);
			mediaRecorder.setMaxDuration(0);
            Log.d(LOGTAG, "VideoModel.prepare():: maxFileSize= "+maxVideoFileSize);
			mediaRecorder.setMaxFileSize(maxVideoFileSize);

			mediaRecorder.prepare();
			isEnabled = true;
		} catch (Exception e) {
			if (mediaRecorder != null) {
				mediaRecorder.release();
			}
			LOG.debug("VideoModel.initMediaRecorder() :" + e.getMessage());
			isEnabled = false;
		}
		return isEnabled;
	}

	// Camera
	boolean initCamera() {
		String cad = "VideoModel.initCamera() ";
		boolean result = false;
		camera = getCameraInstance();
		if (camera == null) {
			cad += "ERROR: Can't access to the Camera";
		} else {
			cad += "camera =" + camera.toString();
			result = true;
		}
		LOG.debug(cad);
		return result;
	}

	private Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			LOG.error("VideoModel.getCameraInstance(): Camera is not available (in use or does not exist)");
		}
		return c; // returns null if camera is unavailable
	}

	/**
	 * Lee la resolución de vídeo de App.getVideoResolution(), la parsea
	 * comprueba que existe en la cámara y en caso de no existir
	 * devuelve -1
	 * @return
	 */
	private int getCamcorderProfile() {
		//LOG.debug("getCamcorderProfile()");
		int profile = parseVideoResolution(App.getVideoResolution());
		if(!CamcorderProfile.hasProfile(profile)) {
			profile = searchAvailableProfile();
			if(!CamcorderProfile.hasProfile(profile)) {
				return -1;
			}
		}
		return profile;
	}
	/**
	 * Convierte la cadena de resolución de vídeo que guarda 
	 * App en una CamcorderProfile
	 * @param videoresolution
	 * @return
	 */
	private int parseVideoResolution(String videoresolution) {
		int profile = -1;
		if(videoresolution.equals("1080")) {
			profile = CamcorderProfile.QUALITY_1080P;
		} else if (videoresolution.equals("1080")) {
			profile = CamcorderProfile.QUALITY_720P;
		} else if (videoresolution.equals("480")) {
			profile = CamcorderProfile.QUALITY_480P;
		} else {
			profile = CamcorderProfile.QUALITY_HIGH;
		}
		//LOG.debug("parseVideoResolution() profile = " +profile );
		return profile;
	}
	/**
	 * Busca una resolución de vídeo que tenga el dispositivo o 
	 * devuelve -1
	 * @return
	 */
	private int searchAvailableProfile() {
		int profile = -1;
		if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P)) {
			profile = CamcorderProfile.QUALITY_1080P;
		} else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P)) {
			profile = CamcorderProfile.QUALITY_720P;
		} else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P)) {
			profile = CamcorderProfile.QUALITY_480P;
		} else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH)) {
			profile = CamcorderProfile.QUALITY_HIGH;
		} else if (CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW)) {
			profile = CamcorderProfile.QUALITY_LOW;
		}
		return profile;
	}
	

	// MediaRecorder management
	public boolean startRecording() {
		// LOG.debug("VideoModel.startRecording()");
		boolean result = prepare();
		if (result) {
			startRecordingTime = new Date().getTime();
			LOG.info("startRecordingTime = " + Util.dateToString(startRecordingTime, true));
			mediaRecorder.start();
			startVideoTimer();
			isRecording = true;

		} else {
			LOG.error("VideoModel.startRecording(): Can't start recording");
			isRecording = false;
			startRecordingTime = -1l;
		}
		return result;
	}

	/**
	 * No se usa. Método alternativo de arranque en segundo plano No me
	 * funcionaba bien en Feb-2014
	 * 
	 * @return
	 */
	public boolean startRecordingWithThread() {
		LOG.debug("VideoModel.startRecording()");
		RecordingStarter starter = new RecordingStarter();
		starter.execute();
		try {
			return starter.get();
		} catch (Exception e) {
			LOG.error("VideoModel.startRecording() : Can't start recording");
			return false;
		}
	}

	class RecordingStarter extends AsyncTask<Void, Void, Boolean> {
		@Override
		protected Boolean doInBackground(Void... voids) {
			boolean result = prepare();
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			if (result) {
				startRecordingTime = new Date().getTime();
				LOG.info("VideoModel.RecordingStatarter startRecordingTime = "
						+ Util.dateToString(startRecordingTime, true));
				mediaRecorder.start();
				isRecording = true;
			} else {
				LOG.error("VideoModel.RecordingStarter : Can't start recording");
				isRecording = false;
				startRecordingTime = -1l;
			}
			super.onPostExecute(result);
		}
	}

	public boolean stopRecording() {
		LOG.debug("VideoModel.stopRecording()");
		boolean result = false;
		try {
			mediaRecorder.stop();
			result = true;
			LOG.debug("VideoModel.stopRecording stopped ");
		} catch (Exception e) {
			LOG.error("VideoModel.stopRecording ERROR stopping mediaRecorder. ");
		}
		releaseMediaRecorder();
		stopVideoTimer();
		isRecording = false;
		startRecordingTime = -1l;
		return result;
	}

	/**
	 * No se usa. Método alternativo de parada en segundo plano No me funcionaba
	 * bien en Feb-2014
	 * 
	 * @return
	 */
	public boolean stopRecordingWithThread() {
		boolean result = false;
		RecordingStopper stopper = new RecordingStopper();
		stopper.execute();
		try {
			result = stopper.get();
		} catch (Exception e) {
			LOG.debug("VideoModel.stopRecording() ERROR stopping VideoModel");
			result = false;
		}
		return result;
	}

	class RecordingStopper extends AsyncTask<Void, Void, Boolean> {

		@Override
		protected Boolean doInBackground(Void... params) {
			boolean result = false;
			try {
				mediaRecorder.stop();
				result = true;
				LOG.debug("VideoModel.RecordingStopper VideoModel stopped ");
			} catch (Exception e) {
				LOG.error("VideoModel.RecordingStopper ERROR stopping mediaRecording. ");
			}
			releaseMediaRecorder();
			isRecording = false;
			startRecordingTime = -1l;
			return result;
		}

		@Override
		protected void onPostExecute(Boolean result) {
			super.onPostExecute(result);
		}

	}

	// Status
	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isRecording() {
		return isRecording;
	}

	// output Directory and file
	public File getOutputDirectory() {
		return outputDirectory;
	}

	public void setOutputDirectory(File directory) {
		this.outputDirectory = directory;
	}

	/**
	 * Establece el directorio por defecto como directorio de grabación.
	 * (Directorio Environment.DIRECTORY_PICTURES, ALBUM_NAME )
	 * 
	 * @return
	 */
	public boolean setDefaultDirectory() {
		String method = "VideoModel.setDefaultDirectory() ";
		// LOG.debug(TAG,method);
		if (!isExternalStorageEnabled()) {
			LOG.error(method + "Error, sdcard isn't mounted");
			return false;
		}
		outputDirectory = new File(Environment.getExternalStorageDirectory(), DEFAULT_DIRECTORY_NAME);
		// LOG.debug(TAG, method + "outputDirectory path: "+
		// outputDirectory.getPath());
		if (!outputDirectory.exists()) {
			LOG.debug(method + outputDirectory.getPath()
					+ " doesnt exist, creating...");
			if (!outputDirectory.mkdir()) {
				LOG.error(method + "Error, can't create default directory "
						+ outputDirectory.getPath());
				return false;
			}
		}
		// LOG.debug(method + "outputDirectory: " + outputDirectory.getPath());
		return true;
	}
    public static boolean isExternalStorageEnabled() {
        return isExternalStorageWritable() && isExternalStorageReadable();
    }
    /* Checks if external storage is available for read and write */
    public static boolean isExternalStorageWritable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state)) {
            return true;
        }
        return false;
    }

    /* Checks if external storage is available to at least read */
    public static boolean isExternalStorageReadable() {
        String state = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(state) ||
                Environment.MEDIA_MOUNTED_READ_ONLY.equals(state)) {
            return true;
        }
        return false;
    }


    public File getOutputFile() {
		return this.outputFile;
	}

	private File createOutputFile() {
		if (outputDirectory != null) {
			String filename = getTimeStamp(new Date(), true) + ".mp4";
			outputFile = new File(outputDirectory, filename);
			return outputFile;
		}
		return null;
	}
    @SuppressLint("SimpleDateFormat")
    public static String getTimeStamp(Date date, boolean gmt) {
        DateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
        if(gmt) {
            timeStamp.setTimeZone(TimeZone.getTimeZone("GMT"));
        }
        return timeStamp.format(date);
    }
	// Utilities
	/**
	 * Tiempo que lleva grabando el vídeo actual
	 * 
	 * @return
	 */
	public long getRecordingTime() {
		//LOG.debug("getRecordingTime()");
		if (isRecording) {
			long recordingtime = new Date().getTime() - this.startRecordingTime;
			return recordingtime;
		}
		return 0;
	}

	// Camera
	public Camera getCamera() {
		return camera;
	}

	// MediaRecorder
	public void reset() {
		if (this.mediaRecorder != null) {
			this.mediaRecorder.reset();
		}
	}

	// Release
	/**
	 * Libera recursos
	 */
	public void release() {
		releaseMediaRecorder();
		releaseCamera();
	}

	private void releaseCamera() {
		if (camera != null) {
			camera.release(); // release the camera for other applications
			camera = null;
		}
	}

	private void releaseMediaRecorder() {
		if (mediaRecorder != null) {
			mediaRecorder.reset(); // clear recorder configuration
			mediaRecorder.release(); // release the recorder object
			mediaRecorder = null;
			camera.lock(); // lock camera for later use
		}
	}

	// Interface SurfaceHolder.Callback
	@Override
	public void surfaceChanged(SurfaceHolder arg0, int arg1, int arg2, int arg3) {
		// TODO Auto-generated method stub

	}

	@Override
	public void surfaceCreated(SurfaceHolder holder) {
		try {
			camera.setPreviewDisplay(holder);
			camera.startPreview();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	@Override
	public void surfaceDestroyed(SurfaceHolder holder) {
		// TODO Auto-generated method stub

	}

	// Geotagging
	public static String getExifTimeStamp(Date date, boolean gmt) {
		DateFormat timeStamp = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
		if (gmt) {
			timeStamp.setTimeZone(TimeZone.getTimeZone("GMT"));
		}
		return timeStamp.format(date);
	}

	@SuppressLint("DefaultLocale")
	public boolean geotagVideoFile(Date startDate, double longitude,
			double latitude, double altitude) {
		boolean result = false;
		try {
			ExifInterface exif = new ExifInterface(outputFile.getPath());
			// Date
			String timestamp = getExifTimeStamp(startDate, true);
			exif.setAttribute(ExifInterface.TAG_DATETIME, timestamp);
			// Altitude
			// String cadalt = String.format("%4.0f", altitude);
			// exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, cadalt);
			// FIXME Falta geoetiquetar las geo
			LOG.debug("VideoModel.geotagVideoFile() saving attributes...");
			exif.saveAttributes();
			result = true;
		} catch (Exception e) {
			String cad = "VideoModel.geotagVideoFile(): Error, can't create ExifInterface";
			LOG.debug(cad);
			// FIXME Toast.makeText(context, cad, Toast.LENGTH_LONG).show();
		}

		return result;
	}

	public static String getExifLongitude() {
		// TODO
		return "";
	}

	public MediaRecorder getMediaRecorder() {
		return mediaRecorder;
	}

	public int getMaxDuration() {
		return maxVideoDuration;
	}

	public void setMaxDuration(int maxDuration) {
		this.maxVideoDuration = maxDuration;
	}

	public long getMaxFileSize() {
		return maxVideoFileSize;
	}

	public void setMaxFileSize(long maxFileSize) {
		this.maxVideoFileSize = maxFileSize;
	}

	private void startVideoTimer() {
		//LOG.debug("startVideoTimer()");
		videoTimer = new VideoTimer();
		videoTimer.executeOnExecutor(executor);
	}

	private void stopVideoTimer() {
		//LOG.debug("stopVideoTimer()");
		if (videoTimer != null) {
			videoTimer.setRunning(false);
		}
		// videoTimer = null;
	}

	/**
	 * Proporciona un mecanismo para notificar periodicamente a los observers
	 * del modelo de vídeo
	 * 
	 * @author shiguera
	 * 
	 */
	class VideoTimer extends AsyncTask<Void, Void, Void> {
		final long INTERVAL = 1000l;
		boolean running;

		public void setRunning(boolean running) {
			//LOG.debug("setRunning("+running+")");
			this.running = running;
		}

		public VideoTimer() {
			//LOG.debug("VideoTimer()");
			setRunning(true);
		}

		@Override
		protected Void doInBackground(Void... voids) {
			//LOG.debug("doInBackground()");
			while(running) {
				try {
					Thread.sleep(INTERVAL);
					publishProgress();
				} catch (Exception e) {
					LOG.debug("Error en doInBackground()");
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void...voids) {
			//LOG.debug("VideoTimer.onProgressUpdate()");
			notifyObservers();
		}

		
		@Override
		protected void onPostExecute(Void result) {
			//LOG.debug("onPostExecute()");
			super.onPostExecute(result);
		}

		@Override
		protected void onPreExecute() {
			//LOG.debug("onPreExecute()");
			super.onPreExecute();
		}

	}

}
