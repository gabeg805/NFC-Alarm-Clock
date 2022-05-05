package com.nfcalarmclock.whatsnew;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.util.dialog.NacDialogFragment;

/**
 */
public class NacWhatsNewDialog
	extends NacDialogFragment
{

	/**
	 * Listener for when what's new dialog has been read.
	 */
	@SuppressWarnings("UnnecessaryInterfaceModifier")
	public interface OnReadWhatsNewListener
	{
		public void onReadWhatsNew();
	}

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacWhatsNewDialog";

	/**
	 * Listener for when a text-to-speech option and frequency is selected.
	 */
	private OnReadWhatsNewListener mOnReadWhatsNewListener;

	/**
	 * Call the OnReadWhatsNewListener object, if it has been set.
	 */
	public void callOnReadWhatsNewListener()
	{
		OnReadWhatsNewListener listener = this.getOnReadWhatsNewListener();

		if (listener != null)
		{
			listener.onReadWhatsNew();
		}
	}

	/**
	 * Get the OnReadWhatsNewListener object.
	 *
	 * @return The OnReadWhatsNewListener object.
	 */
	public OnReadWhatsNewListener getOnReadWhatsNewListener()
	{
		return this.mOnReadWhatsNewListener;
	}

	/**
	 * Called when the dialog is canceled.
	 */
	@Override
	public void onCancel(DialogInterface dialog)
	{
		this.callOnReadWhatsNewListener();
	}

	/**
	 * Called when the dialog is created.
	 */
	@NonNull
	@Override
	public Dialog onCreateDialog(@Nullable Bundle savedInstanceState)
	{
		this.setupSharedPreferences();

		NacSharedConstants cons = this.getSharedConstants();

		return new AlertDialog.Builder(requireContext())
			.setTitle(cons.getTitleWhatsNew())
			.setPositiveButton(cons.getActionOk(), (dialog, which) ->
				this.callOnReadWhatsNewListener())
			.setView(R.layout.dlg_whats_new)
			.create();
	}

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		// Initialize the widgets
		AlertDialog dialog = (AlertDialog) getDialog();
		TextView textview = dialog.findViewById(R.id.whats_new_version);

		// Prepare the strings
		NacSharedConstants cons = this.getSharedConstants();
		String versionName = textview.getText().toString();
		String versionNum = cons.getAppVersion();

		// Set the version
		textview.setText(versionName + " " + versionNum);
	}

	/**
	 * Set the OnReadWhatsNewListener object.
	 *
	 * @param  listener  The OnReadWhatsNewListener object.
	 */
	public void setOnReadWhatsNewListener(OnReadWhatsNewListener listener)
	{
		this.mOnReadWhatsNewListener = listener;
	}

}
