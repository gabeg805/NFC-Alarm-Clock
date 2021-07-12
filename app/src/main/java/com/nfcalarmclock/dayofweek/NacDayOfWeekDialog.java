package com.nfcalarmclock.dayofweek;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import android.widget.LinearLayout;

import com.nfcalarmclock.util.dialog.NacDialog;
import com.nfcalarmclock.R;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 */
public class NacDayOfWeekDialog
	extends NacDialog
	implements NacDialog.OnShowListener
{

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mShared;

	/**
	 * Day of week.
	 */
	private NacDayOfWeek mDayOfWeek;

	/**
	 */
	public NacDayOfWeekDialog()
	{
		super(R.layout.dlg_alarm_days);
		this.addOnShowListener(this);
	}

	/**
	 * @return The day of week object.
	 */
	public NacDayOfWeek getDayOfWeek()
	{
		return this.mDayOfWeek;
	}

	/**
	 * @return The shared preferences.
	 */
	protected NacSharedPreferences getSharedPreferences()
	{
		return this.mShared;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		NacSharedConstants cons = new NacSharedConstants(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);

		builder.setTitle(cons.getTitleDays());
		setPositiveButton(cons.getActionOk());
		setNegativeButton(cons.getActionCancel());

		this.mShared = shared;
	}

	/**
	 * Set the days in the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		LinearLayout dowView = root.findViewById(R.id.days);
		NacDayOfWeek dow = new NacDayOfWeek(dowView);
		int value = this.getDataInt();

		dow.setDays(value);
		dow.setStartWeekOn(shared.getStartWeekOn());

		this.mDayOfWeek = dow;
	}

}
