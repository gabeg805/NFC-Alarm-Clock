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
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.dismissoptions.NacDismissOptionsDialog
import com.nfcalarmclock.alarm.options.mediapicker.NacMediaPickerActivity
import com.nfcalarmclock.alarm.options.mediapicker.NacMediaPickerPreference
import com.nfcalarmclock.alarm.options.name.NacNamePreference
import com.nfcalarmclock.alarm.options.snoozeoptions.NacSnoozeOptionsDialog
import com.nfcalarmclock.view.dayofweek.NacDayOfWeekPreference
import com.nfcalarmclock.alarm.options.volume.NacVolumePreference
import com.nfcalarmclock.alarm.options.volume.NacVolumePreference.OnAudioOptionsClickedListener
import com.nfcalarmclock.card.NacCardPreference
import com.nfcalarmclock.util.addAlarm
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
	 * The sound preference.
	 */
	private var mediaPreference: NacMediaPickerPreference? = null

	///**
	// * Called when the media activity is finished and returns a result.
	// */
	//override fun onActivityResult(result: ActivityResult)
	//{
	//	// Check that the result was OK
	//	if (result.resultCode == Activity.RESULT_OK)
	//	{
	//		// Get the media info from the activity result data
	//		val mediaPath = NacIntent.getMediaPath(result.data)
	//		val shuffleMedia = NacIntent.getShuffleMedia(result.data)
	//		val recursivelyPlayMedia = NacIntent.getRecursivelyPlayMedia(result.data)

	//		// Save the media info for this preference
	//		mediaPreference!!.setAndPersistMediaPath(mediaPath)
	//		sharedPreferences!!.shouldShuffleMedia = shuffleMedia
	//		sharedPreferences!!.recursivelyPlayMedia = recursivelyPlayMedia
	//	}
	//}

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

		// Setup the media preference
		//setupMediaPreference()
		setupYo()

		// Setup the on click listeners
		//setupAlarmDaysOnClickListener()
		//setupAlarmNameOnClickListener()
		//setupAlarmOptionsOnClickListener()
	}

	/**
	 */
	@OptIn(UnstableApi::class)
	private fun setupYo()
	{
		// Get the preference
		val key = getString(R.string.key_default_alarm_card)
		val pref = findPreference<NacCardPreference>(key)!!

		// Define the activity layuncher
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

		// Dismiss options
		pref.onCardDismissClickedListener = NacCardPreference.OnCardDismissOptionsClickedListener { alarm ->

			// Show the dismiss options dialog
			NacDismissOptionsDialog.create(
				alarm,
				onSaveAlarmListener = {
					// TODO
					//updateAlarm(it)
				})
				.show(childFragmentManager, NacDismissOptionsDialog.TAG)

		}

		// Snooze options
		pref.onCardSnoozeClickedListener = NacCardPreference.OnCardSnoozeOptionsClickedListener { alarm ->

			// Show the snooze options dialog
			NacSnoozeOptionsDialog.create(
				alarm,
				onSaveAlarmListener = {
					// TODO
					//updateAlarm(it)
				})
				.show(childFragmentManager, NacSnoozeOptionsDialog.TAG)

		}
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
			val bundle = Bundle().addAlarm(alarm)

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
							sharedPreferences!!.shouldSayCurrentTime = a.sayCurrentTime
							sharedPreferences!!.shouldSayAlarmName = a.sayAlarmName
							sharedPreferences!!.ttsFrequency = a.ttsFrequency
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

						//// Dismiss options
						//R.id.nacDismissOptionsDialog -> {
						//	sharedPreferences!!.shouldAutoDismiss = a.shouldAutoDismiss
						//	sharedPreferences!!.autoDismissTime = a.autoDismissTime
						//	sharedPreferences!!.canDismissEarly = a.useDismissEarly
						//	sharedPreferences!!.dismissEarlyTime = a.dismissEarlyTime
						//	sharedPreferences!!.shouldDeleteAlarmAfterDismissed = a.shouldDeleteAlarmAfterDismissed
						//}

						//// Snooze options
						//R.id.nacSnoozeOptionsDialog -> {
						//	sharedPreferences!!.shouldAutoSnooze = a.shouldAutoSnooze
						//	sharedPreferences!!.autoSnoozeTime = a.autoSnoozeTime
						//	sharedPreferences!!.maxSnooze = a.maxSnooze
						//	sharedPreferences!!.snoozeDuration = a.snoozeDuration
						//	sharedPreferences!!.easySnooze = a.useEasySnooze
						//}

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
	 * Setup the media preference.
	 */
	@OptIn(UnstableApi::class)
	private fun setupMediaPreference()
	{
		// Get the preference
		val key = resources.getString(R.string.key_general_default_alarm_media_path)
		val pref = findPreference<NacMediaPickerPreference>(key)

		// Set the member variables
		//ActivityResultContracts.StartActivityForResult(), this)
		mediaPreference = pref
		activityLauncher = registerForActivityResult(
			ActivityResultContracts.StartActivityForResult()) { result ->

				// Check that the result was OK
				if (result.resultCode == Activity.RESULT_OK)
				{
					// Get the media bundle from the activity result data, and the media
					// path from the bundle
					val bundle = result.data?.getMediaBundle() ?: Bundle()
					val mediaPath = bundle.getMediaPath()

					// Save the media info for this preference
					mediaPreference!!.setAndPersistMediaPath(mediaPath)
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
				}

			}

		// Setup the on click listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener {

			// Create the intent and add the media to the intent
			val intent = Intent(context, NacMediaPickerActivity::class.java)
				.addMediaInfo(sharedPreferences!!.mediaPath,
					sharedPreferences!!.mediaArtist,
					sharedPreferences!!.mediaTitle,
					sharedPreferences!!.mediaType,
					sharedPreferences!!.shouldShuffleMedia,
					sharedPreferences!!.recursivelyPlayMedia)

			// Launch the intent
			activityLauncher!!.launch(intent)

			// Return
			true

		}
	}

}