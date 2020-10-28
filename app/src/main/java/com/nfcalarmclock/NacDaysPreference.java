package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;
import androidx.preference.Preference;

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
	 * Shared preferences.
	 */
	protected final NacSharedPreferences mSharedPreferences;

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

		this.mSharedPreferences = new NacSharedPreferences(context);
	}

	/**
	 * @return The shared preferences.
	 */
	protected NacSharedPreferences getShared()
	{
		return this.mSharedPreferences;
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		return this.getShared().getDaysSummary();
	}

	/**
	 * Save the selected days when the dialog is dismissed.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		NacDayOfWeek dow = ((NacDayOfWeekDialog) dialog).getDayOfWeek();
		this.mValue = NacCalendar.Days.daysToValue(dow.getDays());

		setSummary(this.getSummary());
		persistInt(this.mValue);
		return true;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return a.getInteger(index, NacSharedDefaults.getDaysValue());
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
		NacDayOfWeek dow = ((NacDayOfWeekDialog) dialog).getDayOfWeek();

		dow.setDays(this.mValue);
	}

	/**
	 * Set the initial preference value.
	 */
	@Override
	protected void onSetInitialValue(Object defaultValue)
	{
		if (defaultValue == null)
		{
			this.mValue = getPersistedInt(this.mValue);
		}
		else
		{
			this.mValue = (Integer) defaultValue;
			persistInt(this.mValue);
		}
	}

}
