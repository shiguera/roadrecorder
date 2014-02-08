package com.mlab.roadrecorder.video;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimeZone;

import com.mlab.roadrecorder.NewActivity;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.pm.PackageManager;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.widget.FrameLayout;
import android.widget.Toast;

public class VideoController {
	
	private final String TAG = "ROADRECORDER";

	protected NewActivity activity;
	protected VideoModel model;
	
	protected FrameLayout frameLayout;
	protected SurfaceView view;
	protected SurfaceHolder holder;
	
	
	public VideoController( VideoModel model, NewActivity activity, FrameLayout frameLayout) {
		String method = "VidoController.VideoController() "; 
		Log.i(TAG, method);
		this.activity = activity;
		this.model = model;
		this.frameLayout = frameLayout;
		

		//initDefaultDirectory();
		//initMediaRecorder();
		
	}
	private Context getContext() {
		return this.activity;
	}
	@SuppressWarnings("deprecation")
	public boolean initMediaRecorder() {
		String method="VideoController.initMediaRecorder()";
		Log.i(TAG, method);
		initDefaultDirectory();
		initCamera();
		//System.out.println(model.getCamera().toString());		
		view = new SurfaceView(getContext());		
		holder = view.getHolder();
		holder.addCallback(model);
		holder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);		
		System.out.println("frameLayout==null"+String.format("%b", frameLayout==null));
		System.out.println("view==null"+String.format("%b", view==null));
		frameLayout.addView(view);
		//initMediaRecorder();
		if(!model.initMediaRecorder(holder)) {
			Log.e(TAG, "Can't init media recorder");
			return false;
		}
		return true;
	}
	private void initCamera() {
		String method = "VideoController.initCamera() ";
		String msg = "";
		Log.i(TAG, method);
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
		Log.e(TAG, method + msg);
		Toast.makeText(getContext(), msg,Toast.LENGTH_LONG).show();		
		activity.finish();
		return;
	}
	private void initDefaultDirectory() {
		String method = "VideoController.initDefaultDirectory() ";
		String msg = "";
		Log.i(TAG, method);
		boolean result = model.setDefaultDirectory();
		if(result) { 
			return;
		} else {
			msg = "Error,  can't init default directory";
		}			
		Log.e(TAG, method + msg);
		Toast.makeText(getContext(), msg,Toast.LENGTH_LONG).show();		
		activity.finish();
		return;
	}
	
	// MediaRecorder management
	public boolean startRecording() {
		Log.i(TAG,"VideoController.startRecording()");
		boolean result = model.startRecording();
		if(!result) {
			Log.e(TAG, "VideoController.startRecording() : Can't start recording");
		}
		return result;
	}
	public boolean stopRecording() {
		Log.i(TAG,"VideoController.stopRecording()");	
		boolean result = model.stopRecording();
		if(!result) {
			Log.e(TAG, "VideoController.stopRecording() : Can't stop recording");
		}
		return result;
	}

	// Releasing resources
	public void release() {
		Log.i(TAG,"VideoController.release()");
		model.release();
	}

	// Getters
	public VideoModel getModel() {
		return model;
	}
	public FrameLayout getFrameLayout() {
		return frameLayout;
	}
	
	// Utilities
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


}
