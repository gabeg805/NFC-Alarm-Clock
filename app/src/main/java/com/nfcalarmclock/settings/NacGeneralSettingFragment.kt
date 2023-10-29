package com.nfcalarmclock.settings

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.result.ActivityResult
import androidx.activity.result.ActivityResultCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.audiooptions.NacAlarmAudioOptionsDialog
import com.nfcalarmclock.audiooptions.NacAlarmAudioOptionsDialog.OnAudioOptionClickedListener
import com.nfcalarmclock.audiosource.NacAudioSourceDialog
import com.nfcalarmclock.audiosource.NacAudioSourceDialog.OnAudioSourceSelectedListener
import com.nfcalarmclock.autodismiss.NacAutoDismissPreference
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog
import com.nfcalarmclock.graduallyincreasevolume.NacGraduallyIncreaseVolumeDialog.OnGraduallyIncreaseVolumeListener
import com.nfcalarmclock.maxsnooze.NacMaxSnoozePreference
import com.nfcalarmclock.mediapicker.NacMediaActivity
import com.nfcalarmclock.mediapicker.NacMediaPreference
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog.OnRestrictVolumeListener
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.snoozeduration.NacSnoozeDurationPreference
import com.nfcalarmclock.tts.NacTextToSpeechDialog
import com.nfcalarmclock.tts.NacTextToSpeechDialog.OnTextToSpeechOptionsSelectedListener
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.volume.NacVolumePreference
import com.nfcalarmclock.volume.NacVolumePreference.OnAudioOptionsClickedListener

/**
 * General settings fragment.
 */
class NacGeneralSettingFragment
	: NacGenericSettingFragment(),
	ActivityResultCallback<ActivityResult>
{

	/**
	 * Activity result launcher, used to get results from a finished activity.
	 */
	private var activityLauncher: ActivityResultLauncher<Intent>? = null

	/**
	 * The sound preference.
	 */
	private var mediaPreference: NacMediaPreference? = null

	/**
	 * Called when the NacMediaActivity is finished is returns a result.
	 */
	override fun onActivityResult(result: ActivityResult)
	{
		// Check that the result was OK
		if (result.resultCode == Activity.RESULT_OK)
		{
			// Get the media from the activity result data
			val media = NacIntent.getMedia(result.data)

			// Set the media for this preference
			mediaPreference!!.setMedia(media)
		}
	}

	/**
	 * Called when the preference is created.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.general_preferences)

		// Set the default values on this preference
		// TODO: What does this do? Is it needed?
		PreferenceManager.setDefaultValues(requireContext(), R.xml.general_preferences,
			false)

		// Get each preference that will be used in this fragment
		val autoDismissPref = findPreference<NacAutoDismissPreference>(sharedKeys!!.autoDismiss)
		val maxSnoozePref = findPreference<NacMaxSnoozePreference>(sharedKeys!!.maxSnooze)
		val snoozeDurationPref = findPreference<NacSnoozeDurationPreference>(sharedKeys!!.snoozeDuration)
		val volumePref = findPreference<NacVolumePreference>(sharedKeys!!.volume)
		val mediaPref = findPreference<NacMediaPreference>(sharedKeys!!.mediaPath)

		// Set the member variables
		mediaPreference = mediaPref
		activityLauncher = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult(), this)

		// Create a listener for auto dismiss
		autoDismissPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { pref ->

			// Show the dialog
			(pref as NacAutoDismissPreference).showDialog(childFragmentManager)

			// Return
			true

		}

		// Create a listener for max snooze
		maxSnoozePref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { pref ->

			// Show the dialog
			(pref as NacMaxSnoozePreference).showDialog(childFragmentManager)

			// Return
			true

		}

		// Create a listener for snooze duration
		snoozeDurationPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { pref ->

			// Show the dialog
			(pref as NacSnoozeDurationPreference).showDialog(childFragmentManager)

			// Return
			true

		}

		// Set the listener for volume
		volumePref!!.onAudioOptionsClickedListener = OnAudioOptionsClickedListener {

			// Show the dialog showing all the audio options
			showAudioOptionsDialog()

		}

		// Create a listener for media preference
		mediaPref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {

			// Create the intent
			val intent = NacIntent.toIntent(context, NacMediaActivity::class.java,
				sharedPreferences!!.mediaPath)

			// Launch the intent
			activityLauncher!!.launch(intent)

			// Return
			true

		}
	}

	/**
	 * Show the audio options dialog.
	 */
	private fun showAudioOptionsDialog()
	{
		// Create the dialog
		val dialog = NacAlarmAudioOptionsDialog()

		// Set the listener for when the user is done
		dialog.onAudioOptionClickedListener = OnAudioOptionClickedListener { _, which ->

			// Show the corresponding audio option dialog
			when (which)
			{
				0 -> showAudioSourceDialog()
				1 -> showDismissEarlyDialog()
				2 -> showGraduallyIncreaseVolumeDialog()
				3 -> showRestrictVolumeDialog()
				4 -> showTextToSpeechDialog()
				else ->
				{
				}
			}

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacAlarmAudioOptionsDialog.TAG)
	}

	/**
	 * Show the audio source dialog.
	 */
	private fun showAudioSourceDialog()
	{
		// Create the dialog
		val dialog = NacAudioSourceDialog()

		// Set the default setting
		dialog.defaultAudioSource = sharedPreferences!!.audioSource

		// Set the listener for when the user is done
		dialog.onAudioSourceSelectedListener = OnAudioSourceSelectedListener { audioSource ->

			// Save the audio source that was selected
			sharedPreferences!!.editAudioSource(audioSource)

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacAudioSourceDialog.TAG)
	}

	/**
	 * Show the dismiss early dialog.
	 */
	private fun showDismissEarlyDialog()
	{
		// Create the dialog
		val dialog = NacDismissEarlyDialog()

		// Set the default settings
		dialog.defaultShouldDismissEarly = sharedPreferences!!.useDismissEarly
		dialog.defaultShouldDismissEarlyIndex = sharedPreferences!!.dismissEarlyIndex

		// Set the listener for when the user is done
		dialog.onDismissEarlyOptionSelectedListener = OnDismissEarlyOptionSelectedListener { useDismissEarly, index ->

			// Convert the dismiss early index to a time value
			val time = NacSharedPreferences.getDismissEarlyIndexToTime(index)

			// Save the settings that were selected for dismiss early
			sharedPreferences!!.editUseDismissEarly(useDismissEarly)
			sharedPreferences!!.editDismissEarlyTime(time)

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacGraduallyIncreaseVolumeDialog.TAG)
	}

	/**
	 * Show the gradually increase volume dialog.
	 */
	private fun showGraduallyIncreaseVolumeDialog()
	{
		// Create the dialog
		val dialog = NacGraduallyIncreaseVolumeDialog()

		// Set the default setting
		dialog.defaultShouldGraduallyIncreaseVolume = sharedPreferences!!.shouldGraduallyIncreaseVolume

		// Set the listener for when the user is done
		dialog.onGraduallyIncreaseVolumeListener = OnGraduallyIncreaseVolumeListener { shouldIncrease ->

			// Save the setting for gradually increasing volume
			sharedPreferences!!.editShouldGraduallyIncreaseVolume(shouldIncrease)

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacGraduallyIncreaseVolumeDialog.TAG)
	}

	/**
	 * Show the restrict volume dialog.
	 */
	private fun showRestrictVolumeDialog()
	{
		// Create the dialog
		val dialog = NacRestrictVolumeDialog()

		// Set the default setting
		dialog.defaultShouldRestrictVolume = sharedPreferences!!.shouldRestrictVolume

		// Set the listener for when the user is done
		dialog.onRestrictVolumeListener = OnRestrictVolumeListener { shouldRestrict ->

			// Save the setting for restricting volume
			sharedPreferences!!.editShouldRestrictVolume(shouldRestrict)

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacRestrictVolumeDialog.TAG)
	}

	/**
	 * Show the text-to-speech dialog.
	 */
	private fun showTextToSpeechDialog()
	{
		// Create the dialog
		val dialog = NacTextToSpeechDialog()

		// Set the default settings
		dialog.defaultUseTts = sharedPreferences!!.speakToMe
		dialog.defaultTtsFrequency = sharedPreferences!!.speakFrequency

		// Set the listener for when the user is done
		dialog.onTextToSpeechOptionsSelectedListener = OnTextToSpeechOptionsSelectedListener { useTts, freq ->

			// Save the text to speech settings
			sharedPreferences!!.editSpeakToMe(useTts)
			sharedPreferences!!.editSpeakFrequency(freq)

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacTextToSpeechDialog.TAG)
	}

}