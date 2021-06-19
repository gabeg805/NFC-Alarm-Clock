package com.nfcalarmclock.dialog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.widget.Button;
import androidx.fragment.app.DialogFragment;

import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 */
public abstract class NacDialogFragment
	extends DialogFragment
{

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mSharedPreferences;

	/**
	 * Get the shared constants.
	 *
	 * @return The shared constants.
	 */
	protected NacSharedConstants getSharedConstants()
	{
		return this.getSharedPreferences().getConstants();
	}

	/**
	 * Get the shared preferences.
	 *
	 * @return The shared preferences.
	 */
	protected NacSharedPreferences getSharedPreferences()
	{
		return this.mSharedPreferences;
	}

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		this.setupDialogColor();
	}

	/**
	 * Setup the dialog color.
	 */
	protected void setupDialogColor()
	{
		AlertDialog dialog = (AlertDialog) getDialog();
		Button okButton = dialog.getButton(DialogInterface.BUTTON_POSITIVE);
		Button cancelButton = dialog.getButton(DialogInterface.BUTTON_NEGATIVE);
		int themeColor = this.getSharedPreferences().getThemeColor();

		okButton.setTextColor(themeColor);
		cancelButton.setTextColor(themeColor);
	}

	/**
	 * Setup the shared preferences.
	 */
	protected void setupSharedPreferences()
	{
		Context context = getContext();
		this.mSharedPreferences = new NacSharedPreferences(context);
	}

}
