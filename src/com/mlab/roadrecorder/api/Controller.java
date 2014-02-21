package com.mlab.roadrecorder.api;

import android.view.View;

public interface Controller {
	
	Observable getModel();
	View getView();
	void release();

}
