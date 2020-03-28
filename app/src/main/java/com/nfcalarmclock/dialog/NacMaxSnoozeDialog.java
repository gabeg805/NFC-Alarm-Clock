package com.nfcalarmclock;

import android.app.AlertDialog;
import android.view.View;

/**
 */
public class NacMaxSnoozeDialog
	extends NacSpinnerDialog
	implements NacDialog.OnBuildListener,
		NacDialog.OnShowListener
{

	/**
	 */
	public NacMaxSnoozeDialog()
	{
		this.setOnBuildListener(this);
		this.addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		builder.setTitle("Max Snoozes");
		dialog.setPositiveButton("OK");
		dialog.setNegativeButton("Cancel");
	}

	/**
	 * Show the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		super.onShowDialog(dialog, root);

		int index = this.getDataInt();
		int length = 12;
		String[] values = new String[length];

		for (int i=0; i < length; i++)
		{
			values[i] = NacSharedPreferences.getMaxSnoozeSummary(i);
		}

		this.setDisplayedValues(values);
		this.setValue(index);
	}

}
