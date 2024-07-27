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
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.mediapicker.NacMediaActivity
import com.nfcalarmclock.mediapicker.NacMediaPreference
import com.nfcalarmclock.name.NacNamePreference
import com.nfcalarmclock.util.NacBundle
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
			sharedPreferences!!.shouldShuffleMedia = shuffleMedia
			sharedPreferences!!.recursivelyPlayMedia = recursivelyPlayMedia
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
		setupAlarmDaysOnClickListener()
		setupAlarmNameOnClickListener()
		setupAlarmOptionsOnClickListener()
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
	 * Setup the alarm options on click listener.
	 */
	private fun setupAlarmOptionsOnClickListener()
	{
		// Get the preference
		val key = getString(R.string.alarm_volume_key)
		val pref = findPreference<NacVolumePreference>(key)

		// Setup the listener
		pref!!.onAudioOptionsClickedListener = OnAudioOptionsClickedListener {

			// Create an alarm from shared preferences defaults
			val alarm = NacAlarm.build(sharedPreferences)
			val bundle = NacBundle.alarmToBundle(alarm)

			// Set the graph of the nav controller
			val navController = (activity as NacMainSettingActivity).navController

			navController.setGraph(R.navigation.nav_alarm_options, bundle)

			// Check if the nav controller did not navigate to the destination
			if (navController.currentDestination == null)
			{
				// Navigate to the destination manually
				navController.navigate(R.id.nacAlarmOptionsDialog, bundle)
			}

			// Setup an observe to watch for any changes to the alarm
			navController.currentBackStackEntry
				?.savedStateHandle
				?.getLiveData<NacAlarm>("YOYOYO")
				?.observe(this) { alarm ->

					// Check which destination this alarm update came from
					when (navController.currentDestination?.id)
					{

						// Audio source
						R.id.nacAudioSourceDialog -> {
							sharedPreferences!!.audioSource = alarm.audioSource
						}

						// Flashlight
						R.id.nacFlashlightOptionsDialog -> {
							sharedPreferences!!.flashlightStrengthLevel = alarm.flashlightStrengthLevel
							sharedPreferences!!.flashlightOnDuration = alarm.flashlightOnDuration
							sharedPreferences!!.flashlightOffDuration = alarm.flashlightOffDuration
						}

						// Gradually increase volume
						R.id.nacGraduallyIncreaseVolumeDialog -> {
							sharedPreferences!!.shouldGraduallyIncreaseVolume = alarm.shouldGraduallyIncreaseVolume
							sharedPreferences!!.graduallyIncreaseVolumeWaitTime = alarm.graduallyIncreaseVolumeWaitTime
						}

						// Restrict volume
						R.id.nacRestrictVolumeDialog -> {
							sharedPreferences!!.shouldRestrictVolume = alarm.shouldRestrictVolume
						}

						// Text-to-speech
						R.id.nacTextToSpeechDialog -> {
							sharedPreferences!!.shouldSayCurrentTime = alarm.sayCurrentTime
							sharedPreferences!!.shouldSayAlarmName = alarm.sayAlarmName
							sharedPreferences!!.ttsFrequency = alarm.ttsFrequency
						}

						// Dismiss options
						R.id.nacDismissOptionsDialog -> {
							sharedPreferences!!.autoDismissTime = alarm.autoDismissTime
							sharedPreferences!!.canDismissEarly = alarm.useDismissEarly
							sharedPreferences!!.dismissEarlyTime = alarm.dismissEarlyTime
						}

						// Snooze options
						R.id.nacSnoozeOptionsDialog -> {
							sharedPreferences!!.maxSnooze = alarm.maxSnooze
							sharedPreferences!!.snoozeDuration = alarm.snoozeDuration
							sharedPreferences!!.easySnooze = alarm.useEasySnooze
						}

						// Upcoming reminder
						R.id.nacUpcomingReminderDialog -> {
							sharedPreferences!!.shouldShowReminder = alarm.showReminder
							sharedPreferences!!.timeToShowReminder = alarm.timeToShowReminder
							sharedPreferences!!.reminderFrequency = alarm.reminderFrequency
							sharedPreferences!!.shouldUseTtsForReminder = alarm.shouldUseTtsForReminder
						}

						// Unknown
						else -> {}

					}

				}

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
				sharedPreferences!!.shouldShuffleMedia,
				sharedPreferences!!.recursivelyPlayMedia)

			// Launch the intent
			activityLauncher!!.launch(intent)

			// Return
			true

		}
	}

}