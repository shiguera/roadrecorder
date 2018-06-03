package com.mlab.roadrecorder.view;

import org.apache.log4j.Logger;

import android.widget.TextView;

import com.mlab.roadrecorder.api.GetValueCommand;
import com.mlab.roadrecorder.api.Observable;
import com.mlab.roadrecorder.api.SingleObserver;
import com.mlab.roadrecorder.api.UpdateCommand;

public class TextViewUpdater extends SingleObserver {

	private final Logger LOG = Logger.getLogger(TextViewUpdater.class);
	
	protected TextView textView;
	protected UpdateCommand command;
	
	public TextViewUpdater(TextView tv, GetValueCommand command) {
		super(command.getModel());
		this.textView = tv;
		this.command = command;
		
	}

	@Override
	public void update() {
		//LOG.debug("TextViewUpdater.update() "+command.getValue());
		this.textView.setText(command.getValue());
	}
	
}
