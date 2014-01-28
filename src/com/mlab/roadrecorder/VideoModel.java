package com.mlab.roadrecorder;

import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceHolder;

import com.mlab.roadrecorder.api.AbstractObservable;

public class VideoModel extends AbstractObservable implements
		SurfaceHolder.Callback {

	public static final String TAG = "ROADRECORDER";

	public static final int LEVEL_INFO = 0;
	public static final int LEVEL_DEBUG = 1;
	public static final int LEVEL_WARNING = 2;
	public static final int LEVEL_ERROR = 3;

	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2;
	public static final int VIDEO_MAX_DURATION = 6000000; // 6000 sg
	public static final int VIDEO_MAX_FILE_SIZE = 500000000; // 500 Mb
	private static final String DEFAULT_DIRECTORY_NAME = "RoadRecorder";
	// private static final CamcorderProfile CAMCORDER_PROFILE =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
	// private static final CamcorderProfile CAMCORDER_PROFILE =
	// CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
	private static final CamcorderProfile CAMCORDER_PROFILE = CamcorderProfile
			.get(CamcorderProfile.QUALITY_LOW);

	private MediaRecorder mediaRecorder;
	private Camera camera;
	
	private File outputFile;
	private File outputDirectory;
	// private Date startDate;
	private boolean isRecording;
	private boolean isEnabled;
	
	//SurfaceHolder holder;

	// Constructor
	/**
	 * Crea una instancia de Model. <br/>
	 * Tras crear la instancia hay que comprobar la disponibilidad con el método
	 * isEnabled(); <br/>
	 * El directorio y el nombre del fichero de salida se pueden asignar con los
	 * métodos setOutputDirectory() y setOutputFile(). Si no el programa
	 * asignará valores por defecto. <br/>
	 * 
	 * @param context
	 *            Context
	 * @param frameLayout
	 *            FrameLayout donde se insertara el SurfaceView de la cámara
	 */
	public VideoModel() {
		Log.i(TAG, "VideoModel.VideoModel()");

		isRecording = false;
		isEnabled = false;
	}

	/**
	 * El método initMediaRecorder() debe ser llamado antes de empezar a
	 * operar con el MediaRecorder. En concreto, la MainActivity lo llama
	 * en su método onStart() a través del controlador.
	 * @return
	 */
	public boolean initMediaRecorder(SurfaceHolder holder) {
		String cad = "VideoModel.initMediaRecorder() ";
		//this.holder = holder;
		mediaRecorder = new MediaRecorder();
		//camera.unlock();
		mediaRecorder.setCamera(camera);
		return true;
	}
	private boolean prepare() {
		mediaRecorder = null;
		mediaRecorder=new MediaRecorder();
		camera.unlock();
		mediaRecorder.setCamera(camera);
		try {
			mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
			mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);
			mediaRecorder.setProfile(CamcorderProfile.get(0,CamcorderProfile.QUALITY_HIGH));
			mediaRecorder.setOutputFile(createOutputFile().getPath());
			//mediaRecorder.setPreviewDisplay(holder.getSurface());
			mediaRecorder.prepare();
			isEnabled = true;
		} catch (Exception e) {
			if (mediaRecorder != null) {
				mediaRecorder.release();
			}
			Log.d("HAL", "VideoModel.initMediaRecorder() :" + e.getMessage());
			isEnabled = false;
		}
		return isEnabled;
	}

	// Camera
	public boolean initCamera() {
		String cad = "VideoModel.initCamera() ";
		boolean result = false;
		camera = getCameraInstance();
		if (camera == null) {
			cad += "ERROR: Can't access to the Camera";
		} else {
			cad += "camera =" + camera.toString();
			result = true;
		}
		Log.d(TAG, cad);
		return result;
	}
	private Camera getCameraInstance() {
		Camera c = null;
		try {
			c = Camera.open(); // attempt to get a Camera instance
		} catch (Exception e) {
			Log.e(TAG,
					"VideoModel.getCameraInstance(): Camera is not available (in use or does not exist)");
		}
		return c; // returns null if camera is unavailable
	}

	// MediaRecorder management
	public boolean startRecording() {
		// Log.d(TAG, "VideoModel.startRecording() "+outputfile.getPath());
		boolean result = prepare();
		if(result) {
			mediaRecorder.start();
			isRecording = true;
		} else {
			Log.e(TAG, "VideoModel.startRecording() : Can't start recording");
			isRecording = false;
		}
		return result;
	}
	public boolean stopRecording() {
		boolean result = false;
		try {
			Log.d(TAG, "VideoModel.stopRecording() stopping VideoModel... ");
			mediaRecorder.stop();
			//releaseMediaRecorder();
			result = true;
		} catch (Exception e) {
			Log.e(TAG, "VideoModel.stopRecording() ERROR stopping mediaRecording. ");
		}
		releaseMediaRecorder();
		isRecording = false;
		return result;
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
	/**
	 * Establece el directorio por defecto como directorio de grabación.
	 * (Directorio Environment.DIRECTORY_PICTURES, ALBUM_NAME )
	 * 
	 * @return
	 */
	public boolean setDefaultDirectory() {
		String method = "VideoModel.setDefaultDirectory() ";
		// Log.d(TAG,method);
		if (!this.isExternalStorageEnabled()) {
			Log.e(TAG, method + "Error, sdcard isn't mounted");
			return false;
		}
		outputDirectory = new File(getExternalStorageDirectory(),
				DEFAULT_DIRECTORY_NAME);
		// Log.d(TAG, method + "outputDirectory path: "+
		// outputDirectory.getPath());
		if (!outputDirectory.exists()) {
			Log.d(method + TAG, method + outputDirectory.getPath()
					+ " doesnt exist, creating...");
			if (!outputDirectory.mkdir()) {
				Log.e(TAG, method + "Error, can't create default directory "
						+ outputDirectory.getPath());
				return false;
			}
		}
		Log.d(method + TAG,
				method + "outputDirectory: " + outputDirectory.getPath());
		return true;
	}
	public File getOutputFile() {
		return this.outputFile;
	}
	public void setOutputFile(File file) {
		outputFile = file;
		if(mediaRecorder != null) {
			mediaRecorder.setOutputFile(outputFile.getPath());
		}
	}
	private File createOutputFile() {
		if(outputDirectory != null) {
			String filename = this.getTimeStamp(new Date(), true) +".mp4";
			outputFile = new File(outputDirectory, filename);
			return outputFile;
		}
		return null;
	}
	
	// Utilities
	/**
	 * Devuelve el directorio público de Android para vídeos
	 * 
	 * @return
	 */
	public File getMoviesDirectory() {
		return Environment
				.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES);
	}
	public File getExternalStorageDirectory() {
		return Environment.getExternalStorageDirectory();
	}
	public boolean isExternalStorageEnabled() {
		// Log.d(TAG,
		// "VideoModel.isExternalStorageEnabled() ExternalStorage.MEDIA_MOUNTED: "+Environment.MEDIA_MOUNTED);
		// Log.d(TAG,
		// "VideoModel.isExternalStorageEnabled() ExternalStorage state: "+Environment.getExternalStorageState());
		return (Environment.getExternalStorageState()
				.equalsIgnoreCase(Environment.MEDIA_MOUNTED));
	}
	/**
     * Devuelve una cadena de la forma "yyyyMMdd_HHmmss" con
     * la fecha y hora GMT correspondiente a la fecha pasada
     * como argumento.
     * @param date Date 
     * @return Cadena "yyyyMMdd_HHmmss" con la fecha GMT
     */
    @SuppressLint("SimpleDateFormat")
    public String getTimeStamp(Date date, boolean gmt) {
    	DateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
    	if(gmt) {
    		timeStamp.setTimeZone(TimeZone.getTimeZone("GMT"));
    	}
    	return timeStamp.format(date);
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
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            camera.lock();           // lock camera for later use
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
			Log.d(TAG, "VideoModel.geotagVideoFile() saving attributes...");
			exif.saveAttributes();
			result = true;
		} catch (Exception e) {
			String cad = "VideoModel.geotagVideoFile(): Error, can't create ExifInterface";
			Log.d(TAG, cad);
			// FIXME Toast.makeText(context, cad, Toast.LENGTH_LONG).show();
		}

		return result;
	}
	public static String getExifLongitude() {
		// TODO
		return "";
	}
}
