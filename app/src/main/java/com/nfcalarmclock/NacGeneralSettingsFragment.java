package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContract;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

/**
 * General settings fragment.
 */
public class NacGeneralSettingsFragment
	extends NacSettingsFragment
	implements Preference.OnPreferenceClickListener,
		ActivityResultCallback<ActivityResult>
{

	/**
	 * Activity result launcher, used to get results from a finished activity.
	 */
	private ActivityResultLauncher<Intent> mActivityLauncher;

	/**
	 * The sound preference.
	 */
	private NacMediaPreference mMediaPreference;

	/**
	 * @return The activity result launcher.
	 */
	private ActivityResultLauncher<Intent> getActivityLauncher()
	{
		return this.mActivityLauncher;
	}

	/**
	 * @return The media preference.
	 */
	private NacMediaPreference getMediaPreference()
	{
		return this.mMediaPreference;
	}

	/**
	 * Called when the NacMediaActivity is finished is returns a result.
	 */
	@Override
	public void onActivityResult(ActivityResult result)
	{
		Intent data = result.getData();
		int code = result.getResultCode();

		if (code == Activity.RESULT_OK)
		{
			String media = NacIntent.getMedia(data);
			this.setPreferenceMedia(media);
		}

	}

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.general_preferences);
		PreferenceManager.setDefaultValues(getContext(), R.xml.general_preferences,
			false);

		NacSharedKeys keys = this.getSharedKeys();
		String path = keys.getMediaPath();
		this.mMediaPreference = findPreference(path);
		this.mActivityLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), this);

		this.getMediaPreference().setOnPreferenceClickListener(this);
	}

	/**
	 */
	@Override
	public boolean onPreferenceClick(Preference preference)
	{
		Context context = getContext();
		NacSharedPreferences shared = this.getSharedPreferences();
		String media = shared.getMediaPath();
		Intent intent = NacIntent.toIntent(context, NacMediaActivity.class, media);

		this.getActivityLauncher().launch(intent);
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
