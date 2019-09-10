package com.nfcalarmclock;

import android.content.Context;
//import android.preference.PreferenceCategory;
import android.util.AttributeSet;
import android.view.View;
import android.widget.TextView;

import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

/**
 * Preference category.
 */
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
	public void onBindViewHolder(PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		Context context = getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		TextView title = (TextView) holder.findViewById(android.R.id.title);

		title.setTextColor(shared.getThemeColor());
	}

}
