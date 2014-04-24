package com.mlab.roadrecorder.view;

import org.apache.log4j.Logger;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

public class ShowMessageDialogFragment extends DialogFragment {
	private final Logger LOG = Logger.getLogger(ShowMessageDialogFragment.class);
	
	String message = "";
	public ShowMessageDialogFragment() {
		super();
	}
	public void setMessage(String message) {
		this.message = message;
	}
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage(message);
		builder.setPositiveButton("OK", new DialogInterface.OnClickListener() {
			@Override
			public void onClick(DialogInterface dialog, int which) {
				LOG.info("OnClick()");
			}
		});
		return builder.create();
	}
}