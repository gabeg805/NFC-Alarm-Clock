package com.nfcalarmclock;

import android.content.Context;
import android.content.SharedPreferences;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceFragmentCompat;

/**
 * Settings fragment.
 */
public abstract class NacSettingsFragment
	extends PreferenceFragmentCompat
	implements SharedPreferences.OnSharedPreferenceChangeListener
{

	/**
	 * Application context.
	 */
	private Context mContext;

	/**
	 * Shared preference store.
	 */
	private NacSharedPreferences mShared;

	/**
	 * @return The context.
	 */
	public Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The shared preference keys.
	 */
	protected NacSharedKeys getKeys()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		return (shared != null) ? shared.getKeys() : null;
	}

	/**
	 * @return The shared preferences object.
	 */
	protected NacSharedPreferences getSharedPreferences()
	{
		return this.mShared;
	}

	/**
	 */
	@Override
	public void onAttach(Context context)
	{
		super.onAttach(context);

		this.mContext = context;
		this.mShared = new NacSharedPreferences(context);
	}

	/**
	 */
	@Override
	public void onResume()
	{
		super.onResume();

		getPreferenceScreen().getSharedPreferences()
			.registerOnSharedPreferenceChangeListener(this);
	}

	/**
	 */
	@Override
	public void onPause()
	{
		super.onPause();

		getPreferenceScreen().getSharedPreferences()
			.unregisterOnSharedPreferenceChangeListener(this);
	}

	/**
	 */
	@Override
	public void onSharedPreferenceChanged(
		SharedPreferences sharedPreferences, String preferenceKey)
	{
	}

}
