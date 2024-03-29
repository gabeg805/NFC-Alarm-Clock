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
import com.nfcalarmclock.alarmoptions.NacAlarmOptionsDialog
import com.nfcalarmclock.alarmoptions.NacAlarmOptionsDialog.OnAlarmOptionClickedListener
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
import com.nfcalarmclock.name.NacNamePreference
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog
import com.nfcalarmclock.restrictvolume.NacRestrictVolumeDialog.OnRestrictVolumeListener
import com.nfcalarmclock.snoozeduration.NacSnoozeDurationPreference
import com.nfcalarmclock.tts.NacTextToSpeechDialog
import com.nfcalarmclock.tts.NacTextToSpeechDialog.OnTextToSpeechOptionsSelectedListener
import com.nfcalarmclock.upcomingreminder.NacUpcomingReminderDialog
import com.nfcalarmclock.util.NacIntent
import com.nfcalarmclock.view.dayofweek.NacDayOfWeekPreference
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
	 * Called when the media activity is finished and returns a result.
	 */
	override fun onActivityResult(result: ActivityResult)
	{
		// Check that the result was OK
		if (result.resultCode == Activity.RESULT_OK)
		{
			// Get the media info from the activity result data
			val mediaPath = NacIntent.getMediaPath(result.data)
			val shuffleMedia = NacIntent.getShuffleMedia(result.data)
			val recursivelyPlayMedia = NacIntent.getRecursivelyPlayMedia(result.data)

			// Save the media info for this preference
			mediaPreference!!.setAndPersistMediaPath(mediaPath)
			sharedPreferences!!.editShuffleMedia(shuffleMedia)
			sharedPreferences!!.editRecursivelyPlayMedia(recursivelyPlayMedia)
		}
	}

	/**
	 * Called when the preference is created.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.general_preferences)

		// Set the default values on this preference that are in the
		// android:defaultValue attribute
		PreferenceManager.setDefaultValues(requireContext(), R.xml.general_preferences,
			false)

		// Setup the media preference
		setupMediaPreference()

		// Setup the on click listeners
		setupAutoDismissOnClickListener()
		setupMaxSnoozeOnClickListener()
		setupSnoozeDurationOnClickListener()
		setupAlarmDaysOnClickListener()
		setupAlarmNameOnClickListener()
		setupAudioOptionsOnClickListener()
	}

	/**
	 * Setup the alarm days on click listener.
	 */
	private fun setupAlarmDaysOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.alarm_days_key)
		val pref = findPreference<NacDayOfWeekPreference>(key)

		// Setup the listener
		pref!!.onPreferenceClickListener  = Preference.OnPreferenceClickListener { p ->

			// Show the dialog
			(p as NacDayOfWeekPreference).showDialog(childFragmentManager)

			// Return
			true
		}
	}

	/**
	 * Setup the alarm name on click listener.
	 */
	private fun setupAlarmNameOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.alarm_name_key)
		val pref = findPreference<NacNamePreference>(key)

		// Setup the listener
		pref!!.onPreferenceClickListener  = Preference.OnPreferenceClickListener { p ->

			// Show the dialog
			(p as NacNamePreference).showDialog(childFragmentManager)

			// Return
			true
		}
	}

	/**
	 * Setup the audio options on click listener.
	 */
	private fun setupAudioOptionsOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.alarm_volume_key)
		val pref = findPreference<NacVolumePreference>(key)

		// Setup the listener
		pref!!.onAudioOptionsClickedListener = OnAudioOptionsClickedListener {

			// Show the dialog showing all the audio options
			showAlarmOptionsDialog()

		}
	}

	/**
	 * Setup the auto dismiss on click listener.
	 */
	private fun setupAutoDismissOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.auto_dismiss_key)
		val pref = findPreference<NacAutoDismissPreference>(key)

		// Setup the listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->

			// Show the dialog
			(p as NacAutoDismissPreference).showDialog(childFragmentManager)

			// Return
			true

		}
	}

	/**
	 * Setup the max snooze on click listener.
	 */
	private fun setupMaxSnoozeOnClickListener()
	{
		// Get the preference
		val key = resources.getString(R.string.max_snooze_key)
		val pref = findPreference<NacMaxSnoozePreference>(key)

		// Setup the listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->

			// Show the dialog
			(p as NacMaxSnoozePreference).showDialog(childFragmentManager)

			// Return
			true

		}
	}

	/**
	 * Setup the media preference.
	 */
	private fun setupMediaPreference()
	{
		// Get the preference
		val key = resources.getString(R.string.alarm_sound_key)
		val pref = findPreference<NacMediaPreference>(key)

		// Set the member variables
		mediaPreference = pref
		activityLauncher = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult(), this)

		// Setup the on click listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {

			// Create the intent
			val intent = NacMediaActivity.getStartIntentWithMedia(
				context,
				sharedPreferences!!.mediaPath,
				sharedPreferences!!.shuffleMedia,
				sharedPreferences!!.recursivelyPlayMedia)

			// Launch the intent
			activityLauncher!!.launch(intent)

			// Return
			true

		}
	}

	/**
	 * Setup the snooze duration on click listener.
	 */
	private fun setupSnoozeDurationOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.snooze_duration_key)
		val pref = findPreference<NacSnoozeDurationPreference>(key)

		// Setup the listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { p ->

			// Show the dialog
			(p as NacSnoozeDurationPreference).showDialog(childFragmentManager)

			// Return
			true

		}
	}

	/**
	 * Show the alarm options dialog.
	 */
	private fun showAlarmOptionsDialog()
	{
		// Create the dialog
		val dialog = NacAlarmOptionsDialog()

		// Set the listener for when the user is done
		dialog.onAlarmOptionClickedListener = OnAlarmOptionClickedListener { _, id ->

			// Show the corresponding alarm option dialog
			when (id)
			{
				R.id.alarm_option_audio_source -> showAudioSourceDialog()
				R.id.alarm_option_gradually_increase_volume -> showGraduallyIncreaseVolumeDialog()
				R.id.alarm_option_restrict_volume -> showRestrictVolumeDialog()
				R.id.alarm_option_text_to_speech -> showTextToSpeechDialog()
				R.id.alarm_option_dismiss_early -> showDismissEarlyDialog()
				R.id.alarm_option_upcoming_reminder -> showUpcomingReminderDialog()
				else -> {}
			}

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacAlarmOptionsDialog.TAG)
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
		dialog.setDefaultIndexFromDismissEarlyTime(sharedPreferences!!.dismissEarlyTime)

		// Set the listener for when the user is done
		dialog.onDismissEarlyOptionSelectedListener = OnDismissEarlyOptionSelectedListener { useDismissEarly, _, time ->

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
		dialog.setDefaultIndexFromWaitTime(sharedPreferences!!.graduallyIncreaseVolumeWaitTime)

		// Set the listener for when the user is done
		dialog.onGraduallyIncreaseVolumeListener = OnGraduallyIncreaseVolumeListener { shouldIncrease, _, waitTime ->

			// Save the setting for gradually increasing volume
			sharedPreferences!!.editShouldGraduallyIncreaseVolume(shouldIncrease)
			sharedPreferences!!.editGraduallyIncreaseVolumeWaitTime(waitTime)

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
		dialog.defaultSayCurrentTime = sharedPreferences!!.shouldSayCurrentTime
		dialog.defaultSayAlarmName = sharedPreferences!!.shouldSayAlarmName
		dialog.defaultTtsFrequency = sharedPreferences!!.speakFrequency

		// Set the listener for when the user is done
		dialog.onTextToSpeechOptionsSelectedListener = OnTextToSpeechOptionsSelectedListener { shouldSayCurrentTime, shouldSayAlarmName, ttsFreq ->

			// Save the text to speech settings
			sharedPreferences!!.editShouldSayCurrentTime(shouldSayCurrentTime)
			sharedPreferences!!.editShouldSayAlarmName(shouldSayAlarmName)
			sharedPreferences!!.editSpeakFrequency(ttsFreq)

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacTextToSpeechDialog.TAG)
	}

	/**
	 * Show the upcoming reminder dialog.
	 */
	private fun showUpcomingReminderDialog()
	{
		// Create the dialog
		val dialog = NacUpcomingReminderDialog()

		// Set the default values
		dialog.defaultShouldShowReminder = sharedPreferences!!.shouldShowReminder
		dialog.setDefaultIndexFromTime(sharedPreferences!!.timeToShowReminder)
		dialog.defaultReminderFrequencyIndex = sharedPreferences!!.reminderFrequency
		dialog.defaultShouldUseTts = sharedPreferences!!.shouldUseTtsForReminder
		dialog.canShowTts = sharedPreferences!!.shouldUseTts

		// Setup the listener
		dialog.onUpcomingReminderOptionSelectedListener = NacUpcomingReminderDialog.OnUpcomingReminderOptionSelectedListener { shouldShowReminder, timeToShow, reminderFreq, shouldUseTts ->

			// Save the upcoming reminder options
			sharedPreferences!!.editShouldShowReminder(shouldShowReminder)
			sharedPreferences!!.editTimeToShowReminder(timeToShow)
			sharedPreferences!!.editReminderFrequency(reminderFreq)
			sharedPreferences!!.editShouldUseTtsForReminder(shouldUseTts)

		}

		// Show the dialog
		dialog.show(childFragmentManager, NacUpcomingReminderDialog.TAG)
	}

}