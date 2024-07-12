package com.nfcalarmclock.settings

import android.content.Context
import android.content.SharedPreferences
import android.content.SharedPreferences.OnSharedPreferenceChangeListener
import androidx.preference.PreferenceFragmentCompat
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Settings fragment.
 */
abstract class NacGenericSettingFragment
	: PreferenceFragmentCompat(),
	OnSharedPreferenceChangeListener
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

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Setuper
		super.onResume()

		// Register listener when preferences are changed
		preferenceScreen.sharedPreferences?.registerOnSharedPreferenceChangeListener(
			this)
	}

	/**
	 * Called when the fragment is paused.
	 */
	override fun onPause()
	{
		// Super
		super.onPause()

		// Unregister listener when preferences are changed.
		preferenceScreen.sharedPreferences?.unregisterOnSharedPreferenceChangeListener(
			this)
	}

	/**
	 * Called when the shared preference is changed.
	 *
	 * TODO: Can this be removed?
	 */
	override fun onSharedPreferenceChanged(sharedPreferences: SharedPreferences?, key: String?)
	{
	}

}