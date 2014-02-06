package com.mlab.roadrecorder.view.command;

import com.mlab.gpx.api.WayPoint;
import com.mlab.gpx.impl.AndroidWayPoint;
import com.mlab.roadrecorder.MainModel;
import com.mlab.roadrecorder.api.AbstractGetValueCommand;

public class GetAccuracyCommand extends AbstractGetValueCommand {

	public GetAccuracyCommand(MainModel model) {
		super(model);
	}

	@Override
	public String getValue() {
		
		WayPoint wp = ((MainModel)model).getGpsModel().getLastWayPoint();
		double acc = -1.0;
		if(wp!=null && wp.getClass().isAssignableFrom(AndroidWayPoint.class)) {
			acc = ((AndroidWayPoint)wp).getAccuracy();
		}
		String value = String.format("%4.1f", acc);
		return value;
	}
	

}
