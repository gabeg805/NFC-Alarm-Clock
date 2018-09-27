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
	implements NacDayOfWeek.OnClickListener
{

	/**
	 * @brief Day of week buttons.
	 */
	private NacDayOfWeek mDayOfWeek = null;

	/**
	 * @brief Value of days.
	 */
	private int mValue;

	/**
	 */
	public NacPreferenceDayOfWeek(Context context)
	{
		super(context, null);
	}

	/**
	 */
	public NacPreferenceDayOfWeek(Context context, AttributeSet attrs)
	{
		super(context, attrs, 0);
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

		this.mDayOfWeek = (NacDayOfWeek) v.findViewById(R.id.widget);
		//int color = NacUtility.getThemeAttrColor(getContext(),
		//	R.attr.colorCard);
		//this.mDays.setButtonColor(color);

		this.mDayOfWeek.setDays(this.mValue);
		this.mDayOfWeek.setOnClickListener(this);
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
	public void onClick(NacDayButton button, int index)
	{
		button.animateToggle();
		this.mValue = this.mDayOfWeek.getDays();
		persistInt(this.mValue);
	}

}
