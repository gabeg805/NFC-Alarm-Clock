package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

/**
 */
public class NacMaxSnoozeDialog
	extends NacSpinnerDialog
	implements NacDialog.OnShowListener
{

	/**
	 */
	public NacMaxSnoozeDialog()
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

		builder.setTitle(cons.getMaxSnooze());
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
