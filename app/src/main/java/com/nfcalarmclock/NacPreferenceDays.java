package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

/**
 * Day of week preference.
 */
public class NacPreferenceDays
	extends Preference
	implements Preference.OnPreferenceClickListener,NacDialog.OnDismissedListener,NacDialog.OnBuildListener,NacDialog.OnShowListener
{

	/**
	 * Value of days.
	 */
	protected int mValue;

	/**
	 */
	public NacPreferenceDays(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceDays(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceDays(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.pref_days);
		setOnPreferenceClickListener(this);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		Alarm alarm = new Alarm(this.mValue);
		String days = alarm.getDaysString();

		//alarm.setDays(this.mValue);
		return (!days.isEmpty()) ? days : Alarm.getDaysStringDefault();
		//return alarm.getDaysString();
	}

	/**
	 * Set the summary text.
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);
		this.setSummary(this.getSummary());
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		String title = "Select Days";

		builder.setTitle(title);
		dialog.setPositiveButton("OK");
		dialog.setNegativeButton("Cancel");
	}

	/**
	 * Save the selected days when the dialog is dismissed.
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		View root = dialog.getRootView();
		NacDayOfWeek dow = root.findViewById(R.id.days);
		this.mValue = dow.getDays();

		this.setSummary(this.getSummary());
		persistInt(this.mValue);
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index, Alarm.getDaysDefault());
	}

	/**
	 * Display the dialog when the preference is selected.
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		Context context = getContext();
		NacDialog dialog = new NacDialog();

		dialog.setOnBuildListener(this);
		dialog.setOnShowListener(this);
		dialog.build(context, R.layout.dlg_alarm_days);
		dialog.addDismissListener(this);
		dialog.show();

		return true;
	}

	/**
	 * Set the days in the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		NacDayOfWeek dow = root.findViewById(R.id.days);

		dow.setDays(this.mValue);
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(boolean restore, Object defval)
	{
		if (restore)
		{
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defval;

			persistInt(this.mValue);
		}
	}

}
