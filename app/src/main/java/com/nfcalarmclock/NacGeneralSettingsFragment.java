package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
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
		ActivityResultCallback<ActivityResult>,
		NacVolumePreference.OnAudioOptionsClickedListener,
		NacAlarmAudioOptionsDialog.OnAudioOptionClickedListener,
		NacAlarmAudioSourceDialog.OnAudioSourceSelectedListener,
		NacAlarmTextToSpeechDialog.OnTextToSpeechOptionsSelectedListener
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
	 * Called when an item in the audio options dialog is clicked.
	 */
	@Override
	public void onAudioOptionClicked(long alarmId, int which)
	{
		switch (which)
		{
			case 0:
				this.showAudioSourceDialog();
				break;
			case 1:
				this.showTextToSpeechDialog();
				break;
			default:
				return;
		}
	}

	/**
	 */
	@Override
	public void onAudioOptionsClicked()
	{
		this.showAudioOptionsDialog();
	}

	/**
	 * Called when an audio source is selected.
	 */
	@Override
	public void onAudioSourceSelected(String audioSource)
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		shared.editAudioSource(audioSource);
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
		NacVolumePreference volumePreference = (NacVolumePreference)
			findPreference(keys.getVolume());
		this.mMediaPreference = findPreference(keys.getMediaPath());
		this.mActivityLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), this);

		volumePreference.setOnAudioOptionsClickedListener(this);
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
	 * Called when a text-to-speech option is selected.
	 */
	@Override
	public void onTextToSpeechOptionsSelected(boolean useTts, int freq)
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		shared.editSpeakToMe(useTts);
		shared.editSpeakFrequency(freq);
	}

	/**
	 * Set the media to be used in the media preference.
	 */
	public void setPreferenceMedia(String media)
	{
		this.getMediaPreference().setMedia(media);
	}

	/**
	 * Show the audio options dialog.
	 */
	public void showAudioOptionsDialog()
	{
		NacAlarmAudioOptionsDialog dialog = new NacAlarmAudioOptionsDialog();

		dialog.setOnAudioOptionClickedListener(this);
		dialog.show(getChildFragmentManager(), NacAlarmAudioOptionsDialog.TAG);
	}

	/**
	 * Show the audio source dialog.
	 */
	public void showAudioSourceDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		String audioSource = shared.getAudioSource();
		NacAlarmAudioSourceDialog dialog = new NacAlarmAudioSourceDialog();

		dialog.setDefaultAudioSource(audioSource);
		dialog.setOnAudioSourceSelectedListener(this);
		dialog.show(getChildFragmentManager(), NacAlarmAudioSourceDialog.TAG);
	}

	/**
	 * Show the text-to-speech dialog.
	 */
	public void showTextToSpeechDialog()
	{
		NacSharedPreferences shared = this.getSharedPreferences();
		boolean useTts = shared.getSpeakToMe();
		int freq = shared.getSpeakFrequency();
		NacAlarmTextToSpeechDialog dialog = new NacAlarmTextToSpeechDialog();

		dialog.setDefaultUseTts(useTts);
		dialog.setDefaultTtsFrequency(freq);
		dialog.setOnTextToSpeechOptionsSelectedListener(this);
		dialog.show(getChildFragmentManager(), NacAlarmTextToSpeechDialog.TAG);
	}

}
