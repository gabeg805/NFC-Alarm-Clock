package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

/**
 */
public class NacSnoozeDurationDialog
	extends NacSpinnerDialog
	implements NacDialog.OnShowListener
{

	/**
	 */
	public NacSnoozeDurationDialog()
	{
		this.addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getSnoozeDuration());
		setPositiveButton(cons.getActionOk());
		setNegativeButton(cons.getActionCancel());
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
