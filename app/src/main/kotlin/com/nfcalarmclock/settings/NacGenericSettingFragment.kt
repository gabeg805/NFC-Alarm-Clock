package com.nfcalarmclock.settings

import android.content.Context
import androidx.preference.PreferenceFragmentCompat
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Settings fragment.
 */
abstract class NacGenericSettingFragment
	: PreferenceFragmentCompat()
{

	/**
	 * Shared preference store.
	 */
	protected var sharedPreferences: NacSharedPreferences? = null

	/**
	 * Called when the fragment is attached.
	 */
	override fun onAttach(context: Context)
	{
		// Super
		super.onAttach(context)

		// Set the shared preferences
		sharedPreferences = NacSharedPreferences(context)
	}

}