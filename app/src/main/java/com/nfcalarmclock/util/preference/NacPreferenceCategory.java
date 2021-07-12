package com.nfcalarmclock.util.preference;

import android.content.Context;
import android.util.AttributeSet;
import android.widget.TextView;
import androidx.preference.PreferenceCategory;
import androidx.preference.PreferenceViewHolder;

import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Preference category.
 */
public class NacPreferenceCategory
	extends PreferenceCategory
{

	/**
	 * Shared preferences.
	 */
	private NacSharedPreferences mShared;

	/**
	 */
	public NacPreferenceCategory(Context context)
	{
		super(context);
		this.init();
	}

	/**
	 */
	public NacPreferenceCategory(Context context, AttributeSet attrs)
	{
		super(context, attrs);
		this.init();
	}

	/**
	 */
	public NacPreferenceCategory(Context context, AttributeSet attrs, int style)
	{
		super(context, attrs, style);
		this.init();
	}

	/**
	 * @return The shared preferences.
	 */
	private NacSharedPreferences getShared()
	{
		return this.mShared;
	}

	/**
	 * Initialize the preference category.
	 */
	private void init()
	{
		Context context = getContext();
		this.mShared = new NacSharedPreferences(context);

		setIconSpaceReserved(false);
	}

	/**
	 */
	@Override
	public void onBindViewHolder(PreferenceViewHolder holder)
	{
		super.onBindViewHolder(holder);

		TextView title = (TextView) holder.findViewById(android.R.id.title);

		title.setTextColor(this.getShared().getThemeColor());
	}

}
