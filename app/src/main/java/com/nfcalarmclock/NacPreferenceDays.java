package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.res.TypedArray;
import android.content.SharedPreferences;
import android.preference.Preference;
import android.preference.PreferenceManager;
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
	 * Default value to input into a DayOfWeek object.
	 */
	protected static final int mDefault = 0;

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
	 */
	@Override
	public CharSequence getSummary()
	{
		Alarm alarm = new Alarm();

		alarm.setDays(this.mValue);

		return alarm.getDaysString();
	}

	/**
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);

		NacUtility.printf("Days onBindView : %s", this.getSummary());
		this.setSummary(this.getSummary());
	}

	/**
	 */
	@Override
	public void onBuildDialog(NacDialog dialog, AlertDialog.Builder builder)
	{
		String title = "Select Days";

		builder.setTitle(title);
		dialog.setPositiveButton("OK");
		dialog.setNegativeButton("Cancel");
	}

	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		NacUtility.printf("DoW onShowDialog : %d", this.mValue);
		NacDayOfWeek dow = root.findViewById(R.id.days);

		dow.setDays(this.mValue);
	}

	/**
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		View root = dialog.getRootView();
		NacDayOfWeek dow = root.findViewById(R.id.days);
		this.mValue = dow.getDays();

		NacUtility.printf("DayOfWeek dialog dismissed with value : %d", this.mValue);
		this.setSummary(this.getSummary());
		persistInt(this.mValue);
	}

	/**
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index, this.mDefault);
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference pref)
	{
		NacUtility.printf("Days preference clicked.");
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
