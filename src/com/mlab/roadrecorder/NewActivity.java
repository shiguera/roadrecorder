package com.mlab.roadrecorder;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

import org.apache.log4j.Logger;

import android.app.Activity;
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
import com.mlab.roadrecorder.api.CompositeObserver;
import com.mlab.roadrecorder.api.Observer;
import com.mlab.roadrecorder.gps.GpsModel;
import com.mlab.roadrecorder.video.VideoController;
import com.mlab.roadrecorder.video.VideoModel;
import com.mlab.roadrecorder.view.TitleValueLabel;
import com.mlab.roadrecorder.view.command.GetPointsCountCommand;
import com.mlab.roadrecorder.view.command.GetRecordingTimeCommand;

import de.mindpipe.android.logging.log4j.LogConfigurator;


public class NewActivity extends Activity  {
	private final Logger LOG = Logger.getLogger(NewActivity.class);
	
	public static final String TAG = "ROADRECORDER";
    public enum NotificationLevel {INFO,DEBUG,WARNING,ERROR};


    // 
    MainModel model;
    MainController controller;
    
    List<Observer> observerComponents;

    // Layout
	protected Button btnStartStop;
	protected TextView lblInfo, lblposition;
	protected FrameLayout videoFrame;
	protected LinearLayout rightPanel;
	TitleValueLabel lblRecordTime, lblPointsCount;
	
	// Live cycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		configureLogger();
		LOG.info("\n-----------------------");
		LOG.info("MainActivity.onCreate()");
		LOG.info("\n-----------------------");
		
		super.onCreate(savedInstanceState);
	
		this.observerComponents = new ArrayList<Observer>();
		
		model = new MainModel(getApplicationContext());
		App.setMainModel(model);
		
		//controller = new MainController(model, this, videoFrame);
		//App.setMainController(controller);

		//videoController = new VideoController(this, frameLayout, videoModel);

		initLayout();
		

	}
	@Override
	protected void onPause() {
		LOG.info("MainActivity.onPause()");
		if(videoFrame != null) {
			videoFrame.removeAllViews();
		}
		if (controller != null && controller.getVideoController()!=null) {
			controller.getVideoController().release();
		}
		if(model != null && model.getGpsModel() != null) {
			// TODO ¿Grabar el track¿ ¿Detener el recording del GPS?
		}
		super.onPause();
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
		getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);

		setContentView(R.layout.activity_main);
		
		videoFrame = (FrameLayout)this.findViewById(R.id.videoview);
		
		configureBtnStartStop();
        
        rightPanel = (LinearLayout)this.findViewById(R.id.rightPanel);
        
        configureLabels();

	}
	private void configureLabels() {
		// lblInfo
        lblInfo=(TextView)this.findViewById(R.id.lblInfo);

        
        lblRecordTime = new TitleValueLabel(this, new GetRecordingTimeCommand(model));
        lblRecordTime.setTitle("SG");
        lblRecordTime.setValue("-");
        rightPanel.addView(lblRecordTime.getView());
        lblRecordTime.update(model, null);
        
        lblPointsCount = new TitleValueLabel(this, new GetPointsCountCommand(model));
        lblPointsCount.setTitle("PT");
        lblPointsCount.setValue("-");
        rightPanel.addView(lblPointsCount.getView());
        lblPointsCount.update(model, null);
                
        lblposition=(TextView)this.findViewById(R.id.lblposition);            
	}
	private void configureBtnStartStop() {
		btnStartStop = (Button)findViewById(R.id.btn_rec);
        btnStartStop.setOnClickListener(new Button.OnClickListener(){
    		@Override
    		public void onClick(View v) {
    			if(getVideoModel().isRecording()) {
    				showNotification("Stoping",NotificationLevel.INFO, false);
    				getVideoController().stopRecording();
    				btnStartStop.setBackgroundResource(R.drawable.button_start);
    				//btnStartStop.setPressed(false);
    		        //btnStartStop.setText("Grabar"); 
    		        //btnStartStop.setBackgroundColor(Color.GREEN);
    				//Exit after saved
                    //finish();
                    return;
    			} else {
    				showNotification("Starting",NotificationLevel.INFO, false);
    				getVideoController().startRecording();
    				btnStartStop.setBackgroundResource(R.drawable.button_stop);
    		        //btnStartStop.setText("Parar");
    		        //btnStartStop.setBackgroundColor(Color.RED);
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
	
	private VideoModel initVideoManager(File outputDirectory) {
       	//videoManager = new VideoModel(this, frameLayout);
       	// Asignar el outputDirectory al videoManager
     	//videoManager.setOutputDirectory(outputDirectory);
       	return null;//videoManager;
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
