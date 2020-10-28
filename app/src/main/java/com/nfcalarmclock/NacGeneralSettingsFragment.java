package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

/**
 * General settings fragment.
 */
public class NacGeneralSettingsFragment
	extends NacSettingsFragment
	implements Preference.OnPreferenceClickListener
{

	/**
	 * Activity request code.
	 */
	private static final int MEDIA_REQUEST_CODE = 222;

	/**
	 * The sound preference.
	 */
	private NacMediaPreference mMediaPreference;

	/**
	 * @return The media preference.
	 */
	private NacMediaPreference getMediaPreference()
	{
		return this.mMediaPreference;
	}

	/**
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode, Intent data)
	{
		super.onActivityResult(requestCode, resultCode, data);

		if (requestCode != MEDIA_REQUEST_CODE)
		{
			return;
		}

		String media = NacIntent.getMedia(data);
		this.setPreferenceMedia(media);
	}

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		Context context = getContext();

		addPreferencesFromResource(R.xml.general_preferences);
		PreferenceManager.setDefaultValues(context, R.xml.general_preferences, false);

		NacSharedKeys keys = this.getSharedPreferences().getKeys();
		NacMediaPreference mediaPreference = findPreference(keys.getMediaPath());
		this.mMediaPreference = mediaPreference;

		mediaPreference.setOnPreferenceClickListener(this);
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		Context context = getContext();
		String media = this.getSharedPreferences().getMediaPath();
		Intent intent = NacIntent.toIntent(context, NacMediaActivity.class,
			media);

		startActivityForResult(intent, MEDIA_REQUEST_CODE);
		return true;
	}

	/**
	 * Set the media to be used in the media preference.
	 */
	public void setPreferenceMedia(String media)
	{
		this.getMediaPreference().setMedia(media);
	}

}
