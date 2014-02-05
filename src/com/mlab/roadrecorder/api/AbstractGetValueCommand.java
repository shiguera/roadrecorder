package com.mlab.roadrecorder.api;

import org.apache.log4j.Logger;

public abstract class AbstractGetValueCommand implements UpdateCommand {

	private final Logger LOG = Logger.getLogger(AbstractGetValueCommand.class);
	protected Observable model;
	
	public AbstractGetValueCommand(Observable model) {
		this.model = model;
	}
	@Override
	public Observable getObservable() {
		return model;
	}


}
