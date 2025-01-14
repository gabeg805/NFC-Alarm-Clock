package com.nfcalarmclock.settings

import android.app.Activity
import android.content.Intent
import android.os.Build
import android.os.Bundle
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.OptIn
import androidx.media3.common.util.UnstableApi
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.NacAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.alarm.options.mediapicker.NacMediaPickerActivity
import com.nfcalarmclock.alarm.options.name.NacNameDialog
import com.nfcalarmclock.alarm.options.snoozeoptions.NacSnoozeOptionsDialog
import com.nfcalarmclock.card.NacCardPreference
import com.nfcalarmclock.settings.preference.NacCheckboxPreference
import com.nfcalarmclock.util.addMediaInfo
import com.nfcalarmclock.util.getDeviceProtectedStorageContext
import com.nfcalarmclock.util.getMediaArtist
import com.nfcalarmclock.util.getMediaBundle
import com.nfcalarmclock.util.getMediaPath
import com.nfcalarmclock.util.getMediaTitle
import com.nfcalarmclock.util.getMediaType
import com.nfcalarmclock.util.getRecursivelyPlayMedia
import com.nfcalarmclock.util.getShuffleMedia
import com.nfcalarmclock.util.media.buildLocalMediaPath

/**
 * General settings fragment.
 */
class NacGeneralSettingFragment
	: NacGenericSettingFragment()
{

	/**
	 * Activity result launcher, used to get results from a finished activity.
	 */
	private var activityLauncher: ActivityResultLauncher<Intent>? = null

	/**
	 * Called when the preference is created.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		// Get the device protected storage context, if available
		val deviceContext = getDeviceProtectedStorageContext(requireContext())

		// Check if should set device protected storage as the storage location to use
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			preferenceManager.setStorageDeviceProtected()
		}

		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.general_preferences)

		// Set the default values on this preference that are in the
		// android:defaultValue attribute
		PreferenceManager.setDefaultValues(deviceContext, R.xml.general_preferences,  false)

		// Setup the preferences
		setupDefaultAlarm()
		setupAlarmScreen()
	}

	/**
	 * Setup the preferences for the alarm screen.
	 */
	private fun setupAlarmScreen()
	{
		// Get the new alarm screen preference
		val newScreenKey = getString(R.string.key_use_new_alarm_screen)
		val newScreenPref = findPreference<NacCheckboxPreference>(newScreenKey)!!

		// Setup the dependent alarm screen preferences
		setupDependentNewAlarmScreenPreferences(newScreenPref.isChecked)

		// Set the listener for when the new screen preference is changed
		newScreenPref.onPreferenceChangeListener = Preference.OnPreferenceChangeListener { _, status ->

			// Set the usability of the dependent preferences
			setupDependentNewAlarmScreenPreferences(status as Boolean)

			// Return
			true

		}
	}

	/**
	 * Setup the default alarm preference.
	 */
	@OptIn(UnstableApi::class)
	private fun setupDefaultAlarm()
	{
		// Get the preference
		val key = getString(R.string.key_default_alarm_card)
		val pref = findPreference<NacCardPreference>(key)!!

		// Define the activity launcher
		activityLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->

			// Check that the result was OK
			if (result.resultCode == Activity.RESULT_OK)
			{
				// Get the media bundle from the activity result data, and the media
				// path from the bundle
				val bundle = result.data?.getMediaBundle() ?: Bundle()
				val mediaPath = bundle.getMediaPath()

				// Save the media info for this preference
				sharedPreferences!!.mediaPath = mediaPath
				sharedPreferences!!.mediaArtist = bundle.getMediaArtist()
				sharedPreferences!!.mediaTitle = bundle.getMediaTitle()
				sharedPreferences!!.mediaType = bundle.getMediaType()
				sharedPreferences!!.localMediaPath = buildLocalMediaPath(
					requireContext(),
					sharedPreferences!!.mediaArtist,
					sharedPreferences!!.mediaTitle,
					sharedPreferences!!.mediaType)
				sharedPreferences!!.shouldShuffleMedia = bundle.getShuffleMedia()
				sharedPreferences!!.recursivelyPlayMedia = bundle.getRecursivelyPlayMedia()

				// Update the card
				pref.card.alarm!!.mediaPath = mediaPath
				pref.card.alarm!!.mediaArtist = sharedPreferences!!.mediaArtist
				pref.card.alarm!!.mediaTitle = sharedPreferences!!.mediaTitle
				pref.card.alarm!!.mediaType = sharedPreferences!!.mediaType
				pref.card.setMediaButton()
			}

		}

		// Media
		pref.onCardMediaClickedListener = NacCardPreference.OnCardMediaClickedListener { alarm ->

			// Create the intent and add the media to the intent
			val intent = Intent(context, NacMediaPickerActivity::class.java)
				.addMediaInfo(
					alarm.mediaPath,
					alarm.mediaArtist,
					alarm.mediaTitle,
					alarm.mediaType,
					alarm.shouldShuffleMedia,
					alarm.shouldRecursivelyPlayMedia)

			// Launch the intent
			activityLauncher!!.launch(intent)

		}

		// Name
		pref.onCardNameClickedListener = NacCardPreference.OnCardNameClickedListener { alarm ->

			// Show the name dialog
			NacNameDialog.create(
				alarm.name,
				onNameEnteredListener = {

					// Save the name and update the card
					sharedPreferences!!.name = it
					pref.card.setName(it)

				})
				.show(childFragmentManager, NacNameDialog.TAG)

		}

		// Dismiss options
		pref.onCardDismissOptionsClickedListener = NacCardPreference.OnCardDismissOptionsClickedListener { alarm ->

			// Show the dismiss options dialog
			NacDismissOptionsDialog.create(
				alarm,
				onSaveAlarmListener = { a ->

					// Save the changes
					sharedPreferences!!.shouldAutoDismiss = a.shouldAutoDismiss
					sharedPreferences!!.autoDismissTime = a.autoDismissTime
					sharedPreferences!!.canDismissEarly = a.useDismissEarly
					sharedPreferences!!.dismissEarlyTime = a.dismissEarlyTime
					sharedPreferences!!.shouldDeleteAlarmAfterDismissed = a.shouldDeleteAlarmAfterDismissed

				})
				.show(childFragmentManager, NacDismissOptionsDialog.TAG)

		}

		// Snooze options
		pref.onCardSnoozeOptionsClickedListener = NacCardPreference.OnCardSnoozeOptionsClickedListener { alarm ->

			// Show the snooze options dialog
			NacSnoozeOptionsDialog.create(
				alarm,
				onSaveAlarmListener = { a ->

					// Save the changes
					sharedPreferences!!.shouldAutoSnooze = a.shouldAutoSnooze
					sharedPreferences!!.autoSnoozeTime = a.autoSnoozeTime
					sharedPreferences!!.maxSnooze = a.maxSnooze
					sharedPreferences!!.snoozeDuration = a.snoozeDuration
					sharedPreferences!!.easySnooze = a.useEasySnooze

				})
				.show(childFragmentManager, NacSnoozeOptionsDialog.TAG)

		}

		// Alarm options
		pref.onCardAlarmOptionsClickedListener = NacCardPreference.OnCardAlarmOptionsClickedListener { alarm ->

			// Get the nav controller
			val navController = (activity as NacMainSettingActivity).navController

			// Show the alarm options dialog
			NacAlarmOptionsDialog.navigate(navController, alarm)
				?.observe(this) { a ->

					// Check which destination this alarm update came from
					when (navController.currentDestination?.id)
					{

						// Audio source
						R.id.nacAudioSourceDialog -> {
							sharedPreferences!!.audioSource = a.audioSource
						}

						// Text-to-speech
						R.id.nacTextToSpeechDialog -> {
							sharedPreferences!!.shouldSayCurrentTime = a.shouldSayCurrentTime
							sharedPreferences!!.shouldSayAlarmName = a.shouldSayAlarmName
							sharedPreferences!!.ttsFrequency = a.ttsFrequency
							sharedPreferences!!.ttsVoice = a.ttsVoice
						}

						// Volume
						R.id.nacVolumeOptionsDialog -> {
							sharedPreferences!!.shouldGraduallyIncreaseVolume = a.shouldGraduallyIncreaseVolume
							sharedPreferences!!.graduallyIncreaseVolumeWaitTime = a.graduallyIncreaseVolumeWaitTime
							sharedPreferences!!.shouldRestrictVolume = a.shouldRestrictVolume
						}

						// Flashlight
						R.id.nacFlashlightOptionsDialog -> {
							sharedPreferences!!.flashlightStrengthLevel = a.flashlightStrengthLevel
							sharedPreferences!!.shouldBlinkFlashlight = a.shouldBlinkFlashlight
							sharedPreferences!!.flashlightOnDuration = a.flashlightOnDuration
							sharedPreferences!!.flashlightOffDuration = a.flashlightOffDuration
						}

						// NFC
						R.id.nacSelectNfcTagDialog -> {
							sharedPreferences!!.nfcTagId = a.nfcTagId
						}

						// Vibrate
						R.id.nacVibrateOptionsDialog -> {
							sharedPreferences!!.vibrateDuration = a.vibrateDuration
							sharedPreferences!!.vibrateWaitTime = a.vibrateWaitTime
							sharedPreferences!!.shouldVibratePattern = a.shouldVibratePattern
							sharedPreferences!!.vibrateRepeatPattern = a.vibrateRepeatPattern
							sharedPreferences!!.vibrateWaitTimeAfterPattern = a.vibrateWaitTimeAfterPattern
						}

						// Upcoming reminder
						R.id.nacUpcomingReminderDialog -> {
							sharedPreferences!!.shouldShowReminder = a.showReminder
							sharedPreferences!!.timeToShowReminder = a.timeToShowReminder
							sharedPreferences!!.reminderFrequency = a.reminderFrequency
							sharedPreferences!!.shouldUseTtsForReminder = a.shouldUseTtsForReminder
						}

						// Unknown
						else -> {}

					}

				}

		}
	}

	/**
	 * Setup the preferences that are dependent on the new alarm screen.
	 */
	private fun setupDependentNewAlarmScreenPreferences(enabled: Boolean)
	{
		// Get the keys
		val currentDateAndTimeKey = getString(R.string.key_alarm_screen_show_current_date_and_time)
		val musicInfoKey = getString(R.string.key_alarm_screen_show_music_info)

		// Get the dependent preferences
		val currentDateAndTimePref = findPreference<NacCheckboxPreference>(currentDateAndTimeKey)!!
		val musicInfoPref = findPreference<NacCheckboxPreference>(musicInfoKey)!!

		// Set the usability of those preferences
		currentDateAndTimePref.isEnabled = enabled
		musicInfoPref.isEnabled = enabled
	}

}