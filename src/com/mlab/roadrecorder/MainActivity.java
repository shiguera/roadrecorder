package com.mlab.roadrecorder;

import java.io.File;
import java.util.Locale;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import android.os.Environment;
import android.util.Log;
import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.media.CamcorderProfile;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.speech.tts.TextToSpeech;
import android.support.v4.app.FragmentActivity;
import android.support.v4.content.ContextCompat;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.mlab.roadrecorder.helpabout.AboutActivity;
import com.mlab.roadrecorder.helpabout.HelpActivity;
import com.mlab.roadrecorder.settings.SettingsActivity;
import com.mlab.roadrecorder.state.ActivityState;
import com.mlab.roadrecorder.state.ButtonState;
import com.mlab.roadrecorder.view.TextViewUpdater;
import com.mlab.roadrecorder.view.command.GetAccuracyCommand;
import com.mlab.roadrecorder.view.command.GetBearingCommand;
import com.mlab.roadrecorder.view.command.GetDistanceCommand;
import com.mlab.roadrecorder.view.command.GetLatCommand;
import com.mlab.roadrecorder.view.command.GetLonCommand;
import com.mlab.roadrecorder.view.command.GetPointsCountCommand;
import com.mlab.roadrecorder.view.command.GetRecordingTimeCommand;
import com.mlab.roadrecorder.view.command.GetSpeedCommand;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class MainActivity extends FragmentActivity implements TextToSpeech.OnInitListener {
    final String LOGTAG = "ROADRECORDER";
	private final static Logger LOG = Logger.getLogger(MainActivity.class);

	private enum RunModes {
		Test, Debug, Production
	};
	/**
	 * Interviene en la configuración del Logger
	 */
	private final RunModes RUNMODE = RunModes.Test;

	public enum GPSICON {
		DISABLED, FIXING, FIXED
	};

	public enum BTNBACKGROUND {
		DISABLED, STOPPED, RECORDING
	}

	public enum NotificationLevel {
		INFO, DEBUG, WARNING, ERROR
	};

	SoundPool soundPool;
	int sound;

	private TextToSpeech textToSpeech;
	private boolean isTextToSpeechEnabled = false;
	
	// Controller
	MainController controller;

	// States
	ActivityState gpsState;
	ActivityState btnState;

	// Layout
	protected Menu menu;
	protected MenuItem menuItemBack, menuItemConfig, menuItemHelp,
			menuItemAbout;
	protected Button btnStartStop;
	protected FrameLayout videoFrame;
	protected LinearLayout rightPanel;
	TextView lblLon, lblLat, lblAcc, lblSpeed, lblBearing, lblTime, lblPts,
			lblDistance;
	TextViewUpdater latUpdater, lonUpdater, accUpdater, speedUpdater,
			bearingUpdater, timeUpdater, ptsUpdater, distanceUpdater;
	protected ImageView gpsIcon;
	protected TextView gpsIconLabel;
	protected GpsIconBlinker gpsIconBlinker;
	protected TextView lblInfo;
	protected LabelInfoBlinker labelInfoBlinker;

	public ExecutorService executor;
	// Live cycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		textToSpeech = new TextToSpeech(this, this);
		
		executor = Executors.newFixedThreadPool(5);

		if (!initApplicationDirectory()) {
			exit("ERROR: Can't open application directory");
			return;
		}
		
		configureLogger();
		LOG.info("-----------------------");
		LOG.info("MainActivity.onCreate()");
		LOG.info("-----------------------");

		loadPreferences();

		
		preInitLayout();

		controller = new MainController(this);

		postInitLayout();
		
		//checkVideoResolutions();
	}

	@Override
	protected void onStart() {
		LOG.info("MainActivity.onStart()");
		if (controller != null) {
			controller.onRestart();
		}

		super.onStart();
	}

	@Override
	protected void onRestart() {
		LOG.info("MainActivity.onRestart()");
		super.onRestart();
	}

	@Override
	protected void onPause() {
		LOG.info("MainActivity.onPause()");

		if (controller != null) {
			controller.onPause();
		}
		if (videoFrame != null) {
			videoFrame.removeAllViews();
		}
		if (labelInfoBlinker != null) {
			this.stopLabelInfoBlinker("");
		}
		if (gpsIconBlinker != null) {
			this.stopGpsIconBlinker();
		}
		if (soundPool != null) {
			soundPool.release();
		}
		super.onPause();
	}

	@Override
	protected void onStop() {
		LOG.info("MainActivity.onStop()");
		super.onStop();
	}

	@Override
	protected void onDestroy() {
		LOG.info("MainActivity.onDestroy()");
		if(textToSpeech != null) {
			textToSpeech.stop();
			textToSpeech.shutdown();
		}
		super.onDestroy();
	}

	// Private methods
	private void checkVideoResolutions() {
		LOG.debug("checkVideoResolutions()");
		LOG.debug("has 1080P" + CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_1080P));
		LOG.debug("has 720P" + CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_720P));
		LOG.debug("has 480P" + CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_480P));
		LOG.debug("has HIGH" + CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_HIGH));
		LOG.debug("has LOW" + CamcorderProfile.hasProfile(CamcorderProfile.QUALITY_LOW));
	}
	
	private void loadPreferences() {
		//LOG.debug("MainActivity.loadPreferences()");

		PreferenceManager.setDefaultValues(this, R.xml.prefs, false);
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		
		String videores = prefs.getString("videoresolution", App.getVideoResolution());
		App.setVideoResolution(videores);
		
		boolean useextendedsdcard = prefs.getBoolean("useextendedsdcard", App.isUseExtendedSdcard());
		App.setUseExtendedSdcard(useextendedsdcard);

		boolean saveascsv = prefs.getBoolean("saveascsv", App.isSaveAsCsv());
		App.setSaveAsCsv(saveascsv);
		
		boolean voiceMessages = prefs.getBoolean("voicemessages", App.isUseVoiceSyntetizer());
		App.setUseVoiceSyntetizer(voiceMessages);

		// Se guarda como una cadena de texto
		long mindiskspace = parseMinDiskSpace(prefs);
		App.setMinDiskSpaceToSave(mindiskspace);
		
	}
	private long parseMinDiskSpace(SharedPreferences prefs) {
		String diskspaceCad = prefs.getString("mindiskspace", "");
		long result = App.getMinDiskSpaceToSave();
		try {
			result = Long.parseLong(diskspaceCad);
		} catch (Exception e) {
			LOG.error("parseMinDiskSpace() ERROR: Can't parse mindikspace");
		}
		return result;
	}

	/**
	 * Configura el Logger de android-logging-log4j
	 */
	private void configureLogger() {
		File logfile = new File(App.getApplicationDirectory(),
				App.getLogfileName());
		final LogConfigurator logConfigurator = new LogConfigurator();
		logConfigurator.setMaxBackupSize(1);
		logConfigurator.setMaxFileSize(500 * 1024);
		logConfigurator.setFileName(logfile.getPath());
		logConfigurator.setFilePattern("%d - %t - %p [%c{1}]: %m%n");

		if (RUNMODE == RunModes.Production) {
			logConfigurator.setRootLevel(org.apache.log4j.Level.INFO);
			logConfigurator.setLevel("com.mlab.roadrecorder",
					org.apache.log4j.Level.INFO);
			logConfigurator.setUseLogCatAppender(false);
		} else {
			logConfigurator.setRootLevel(org.apache.log4j.Level.ALL);
			logConfigurator.setLevel("com.mlab.roadrecorder",
					org.apache.log4j.Level.ALL);
			logConfigurator.setUseLogCatAppender(true);
		}

		logConfigurator.configure();
	}


	/**
	 * Inicializa el directorio utilizado por la aplicación.<br/>
	 * Si existe una secondary sdcard la selecciona y si no selecciona la sdcard
	 * normal.<br/>
	 * Crea o utiliza un directorio llamado App.getAppDirectoryName().
	 * 
	 * NOTA: (Nov, 11 2014 : Cancel extended card writing, now android kitkat 
	 * doesn't allow writing on it.
	 * 
	 * @return True si todo va bien, flse si no es posible asignar un directorio
	 *         a la aplicación
	 */
	private boolean initApplicationDirectory() {
		//doTests();
		//System.out.println("isExternalStorageEnabled(): " + AndroidUtils.isExternalStorageEnabled());
		
		
//		if(App.isUseExtendedSdcard()) {
//			boolean result = checkExtendedSdcard();
//			if (result) {
//				return result;
//			} else {
//				System.out.println("ERROR, no se pudo acceder a la memoria extendida del dispositivo");
//				speak("ERROR, no se pudo acceder a la memoria extendida del dispositivo");
//			}
//		}
//
//		App.setUseExtendedSdcard(false);
		
		//File outdir = new File(AndroidUtils.getExternalStorageDirectory(), App.getAppDirectoryName());
		File outdir = getMoviesStorageDir(App.getAppDirectoryName());

		boolean result = setApplicationDirectory(outdir);
		if (result) {
			LOG.info("App directory: " + outdir.getPath());
			Log.d(LOGTAG, "MainActivity.initApplicationDirectory():: App directory= " + outdir.toString());
		} else {
			System.out.println("ERROR: Can't access to application directory");
			LOG.error("ERROR: Can't access to application directory");
			Log.e(LOGTAG, "ERROR: Can't access to application directory");
		}		
		return result;
	}
    public static File getMoviesStorageDir(String folderName) {
        // Get the directory for the user's public pictures directory.
        File file = new File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES), folderName);
        if (file.exists()==false && !file.mkdirs()) {
            LOG.error("getMoviesStorageDir(): Directory not created");
            System.out.println("getMoviesStorageDir(): Directory " + file.getPath() + " not created");
            return null;
        }
        return file;
    }

	private boolean setApplicationDirectory(File outdir) {
		if(outdir == null) {
			System.out.println("setApplicationDirectory() outdir=NULL" );
			return false;
		}
		System.out.println("setApplicationDirectory() " + outdir.getPath());
		if (!outdir.exists()) {
			System.out.println("setApplicationDirectory() outdir doesn't exist");
			if (!outdir.mkdir()) {
				System.out.println("setApplicationDirectory() can't create directory");
				return false;
			}
		}
		System.out.println("setApplicationDirectory() outdir=" + outdir.getPath());					
		App.setApplicationDirectory(outdir);
		return true;
	}

	private void preInitLayout() {
		//LOG.debug("MainActivity.preInitLayout()");
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
		setContentView(R.layout.activity_main);
		videoFrame = (FrameLayout) this.findViewById(R.id.videoview);
		rightPanel = (LinearLayout) this.findViewById(R.id.rightPanel);
		lblInfo = (TextView) this.findViewById(R.id.lblInfo);
		gpsIcon = (ImageView) this.findViewById(R.id.gps_icon);
		gpsIconLabel = (TextView) this.findViewById(R.id.gps_icon_label);
		btnStartStop = (Button) findViewById(R.id.btn_rec);

		soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
		sound = soundPool.load(this, R.raw.btnclick, 1);

	}

	private void postInitLayout() {
		//LOG.debug("MainActivity.postInitLayout()");

		configureBtnStartStop();

		configureLabels();

	}

	private void configureLabels() {

		// lonlat_panel
		lblLat = (TextView) this.findViewById(R.id.lbl_lat);
		latUpdater = new TextViewUpdater(lblLat, new GetLatCommand(
				controller.getGpsModel()));

		lblLon = (TextView) this.findViewById(R.id.lbl_lon);
		lonUpdater = new TextViewUpdater(lblLon, new GetLonCommand(
				controller.getGpsModel()));

		lblAcc = (TextView) this.findViewById(R.id.lbl_acc);
		accUpdater = new TextViewUpdater(lblAcc, new GetAccuracyCommand(
				controller.getGpsModel()));

		// speed_panel
		lblSpeed = (TextView) this.findViewById(R.id.lbl_speed);
		speedUpdater = new TextViewUpdater(lblSpeed, new GetSpeedCommand(
				controller.getGpsModel()));

		lblBearing = (TextView) this.findViewById(R.id.lbl_bearing);
		bearingUpdater = new TextViewUpdater(lblBearing, new GetBearingCommand(
				controller.getGpsModel()));

		// status_panel
		lblTime = (TextView) this.findViewById(R.id.lbl_time);
		timeUpdater = new TextViewUpdater(lblTime, new GetRecordingTimeCommand(
				controller.getVideoController().getModel()));

		lblPts = (TextView) this.findViewById(R.id.lbl_pts);
		ptsUpdater = new TextViewUpdater(lblPts, new GetPointsCountCommand(
				controller.getGpsModel()));

		lblDistance = (TextView) this.findViewById(R.id.lbl_dto);
		distanceUpdater = new TextViewUpdater(lblDistance,
				new GetDistanceCommand(controller.getGpsModel()));

	}

	private void configureBtnStartStop() {
		btnStartStop.setOnClickListener(new Button.OnClickListener() {
			@Override
			public void onClick(View v) {
				soundPool.play(sound, 1.0f, 1.0f, 0, 0, 1.0f);
				if (controller.isRecording()) {
					stopRecording();
				} else {
					startRecording();
				}
			}
		});
	}

	private void startRecording() {
		showNotification("Starting media recorder", NotificationLevel.INFO,
				false);
		controller.startRecording();
	}

	private void stopRecording() {
		showNotification("Stopping media recorder and saving files",
				NotificationLevel.INFO, true);
		controller.stopRecording();
		return;
	}

	// States
	public void setGpsState(ActivityState state) {
		//LOG.debug("MainActivity.setGpsState()");
		this.gpsState = state;
		this.gpsState.doAction();
	}

	public void setButtonState(ButtonState state) {
		// LOG.debug("MainActivity.setButtonState()");
		this.btnState = state;
		this.btnState.doAction();
	}

	// Button StartStop
	public void setButtonEnabled(boolean enabled) {
		// LOG.debug("setButtonEnabled()"+String.format("%b", enabled));
		if (enabled) {
			btnStartStop.setEnabled(true);
		} else {
			btnStartStop.setEnabled(false);
		}
	}

	public void setButtonBackground(BTNBACKGROUND back) {
		if (back == BTNBACKGROUND.DISABLED) {
			// LOG.debug("setButtonBackground() orange");
			btnStartStop.setBackgroundResource(R.drawable.button_orange);
		} else if (back == BTNBACKGROUND.STOPPED) {
			// LOG.debug("setButtonBackground() start");
			btnStartStop.setBackgroundResource(R.drawable.button_start);
		} else if (back == BTNBACKGROUND.RECORDING) {
			// LOG.debug("setButtonBackground() stop");
			btnStartStop.setBackgroundResource(R.drawable.button_stop);
		}
	}

	// Menu
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		this.menu = menu;
		menuItemBack = menu.findItem(R.id.menuitem_back);
		menuItemConfig = menu.findItem(R.id.menuitem_settings);
		menuItemAbout = menu.findItem(R.id.menuitem_about);
		menuItemHelp = menu.findItem(R.id.menuitem_help);
		return true;
	}
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case R.id.menuitem_settings:
			startActivitySettings();
			initApplicationDirectory();
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
	public void setActionBarEnabled(boolean enabled) {
		this.menuItemAbout.setEnabled(enabled);
		this.menuItemBack.setEnabled(enabled);
		this.menuItemConfig.setEnabled(enabled);
		this.menuItemHelp.setEnabled(enabled);
	}

	private void startActivitySettings() {
		// this.showNotification("Opción en desarrollo", NotificationLevel.INFO,
		// true);
		Intent i = new Intent(this, SettingsActivity.class);
		startActivity(i);
	}
	private void startActivityHelp() {
		// this.showNotification("Opción en desarrollo", NotificationLevel.INFO,
		// true);
		Intent i = new Intent(this, HelpActivity.class);
		startActivity(i);
	}
	private void startActivityAbout() {
		// this.showNotification("Opción en desarrollo", NotificationLevel.INFO,
		// true);
		Intent i = new Intent(this, AboutActivity.class);
		startActivity(i);
	}

	// LabelInfo
	class LabelInfoBlinker extends AsyncTask<Void, Void, Void> {
		boolean blink;

		public LabelInfoBlinker() {
			//LOG.info("LabelInfoBlinker()");
			setBlink(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			//LOG.info("LabelInfoBlinker().doInBackground()");
			while (blink) {
				try {
					Thread.sleep(1000);
					publishProgress();
				} catch (Exception e) {
					LOG.warn("Exception in LabelInfoBlinker: " + e.getMessage());
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// LOG.info("LabelInfoBlinker.onProgressUpdate()");
			if (lblInfo.getVisibility() == View.VISIBLE) {
				// LOG.info("LabelInfoBlinker.onProgressUpdate() INVISIBLE");
				lblInfo.setVisibility(View.INVISIBLE);
			} else {
				// LOG.info("LabelInfoBlinker.onProgressUpdate() VISIBLE");
				lblInfo.setVisibility(View.VISIBLE);
			}
		}

		synchronized public void setBlink(boolean blink) {
			//LOG.info("LabelInfoBlinker.setBlink() : "
			//		+ Boolean.valueOf(blink).toString());
			this.blink = blink;
		}
	}
	public void setLabelInfoText(String text) {
		this.lblInfo.setText(text);
	}
	public void setLabelInfoColor(int color) {
		this.lblInfo.setTextColor(color);
	}
	public void startLabelInfoBlinker(String message) {
		//LOG.debug("MainActivity.startLabelInfoBlinker()");
		// if (labelInfoBlinker != null) {
		// this.stopLabelInfoBlinker("");
		// }
		this.lblInfo.setText(message);
		labelInfoBlinker = new LabelInfoBlinker();
		labelInfoBlinker.executeOnExecutor(executor);
	}
	public void stopLabelInfoBlinker(String message) {
		//LOG.debug("MainActivity.stopLabelInfoBlinker()");
		this.lblInfo.setText(message);
		if (labelInfoBlinker != null) {
			labelInfoBlinker.setBlink(false);
		}
		this.lblInfo.setVisibility(View.VISIBLE);
	}

	// Utilities
	public void showNotification(String message, NotificationLevel level,
			boolean withToast) {
		switch (level) {
		case INFO:
			// Log.i(TAG, message);
			break;
		case DEBUG:
			// Log.d(TAG, message);
			break;
		case WARNING:
			// Log.w(TAG, message);
			break;
		case ERROR:
			// Log.e(TAG, message);
			break;
		}
		if (withToast) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
	public void showDialogNotification(String message, NotificationLevel level) {
		switch (level) {
		case INFO:
			// Log.i(TAG, message);
			break;
		case DEBUG:
			// Log.d(TAG, message);
			break;
		case WARNING:
			// Log.w(TAG, message);
			break;
		case ERROR:
			// Log.e(TAG, message);
			break;
		}

	}

	private void exit(final String message) {
		AlertDialog.Builder builder = new AlertDialog.Builder(this);
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LOG.info("exit(): " + message);
				finish();
				return;
			}
		});
		AlertDialog dialog = builder.create();
		dialog.show();
	}

	// Getters
	public MainController getController() {
		return controller;
	}
	public Button getBtnStartStop() {
		return btnStartStop;
	}
	public TextView getLblInfo() {
		return lblInfo;
	}
	public FrameLayout getVideoFrame() {
		return videoFrame;
	}
	public LinearLayout getRightPanel() {
		return rightPanel;
	}
	public ImageView getGpsIcon() {
		return gpsIcon;
	}
	public void setGpsIcon(MainActivity.GPSICON icon, String label) {
		gpsIconLabel.setText(label);
		switch (icon) {
		case DISABLED:
			gpsIcon.setBackgroundResource(R.drawable.mylocation_1);
			break;
		case FIXING:
			gpsIcon.setBackgroundResource(R.drawable.mylocation_2);
			break;
		case FIXED:
			gpsIcon.setBackgroundResource(R.drawable.mylocation_3);
			break;
		}
	}

	class GpsIconBlinker extends AsyncTask<Void, Void, Void> {
		private boolean blink;

		public GpsIconBlinker() {
			//LOG.info("GpsIconBlinker()");
			setBlink(true);
		}

		@Override
		protected Void doInBackground(Void... params) {
			//LOG.info("GpsIconBlinker.doInBackground()");
			while (blink) {
				try {
					Thread.sleep(1000);
				} catch (Exception e) {
					LOG.warn("Error in GpsIconBlinker");
				}
				publishProgress();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			// LOG.debug("GpsIconBlinker.onProgressUpddate()");
			if (gpsIcon.getVisibility() == View.VISIBLE) {
				// LOG.debug("GpsIconBlinker.onProgressUpddate(): VISIBLE");
				gpsIcon.setVisibility(View.INVISIBLE);
			} else {
				// LOG.debug("GpsIconBlinker.onProgressUpddate(): INVISIBLE");
				gpsIcon.setVisibility(View.VISIBLE);
			}
		}

		synchronized public void setBlink(boolean blink) {
			this.blink = blink;
		}
	}

	public void startGpsIconBlinker() {
		//LOG.info("startGpsIconBlinker");
		// if (gpsIconBlinker != null) {
		// this.stopGpsIconBlinker();
		// }
		gpsIconBlinker = new GpsIconBlinker();
		gpsIconBlinker.executeOnExecutor(executor);
	}
	public void stopGpsIconBlinker() {
		//LOG.info("stopGpsIconBlinker");
		if (gpsIconBlinker != null) {
			gpsIconBlinker.setBlink(false);
		}
		this.gpsIcon.setVisibility(View.VISIBLE);
	}

	public void speak(String text) {
		if(App.isUseVoiceSyntetizer() && textToSpeech != null && isTextToSpeechEnabled) {
			textToSpeech.speak(text, TextToSpeech.QUEUE_FLUSH, null);
		}
	}
	// Interface TextToSpeech.OnInitListener
	@Override
	public void onInit(int status) {
		if(status != TextToSpeech.SUCCESS) {
			LOG.error("MainActivity.onInit() ERROR: TextToSpeech init status isn't success");
			return;
		}
		int result = textToSpeech.setLanguage(Locale.getDefault());
		if(result == TextToSpeech.LANG_MISSING_DATA || 
				result == TextToSpeech.LANG_NOT_SUPPORTED) {
			LOG.error("MainActivity.onInit() ERROR: Can't activate language");
			return;
		}
		isTextToSpeechEnabled = true;
		
	}

}
