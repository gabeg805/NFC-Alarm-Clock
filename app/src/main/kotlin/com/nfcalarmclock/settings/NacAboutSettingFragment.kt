package com.nfcalarmclock.settings

import android.app.Activity
import android.os.Bundle
import androidx.preference.Preference
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.permission.ignorebatteryoptimization.NacIgnoreBatteryOptimizationPermission
import com.nfcalarmclock.permission.postnotifications.NacPostNotificationsPermission
import com.nfcalarmclock.permission.readmediaaudio.NacReadMediaAudioPermission
import com.nfcalarmclock.permission.scheduleexactalarm.NacScheduleExactAlarmPermission
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog

/**
 * Fragment to show the About preferences.
 */
class NacAboutSettingFragment
	: NacGenericSettingFragment()
{

	/**
	 * Called when creating the preferences.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.about_preferences)

		// Get the version preference
		val versionKey = getString(R.string.version_key)
		val versionPref = findPreference<Preference>(versionKey)

		// Set the version name as the summary
		versionPref!!.summary = BuildConfig.VERSION_NAME
	}

	/**
	 * A preference in the tree was clicked.
	 */
	override fun onPreferenceTreeClick(preference: Preference): Boolean
	{
		val context = requireContext()
		val preferenceKey = preference.key

		// Keys to compare against
		val versionKey = getString(R.string.version_key)
		val disableBatteryOptimizationKey =
			getString(R.string.about_ignore_battery_optimization_key)
		val nfcKey = getString(R.string.about_nfc_key)
		val storageKey = getString(R.string.about_storage_key)
		val vibrateKey = getString(R.string.about_vibrate_key)
		val foregroundServiceKey = getString(R.string.about_foreground_service_key)
		val fullScreenKey = getString(R.string.about_full_screen_intent_key)
		val scheduleAlarmsKey = getString(R.string.about_schedule_alarm_key)
		val setAlarmKey = getString(R.string.about_set_alarm_key)
		val showNotificationsKey = getString(R.string.about_post_notifications_key)
		val startupKey = getString(R.string.about_boot_key)
		val wakelockKey = getString(R.string.about_wakelock_key)

		// Message if already have this permission
		val messageId = R.string.message_already_have_permission

		// Version
		if (preferenceKey == versionKey)
		{
			// Create the What's New dialog
			val dialog = NacWhatsNewDialog()

			// Show the What's New dialog
			dialog.show(childFragmentManager, NacWhatsNewDialog.TAG)
		}
		// Disable battery optimization
		else if (preferenceKey == disableBatteryOptimizationKey)
		{

			// Show toast to the user saying that the app already has the permission
			if (NacIgnoreBatteryOptimizationPermission.hasPermission(context))
			{
				quickToast(context, messageId)
			}
			// Request the permission
			else
			{
				val activity: Activity = requireActivity()

				NacIgnoreBatteryOptimizationPermission.requestPermission(activity)
			}

		}
		// NFC
		else if (preferenceKey == nfcKey)
		{
			quickToast(context, messageId)
		}
		// Storage
		else if (preferenceKey == storageKey)
		{

			// Show toast to the user saying that the app already has the permission
			if (NacReadMediaAudioPermission.hasPermission(context))
			{
				quickToast(context, messageId)
			}
			// Request the permission
			else
			{
				val activity: Activity = requireActivity()

				NacReadMediaAudioPermission.requestPermission(activity, 0)
			}

		}
		// Vibrate
		else if (preferenceKey == vibrateKey)
		{
			quickToast(context, messageId)
		}
		// Foreground service
		else if (preferenceKey == foregroundServiceKey)
		{
			quickToast(context, messageId)
		}
		// Full screen
		else if (preferenceKey == fullScreenKey)
		{
			quickToast(context, messageId)
		}
		// Schedule exact alarms
		else if (preferenceKey == scheduleAlarmsKey)
		{

			// Show toast to the user saying that the app already has the permission
			if (NacScheduleExactAlarmPermission.hasPermission(context))
			{
				quickToast(context, messageId)
			}
			// Request the permission
			else
			{
				NacScheduleExactAlarmPermission.requestPermission(requireActivity())
			}

		}
		// Set alarm
		else if (preferenceKey == setAlarmKey)
		{
			quickToast(context, messageId)
		}
		// Show notifications
		else if (preferenceKey == showNotificationsKey)
		{

			// Show toast to the user saying that the app already has the permission
			if (NacPostNotificationsPermission.hasPermission(context))
			{
				quickToast(context, messageId)
			}
			// Request the permission
			else
			{
				val activity: Activity = requireActivity()

				NacPostNotificationsPermission.requestPermission(activity, 0)
			}

		}
		// Startup
		else if (preferenceKey == startupKey)
		{
			quickToast(context, messageId)
		}
		// Wakelock
		else if (preferenceKey == wakelockKey)
		{
			quickToast(context, messageId)
		}

		// Default return
		return super.onPreferenceTreeClick(preference)
	}
}