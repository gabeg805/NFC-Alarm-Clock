package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.View;

/**
 * Preference that displays how long to wait before auto dismissing the alarm.
 */
public class NacPreferenceAutoDismiss
	extends NacPreferenceValuePicker
{

	/**
	 */
	public NacPreferenceAutoDismiss(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceAutoDismiss(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceAutoDismiss(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
	}

	/**
	 */
	@Override
	public String getDialogTitle()
	{
		return "Auto-Dismiss";
	}

	/**
	 */
	@Override
	public int getMaxValue()
	{
		return 13;
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

		if (this.mValue == min)
		{
			return "Off";
		}
		else
		{
			return (this.mValue == 1) ? "1 minute"
				: String.valueOf((this.mValue-1)*5)+" minutes";
		}
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index, 4);
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
		values[0] = "Off";
		values[1] = "1";

		for (int i=2; i < length; i++)
		{
			values[i] = String.valueOf((i-1)*5);
		}

		this.mPicker.setMinValue(min);
		this.mPicker.setMaxValue(max);
		this.mPicker.setDisplayedValues(values);
		this.mPicker.setValue(this.mValue);
		this.mPicker.setWrapSelectorWheel(false);
	}

}
