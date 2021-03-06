package com.mlab.roadrecorder.activities;

import java.util.Timer;
import java.util.TimerTask;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import com.mlab.roadrecorder.MainActivity;
import com.mlab.roadrecorder.R;

public class SplashActivity extends Activity {

	private long splashDelay = 500; //3 segundos

	  @Override
	  protected void onCreate(Bundle savedInstanceState) {
	    super.onCreate(savedInstanceState);
	    setContentView(R.layout.activity_splash);

	    TimerTask task = new TimerTask() {
	      @Override
	      public void run() {
	        Intent mainIntent = new Intent().setClass(SplashActivity.this, MainActivity.class);
	        startActivity(mainIntent);
	        finish();//Destruimos esta activity para prevenit que el usuario retorne aqui presionando el boton Atras.
	      }
	    };

	    Timer timer = new Timer();
	    timer.schedule(task, splashDelay);//Pasado los 6 segundos dispara la tarea
	  }

}
