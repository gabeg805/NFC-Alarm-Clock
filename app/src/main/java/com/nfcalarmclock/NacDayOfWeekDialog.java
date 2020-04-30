package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;

/**
 */
public class NacDayOfWeekDialog
	extends NacDialog
	implements NacDialog.OnShowListener
{

	/**
	 */
	public NacDayOfWeekDialog()
	{
		super(R.layout.dlg_alarm_days);
		this.addOnShowListener(this);
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);

		builder.setTitle(cons.getSelectDays());
		setPositiveButton(cons.getOk());
		setNegativeButton(cons.getCancel());
	}

	/**
	 * Set the days in the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		NacDayOfWeek dow = root.findViewById(R.id.days);
		int value = this.getDataInt();
		dow.setDays(value);
	}

}
