package com.nfcalarmclock.settings.preference

import android.R
import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Preference category.
 */
class NacPreferenceCategory
	: PreferenceCategory
{

	/**
	 * Shared preferences.
	 */
	private var sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Constructor.
	 */
	constructor(context: Context) : super(context)
	{
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context, attrs: AttributeSet?) : super(context, attrs)
	{
	}

	/**
	 * Constructor.
	 */
	constructor(context: Context, attrs: AttributeSet?, style: Int) : super(context, attrs, style)
	{
	}

	/**
	 * Constructor.
	 */
	init
	{
		isIconSpaceReserved = false
	}

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Get the title text view
		val title = holder.findViewById(R.id.title) as TextView

		// Set the title text color
		title.setTextColor(sharedPreferences.themeColor)
	}
}