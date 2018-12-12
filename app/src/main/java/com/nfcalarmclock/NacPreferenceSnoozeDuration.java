package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

/**
 * Preference that displays how long to snooze for.
 */
public class NacPreferenceSnoozeDuration
	extends NacPreferenceValuePicker
{

	/**
	 */
	public NacPreferenceSnoozeDuration(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceSnoozeDuration(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceSnoozeDuration(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
	}

	/**
	 */
	@Override
	public String getDialogTitle()
	{
		return "Snooze Duration";
	}

	/**
	 */
	@Override
	public int getMaxValue()
	{
		return 18;
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
		int min = this.getMinValue();

		return (this.mValue == min) ? "1 minute" :
			String.valueOf(5*this.mValue)+" minutes";
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index, 1);
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
		values[0] = "1";

		for (int i=1; i < length; i++)
		{
			values[i] = String.valueOf(5*i);
		}

		this.mPicker.setMinValue(min);
		this.mPicker.setMaxValue(max);
		this.mPicker.setDisplayedValues(values);
		this.mPicker.setValue(this.mValue);
		this.mPicker.setWrapSelectorWheel(false);
	}

}
