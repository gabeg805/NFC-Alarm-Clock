package com.nfcalarmclock;

//import android.app.FragmentManager;
//import android.support.v4.app.FragmentManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
//import android.preference.Preference;
//import android.preference.PreferenceManager;

import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

/**
 * Default alarm fragment.
 */
public class NacDefaultAlarmSettings
	extends NacSettingsFragment
	implements Preference.OnPreferenceClickListener
{

	/**
	 * Activity request code.
	 */
	private static final int REQUEST_CODE = 69;

	/**
	 * The sound preference.
	 */
	private NacSoundPreference mSound;

	/**
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode != REQUEST_CODE)
		{
			return;
		}

		NacSound sound = NacIntent.getSound(data);

		this.mSound.setSound(sound);
	}

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		//super.onCreatePreferences(savedInstanceState, rootKey);
		addPreferencesFromResource(R.xml.default_alarm_preferences);
		PreferenceManager.setDefaultValues(getContext(),
			R.xml.default_alarm_preferences, false);

		NacSharedKeys keys = this.getSharedPreferences().getKeys();
		this.mSound = (NacSoundPreference) findPreference(keys.getSound());

		this.mSound.setOnPreferenceClickListener(this);
	}

	/**
	 * When the preference is clicked, display the dialog.
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		Context context = getContext();
		String path = this.getSharedPreferences().getSound();
		NacSound sound = new NacSound(context, path);
		Intent intent = NacIntent.toIntent(context, NacMediaActivity.class,
			sound);

		startActivityForResult(intent, REQUEST_CODE);

		return true;
	}

}
