package com.mlab.roadrecorder.video;

import java.io.File;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import org.apache.log4j.Logger;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.media.MediaRecorder;
import android.media.MediaRecorder.OnErrorListener;
import android.media.MediaRecorder.OnInfoListener;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.mlab.android.utils.AvailableSpaceHandler;
import com.mlab.roadrecorder.MainActivity;
import com.mlab.roadrecorder.api.Controller;

public class VideoController implements Controller, OnInfoListener, OnErrorListener {
	private final Logger LOG = Logger.getLogger(VideoController.class);
	//private final String TAG = "ROADRECORDER";

	protected MainActivity activity;
	protected VideoModel model;
	
	protected FrameLayout frameLayout;
	protected SurfaceView view;
	protected SurfaceHolder holder;
	
	// Constructor
	public VideoController(MainActivity activity, FrameLayout frameLayout) {
		LOG.info("VideoController()");
		this.activity = activity;
		this.frameLayout = frameLayout;		
		
		this.model = new VideoModel();
	}
	private Context getContext() {
		return this.activity;
	}
	/**
	 * Este método se debe llamar antes de usar startRecording().<br/>
	 * En la MainActivity en onCreate()
	 * 
	 * @return
	 */
	@SuppressWarnings("deprecation")
	public boolean initMediaRecorder(File outputDirectory) {
		String method="VideoController.initMediaRecorder()";
		LOG.info( method);
		//initDefaultDirectory();
		model.setOutputDirectory(outputDirectory);
		initCamera();			
		//System.out.println(model.getCamera().toString());		
		view = new SurfaceView(getContext());		
		holder = view.getHolder();
		holder.addCallback(model);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);		
		//System.out.println("frameLayout==null"+String.format("%b", frameLayout==null));
		//System.out.println("view==null"+String.format("%b", view==null));
		frameLayout.addView(view);
		//initMediaRecorder();
		if(!model.initMediaRecorder()) {
			LOG.error( "Can't init media recorder");
			return false;
		}
		model.getMediaRecorder().setOnErrorListener(this);
		model.getMediaRecorder().setOnInfoListener(this);
		return true;
	}
	@SuppressWarnings("deprecation")
	private void initHolder() {
		view = new SurfaceView(getContext());		
		holder = view.getHolder();
		holder.addCallback(model);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);		
		//System.out.println("frameLayout==null"+String.format("%b", frameLayout==null));
		//System.out.println("view==null"+String.format("%b", view==null));
		frameLayout.removeAllViews();
		frameLayout.addView(view);
	}
	public void postInitMediaRecorder() {
		if(model.getCamera()==null) {
			initCamera();
		}
		initHolder();
	}
	private void initCamera() {
		String method = "VideoController.initCamera() ";
		String msg = "";
		LOG.info( method);
		boolean result = hasCameraHardware();
		if(result) {
			result = model.initCamera();
			if(result) { 
				return;
			} else {
				msg = "Error,  can't init camera";
			}			
		} else {
			msg = "Error,  there isn't camera hardware";
		}
		LOG.error( method + msg);
		Toast.makeText(getContext(), msg,Toast.LENGTH_LONG).show();		
		activity.finish();
		return;
	}
	
	// MediaRecorder management
	public boolean startRecording() {
		LOG.info("VideoController.startRecording()");
		boolean result = model.startRecording();
		if(!result) {
			LOG.error( "VideoController.startRecording() : Can't start recording");
		}
		return result;
	}
	public boolean stopRecording() {
		LOG.info("VideoController.stopRecording()");
		boolean result = false;
		if(model.isRecording()) {
			result = model.stopRecording();
			if(!result) {
				LOG.error( "VideoController.stopRecording() : Can't stop recording");
			}
		} else {
			LOG.debug("VideoController.stopRecording(): OK");
			result = true;
		}
		return result;
	}
	public void setMaxVideoDuration(int maxduration) {
		model.setMaxDuration(maxduration);
	}
	public void setMaxVideoFileSize(int maxFileSize) {
		model.setMaxFileSize(maxFileSize);
	}
	// Utilities
	public boolean isRecording() {
		return model.isRecording();
	}
	public boolean isEnabled() {
		return model.isEnabled();
	}
	public boolean hasCameraHardware() {
		return getContext().getPackageManager().hasSystemFeature(PackageManager.FEATURE_CAMERA);
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

    // Interface Controller
	@Override
	public VideoModel getModel() {
		return model;
	}
	@Override
	public void release() {
		LOG.info("VideoController.release()");
		model.release();
	}
    @Override
	public FrameLayout getView() {
		return this.frameLayout;
	}
	// Interface onInfoListener
    @Override
	public void onInfo(MediaRecorder mr, int what, int extra) {
		switch(what) {
		case MediaRecorder.MEDIA_RECORDER_INFO_UNKNOWN:
			LOG.debug("VideoController.onInfo():  MEDIA_RECORDER_INFO_UNKNOWN");
			break;
		case MediaRecorder.MEDIA_RECORDER_INFO_MAX_DURATION_REACHED:
			LOG.debug("VideoController.onInfo():  MEDIA_RECORDER_INFO_MAX_DURATION_REACHED");
			break;
		case MediaRecorder.MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED:
			LOG.debug("VideoController.onInfo():  MEDIA_RECORDER_INFO_MAX_FILESIZE_REACHED");
			break;
		default:
			LOG.debug("VideoController.onInfo():  unknown");				
		}
	}
    // Interface onErrorListener
	@Override
	public void onError(MediaRecorder mr, int what, int extra) {
		switch(what) {
		case MediaRecorder.MEDIA_RECORDER_ERROR_UNKNOWN:
			LOG.debug("VideoController.onError():  MEDIA_RECORDER_ERROR_UNKNOWN");
			break;
		case MediaRecorder.MEDIA_ERROR_SERVER_DIED:
			LOG.debug("VideoController.onError():  MEDIA_ERROR_SERVER_DIED");
			break;
		default:
			LOG.debug("VideoController.onError():  unknown");				
		}
		
	}


}
