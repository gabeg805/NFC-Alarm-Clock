package com.nfcalarmclock;

import android.app.AlertDialog;
import android.view.View;

/**
 */
public class NacSpeakFrequencyDialog
	extends NacSpinnerDialog
	implements NacDialog.OnBuildListener,
		NacDialog.OnShowListener
{

	/**
	 */
	public NacSpeakFrequencyDialog()
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
		builder.setTitle("Frequency");
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
		int length = 31;
		String[] values = new String[length];

		values[0] = "Once";

		for (int i=1; i < length; i++)
		{
			values[i] = String.valueOf(i);
		}

		this.setDisplayedValues(values);
		this.setValue(index);
	}

}
