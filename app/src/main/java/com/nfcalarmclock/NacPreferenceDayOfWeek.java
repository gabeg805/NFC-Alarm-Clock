package com.nfcalarmclock;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.Preference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

public class NacPreferenceDayOfWeek
	extends Preference
	implements View.OnClickListener
{

	/**
	 * @brief Day of week buttons.
	 */
	private DayOfWeekButtons mDays = null;

	/**
	 * @brief Value of days.
	 */
	private int mValue = 0;

	/**
	 */
	public NacPreferenceDayOfWeek(Context context)
	{
		this(context, null);
	}

	/**
	 */
	public NacPreferenceDayOfWeek(Context context, AttributeSet attrs)
	{
		this(context, attrs, 0);
	}

	/**
	 */
	public NacPreferenceDayOfWeek(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
	}

	/**
	 */
	@Override
	protected View onCreateView(ViewGroup parent)
	{
		super.onCreateView(parent);

		Context context = getContext();
		LayoutInflater inflater = (LayoutInflater) context.getSystemService(
			Context.LAYOUT_INFLATER_SERVICE);

		return inflater.inflate(R.layout.pref_dayofweek, parent, false);
	}

	/**
	 */
	@Override
	protected void onBindView(View v)
	{
		super.onBindView(v);

		this.mDays = (DayOfWeekButtons) v.findViewById(R.id.widget);
		int color = NacUtility.getThemeAttrColor(getContext(),
			R.attr.colorCard);
		this.mDays.setButtonColor(color);

		this.mDays.init(this.mValue);
		this.mDays.setOnClickListener(this);
	}

	/**
	 */
	@Override
	protected Object onGetDefaultValue(TypedArray a, int index)
	{
		return (Integer) a.getInteger(index, 0);
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

	/**
	 */
	@Override
	public void onClick(View v)
	{
		int index = (int) v.getTag();

		this.mDays.toggleButton(index);

		this.mValue = this.mDays.getDays();

		persistInt(this.mValue);
	}

}
