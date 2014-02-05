package com.mlab.roadrecorder.api;

import android.view.View;

/**
 * ModelView sería el equivalente en RoadPlayer al 
 * interface Panel que devuelve un JPanel. En Android se devuelve 
 * un View
 * 
 * @author shiguera
 *
 */
public interface ModelView extends Observer {
	public View getView();
}
