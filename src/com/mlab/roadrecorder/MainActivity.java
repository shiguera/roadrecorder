package com.mlab.roadrecorder;

import java.io.File;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.graphics.Point;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Display;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roadrecorderalvac.R;
import com.mlab.android.gpsmanager.GpsListener;
import com.mlab.android.gpsmanager.GpsManager;
import com.mlab.gpx.api.GpxDocument;
import com.mlab.gpx.api.GpxFactory;
import com.mlab.gpx.api.GpxFactory.Type;
import com.mlab.gpx.api.WayPoint;
import com.mlab.gpx.impl.Track;
import com.mlab.gpx.impl.util.Util;

public class MainActivity extends Activity implements GpsListener, SensorEventListener {

	private final String TAG = "RoadRecorder";
    private enum LogLevel {INFO,DEBUG,WARNING,ERROR};
    
    public static final String EXTSDCARD_PATH = "/storage/extSdCard";

    public static final String APP_FILENAME = "RoadRecorder";
    public static final String DATABASE_NAME = "rvt.db";

    public static final int TIME_LAPSE = 1000; // Tiempo en milisegundos para el timer principal
    public static final int BLINK_LABEL_LAPSE = 500; 
    private static final long MAX_RECORDING_TIME = 1200000; // Número de milisegundos de cada grabación de ficheros 
	   
    // Layout
	Button btnStartStop;
	TextView lblInfo,lblrecordtime,lblposition, lblpointscount;
	FrameLayout frameLayout;
	int screenWidth, screenHeight;

	// Data
    private File appDirectory;
	Track track;	
	String trackLabel = "";
	Date startDate;
	
	// Components: VideoManager
	VideoModel videoManager;
	
	// Components: GpsManager
	GpsManager gpsManager=null;
	private Location lastLocReceived=null;
	private Location lastLocSaved=null;

	// Components: SensorManager
	SensorManager sensorManager;
	Sensor accelerometerSensor, magneticSensor, linearAccSensor, pressureSensor;
	double pressure = 0.0;
	
	// SensorTrack
	//SensorTrack sensorTrack = null;
	Track sensorTrack = null;
	
	// Components DatabaseManager
	SQLiteDatabase db = null;
		
	// Components: Timer
	Timer timer;     // Se utiliza para lanzar PointRecorder como tarea de fondo 
	Timer mainTimer; // A intervalos definidos en TIME_LAPSE proporciona actualizaciones de pantalla
	Timer blinkLabelTimer;
	
	// Status
	private enum Status {FIXING_GPS, GPS_FIXED, RECORDING, SAVING};
	private Status status;
	
	// Live cycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		Log.i(TAG,"MainActivity.onCreate()");
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		
		// Comprobar si es la primera ejecución
		//checkFirstExecution();
		
		String cad="";
		
		// Ajustar el layout a la pantalla
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		adjustLayout();
		
		// Inicializar directorio de la aplicación
        appDirectory = initAppDirectory();
        if(appDirectory==null) {
			notify("ERROR: Can't create storage directory", LogLevel.ERROR, true);
           	finish();
           	return;        		
       	} 
       	notify("MainActivity.onCreate() App directory at"+appDirectory.getPath(),LogLevel.DEBUG,false);
		
        // Inicializar videoManager        
		videoManager = initVideoManager(appDirectory);
		if(videoManager.isEnabled() == false) {
			notify("ERROR: Can't enable VideoManager",LogLevel.ERROR,true);		
			finish();
			return;
		}
		notify("videoManager.isEnabled= "+videoManager.isEnabled(),LogLevel.DEBUG,false);
		        
		// Inicializar sensores
        // Inicializar Sensores: en onResume()
		initSensors();
		//sensorTrack = new SensorTrack();
		sensorTrack = new Track();
		        	        
        // Inicializar GpsManager
		gpsManager = initGpsManager();
        if(!gpsManager.isGpsEnabled()) {
        	cad = "Error : Inicialización incorrecta del GpsManager";
        	Toast.makeText(getApplicationContext(), cad, 
        		Toast.LENGTH_LONG).show();	
        	finish();
        	return;
        } else {
        	cad = "MainActivity(): GpsManager inicializado corectamente";
        }
    	Log.d(TAG, cad);
        gpsManager.registerGpsListener(this);
        gpsManager.startGpsUpdates();
        lastLocSaved=new Location(LocationManager.GPS_PROVIDER);
        
        
        // Inicializar Timer
        mainTimer = new Timer();
        mainTimer.scheduleAtFixedRate(new MainTimerTask(), 0, TIME_LAPSE);
        
        initBlinkLabelTimer();

        // Establecer status
        status = Status.FIXING_GPS;
	}
	@Override
	protected void onRestart() {
		Log.i(TAG,"MainActivity.onRestart()");
		super.onRestart();
	}
	@Override
	protected void onStart() {
		Log.i(TAG,"MainActivity.onStart()");
		super.onStart();
	}
	@Override
	protected void onResume() {
		Log.i(TAG,"MainActivity.onResume()");
		sensorManager.registerListener(this,accelerometerSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this,magneticSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, linearAccSensor, SensorManager.SENSOR_DELAY_NORMAL);
		sensorManager.registerListener(this, pressureSensor, SensorManager.SENSOR_DELAY_NORMAL);
		super.onResume();
	}
	@Override
	protected void onPause() {
		Log.i(TAG,"MainActivity.onPause()");
		if(mainTimer != null ) {
        	mainTimer.cancel();    
        }
        if(blinkLabelTimer != null ) {
        	blinkLabelTimer.cancel();    
        }
        if(timer != null ) {
        	timer.cancel();    
        }
        if(videoManager != null ) {
        	if(videoManager.isRecording()) {
        		videoManager.stopRecording();
        	}
        	videoManager.release();
        }

        if(gpsManager != null) {
        	gpsManager.stopGpsUpdates();
        }
		if(sensorManager!=null) {
			sensorManager.unregisterListener(this);
		}
        super.onPause();

	}
	@Override
	protected void onStop() {
		Log.i(TAG,"MainActivity.onStop()");
		super.onStop();
	}
	@Override
	protected void onDestroy() {
		Log.i(TAG,"MainActivity.onDestroy()");
		super.onDestroy();
	}

	// First execution
	private void checkFirstExecution() {
		if(isFirstExecution()) {
			Log.d(TAG, "First execution");
			if(!createAppDirectory()) {
				Log.e(TAG, "Can't create application directory");
				showMessage("Error Creating Application Directory");
				finish();
				return;
			}
			Log.d(TAG, "Created application directory");
			if(!createSettingsFile()) {
				Log.e(TAG, "Error Creating settings file");
				showMessage("Error Creating File");
				finish();
				return;
			}
			Log.d(TAG, "Created settings file");
		}
	}
	private boolean isFirstExecution() {
		// Se comprueba si es la primera ejecución del programa
		// por la existencia o no del directorio de la aplicación
		// TODO Complete method
		return true;
	}
	private boolean createAppDirectory() {
		return true;
	}
	private boolean createSettingsFile() {
		return true;
	}
	private void showMessage(String msg) {
		Toast.makeText(this, msg, Toast.LENGTH_LONG).show();
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch(item.getItemId()) {
		case R.id.menuitem_config:
			startActivityConfig();
			break;
		case R.id.menuitem_help:
			startActivityHelp();			
			break;
		case R.id.menuitem_about:
			startActivityAbout();
			break;
		case R.id.menuitem_back:
			finish();
			break;
		}
		return super.onOptionsItemSelected(item);
	}
	private void startActivityConfig() {
		this.showMessage("Opción en desarrollo");
//		Intent i = new Intent(this, ConfigActivity.class);
//		startActivity(i);
	}
	private void startActivityHelp() {
		this.showMessage("Opción en desarrollo");
//		Intent i = new Intent(this, HelpActivity.class);
//		startActivity(i);	
	}
	private void startActivityAbout() {
		this.showMessage("Opción en desarrollo");
//		Intent i = new Intent(this, AboutActivity.class);
//		startActivity(i);
	}
	
	private VideoModel initVideoManager(File outputDirectory) {
       	//videoManager = new VideoModel(this, frameLayout);
       	// Asignar el outputDirectory al videoManager
     	//videoManager.setOutputDirectory(outputDirectory);
       	return videoManager;
	}
	private GpsManager initGpsManager() {
		Log.d(TAG,"MainActivity.initGpsManager()");
		gpsManager = new GpsManager(this.getApplicationContext());
		return gpsManager;
	}
	private File initAppDirectory() {
		// Probamos a ver si está montada la tarjeta externa
		File device = new File(EXTSDCARD_PATH);
		notify("File "+device.getPath()+" exists: "+String.format("%b", device.exists()),LogLevel.DEBUG,false);
		notify("File "+device.getPath()+" can write: "+String.format("%b", device.canWrite()),LogLevel.DEBUG,false);
		if(!device.exists() || !device.canWrite()) {
			device = new File(Environment.getExternalStorageDirectory().getPath());
		}
		File appdirectory = new File(device, APP_FILENAME);
		if(appdirectory.exists()==false) {
			if(appdirectory.mkdir()==false) {
				return null;
			}
			notify("appDirectory created at "+appdirectory.getPath(),LogLevel.DEBUG, false);
		}
        return appdirectory;
        
	}
	private void initSensors() {
		sensorManager=(SensorManager)getSystemService(SENSOR_SERVICE);
		accelerometerSensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		magneticSensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD);
		linearAccSensor = sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION);
		pressureSensor = sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
	}
	
	private void adjustLayout() {
		// Calcular el tamaño de la pantalla
		Display display = getWindowManager().getDefaultDisplay();
		Point size = new Point();
		display.getSize(size);
		screenWidth = size.x;
		screenHeight = size.y;
		Log.i(TAG, String.format("ScreenSize=%d x %d", screenWidth, screenHeight ));
		
		// Ajustar el tamaño del FrameLayout
		int fheight = (int) (0.65*(float)size.y);
		int fwidth = (int) ((float)fheight*1.5);
		Log.i(TAG, String.format("FrameLayout size: %d x %d", fwidth, fheight));
        frameLayout = (FrameLayout)this.findViewById(R.id.videoview);
        LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(fwidth, fheight);
        frameLayout.setLayoutParams(pars);		

        // Inicializar UI
        // Button Start-Stop
        btnStartStop = (Button)findViewById(R.id.mybutton);
        btnStartStop.setOnClickListener(myButtonOnClickListener);
        btnStartStop.setEnabled(false); // Se activa en firstFixEvent() 
        // lblInfo
        lblInfo=(TextView)this.findViewById(R.id.lblInfo);
        lblrecordtime=(TextView)this.findViewById(R.id.lblrecordtime);
        lblposition=(TextView)this.findViewById(R.id.lblposition);  
        lblpointscount=(TextView)this.findViewById(R.id.lblpointscount);          

	}

	// Button StartStop
	Button.OnClickListener myButtonOnClickListener
    = new Button.OnClickListener(){

		@Override
		public void onClick(View v) {
			if(videoManager.isRecording()) {
				MainActivity.this.notify("Stoping",LogLevel.INFO, false);
				stopRecording();
                //Exit after saved
                finish();
                return;
			} else {
				MainActivity.this.notify("Starting",LogLevel.INFO, false);
				startRecording();
			}
		}	
	};

	private void initBlinkLabelTimer() {
		if(blinkLabelTimer != null) {
			blinkLabelTimer.cancel();
		}
        blinkLabelTimer = new Timer();
        blinkLabelTimer.scheduleAtFixedRate(new BlinkLabelTimerTask(), 0, BLINK_LABEL_LAPSE);
	}
		
		
	public void notify(String message, LogLevel level, boolean withToast) {
		switch(level) {
		case INFO:
			Log.i(TAG, message);
			break;
		case DEBUG:
			Log.d(TAG, message);			
			break;
		case WARNING:
			Log.w(TAG, message);						
			break;
		case ERROR:
			Log.e(TAG, message);						
			break;
		}
		if(withToast) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
	
	// Interface GpsListener
    @Override
    public void firstFixEvent() {
		//Log.d(TAG,"MainActivity.firstFixEvent()");
		btnStartStop.setEnabled(true);
		status = Status.GPS_FIXED;
		lblInfo.setText("GPS fixed, you can start recording");
		blinkLabelTimer.cancel();
		lblInfo.setVisibility(View.VISIBLE);
		notify("Gps fixed, you can start recording",LogLevel.DEBUG,true);
	}

// Before modification 4-6-2013
//    @Override
//	public void updateLocation(Location loc) {
//		this.lastLocReceived = new Location(loc);
//		float[] globalacc = calculateGlobalAcceleration();
//		SensorPoint point = new SensorPoint(lastLocReceived.getTime(), lastLocReceived.getLongitude(),
//			lastLocReceived.getLatitude(), lastLocReceived.getAltitude(), (double)globalacc[0],
//			(double)globalacc[1], (double)globalacc[2], pressure);
//		if(status == Status.RECORDING) {
//			this.sensorTrack.add(point);			
//		}
//	}
    @Override
	public void updateLocation(Location loc) {
		this.lastLocReceived = new Location(loc);
		float[] globalacc = calculateGlobalAcceleration();
		GpxFactory factory = GpxFactory.getFactory(Type.ExtendedGpxFactory);
		List<Double> listvalues = Arrays.asList(new Double[]{lastLocReceived.getLongitude(),
				lastLocReceived.getLatitude(), lastLocReceived.getAltitude(),
				(double) lastLocReceived.getSpeed(), (double) lastLocReceived.getBearing(), 
				(double) lastLocReceived.getAccuracy(),	(double)globalacc[0], 
				(double)globalacc[1], (double)globalacc[2], pressure});
		WayPoint point = factory.createWayPoint("", "", lastLocReceived.getTime(), listvalues); 
		if(status == Status.RECORDING) {
			this.sensorTrack.addWayPoint(point, false);			
		}
	}
    
    private void startRecording() {
		startDate = new Date();
		//trackLabel = VideoModel.getTimeStamp(startDate, true);
		//videoManager.setOutputFile(trackLabel);
		
		//sensorTrack = new SensorTrack();
		sensorTrack = new Track();
		
		updateLocation(lastLocReceived);
        
		//boolean result = videoManager.startRecording();
//        if(!result){
//        	MainActivity.this.notify("MainActivity: Fail in startRecording!\n - Ended -",LogLevel.ERROR,true);
//        	finish();
//        	return;
//        }

        timer=new Timer();
        
		btnStartStop.setText("STOP");
		btnStartStop.setEnabled(true);
		lblInfo.setText("Recording ...");
		lblInfo.setTextColor(Color.RED);
		initBlinkLabelTimer();
		
		status = Status.RECORDING;
	}
	private void stopRecording() {
		notify("Stopping mediaRecorder...", LogLevel.INFO, false);
		timer.cancel();
		
		// stop recording and release camera
        videoManager.stopRecording();  // stop the recording
                         
        // Grabar track gpx
        // FIXME Gestionar resultado del método
        Log.d(TAG, "Saving gpxFile...");
        saveGpxFile();
        
        // Geoetiquetar vídeo
        // FIXME Gestionar resultado del método
        //Log.d(TAG, "Geotaging videofile");
        //geotagVideoFile();		
	}

	private boolean saveGpxFile() {
		boolean result=false;
		// FIXME Hacerlo en segundo plano
        String filename=appDirectory.getPath()+"/";
        	//+VideoModel.getTimeStamp(startDate, true);
        String gpxFilename = filename +".gpx";
        String csvFilename = filename +".csv";
        try {
        	GpxFactory factory = GpxFactory.getFactory(Type.ExtendedGpxFactory);
        	GpxDocument doc = factory.createGpxDocument();
        	doc.addTrack(sensorTrack);
        	Util.write(gpxFilename, doc.asGpx());
        	Util.write(csvFilename, sensorTrack.asCsv(true));
        	result = true;
        } catch (Exception e) {
        	notify("Error can't save GpxDocument",LogLevel.ERROR,true);
        }
		return result;
	}
	private void saveAndResume() {
		notify("saveAndResume()",LogLevel.DEBUG,true);
		this.btnStartStop.setEnabled(false);
		status = Status.SAVING;
		stopRecording();

		// Empezar de nuevo
		startRecording();
		
	}

	// UpdateUI
	private void updateUI() {
		updateLabelPointsCount(this.sensorTrack.wayPointCount());
		if(status == Status.RECORDING) {
			updateLabelRecordingTime(new Date());
		}
		if(lastLocReceived != null) {
			updateLabelPosition(lastLocReceived);
		}
	}
	private void updateLabelPointsCount(int pointscount) {
		lblpointscount.setText(String.format("%d", pointscount));		
		
	}
	private void updateLabelRecordingTime(Date date) {
		long t = (long)((date.getTime() - startDate.getTime())/1000l);
		lblrecordtime.setText(String.format("%d sg.", t));
	}
	private void updateLabelPosition(Location loc) {
		String cad = String.format("lat=%7.3f  lon=%7.3f" , loc.getLatitude(),loc.getLongitude());
		lblposition.setText(cad);			
	}

	// Timers
	class MainTimerTask extends TimerTask {
		@Override
		public void run() {
			MainTimerTaskOnUIThread task = new MainTimerTaskOnUIThread();
			runOnUiThread(task);
			// Comprobar el límite de tiempo de grabación
			if(status == Status.RECORDING) {
				Date now = new Date();
				long recordingTime = now.getTime() - startDate.getTime();
				if(recordingTime>=MAX_RECORDING_TIME) {
					saveAndResume();
				}	
			}
		}
	}
	class MainTimerTaskOnUIThread implements Runnable {
		@Override
		public void run() {
			updateUI();
		}		
	}
	class BlinkLabelTimerTask extends TimerTask {
		@Override
		public void run() {
			BlinkLabelTimerTaskOnUIThread task = new BlinkLabelTimerTaskOnUIThread();
			runOnUiThread(task);
		}
	}
	class BlinkLabelTimerTaskOnUIThread implements Runnable {

		@Override
		public void run() {
			int visibility = lblInfo.getVisibility();
			if(visibility == View.VISIBLE) {
				lblInfo.setVisibility(View.INVISIBLE);
			} else {
				lblInfo.setVisibility(View.VISIBLE);
			}
		}		
	}
	
	// Base de datos
	/**
	 * Inicializar la base de datos
	 */
	protected SQLiteDatabase initDatabase() {
		Log.d(TAG,"initDatabase()");
		String dbpath = appDirectory.getPath()+"/"+DATABASE_NAME;
		//String dbpath=Environment.getExternalStorageDirectory()+"/"+APP_FILENAME+"/"+DATABASE_NAME;
		File dbfile = new File(dbpath);
		if(!dbfile.exists()) {
			Log.d(TAG,"initDatabase(): Create");
			db = createDatabase(dbpath);
		} else {
			Log.d(TAG,"initDatabase(): open");
			db = openDatabase(dbpath);
		}
		return db;
	}
	protected SQLiteDatabase createDatabase(String dbpath) {
		SQLiteDatabase db = null;
		try {
			db = this.openOrCreateDatabase(dbpath, MODE_PRIVATE, null);
			String query = "CREATE TABLE trkpt (id int primary key, "+
				"label text, time int, lon double, lat double, "+
				"alt double, vel double, rumbo double, acc double)";
			db.execSQL(query);
			return db;
		} catch (Exception e) {
			Log.d(TAG, "Can't create database");
			return null;
		}		
	}
	protected SQLiteDatabase openDatabase(String dbpath) {
		SQLiteDatabase db = this.openOrCreateDatabase(dbpath, MODE_PRIVATE, null);
		return db;
	}

	// Sensores
	float[] localGravity = new float[3];
	float[] geomagneticVector = new float[3];
	float[] localAcceleration = new float[3];
	float[] rotationMatrix = new float[9];
	float[] inclinationMatrix = new float[9];
	float[] orientation = new float[3];
	//float[] oldOreintation = new float[3];
	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	@Override
	public void onSensorChanged(SensorEvent event) {
		switch(event.sensor.getType()) {
		case Sensor.TYPE_ACCELEROMETER:
			System.arraycopy(event.values, 0, localGravity, 0, 3);
			break;
		case Sensor.TYPE_MAGNETIC_FIELD:
			System.arraycopy(event.values, 0, geomagneticVector, 0, 3);			
			break;
		case Sensor.TYPE_LINEAR_ACCELERATION:
			System.arraycopy(event.values, 0, localAcceleration, 0, 3);			
			break;
		case Sensor.TYPE_PRESSURE:
			pressure = event.values[0];
		}
		
        boolean success = SensorManager.getRotationMatrix(rotationMatrix, inclinationMatrix, 
        		localGravity, geomagneticVector);
        if(success) {
            SensorManager.getOrientation(rotationMatrix, orientation);
        }		
		
	}
	private float[] calculateGlobalAcceleration() {
		float[] rtrasp = traspose(rotationMatrix);
		float[] globalAcceleration = this.vectorByMatrixMultiplication(localAcceleration, rtrasp);
		return globalAcceleration;
	}
	/**
	 * Multiplica un vector de dimensión 3 por una matriz 3x3 y devuelve el vector resultado
	 * @param vector vector fila de tres dimensiones [v0 v1 v2]
	 * @param matrix matriz de 3x3
	 * @return vector fila de tres dimensiones [r0 r1 r2] resultado de multiplicar
	 * el vector original por la matriz
	 */
	private float[] vectorByMatrixMultiplication(float[] vector, float[] matrix) {
		//
		float[] result = new float[3];
		// Comprobar dimension del vector y matriz de entrada
		if (vector.length != 3 || matrix.length != 9) {
			return null;
		}
		// Calcular elproducto vectorxmatriz
		result[0] = vector[0]*matrix[0]+vector[1]*matrix[3]+vector[2]*matrix[6];
		result[1] = vector[0]*matrix[1]+vector[1]*matrix[4]+vector[2]*matrix[7];
		result[2] = vector[0]*matrix[2]+vector[1]*matrix[5]+vector[2]*matrix[8];
		
		// Devolver el resultado
		return result;
	}
	/**
	 * Traspone una matriz 3x3 recibida en forma de Array float  
	 * @param originalMatrix [a00 a01 a02 a10 a11 a12 a20 a21 a22]
	 * @return Trasposed matrix [a00 a10 a20 a01 a11 a21 a02 a12 a22]
	 */
	private float[] traspose(float[] originalMatrix) {
		float[] result = new float[9];
		
		// Comprobar dimensión matriz original
		if(originalMatrix.length!=9) {
			return null;
		}
		// Trasponer
		result[0]=originalMatrix[0];
		result[1]=originalMatrix[3];
		result[2]=originalMatrix[6];
		result[3]=originalMatrix[1];
		result[4]=originalMatrix[4];
		result[5]=originalMatrix[7];
		result[6]=originalMatrix[2];
		result[7]=originalMatrix[5];
		result[8]=originalMatrix[8];
		
		// Devolver el resultado
		return result;
	}

	// Geotagging
	private boolean geotagVideoFile() {
		// FIXME Hacer en segundo plano
		boolean result = false;
		WayPoint startp = track.getStartWayPoint();
		if(startp != null) {
			double longitude = startp.getLongitude();
			double latitude = startp.getLatitude();
			double altitude = startp.getAltitude();
			result = videoManager.geotagVideoFile(startDate, longitude, latitude, altitude);
		} else {
			notify("MainActivity().geotagVideoFile() ERROR: Can't geotag video",
					LogLevel.ERROR, false);
		}
		return result;
	}


}
