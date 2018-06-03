package com.mlab.roadrecorder.api;

import org.apache.log4j.Logger;

public abstract class GetValueCommand implements UpdateCommand {
	private final Logger LOG = Logger.getLogger(GetValueCommand.class);
	
	protected Observable model;
	
	protected GetValueCommand(Observable model) {
		this.model = model;
	}
	
	public Observable getModel() {
		return this.model;
	}
	
	

}
