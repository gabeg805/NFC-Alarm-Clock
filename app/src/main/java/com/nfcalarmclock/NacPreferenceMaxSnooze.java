package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

/**
 * Preference that displays the max allowable number of snoozes.
 */
public class NacPreferenceMaxSnooze
	extends NacPreferenceValuePicker
{

	/**
	 */
	public NacPreferenceMaxSnooze(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceMaxSnooze(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceMaxSnooze(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
	}

	/**
	 */
	@Override
	public String getDialogTitle()
	{
		return "Max Snoozes";
	}

	/**
	 */
	@Override
	public int getMaxValue()
	{
		return 11;
	}

	/**
	 */
	@Override
	public int getMinValue()
	{
		return 0;
	}

	/**
	 * @return The summary text.
	 */
	@Override
	public CharSequence getSummary()
	{
		int max = this.getMaxValue();
		int min = this.getMinValue();

		if (this.mValue == min)
		{
			return "None";
		}
		else if (this.mValue == max)
		{
			return "Unlimited";
		}
		else
		{
			return String.valueOf(this.mValue);
		}
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index,
			NacSharedPreferences.DEFAULT_MAX_SNOOZE_INDEX);
	}

	/**
	 * Set the days in the dialog.
	 */
	@Override
	public void setupValuePicker(View root)
	{
		int max = this.getMaxValue();
		int min = this.getMinValue();
		int length = max+1;
		String[] values = new String[length];
		values[0] = "None";
		values[length-1] = "Unlimited";

		for (int i=1; i < length-1; i++)
		{
			values[i] = String.valueOf(i);
		}

		this.mPicker.setMinValue(min);
		this.mPicker.setMaxValue(max);
		this.mPicker.setDisplayedValues(values);
		this.mPicker.setValue(this.mValue);
		this.mPicker.setWrapSelectorWheel(false);
	}

}
