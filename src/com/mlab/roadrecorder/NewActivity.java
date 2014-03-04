package com.mlab.roadrecorder;

import java.io.File;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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

import com.mlab.roadrecorder.alvac.R;
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

public class NewActivity extends Activity {
	private final static Logger LOG = Logger.getLogger(NewActivity.class);
	
	SoundPool soundPool; 
	int sound; 
	
	public enum GPSICON {
		DISABLED, FIXING, FIXED
	};
	public enum BTNBACKGROUND {
		DISABLED, STOPPED, RECORDING
	}

	public static final String TAG = "ROADRECORDER";

	public enum NotificationLevel {
		INFO, DEBUG, WARNING, ERROR
	};

	//
	MainController controller;

	// States
	ActivityState gpsState;
	ActivityState btnState;

	// Layout
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

	// Live cycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		configureLogger();
		LOG.info("\n-----------------------");
		LOG.info("MainActivity.onCreate()");
		LOG.info("\n-----------------------");
		super.onCreate(savedInstanceState);


		preInitLayout();

		controller = new MainController(this);

		postInitLayout();

	}

	

	@Override
	protected void onStart() {
		LOG.info("MainActivity.onStart()");
		controller.onRestart();
//		if (getVideoController() != null) {
//			boolean result = getVideoController().initMediaRecorder();
//			if (!result) {
//				this.showNotification("Can't init MediaRecorder",
//						NotificationLevel.ERROR, true);
//				super.onStart();
//				finish();
//				return;
//			}
//		}
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
		if(gpsIconBlinker != null) {
			this.stopGpsIconBlinker();
		}
		soundPool.release();
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
		super.onDestroy();
	}

	// Private methods
	/**
	 * Configura el Logger de android-logging-log4j
	 */
	private void configureLogger() {
		final LogConfigurator logConfigurator = new LogConfigurator();

		String filename = Environment.getExternalStorageDirectory()
				+ File.separator + "myapp.log";
		// System.out.println(filename);
		logConfigurator.setFileName(filename);

		logConfigurator.setRootLevel(org.apache.log4j.Level.ALL);
		// Set log level of a specific logger
		logConfigurator.setLevel("com.mlab.roadrecorder",
				org.apache.log4j.Level.ALL);
		logConfigurator.setUseLogCatAppender(true);
		logConfigurator.configure();

	}
	private void preInitLayout() {
		LOG.debug("MainActivity.preInitLayout()");
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
		LOG.debug("MainActivity.postInitLayout()");

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
					showNotification(
							"Stopping media recorder and saving files",
							NotificationLevel.INFO, true);
					controller.stopRecording();
					btnStartStop.setBackgroundResource(R.drawable.button_start);
					return;
				} else {
					showNotification("Starting recording",
							NotificationLevel.INFO, false);
					controller.startRecording();
					btnStartStop.setBackgroundResource(R.drawable.button_stop);
				}
			}
		});
		//btnStartStop.setEnabled(false);
		//btnStartStop.setBackgroundResource(R.drawable.button_orange);
	}

	// States
	public void setGpsState(ActivityState state) {
		LOG.debug("MainActivity.setGpsState()");
		this.gpsState = state;
		this.gpsState.doAction();
	}

	public void setButtonState(ButtonState state) {
		LOG.debug("MainActivity.setButtonState()");
		this.btnState = state;
		this.btnState.doAction();
	}
	// Button StartStop
	public void setButtonEnabled(boolean enabled) {
		LOG.debug("setButtonEnabled()"+String.format("%b", enabled));
		if (enabled) {
			btnStartStop.setEnabled(true);
		} else {
			btnStartStop.setEnabled(false);
		}
	}
	public void setButtonBackground(BTNBACKGROUND back) {
		if(back == BTNBACKGROUND.DISABLED) {
			LOG.debug("setButtonBackground() orange");
			btnStartStop.setBackgroundResource(R.drawable.button_orange);
		} else if(back == BTNBACKGROUND.STOPPED) {
			LOG.debug("setButtonBackground() start");
			btnStartStop.setBackgroundResource(R.drawable.button_start);
		} else if(back == BTNBACKGROUND.RECORDING) {
			LOG.debug("setButtonBackground() stop");
			btnStartStop.setBackgroundResource(R.drawable.button_stop);
		}
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
		switch (item.getItemId()) {
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
		this.showNotification("Opción en desarrollo", NotificationLevel.INFO,
				true);
		// Intent i = new Intent(this, ConfigActivity.class);
		// startActivity(i);
	}

	private void startActivityHelp() {
		this.showNotification("Opción en desarrollo", NotificationLevel.INFO,
				true);
		// Intent i = new Intent(this, HelpActivity.class);
		// startActivity(i);
	}

	private void startActivityAbout() {
		this.showNotification("Opción en desarrollo", NotificationLevel.INFO,
				true);
		// Intent i = new Intent(this, AboutActivity.class);
		// startActivity(i);
	}


	// LabelInfo
	class LabelInfoBlinker extends AsyncTask<Void, Void, Void> {
		boolean blink;

		public LabelInfoBlinker() {
			LOG.info("LabelInfoBlinker()");
			this.blink = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			LOG.info("LabelInfoBlinker().doInBackground()");
			while(this.blink) {
				try {
					Thread.sleep(500);
					publishProgress();
				} catch (Exception e) {
					LOG.warn("Exception in LabelInfoBlinker: " + e.getMessage());
				}
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			LOG.info("LabelInfoBlinker.onProgressUpdate()");
			if (lblInfo.getVisibility() == View.VISIBLE) {
				LOG.info("LabelInfoBlinker.onProgressUpdate() INVISIBLE");
				lblInfo.setVisibility(View.INVISIBLE);
			} else {
				LOG.info("LabelInfoBlinker.onProgressUpdate() VISIBLE");
				lblInfo.setVisibility(View.VISIBLE);
			}
		}

		public void setBlink(boolean blink) {
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
		LOG.debug("MainActivity.startLabelInfoBlinker()");
		if (labelInfoBlinker != null) {
			this.stopLabelInfoBlinker("");
		}
		this.lblInfo.setText(message);
		labelInfoBlinker = new LabelInfoBlinker();
		labelInfoBlinker.execute();
	}
	public void stopLabelInfoBlinker(String message) {
		LOG.debug("MainActivity.stopLabelInfoBlinker()");
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

	public void setGpsIcon(NewActivity.GPSICON icon, String label) {
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
			LOG.info("GpsIconBlinker()");
			this.blink = true;
		}

		@Override
		protected Void doInBackground(Void... params) {
			LOG.info("GpsIconBlinker.doInBackground()");
			while(blink) {
				try {
					Thread.sleep(500);
				} catch (Exception e) {
					LOG.warn("Error in GpsIconBlinker");
				}
				this.publishProgress();
			}
			return null;
		}

		@Override
		protected void onProgressUpdate(Void... values) {
			//LOG.debug("GpsIconBlinker.onProgressUpddate()");
			if (gpsIcon.getVisibility() == View.VISIBLE) {
				//LOG.debug("GpsIconBlinker.onProgressUpddate(): VISIBLE");				
				gpsIcon.setVisibility(View.INVISIBLE);
			} else {
				//LOG.debug("GpsIconBlinker.onProgressUpddate(): INVISIBLE");
				gpsIcon.setVisibility(View.VISIBLE);
			}
		}

		public void setBlink(boolean blink) {
			this.blink = blink;
		}
	}

	public void startGpsIconBlinker() {
		LOG.info("startGpsIconBlinker");
		if (gpsIconBlinker != null) {
			this.stopGpsIconBlinker();
		}
		gpsIconBlinker = new GpsIconBlinker();
		gpsIconBlinker.execute();
	}

	public void stopGpsIconBlinker() {
		LOG.info("stopGpsIconBlinker");
		if (gpsIconBlinker != null) {
			gpsIconBlinker.setBlink(false);
		}
		this.gpsIcon.setVisibility(View.VISIBLE);
	}
}
