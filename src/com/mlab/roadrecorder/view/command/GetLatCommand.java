package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.api.WayPoint;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;

public class GetLatCommand extends AbstractGetValueCommand {

	public GetLatCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		
		WayPoint wp = ((MainModel)model).getGpsModel().getLastWayPoint();
		
		String value =(wp != null && wp.getLatitude() != 0.0)?String.format("%11.6f", wp.getLatitude()):"  -  ";
		return value;
	}
	

}
