package com.mlab.roadrecorder.view;

import android.content.Context;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.example.roadrecorderalvac.R;
import com.mlab.roadrecorder.api.Observable;

public class TitleValueLabel implements ModelView {

	protected Context context;
	protected Observable model;
	protected String title, value;
	protected TextView valueLabel;
	protected TextView titleLabel;
	protected LinearLayout mainView;

	public TitleValueLabel(Context context, Observable model) {
		this.context = context;
		this.model = model;

		mainView = new LinearLayout(this.context);
		mainView.setOrientation(android.widget.LinearLayout.VERTICAL);
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
		mainView.addView(titleLabel);

		valueLabel = new TextView(context);
		valueLabel.setLayoutParams(pars);
		valueLabel.setWidth(80);
		// ARGB: Solid Blue
		//titleLabel.setBackgroundColor(0xff0000ff);
		valueLabel.setTextColor(Color.GRAY);		
		valueLabel.setTypeface(Typeface.MONOSPACE, Typeface.NORMAL);
		valueLabel.setText("-");
		mainView.addView(valueLabel);
	}

	@Override
	public Observable getObservable() {
		return model;
	}

	@Override
	public void update(Object sender, Bundle parameters) {
		// TODO Auto-generated method stub

	}

	@Override
	public View getView() {
		return mainView;
	}

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
