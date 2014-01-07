package com.example.roadrecorderalvac;


import java.io.File;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.media.CamcorderProfile;
import android.media.ExifInterface;
import android.media.MediaRecorder;
import android.os.Environment;
import android.util.Log;
import android.widget.FrameLayout;
import android.widget.Toast;

public class VideoManager {
	
	public static final int LEVEL_INFO = 0;
    public static final int LEVEL_DEBUG = 1;
    public static final int LEVEL_WARNING = 2;
    public static final int LEVEL_ERROR = 3;
    
	public static final String TAG = "HAL";
	public static final int MEDIA_TYPE_IMAGE = 1;
    public static final int MEDIA_TYPE_VIDEO = 2;
    public static final int VIDEO_MAX_DURATION = 6000000; // 6000 sg
    public static final int VIDEO_MAX_FILE_SIZE = 500000000; // 500 Mb
    private static final String ALBUM_NAME = "RoadRecorder";
    //private static final CamcorderProfile CAMCORDER_PROFILE = CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH);
	private static final CamcorderProfile CAMCORDER_PROFILE = CamcorderProfile.get(CamcorderProfile.QUALITY_480P);
	//private static final CamcorderProfile CAMCORDER_PROFILE = CamcorderProfile.get(CamcorderProfile.QUALITY_LOW);
	
	private MediaRecorder mediaRecorder;
	private Camera camera;
	private CameraSurfaceView cameraSurfaceView;
	
	private File outputFile;
	private File outputDirectory;
	//private Date startDate;
	private boolean isRecording;
	
	private boolean isEnabled;
	//private boolean hasCamera; // Por hardware, si tiene cámara el dispositivo
	private boolean isCameraEnabled; // Por disponibilidad de la cámara para el programa
	private Context context;
		
	/**
	 * Crea una instancia de VideoManager. <br/>
	 * Tras crear la instancia hay que comprobar la disponibilidad 
	 * con el método isEnabled(); <br/>
	 * El directorio  y el nombre del fichero de salida se pueden asignar
	 * con los métodos setOutputDirectory() y setOutputFile(). Si no el 
	 * programa asugnará unos valores por defecto. <br/>
	 * @param context Context
	 * @param frameLayout FrameLayout donde se insertara el SurfaceView de la cámara
	 */
	public VideoManager(Context context, FrameLayout frameLayout) {
		this.context = context;
		// Se marca en true. Si alguna comprobación falla se marca en false
		isEnabled=true;
		String cad ="";
    	
		// Check camera hardware
        isEnabled = hasCameraHardware();        		
				
        // Inicializar La cámara
        camera = getCameraInstance();
        if(camera == null){
        	isCameraEnabled=false;
        	cad = "VideoManager.VideoManager() ERROR: Can't access to the Camera";
        } else {
        	isCameraEnabled=true;
    		cad = "VideoManager.VideoManager() camera="+camera.toString();
        }
        Log.d(TAG, cad);
        
        // Inicializar el SurfaceView
        cameraSurfaceView = new CameraSurfaceView(this.context, camera);
        frameLayout.removeAllViews();
        frameLayout.addView(cameraSurfaceView);
        
        // Establece el directorio y el nombre del fichero 
        // de salida por defecto
        try {
        	setDefaultFile();
        } catch (Exception e) {
            Log.e(TAG, e.getMessage());
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_LONG).show();
            isEnabled=false;
        }
        
        // status not recording
        isRecording = false;
        
	}

	public boolean startRecording() {
		Log.d(TAG, "VideoManager.startRecording()");
		//Release Camera before MediaRecorder start
		releaseCamera();

		isEnabled = prepareMediaRecorder(); 
		if(!isEnabled) {
			release();
			return false;
		}
		
		this.mediaRecorder.start();
		isRecording = true;
		return true;
	}
	
	// Camera and MediaRecorder
	private boolean prepareMediaRecorder(){

	    camera = getCameraInstance();
	    try {
			camera.reconnect();
		} catch (IOException e1) {
			Log.d(TAG, "ERROR in prepareMediaRecorder() can't reconnect camera");
		}
		Log.d(TAG, "Camera: "+camera.toString());

	    mediaRecorder = new MediaRecorder();

	    camera.unlock();
	    mediaRecorder.setCamera(camera);

	    mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
	    mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

	    mediaRecorder.setProfile(CAMCORDER_PROFILE);
	    
	    if(outputFile == null) {
		    Log.e(TAG,"VideoManager.prepareMediaRecorder() ERROR outputFile=null");
	    	return false;
	    }
	    Log.d(TAG,"VideoManager.prepareMediaRecorder() outputFile="+outputFile.getPath());
	    mediaRecorder.setOutputFile(outputFile.getPath());
	    
        mediaRecorder.setMaxDuration(VIDEO_MAX_DURATION); // Set max duration 600 sec.
        mediaRecorder.setMaxFileSize(VIDEO_MAX_FILE_SIZE); // Set max file size 50M

	    mediaRecorder.setPreviewDisplay(cameraSurfaceView.getHolder().getSurface());
	    Log.d(TAG,"VideoManager.prepareMediaRecorder() setPreviewDisplay");

	    try {
	        mediaRecorder.prepare();
		    Log.d(TAG,"VideoManager.prepareMediaRecorder() mediaRecorder.prepare()");
	    } catch (IllegalStateException e) {
	        releaseMediaRecorder();
	        return false;
	    } catch (IOException e) {
	        releaseMediaRecorder();
	        return false;
	    }
	    return true;
		
	}
	
	public void stopRecording() {
		try{
			//Log.d(TAG,"VideoManager.stopRecording() resetting videoManager... ");
			//mediaRecorder.reset();
			Log.d(TAG,"VideoManager.stopRecording() stopping videoManager... ");
			mediaRecorder.stop();
		} catch (Exception e) {
			Log.e(TAG,"VideoManager.stopRecording() ERROR stopping mediaRecording. ");
			Toast.makeText(context, "Error stopping mediaRecorder", Toast.LENGTH_LONG).show();
		}
		release();
		isRecording = false;
	}
	public boolean isEnabled() {
		return isEnabled;
	}

	public boolean isRecording() {
		return isRecording;
	}

	public boolean hasCameraHardware() {
		return context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
	}

	public boolean isCameraEnabled() {
		return isCameraEnabled;
	}

	public File getOutputDirectory() {
		return outputDirectory;
	}
	public String getOutputDirectoryName() {
		if( outputDirectory != null) {
			return outputDirectory.getName();			
		} else {
			return "";
		}
	}
	
	public File setOutputFile(String outputFileNameWithoutExtension) {
		Log.d(TAG,"VideoManager.setOutputFile("+outputFileNameWithoutExtension+")");
		if(outputDirectory == null ) {
			Log.d(TAG,"VideoManager.setOutputFile() : outputDirectory==null => setting default");
			setDefaultDirectory();
		}
		String filename = outputFileNameWithoutExtension + ".mp4";
		outputFile = new File (outputDirectory, filename);
		return outputFile;
	}
	/** Set outputDirectory. It is created if doesn't exist 
	 *  
	 * @param outputDirectoryName
	 * @return File El directorio asignado  outputDirectory. Si no se puede crear
	 * el pasado como parámetro se asigna el valor por defecto
	 */
	public File setOutputDirectory(File outputDirectory) {     
		Log.d(TAG,"VideoManager.setOutputDirectory("+outputDirectory.getName()+")");
		// FIXME Comprobar estado de la tarjeta sdcard
        // Create the storage directory if it does not exist
        if (! outputDirectory.exists()){
            Log.d(TAG,"VideoManager.setOutputDirectory(): outputDirectory doesn't exists");
        	if (! outputDirectory.mkdirs()){
                Log.d("HAL", "VideoManager.setOutputDirectory(): failed to create directory, setting default");
    			outputDirectory = new File(Environment.getExternalStoragePublicDirectory(
                        Environment.DIRECTORY_PICTURES), ALBUM_NAME);    		
            }
        } else {
        	this.outputDirectory = outputDirectory;
        	Log.d(TAG,"VideoManager.setOutputDirectory(): outputDirectory="+outputDirectory.getPath());
        }
        return outputDirectory;
	}
	/** Set outputDirectory. It is created if doesn't exist 
	 *  
	 * @param outputDirectoryName
	 */
	public File setOutputDirectory(String outputDirectoryName) {
		File file = new File (Environment.getExternalStorageDirectory()+"/"+outputDirectoryName); 
		return setOutputDirectory(file);
	}

	public File setDefaultDirectory() {
    	Log.d(TAG,"VideoManager.setDefaultDirectory()");
		outputDirectory = new File(Environment.getExternalStoragePublicDirectory(
                Environment.DIRECTORY_PICTURES), ALBUM_NAME);  
		if(outputDirectory.exists()==false) {
			outputDirectory.mkdir();
		}
		return outputDirectory;
	}

	/** Create a File for saving an image or video */
    private void setDefaultFile() throws Exception {
    	Log.d(TAG,"VideoManager.setDefaultFile()");
 	
    	// FIXME
        // To be safe, you should check that the SDCard is mounted
        // using Environment.getExternalStorageState() before doing this.

    	// Create the storage directory if it does not exist
    	setDefaultDirectory();
    	if (outputDirectory == null || !outputDirectory.exists()) {
        	throw new Exception("Error, can't create output directory");
        } 

        // Create a media file name
        String timestamp = getTimeStamp(new Date(), true);        	
        outputFile = new File(outputDirectory.getPath() + File.separator + timestamp + ".mp4");        
    }
    /**
     * Devuelve una cadena de la forma "yyyyMMdd_HHmmss" con
     * la fecha y hora GMT correspondiente a la fecha pasada
     * como argumento.
     * @param date Date 
     * @return Cadena "yyyyMMdd_HHmmss" con la fecha GMT
     */
    @SuppressLint("SimpleDateFormat")
	public static String getTimeStamp(Date date, boolean gmt) {
    	DateFormat timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss");
    	if(gmt) {
    		timeStamp.setTimeZone(TimeZone.getTimeZone("GMT"));
    	}
    	return timeStamp.format(date);
    }
    
    public static String getExifTimeStamp(Date date, boolean gmt) {
    	DateFormat timeStamp = new SimpleDateFormat("yyyy:MM:dd HH:mm:ss");
    	if(gmt) {
    		timeStamp.setTimeZone(TimeZone.getTimeZone("GMT"));
    	}
    	return timeStamp.format(date);
    }

    @SuppressLint("DefaultLocale")
	public boolean geotagVideoFile (Date startDate, double longitude, double latitude, double altitude)  {
		boolean result = false;
		try {
			ExifInterface exif = new ExifInterface(outputFile.getPath());
			// Date
			String timestamp = getExifTimeStamp(startDate, true);
			exif.setAttribute(ExifInterface.TAG_DATETIME, timestamp);
			// Altitude
			//String cadalt = String.format("%4.0f", altitude);
			//exif.setAttribute(ExifInterface.TAG_GPS_ALTITUDE, cadalt);
			// FIXME Falta geoetiquetar las geo
			Log.d(TAG, "VideoManager.geotagVideoFile() saving attributes...");
			exif.saveAttributes();			
			result = true;
		} catch (Exception e) {
			String cad = "VideoManager.geotagVideoFile(): Error, can't create ExifInterface";
			Log.d(TAG, cad);
			Toast.makeText(context, cad, Toast.LENGTH_LONG).show();
		}
		
		return result;
	}
	public static String getExifLongitude() {
		// TODO
		return "";
	}

	/**
	 * Libera recursos
	 */
	public void release() {
    	releaseMediaRecorder();
    	releaseCamera();
    }
	
	public void reset() {
		if(this.mediaRecorder != null) {
			this.mediaRecorder.reset();
		}
	}
	
    private Camera getCameraInstance(){
    	
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
        	Log.e("HAL","VideoManager.getCameraInstance(): Camera is not available (in use or does not exist)");
        }
        return c; // returns null if camera is unavailable
	}
    
	private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = null;
            //camera.lock();           // lock camera for later use
        }
    }
	
	private void releaseCamera(){
        if (camera != null){
        	camera.release();        // release the camera for other applications
            camera = null;
        }
    }
	
}
