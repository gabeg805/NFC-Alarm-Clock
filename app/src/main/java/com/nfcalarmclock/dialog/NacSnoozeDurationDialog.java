package com.nfcalarmclock;

import android.app.AlertDialog;
import android.view.View;

/**
 */
public class NacSnoozeDurationDialog
	extends NacSpinnerDialog
	implements NacDialog.OnBuildListener,
		NacDialog.OnShowListener
{

	/**
	 */
	public NacSnoozeDurationDialog()
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
		builder.setTitle("Snooze Duration");
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
		int length = 22;
		String[] values = new String[length];

		for (int i=0; i < length; i++)
		{
			String summary = NacSharedPreferences.getSnoozeDurationSummary(i);
			values[i] = summary.split("\\s+")[0];
		}

		this.setDisplayedValues(values);
		this.setValue(index);
	}

}
