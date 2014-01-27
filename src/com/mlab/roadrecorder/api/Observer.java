package com.mlab.roadrecorder.api;

/**
 * Las clases se pueden registrar como observadores de las 
 * clases {@link Observable}. Disponen de un método
 * <em>update()</em> que podrá ser llamado por el {@link Observable}
 * para avisar de modificaciones.<br/>
 *  
 * @author shiguera
 *
 */
public interface Observer {
	Observable getModel();
	public void update();
}
