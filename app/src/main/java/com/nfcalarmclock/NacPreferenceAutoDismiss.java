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
	 * @return The auto dismiss value in string form.
	 */
	public String indexToString(int index)
	{
		int min = this.getMinValue();

		if (index == min)
		{
			return "Off";
		}
		else if ((index > min) && (index < 5))
		{
			return String.valueOf(index);
		}
		else
		{
			return String.valueOf((index-4)*5);
		}
	}

	/**
	 * @return The time units for the summary.
	 */
	public String indexToUnit(int index)
	{
		int min = this.getMinValue();

		if (index == min)
		{
			return "";
		}
		else if (index == 1)
		{
			return " minute";
		}
		else
		{
			return " minutes";
		}
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
		return 16;
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
		String dismiss = this.indexToString(this.mValue);
		String unit = this.indexToUnit(this.mValue);

		return dismiss + unit;
	}

	/**
	 * @return The default value.
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index,
			NacSharedPreferences.DEFAULT_AUTO_DISMISS_INDEX);
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

		for (int i=0; i < length; i++)
		{
			values[i] = this.indexToString(i);
		}

		this.mPicker.setMinValue(min);
		this.mPicker.setMaxValue(max);
		this.mPicker.setDisplayedValues(values);
		this.mPicker.setValue(this.mValue);
		this.mPicker.setWrapSelectorWheel(false);
	}

}
