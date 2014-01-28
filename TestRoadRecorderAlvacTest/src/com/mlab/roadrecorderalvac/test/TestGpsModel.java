package com.mlab.roadrecorderalvac.test;

import java.io.File;

import android.os.Environment;
import android.test.ActivityInstrumentationTestCase2;
import android.util.Log;

import com.mlab.roadrecorder.GpsModel;
import com.mlab.roadrecorder.TestGpsModelActivity;

public class TestGpsModel  
	extends ActivityInstrumentationTestCase2<TestGpsModelActivity>{

	private final String TAG = "ROADRECORDER";
	
	TestGpsModelActivity activity;
	GpsModel model;
	
	@Override
    protected void setUp() throws Exception {
        super.setUp();
		Log.d(TAG, "TestGpsModel.setUp()");
        setActivityInitialTouchMode(false);    
        activity = getActivity();
        assertNotNull(activity);
        model = activity.getModel();
        assertNotNull(model);
	}
	
	public TestGpsModel() {
		super(TestGpsModelActivity.class);
		Log.d(TAG, "TestGpsModel.TestGpsModel()");		
		

	}
	
	public void test() {
		Log.d(TAG, "TestGpsModel.test()");
		
		File extstorage = new File(System.getenv("EXTERNAL_STORAGE"));
		Log.d(TAG, "External Storage: " + extstorage.getPath());	
				
		try {
			String cad = System.getenv("SECONDARY_STORAGE");
			Log.d(TAG, "Secondary Storage: " + cad);		
			String[] dirs = cad.split(":");
			for(String s: dirs) {
				//Log.d(TAG, "\t" + s);
				if(!s.contains("usb")) {
					File file = new File(s);
					Log.d(TAG, "\n\t\t" + file.getPath());					
				}
			}
		} catch (Exception e) {
			Log.e(TAG, e.getMessage());
		}
		
		this.getSecondaryStorage();
		
	}
	
	private void listBrothers(File file) {
		File parent = file.getParentFile();
		if(parent != null && parent.exists()) {
			File[] childs = parent.listFiles();
			for(File child: childs) {
				Log.d(TAG, "\t" + child.getPath());
			}
		}
	}

}
