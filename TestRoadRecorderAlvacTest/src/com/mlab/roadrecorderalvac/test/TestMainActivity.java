package com.mlab.roadrecorderalvac.test;

import android.app.Activity;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.mlab.roadrecorder.NewActivity;
import com.mlab.roadrecorder.VideoController;
import com.mlab.roadrecorder.VideoModel;

public class TestMainActivity extends ActivityInstrumentationTestCase2<NewActivity> {

	private final String TAG = "ROADRECORDER";
	NewActivity activity;
	VideoModel model;
	VideoController controller;
	
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
        setActivityInitialTouchMode(false);    
        activity = getActivity();
        assertNotNull(activity);
        model = activity.getModel();
        assertNotNull(model);
        controller = activity.getController();
        assertNotNull(controller);


	}

	public TestMainActivity() {
		super(NewActivity.class);
		System.out.println("Here we are");
		Log.d("HAL", "Here we are");

		
	}
	public void test() {
        // initDefaultDirectory()
        assertTrue(activity.getModel().isExternalStorageEnabled());
        assertTrue(activity.getModel().setDefaultDirectory());
        
        // initCamera()
        assertTrue(controller.hasCameraHardware());

        assertTrue(controller.initMediaRecorder());
        assertNotNull(controller.getFrameLayout());
        assertEquals(1,controller.getFrameLayout().getChildCount());
        assertTrue(controller.isCameraEnabled());

        //controller.startRecording();
        
        // initSurfaceView
		
	}
	
}
