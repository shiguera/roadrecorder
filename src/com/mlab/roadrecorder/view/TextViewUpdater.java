package com.mlab.roadrecorder.view;

import android.os.Bundle;
import android.widget.TextView;

import com.mlab.roadrecorder.api.Observable;
import com.mlab.roadrecorder.api.Observer;
import com.mlab.roadrecorder.api.UpdateCommand;

public class TextViewUpdater implements Observer {

	protected Observable model;
	protected TextView textView;
	protected UpdateCommand command;
	
	public TextViewUpdater(TextView tv, Observable model, UpdateCommand command) {
		this.model = model;
		this.command = command;
		this.textView = tv;
		
	}

	@Override
	public Observable getObservable() {
		return model;
	}

	@Override
	public void update(Object sender, Bundle parameters) {
		textView.setText(command.getValue());
	}
}
