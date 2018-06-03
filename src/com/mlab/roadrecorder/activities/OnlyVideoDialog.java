package com.mlab.roadrecorder.activities;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.content.DialogInterface;
import android.os.Bundle;

/**
 * Sin usarse. Procede de pruebas con DialogFragment
 * @author shiguera
 *
 */
public class OnlyVideoDialog extends DialogFragment {

	public interface DialogListener {
		public void onDialogPositiveClick(DialogFragment dialog);

		public void onDialogNegativeClick(DialogFragment dialog);
	}

	/*
	 * The activity that creates an instance of this dialog fragment must
	 * implement this interface in order to receive event callbacks. Each method
	 * passes the DialogFragment in case the host needs to query it.
	 */

	// Use this instance of the interface to deliver action events
	DialogListener mListener;

	// Override the Fragment.onAttach() method to instantiate the
	// NoticeDialogListener
	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);
		// Verify that the host activity implements the callback interface
		try {
			// Instantiate the NoticeDialogListener so we can send events to the
			// host
			mListener = (DialogListener) activity;
		} catch (ClassCastException e) {
			// The activity doesn't implement the interface, throw exception
			throw new ClassCastException(activity.toString()
					+ " must implement NoticeDialogListener");
		}
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		// Build the dialog and set up the button click handlers
		AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setMessage("Message")
				.setPositiveButton("Sí", new DialogInterface.OnClickListener() {
					public void onClick(DialogInterface dialog, int id) {
						// Send the positive button event back to the host
						// activity
						mListener.onDialogPositiveClick(OnlyVideoDialog.this);
					}
				})
				.setNegativeButton("No",
						new DialogInterface.OnClickListener() {
							public void onClick(DialogInterface dialog, int id) {
								// Send the negative button event back to the
								// host activity
								mListener
									.onDialogNegativeClick(OnlyVideoDialog.this);
							}
						});
		return builder.create();
	}

}
