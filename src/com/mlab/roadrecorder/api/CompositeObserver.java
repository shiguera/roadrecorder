package com.mlab.roadrecorder.api;

import java.util.ArrayList;
import java.util.List;

import android.os.Bundle;

public interface CompositeObserver extends Observer {

	public boolean addComponent(Observer o);
	public boolean removeComponent(Observer o);
	public void update(Object sender, Bundle parameters);

}
