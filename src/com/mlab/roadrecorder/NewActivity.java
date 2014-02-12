package com.mlab.roadrecorder;

import java.io.File;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
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
import com.mlab.roadrecorder.gps.GpsModel;
import com.mlab.roadrecorder.video.VideoController;
import com.mlab.roadrecorder.video.VideoModel;
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


public class NewActivity extends Activity  {
	private final Logger LOG = Logger.getLogger(NewActivity.class);
	
	public static final String TAG = "ROADRECORDER";
    public enum NotificationLevel {INFO,DEBUG,WARNING,ERROR};

    // 
    MainModel model;
    MainController controller;
    

    // Layout
	protected Button btnStartStop;
	protected TextView lblInfo, lblposition;
	protected FrameLayout videoFrame;
	protected LinearLayout rightPanel;
	TextView lblLon, lblLat, lblAcc, lblSpeed, lblBearing, lblTime, lblPts, lblDistance;
	TextViewUpdater latUpdater, lonUpdater, accUpdater, speedUpdater, bearingUpdater, timeUpdater, ptsUpdater, distanceUpdater;
	LabelInfoBlinker labelInfoBlinker;
	// Live cycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		configureLogger();
		LOG.info("\n-----------------------");
		LOG.info("MainActivity.onCreate()");
		LOG.info("\n-----------------------");
		
		super.onCreate(savedInstanceState);
		
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);		
		videoFrame = (FrameLayout)this.findViewById(R.id.videoview);
        rightPanel = (LinearLayout)this.findViewById(R.id.rightPanel);
        lblInfo = (TextView)this.findViewById(R.id.lblInfo);

		model = new MainModel(getApplicationContext());
		App.setMainModel(model);
		
		controller = new MainController(model, this, videoFrame);
		App.setMainController(controller);

		initLayout();
		

	}
	
	@Override
	protected void onStart() {
		LOG.info("MainActivity.onStart()");
		if(getVideoController() != null) {
			boolean result = getVideoController().initMediaRecorder();
			if(!result) {
				this.showNotification("Can't init MediaRecorder", NotificationLevel.ERROR, true);
				super.onStart();
				finish();
				return;
			}
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
		if(videoFrame != null) {
			videoFrame.removeAllViews();
		}
		if(controller != null) {
			controller.stopRecording();
			if (controller.getVideoController()!=null) {
				controller.getVideoController().release();
			}
		}
		
		if(labelInfoBlinker != null) {
			labelInfoBlinker.setBlink(false);
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
		super.onDestroy();
	}

	// Private methods
	/**
	 * Configura el Logger de android-logging-log4j
	 */
	private void configureLogger() {
		final LogConfigurator logConfigurator = new LogConfigurator();
         
		String filename = Environment.getExternalStorageDirectory() + 
				File.separator + "myapp.log";
		//System.out.println(filename);
        logConfigurator.setFileName(filename);
        		
        logConfigurator.setRootLevel(org.apache.log4j.Level.ALL);
        // Set log level of a specific logger
        logConfigurator.setLevel("com.mlab.roadrecorder", org.apache.log4j.Level.ALL);
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.configure(); 
        
	}
	private void initLayout() {
		
		configureBtnStartStop();
        
        
        configureLabels();

	}
	private void configureLabels() {
        
        // lonlat_panel
        lblLat = (TextView)this.findViewById(R.id.lbl_lat);
        latUpdater = new TextViewUpdater(lblLat, new GetLatCommand(model));

        lblLon = (TextView)this.findViewById(R.id.lbl_lon);
        lonUpdater = new TextViewUpdater(lblLon, new GetLonCommand(model));
        
        lblAcc = (TextView)this.findViewById(R.id.lbl_acc);
        accUpdater = new TextViewUpdater(lblAcc, new GetAccuracyCommand(model));

        // speed_panel
        lblSpeed = (TextView)this.findViewById(R.id.lbl_speed);
        speedUpdater = new TextViewUpdater(lblSpeed, new GetSpeedCommand(model));

        lblBearing = (TextView)this.findViewById(R.id.lbl_bearing);
        bearingUpdater = new TextViewUpdater(lblBearing, new GetBearingCommand(model));

        // status_panel        
        lblTime = (TextView)this.findViewById(R.id.lbl_time);
        timeUpdater = new TextViewUpdater(lblTime, new GetRecordingTimeCommand(model));

        lblPts = (TextView)this.findViewById(R.id.lbl_pts);
        ptsUpdater = new TextViewUpdater(lblPts, new GetPointsCountCommand(model));

        lblDistance = (TextView)this.findViewById(R.id.lbl_dto);
        distanceUpdater = new TextViewUpdater(lblDistance, new GetDistanceCommand(model));

        
        
        
	}
	private void configureBtnStartStop() {
		btnStartStop = (Button)findViewById(R.id.btn_rec);
        btnStartStop.setOnClickListener(new Button.OnClickListener(){
    		@Override
    		public void onClick(View v) {
    			if(getVideoModel().isRecording()) {
    				showNotification("Stopping media recorder and saving files",
    					NotificationLevel.INFO, true);
    				btnStartStop.setBackgroundResource(R.drawable.button_start);
    				controller.stopRecording();
                    return;
    			} else {
    				showNotification("Starting recording",NotificationLevel.INFO, false);
    				btnStartStop.setBackgroundResource(R.drawable.button_stop);
    				controller.startRecording();
    			}
    		}});
        btnStartStop.setEnabled(true);
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
		this.showNotification("Opción en desarrollo", NotificationLevel.INFO, true);
//		Intent i = new Intent(this, ConfigActivity.class);
//		startActivity(i);
	}
	private void startActivityHelp() {
		this.showNotification("Opción en desarrollo", NotificationLevel.INFO, true);
//		Intent i = new Intent(this, HelpActivity.class);
//		startActivity(i);	
	}
	private void startActivityAbout() {
		this.showNotification("Opción en desarrollo", NotificationLevel.INFO, true);
//		Intent i = new Intent(this, AboutActivity.class);
//		startActivity(i);
	}

	// LabelInfo
	class LabelInfoBlinker extends AsyncTask<Void, Void, Void> {
		boolean blink;
		public LabelInfoBlinker() {
			this.blink = true;
		}
		@Override
		protected Void doInBackground(Void... params) {
			while(blink) {
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
			if(lblInfo.getVisibility() == View.VISIBLE) {
				lblInfo.setVisibility(View.INVISIBLE);
			} else {
				lblInfo.setVisibility(View.VISIBLE);
			}
			super.onProgressUpdate(values);
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
	public void startLabelInfoBlinker() {
		if(labelInfoBlinker != null) {
			this.stopLabelInfoBlinker();
		}
		labelInfoBlinker = new LabelInfoBlinker();
		labelInfoBlinker.execute();
	}
	public void stopLabelInfoBlinker() {
		if(labelInfoBlinker != null) {
			labelInfoBlinker.setBlink(false);
		}
	}
	// Utilities
	public void showNotification(String message, NotificationLevel level, boolean withToast) {
		switch(level) {
		case INFO:
			//Log.i(TAG, message);
			break;
		case DEBUG:
			//Log.d(TAG, message);			
			break;
		case WARNING:
			//Log.w(TAG, message);						
			break;
		case ERROR:
			//Log.e(TAG, message);						
			break;
		}
		if(withToast) {
			Toast.makeText(this, message, Toast.LENGTH_LONG).show();
		}
	}
	
	// Getters
	public MainModel getModel() {
		return model;
	}
	public MainController getController() {
		return controller;
	}
	public VideoModel getVideoModel() {
		return model.getVideoModel();
	}
	public VideoController getVideoController() {
		if(controller != null) {
			return controller.getVideoController();			
		} else {
			return null;
		}
	}
	public GpsModel getGpsModel() {
		if(model != null) {
			return model.getGpsModel();			
		} else {
			return null;
		}
	}
	public Button getBtnStartStop() {
		return btnStartStop;
	}
	public TextView getLblInfo() {
		return lblInfo;
	}
	public TextView getLblposition() {
		return lblposition;
	}
	
	public FrameLayout getVideoFrame() {
		return videoFrame;
	}
	public LinearLayout getRightPanel() {
		return rightPanel;
	}

}
