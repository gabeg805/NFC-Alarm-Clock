package com.nfcalarmclock;

import android.content.Context;
import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

public class NacPreferenceCategory
	extends PreferenceCategory
{

	/**
	 */
	public NacPreferenceCategory(Context context)
	{
		super(context);
	}

	/**
	 */
	public NacPreferenceCategory(Context context, AttributeSet attrs)
	{
		super(context, attrs);
	}

	/**
	 */
	public NacPreferenceCategory(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
	}

	/**
	 */
	@Override
	protected void onBindView(View view)
	{
		super.onBindView(view);

		Context context = view.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		TextView title = (TextView) view.findViewById(android.R.id.title);

		title.setTextColor(shared.themeColor);
	}

}
