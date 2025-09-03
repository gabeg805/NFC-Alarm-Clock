package com.nfcalarmclock.settings

import android.app.Activity
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import androidx.core.view.updateLayoutParams
import androidx.preference.Preference
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.system.permission.ignorebatteryoptimization.NacIgnoreBatteryOptimizationPermission
import com.nfcalarmclock.system.permission.postnotifications.NacPostNotificationsPermission
import com.nfcalarmclock.system.permission.readmediaaudio.NacReadMediaAudioPermission
import com.nfcalarmclock.system.permission.scheduleexactalarm.NacScheduleExactAlarmPermission
import com.nfcalarmclock.system.permission.systemalertwindow.NacSystemAlertWindowPermission
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
		val versionKey = getString(R.string.key_settings_about_version)
		val versionPref = findPreference<Preference>(versionKey)

		// Set the version name as the summary
		versionPref!!.summary = BuildConfig.VERSION_NAME

		// Check if the system alert window permission is needed
		if (!NacSystemAlertWindowPermission.isCorrectAndroidVersion)
		{
			val systemAlertWindowKey = getString(R.string.key_settings_about_system_alert_window)
			val systemAlertWindowPref = findPreference<Preference>(systemAlertWindowKey)

			// Hide the system alert window preference
			systemAlertWindowPref?.isVisible = false
		}
	}

	/**
	 * A preference in the tree was clicked.
	 */
	override fun onPreferenceTreeClick(preference: Preference): Boolean
	{
		val context = requireContext()
		val preferenceKey = preference.key

		// Keys to compare against
		val versionKey = getString(R.string.key_settings_about_version)
		val disableBatteryOptimizationKey =
			getString(R.string.key_settings_about_ignore_battery_optimization)
		val nfcKey = getString(R.string.key_settings_about_nfc)
		val storageKey = getString(R.string.key_settings_about_storage)
		val vibrateKey = getString(R.string.key_settings_about_vibrate)
		val foregroundServiceKey = getString(R.string.key_settings_about_foreground_service)
		val fullScreenKey = getString(R.string.key_settings_about_full_screen_intent)
		val scheduleAlarmsKey = getString(R.string.key_settings_about_schedule_alarm)
		val setAlarmKey = getString(R.string.key_settings_about_set_alarm)
		val showNotificationsKey = getString(R.string.key_settings_about_post_notifications)
		val systemAlertWindowKey = getString(R.string.key_settings_about_system_alert_window)
		val startupKey = getString(R.string.key_settings_about_boot)
		val wakelockKey = getString(R.string.key_settings_about_wakelock)

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
		// System alert window
		else if (preferenceKey == systemAlertWindowKey)
		{

			// Show toast to the user saying that the app already has the permission
			if (NacSystemAlertWindowPermission.hasPermission(context))
			{
				quickToast(context, messageId)
			}
			// Request the permission
			else
			{
				val activity: Activity = requireActivity()

				NacSystemAlertWindowPermission.requestPermission(activity)
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

	/**
	 * Called after the view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Setup
		super.onViewCreated(view, savedInstanceState)

		// Check if API < 35, then edge-to-edge is not enforced and do not need to do
		// anything
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
		{
			return
		}

		// Setup edge to edge for the recyclerview by using the margin that was saved in
		// the main settings fragment
		listView.updateLayoutParams<ViewGroup.MarginLayoutParams> {
			topMargin = (activity as NacMainSettingActivity).rvTopMargin
		}
	}

}