package com.nfcalarmclock.settings

import android.content.Context
import androidx.preference.PreferenceFragmentCompat
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Base settings fragment.
 */
abstract class NacBaseSettingFragment
	: PreferenceFragmentCompat()
{

	/**
	 * Shared preference store.
	 */
	protected var sharedPreferences: NacSharedPreferences? = null

	/**
	 * Fragment is attached.
	 */
	override fun onAttach(context: Context)
	{
		// Super
		super.onAttach(context)

		// Set the shared preferences
		sharedPreferences = NacSharedPreferences(context)
	}

}