package com.mlab.roadrecorder.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.roadrecorderalvac.R;
import com.mlab.roadrecorder.api.ModelView;
import com.mlab.roadrecorder.api.Observable;
import com.mlab.roadrecorder.api.UpdateCommand;

public class TitleValueLabel implements ModelView {

	protected Context context;
	protected UpdateCommand updateCommand;
	protected Observable model;
	protected String title, value;
	protected TextView valueLabel;
	protected TextView titleLabel;
	protected ViewGroup parentView;
	protected View mainView;

	// Constructor
	public TitleValueLabel(Context context, ViewGroup parentView, UpdateCommand command) {
		this.context = context;
		this.parentView = parentView;
		this.updateCommand = command;
		this.model = command.getObservable();
		
		buildLayout2();
	
		this.model.registerObserver(this);	
	}
	private void buildLayout2() {
		mainView = (LinearLayout)View.inflate(context, R.layout.titlevalue, parentView);
		titleLabel = (TextView)mainView.findViewById(R.id.titlevalue_title);
		valueLabel = (TextView)mainView.findViewById(R.id.titlevalue_value);
	}
	private void buildLayout() {
		mainView = new LinearLayout(this.context);
		//mainView.setOrientation(android.widget.LinearLayout.VERTICAL);
		mainView.setLayoutParams(new ViewGroup.LayoutParams(-2, -2));
		// ARGB: Opaque Red
		//mainView.setBackgroundColor(0x88ff0000);

		LinearLayout.LayoutParams pars = new LinearLayout.LayoutParams(-2,-2);
		pars.gravity = Gravity.CENTER;
		pars.bottomMargin = 5;

		titleLabel = new TextView(context);
		titleLabel.setLayoutParams(pars);		
		// ARGB: Opaque Green
		//titleLabel.setBackgroundColor(0x5500ff00);
		titleLabel.setTextColor(Color.DKGRAY);	
		titleLabel.setTypeface(Typeface.MONOSPACE, Typeface.BOLD);
		titleLabel.setText("TIT");
		//mainView.addView(titleLabel);

		valueLabel = new TextView(context);
		valueLabel.setLayoutParams(pars);
		valueLabel.setWidth(80);
		// ARGB: Solid Blue
		//titleLabel.setBackgroundColor(0xff0000ff);
		valueLabel.setTextColor(Color.GRAY);		
		valueLabel.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
		valueLabel.setText("-");
		//mainView.addView(valueLabel);
	}

	// Interface Observer
	@Override
	public Observable getObservable() {
		return model;
	}
	@Override
	public void update(Object sender, Bundle parameters) {
		this.setValue(updateCommand.getValue());
	}
	// Interface ModelView
	@Override
	public View getView() {
		return mainView;
	}

	// Getters
	public String getTitle() {
		return title;
	}
	public void setTitle(String text) {
		this.title = text;
		this.titleLabel.setText(text);
	}
	public String getValue() {
		return value;
	}
	public void setValue(String value) {
		this.value = value;
		this.valueLabel.setText(value);
	}

}
