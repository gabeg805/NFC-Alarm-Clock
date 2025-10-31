package com.nfcalarmclock.settings

import android.os.Build
import android.os.Bundle
import android.view.View
import androidx.annotation.OptIn
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import androidx.navigation.fragment.findNavController
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.NacAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.alarm.options.name.NacNameDialog
import com.nfcalarmclock.alarm.options.snoozeoptions.NacSnoozeOptionsDialog
import com.nfcalarmclock.card.NacCardPreference
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.settings.preference.NacCheckboxPreference
import com.nfcalarmclock.system.addMediaInfo
import com.nfcalarmclock.system.daysToValue
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.getMediaArtist
import com.nfcalarmclock.system.getMediaPath
import com.nfcalarmclock.system.getMediaTitle
import com.nfcalarmclock.system.getMediaType
import com.nfcalarmclock.system.getRecursivelyPlayMedia
import com.nfcalarmclock.system.getShuffleMedia
import com.nfcalarmclock.system.media.buildLocalMediaPath
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * General settings fragment.
 */
@AndroidEntryPoint
class NacGeneralSettingFragment
	: NacBaseSettingFragment()
{

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Called when the preference is created.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		// Get the device protected storage context, if available
		val deviceContext = getDeviceProtectedStorageContext(requireContext())

		// Set device protected storage as the storage location to use
		preferenceManager.setStorageDeviceProtected()

		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.general_preferences)

		// Set the default values on this preference that are in the
		// android:defaultValue attribute
		PreferenceManager.setDefaultValues(deviceContext, R.xml.general_preferences,  false)

		// Setup the preferences
		setupDefaultAlarmCard()
		setupAlarmScreen()
	}

	/**
	 * Called after the view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Set the observer for the media picker
		findNavController().currentBackStackEntry
			?.savedStateHandle
			?.getLiveData<Bundle>("YOYOYO")
			?.observe(viewLifecycleOwner) { result ->

				// Get the preference
				val context = requireContext()
				val key = getString(R.string.key_default_alarm_card)
				val pref = findPreference<NacCardPreference>(key)!!

				// Save the media info for this preference
				sharedPreferences!!.mediaPath = result.getMediaPath()
				sharedPreferences!!.mediaArtist = result.getMediaArtist()
				sharedPreferences!!.mediaTitle = result.getMediaTitle()
				sharedPreferences!!.mediaType	= result.getMediaType()
				sharedPreferences!!.localMediaPath = buildLocalMediaPath(
					context,
					sharedPreferences!!.mediaArtist,
					sharedPreferences!!.mediaTitle,
					sharedPreferences!!.mediaType)
				sharedPreferences!!.shouldShuffleMedia = result.getShuffleMedia()
				sharedPreferences!!.recursivelyPlayMedia = result.getRecursivelyPlayMedia()

				// Update the card
				pref.card.alarm!!.mediaPath = sharedPreferences!!.mediaPath
				pref.card.alarm!!.mediaArtist = sharedPreferences!!.mediaArtist
				pref.card.alarm!!.mediaTitle = sharedPreferences!!.mediaTitle
				pref.card.alarm!!.mediaType = sharedPreferences!!.mediaType
				pref.card.setMediaButton()

			}

		// Check if API < 35, then edge-to-edge is not enforced and do not need to do
		// anything
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
		{
			return
		}

		// TODO: Can maybe customize this more when going up to API 36, but for now opting out
		//// Setup edge to edge for the recyclerview by using the margin that was saved in
		//// the main settings fragment
		//listView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
		//	topMargin = (activity as NacMainSettingActivity).rvTopMargin
		//}
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
	 * Setup the default alarm card.
	 *
	 * Note: Only actions that open up a dialog are here, otherwise, the simple stuff is
	 *       handled in NacCardPreference
	 */
	@OptIn(UnstableApi::class)
	private fun setupDefaultAlarmCard()
	{
		// Get the preference
		val key = getString(R.string.key_default_alarm_card)
		val pref = findPreference<NacCardPreference>(key)!!

		// Set the list of NFC tags
		lifecycleScope.launch {
			pref.allNfcTags = nfcTagViewModel.getAllNfcTags()
		}

		// Media
		pref.onCardMediaClickedListener = NacCardPreference.OnCardMediaClickedListener { alarm ->

			// Create a bundle with the media info
			val mediaBundle = Bundle()
				.addMediaInfo(
					alarm.mediaPath,
					alarm.mediaArtist,
					alarm.mediaTitle,
					alarm.mediaType,
					alarm.shouldShuffleMedia,
					alarm.shouldRecursivelyPlayMedia)

			// Navigate to the media picker
			findNavController().navigate(R.id.action_nacGeneralSettingFragment_to_nacAlarmMainMediaPickerFragment2, mediaBundle)

		}

		// Name
		pref.onCardNameClickedListener = NacCardPreference.OnCardNameClickedListener { alarm ->

			// Show the name dialog
			NacNameDialog.create(
				alarm.name,
				onNameEnteredListener = {

					// Save the name
					sharedPreferences!!.name = it

					// Refresh the views
					pref.card.refreshNameViews()

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
					sharedPreferences!!.canDismissEarly = a.canDismissEarly
					sharedPreferences!!.shouldShowDismissEarlyNotification = a.shouldShowDismissEarlyNotification
					sharedPreferences!!.dismissEarlyTime = a.dismissEarlyTime
					sharedPreferences!!.shouldDeleteAfterDismissed = a.shouldDeleteAfterDismissed

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
					sharedPreferences!!.shouldEasySnooze = a.shouldEasySnooze

				})
				.show(childFragmentManager, NacSnoozeOptionsDialog.TAG)

		}

		// TODO: Repeat, vibrate, NFC, and flashlight long click listener

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

						// Repeat
						R.id.nacRepeatOptionsDialog -> {
							sharedPreferences!!.shouldRepeat = true
							sharedPreferences!!.repeatFrequency = a.repeatFrequency
							sharedPreferences!!.repeatFrequencyUnits = a.repeatFrequencyUnits
							sharedPreferences!!.repeatFrequencyDaysToRunBeforeStarting = a.repeatFrequencyDaysToRunBeforeStarting.daysToValue()

							// Weekly frequency unit
							if (a.repeatFrequencyUnits == 4)
							{
								// Days are empty
								if (a.days.isEmpty())
								{
									sharedPreferences!!.days = a.days.daysToValue()
								}
							}
							// Every other frequency unit
							else
							{
								sharedPreferences!!.days = a.days.daysToValue()
							}
						}

						// Vibrate
						R.id.nacVibrateOptionsDialog -> {
							sharedPreferences!!.vibrateDuration = a.vibrateDuration
							sharedPreferences!!.vibrateWaitTime = a.vibrateWaitTime
							sharedPreferences!!.shouldVibratePattern = a.shouldVibratePattern
							sharedPreferences!!.vibrateRepeatPattern = a.vibrateRepeatPattern
							sharedPreferences!!.vibrateWaitTimeAfterPattern = a.vibrateWaitTimeAfterPattern
						}

						// NFC
						R.id.nacSelectNfcTagDialog -> {
							sharedPreferences!!.nfcTagId = a.nfcTagId
						}

						// Flashlight
						R.id.nacFlashlightOptionsDialog -> {
							sharedPreferences!!.flashlightStrengthLevel = a.flashlightStrengthLevel
							sharedPreferences!!.shouldBlinkFlashlight = a.shouldBlinkFlashlight
							sharedPreferences!!.flashlightOnDuration = a.flashlightOnDuration
							sharedPreferences!!.flashlightOffDuration = a.flashlightOffDuration
						}

						// Audio source
						R.id.nacAudioSourceDialog -> {
							sharedPreferences!!.audioSource = a.audioSource
						}

						// Text-to-speech
						R.id.nacTextToSpeechDialog -> {
							sharedPreferences!!.shouldSayCurrentTime = a.shouldSayCurrentTime
							sharedPreferences!!.shouldSayAlarmName = a.shouldSayName
							sharedPreferences!!.ttsFrequency = a.ttsFrequency
							sharedPreferences!!.ttsVoice = a.ttsVoice
						}

						// Upcoming reminder
						R.id.nacUpcomingReminderDialog -> {
							sharedPreferences!!.shouldShowReminder = a.shouldShowReminder
							sharedPreferences!!.timeToShowReminder = a.timeToShowReminder
							sharedPreferences!!.reminderFrequency = a.reminderFrequency
							sharedPreferences!!.shouldUseTtsForReminder = a.shouldUseTts && a.shouldUseTtsForReminder
						}

						// Volume
						R.id.nacVolumeOptionsDialog -> {
							sharedPreferences!!.shouldGraduallyIncreaseVolume = a.shouldGraduallyIncreaseVolume
							sharedPreferences!!.graduallyIncreaseVolumeWaitTime = a.graduallyIncreaseVolumeWaitTime
							sharedPreferences!!.shouldRestrictVolume = a.shouldRestrictVolume
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