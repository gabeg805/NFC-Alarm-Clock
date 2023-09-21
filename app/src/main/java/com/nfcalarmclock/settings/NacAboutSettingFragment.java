package com.nfcalarmclock.settings;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import androidx.preference.Preference;
import com.nfcalarmclock.BuildConfig;
import com.nfcalarmclock.R;
import com.nfcalarmclock.permission.ignorebatteryoptimization.NacIgnoreBatteryOptimizationPermission;
import com.nfcalarmclock.permission.postnotifications.NacPostNotificationsPermission;
import com.nfcalarmclock.permission.readmediaaudio.NacReadMediaAudioPermission;
import com.nfcalarmclock.permission.scheduleexactalarm.NacScheduleExactAlarmPermission;
import com.nfcalarmclock.shared.NacSharedKeys;
import com.nfcalarmclock.util.NacUtility;
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog;

/**
 * Fragment to show the About preferences.
 */
public class NacAboutSettingFragment
	extends NacGenericSettingFragment
{

	/**
	 */
	@Override
	public void onCreatePreferences(Bundle savedInstanceState, String rootKey)
	{
		addPreferencesFromResource(R.xml.about_preferences);

		// Get the version preference
		NacSharedKeys keys = this.getSharedKeys();
		Preference version = findPreference(keys.getVersionPreference());

		// Set the version name as the summary
		version.setSummary(BuildConfig.VERSION_NAME);
	}

	/**
	 * A preference in the tree was clicked.
	 */
	@Override
	public boolean onPreferenceTreeClick(Preference preference)
	{
		Context context = requireContext();
		NacSharedKeys keys = this.getSharedKeys();
		String preferenceKey = preference.getKey();

		// Keys to compare against
		String versionKey = keys.getVersionPreference();
		String disableBatteryOptimizationKey = getString(R.string.about_ignore_battery_optimization_key);
		String nfcKey = getString(R.string.about_nfc_key);
		String storageKey = getString(R.string.about_storage_key);
		String vibrateKey = getString(R.string.about_vibrate_key);
		String foregroundServiceKey = getString(R.string.about_foreground_service_key);
		String fullScreenKey = getString(R.string.about_full_screen_intent_key);
		String scheduleAlarmsKey = getString(R.string.about_schedule_alarm_key);
		String setAlarmKey = getString(R.string.about_set_alarm_key);
		String showNotificationsKey = getString(R.string.about_post_notifications_key);
		String startupKey = getString(R.string.about_boot_key);
		String wakelockKey = getString(R.string.about_wakelock_key);

		// Message if already have this permission
		String message = getString(R.string.message_already_have_permission);

		// Version
		if (preferenceKey.equals(versionKey))
		{
			// Show the What's New dialog
			NacWhatsNewDialog dialog = new NacWhatsNewDialog();

			dialog.show(getChildFragmentManager(), NacWhatsNewDialog.TAG);
		}
		// Disable battery optimization
		else if (preferenceKey.equals(disableBatteryOptimizationKey))
		{
			if (NacIgnoreBatteryOptimizationPermission.hasPermission(context))
			{
				NacUtility.quickToast(context, message);
			}
			else
			{
				Activity activity = requireActivity();

				NacIgnoreBatteryOptimizationPermission.requestPermission(activity);
			}
		}
		// NFC
		else if (preferenceKey.equals(nfcKey))
		{
			NacUtility.quickToast(context, message);
		}
		// Storage
		else if (preferenceKey.equals(storageKey))
		{
			if (NacReadMediaAudioPermission.hasPermission(context))
			{
				NacUtility.quickToast(context, message);
			}
			else
			{
				Activity activity = requireActivity();

				NacReadMediaAudioPermission.requestPermission(activity, 0);
			}
		}
		// Vibrate
		else if (preferenceKey.equals(vibrateKey))
		{
			NacUtility.quickToast(context, message);
		}
		// Foreground service
		else if (preferenceKey.equals(foregroundServiceKey))
		{
			NacUtility.quickToast(context, message);
		}
		// Full screen
		else if (preferenceKey.equals(fullScreenKey))
		{
			NacUtility.quickToast(context, message);
		}
		// Schedule exact alarms
		else if (preferenceKey.equals(scheduleAlarmsKey))
		{
			if (NacScheduleExactAlarmPermission.hasPermission(context))
			{
				NacUtility.quickToast(context, message);
			}
			else
			{
				NacScheduleExactAlarmPermission.requestPermission(requireActivity());
			}
		}
		// Set alarm
		else if (preferenceKey.equals(setAlarmKey))
		{
			NacUtility.quickToast(context, message);
		}
		// Show notifications
		else if (preferenceKey.equals(showNotificationsKey))
		{
			if (NacPostNotificationsPermission.hasPermission(context))
			{
				NacUtility.quickToast(context, message);
			}
			else
			{
				Activity activity = requireActivity();

				NacPostNotificationsPermission.requestPermission(activity, 0);
			}
		}
		// Startup
		else if (preferenceKey.equals(startupKey))
		{
			NacUtility.quickToast(context, message);
		}
		// Wakelock
		else if (preferenceKey.equals(wakelockKey))
		{
			NacUtility.quickToast(context, message);
		}

		// Default return
		return super.onPreferenceTreeClick(preference);
	}

}
