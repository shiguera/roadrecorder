package com.mlab.roadrecorder;

import java.io.File;

import org.apache.log4j.Logger;

import android.app.Activity;
import android.os.Bundle;
import android.os.Environment;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.example.roadrecorderalvac.R;
import com.mlab.roadrecorder.api.Observer;

import de.mindpipe.android.logging.log4j.LogConfigurator;

public class NewActivity extends Activity implements Observer {
	private final Logger LOG = Logger.getLogger(NewActivity.class);
	
	public static final String TAG = "ROADRECORDER";
    private enum LogLevel {INFO,DEBUG,WARNING,ERROR};


    // Video
	protected VideoModel videoModel;
	protected VideoController videoController;
	// Gps
	protected GpsModel gpsModel;
	//protected GpsController gpsController;
	
	// Layout
	protected Button btnStartStop;
	protected TextView lblInfo,lblrecordtime,lblposition, lblpointscount;
	protected FrameLayout frameLayout;
	
	// Live cycle
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		configureLogger();
		LOG.info("MainActivity.onCreate()");
		super.onCreate(savedInstanceState);
		
		
		initLayout();
		
		videoModel = new VideoModel();
		//videoController = new VideoController(this, frameLayout, videoModel);

	}
	
	private void configureLogger() {
		final LogConfigurator logConfigurator = new LogConfigurator();
         
        logConfigurator.setFileName(
        		Environment.getExternalStorageDirectory() + File.separator + "myapp.log");
        logConfigurator.setRootLevel(org.apache.log4j.Level.DEBUG);
        // Set log level of a specific logger
        logConfigurator.setLevel("org.apache", org.apache.log4j.Level.ERROR);
        logConfigurator.setUseLogCatAppender(true);
        logConfigurator.configure(); 
        
	}
	
	private void initLayout() {
		setContentView(R.layout.activity_main);
		frameLayout = (FrameLayout)this.findViewById(R.id.videoview);
		
        configureBtnStartStop();
        
        configureLabelInfo();

	}
	private void configureLabelInfo() {
		// lblInfo
        lblInfo=(TextView)this.findViewById(R.id.lblInfo);
        lblrecordtime=(TextView)this.findViewById(R.id.lblrecordtime);
        lblposition=(TextView)this.findViewById(R.id.lblposition);  
        lblpointscount=(TextView)this.findViewById(R.id.lblpointscount);          
	}
	private void configureBtnStartStop() {
		btnStartStop = (Button)findViewById(R.id.mybutton);
        btnStartStop.setOnClickListener(new Button.OnClickListener(){
    		@Override
    		public void onClick(View v) {
    			if(videoModel.isRecording()) {
    				showNotification("Stoping",LogLevel.INFO, false);
    				videoController.stopRecording();
    				btnStartStop.setBackgroundResource(R.drawable.button_start);
    				//btnStartStop.setPressed(false);
    		        //btnStartStop.setText("Grabar"); 
    		        //btnStartStop.setBackgroundColor(Color.GREEN);
    				//Exit after saved
                    //finish();
                    return;
    			} else {
    				showNotification("Starting",LogLevel.INFO, false);
    				videoController.startRecording();
    				btnStartStop.setBackgroundResource(R.drawable.button_stop);
    		        //btnStartStop.setText("Parar");
    		        //btnStartStop.setBackgroundColor(Color.RED);
    			}
    		}});
        btnStartStop.setEnabled(true);
	}
	@Override
	protected void onPause() {
		//Log.i(TAG,"MainActivity.onPause()");
		if(frameLayout != null) {
			frameLayout.removeAllViews();
		}
		if (videoController != null) {
			videoController.release();
		}
		super.onPause();
	}
	@Override
	protected void onStart() {
		//Log.i(TAG,"MainActivity.onStart()");
		if(videoController != null) {
			boolean result = videoController.initMediaRecorder();
			if(!result) {
				this.showNotification("Can't init MediaRecorder", LogLevel.ERROR, true);
				super.onStart();
				finish();
				return;
			}
		}
		super.onStart();
	}
	
	// Getters
	public VideoModel getModel() {
		return videoModel;
	}

	public VideoController getController() {
		return videoController;
	}
	
	// Interface Observer
	@Override
	public void update() {
		// TODO Auto-generated method stub
		
	}
	
	// Utilities
	public void showNotification(String message, LogLevel level, boolean withToast) {
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

}
