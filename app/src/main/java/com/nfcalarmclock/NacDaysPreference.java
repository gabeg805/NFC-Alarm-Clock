package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.View;

/**
 * Preference that displays the day of week dialog.
 */
public class NacDaysPreference
	extends Preference
	implements Preference.OnPreferenceClickListener,
		NacDialog.OnShowListener,
		NacDialog.OnDismissListener
{

	/**
	 * Value of days.
	 */
	protected int mValue;

	/**
	 */
	public NacDaysPreference(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacDaysPreference(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacDaysPreference(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		setLayoutResource(R.layout.nac_preference);
		setOnPreferenceClickListener(this);
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		return NacSharedPreferences.getDaysSummary(this.mValue);
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
	 * Save the selected days when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		View root = dialog.getRoot();
		NacDayOfWeek dow = root.findViewById(R.id.days);
		this.mValue = NacCalendar.daysToValue(dow.getDays());

		this.setSummary(this.getSummary());
		persistInt(this.mValue);

		return true;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index, NacSharedPreferences.DEFAULT_DAYS);
	}

	/**
	 * Display the dialog when the preference is selected.
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		Context context = getContext();
		NacDayOfWeekDialog dialog = new NacDayOfWeekDialog();

		dialog.build(context);
		dialog.addOnShowListener(this);
		dialog.addOnDismissListener(this);
		dialog.show();

		return true;
	}

	/**
	 * Set the days in the dialog.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		NacDayOfWeek dow = root.findViewById(R.id.days);
		boolean mondayFirst = shared.getMondayFirst();

		dow.setDays(this.mValue);
		dow.setMondayFirst(mondayFirst);
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
