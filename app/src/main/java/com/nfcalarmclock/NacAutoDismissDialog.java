package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

/**
 */
public class NacAutoDismissDialog
	extends NacSpinnerDialog
	implements NacDialog.OnShowListener
{

	/**
	 */
	public NacAutoDismissDialog()
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

		builder.setTitle(cons.getAutoDismiss());
		setPositiveButton(cons.getOk());
		setNegativeButton(cons.getCancel());
	}

	/**
	 * Show the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		super.onShowDialog(dialog, root);

		Context context = getContext();
		int index = this.getDataInt();
		int length = 17;
		String[] values = new String[length];

		for (int i=0; i < length; i++)
		{
			String summary = NacSharedPreferences.getAutoDismissSummary(
				context, i);
			values[i] = summary.split("\\s+")[0];
		}

		this.setDisplayedValues(values);
		this.setValue(index);
	}

}
