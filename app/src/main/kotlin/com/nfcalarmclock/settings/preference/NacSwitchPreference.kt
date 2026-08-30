package com.nfcalarmclock.settings.preference

import android.content.Context
import android.util.AttributeSet
import androidx.appcompat.widget.SwitchCompat
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.setupSwitchColor

/**
 * Preference with a switch button.
 *
 * @param context Context.
 * @param attrs Attribute set.
 */
class NacSwitchPreference(
	context: Context,
	attrs: AttributeSet?
) : NacCompoundButtonPreference(context, attrs, R.layout.nac_preference_switch)
{

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Create the shared preferences
		val shared = NacSharedPreferences(context)

		// Setup the color
		(compoundButton as SwitchCompat).setupSwitchColor(shared)
	}

}