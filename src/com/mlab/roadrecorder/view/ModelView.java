package com.mlab.roadrecorder.view;

import android.view.View;

import com.mlab.roadrecorder.api.Observer;

public interface ModelView extends Observer {

	public View getView();
}
