package com.nfcalarmclock.shared

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R

/**
 * Container for the values of each preference.
 */
class NacSharedPreferences(context: Context)
{

	/**
	 * Shared preferences instance.
	 */
	val instance: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(context)

	/**
	 * Resources.
	 */
	val resources: Resources = context.resources

	/**
	 * AM color.
	 */
	val amColor: Int
		get()
		{
			val key = resources.getString(R.string.am_color_key)
			val defaultValue = resources.getInteger(R.integer.default_am_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * App's first run value.
	 */
	val appFirstRun: Boolean
		get()
		{
			val key = resources.getString(R.string.app_first_run)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether statistics should start to be collected or not.
	 */
	val appStartStatistics: Boolean
		get()
		{
			val key = resources.getString(R.string.app_start_statistics)
			val defaultValue = resources.getBoolean(R.bool.default_app_start_statistics)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Audio source.
	 */
	val audioSource: String
		get()
		{
			val key = resources.getString(R.string.alarm_audio_source_key)
			val audioSources = resources.getStringArray(R.array.audio_sources)
			val defaultValue = audioSources[2]

			return instance.getString(key, defaultValue) ?: ""
		}

	/**
	 * Auto dismiss duration.
	 */
	private val autoDismissIndex: Int
		get()
		{
			val key = resources.getString(R.string.auto_dismiss_key)
			val defaultValue = resources.getInteger(R.integer.default_auto_dismiss_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * @see .getAutoDismissTime
	 */
	val autoDismissTime: Long
		get()
		{
			return if (autoDismissIndex < 5)
			{
				autoDismissIndex.toLong()
			}
			else
			{
				(autoDismissIndex - 4) * 5L
			}
		}

	/**
	 * Alarm card height when it is collapsed.
	 */
	val cardHeightCollapsed: Int
		get()
		{
			val key = resources.getString(R.string.card_height_collapsed)
			val defaultValue = resources.getInteger(R.integer.default_card_height_collapsed)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Alarm card height when it is collapsed, with dismiss showing.
	 */
	val cardHeightCollapsedDismiss: Int
		get()
		{
			val key = resources.getString(R.string.card_height_collapsed_dismiss)
			val defaultValue = resources.getInteger(R.integer.default_card_height_collapsed_dismiss)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Alarm card height when it is expanded.
	 */
	val cardHeightExpanded: Int
		get()
		{
			val key = resources.getString(R.string.card_height_expanded)
			val defaultValue = resources.getInteger(R.integer.default_card_height_expanded)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Check if the alarm card has been measured.
	 */
	val cardIsMeasured: Boolean
		get()
		{
			val key = resources.getString(R.string.card_is_measured)
			val defaultValue = resources.getBoolean(R.bool.default_card_is_measured)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Which style to use for the day buttons.
	 *
	 * 1: Represents using the filled-in buttons (Default)
	 * 2: Represents the outlined button style
	 */
	val dayButtonStyle: Int
		get()
		{
			val key = resources.getString(R.string.day_button_style_key)
			val defaultValue = resources.getInteger(R.integer.default_day_button_style)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Alarm days.
	 */
	val days: Int
		get()
		{
			val key = resources.getString(R.string.alarm_days_key)
			val defaultValue = resources.getInteger(R.integer.default_days)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Days color.
	 */
	val daysColor: Int
		get()
		{
			val key = resources.getString(R.string.days_color_key)
			val defaultValue = resources.getInteger(R.integer.default_days_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Counter to delay showing the What's New dialog.
	 */
	val delayShowingWhatsNewDialogCounter: Int
		get()
		{
			val key = resources.getString(R.string.key_delay_showing_whats_new_dialog_counter)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}

	/**
	 * The time before an alarm goes off to start showing the dismiss early button by.
	 */
	val dismissEarlyTime: Int
		get()
		{
			val key = resources.getString(R.string.alarm_dismiss_early_time_key)
			val defaultValue = resources.getInteger(R.integer.default_dismiss_early_time)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Whether easy snooze is enabled or not.
	 */
	val easySnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.easy_snooze_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether a new alarm card should be expanded or not.
	 */
	val expandNewAlarm: Boolean
		get()
		{
			val key = resources.getString(R.string.expand_new_alarm_key)
			val defaultValue = resources.getBoolean(R.bool.default_expand_new_alarm)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether volume should be gradually increased or not.
	 */
	val graduallyIncreaseVolumeWaitTime: Int
		get()
		{
			val key = resources.getString(R.string.alarm_gradually_increase_volume_wait_time_key)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Check if the app has reached the counter limit.
	 */
	val isRateMyAppLimit: Boolean
		get()
		{
			return rateMyAppCounter >= 50
		}

	/**
	 * Check if the app has been rated.
	 */
	val isRateMyAppRated: Boolean
		get()
		{
			val rated = resources.getInteger(R.integer.default_rate_my_app_rated)

			return rateMyAppCounter == rated
		}

	/**
	 * The index for the max number of snoozes.
	 */
	private val maxSnoozeIndex: Int
		get()
		{
			val key = resources.getString(R.string.max_snooze_key)
			val defaultValue = resources.getInteger(R.integer.default_max_snooze_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Max number of snoozes.
	 */
	val maxSnoozeValue: Int
		get()
		{
			return if (maxSnoozeIndex == 11)
			{
				-1
			}
			else
			{
				maxSnoozeIndex
			}
		}

	/**
	 * Media path.
	 */
	val mediaPath: String
		get()
		{
			val key = resources.getString(R.string.alarm_sound_key)

			return instance.getString(key, "") ?: ""
		}

	/**
	 * Whether the missed alarm notifications should be displayed.
	 */
	val missedAlarmNotification: Boolean
		get()
		{
			val key = resources.getString(R.string.missed_alarm_key)
			val defaultValue = resources.getBoolean(R.bool.default_missed_alarm)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Name of the alarm.
	 */
	val name: String
		get()
		{
			val key = resources.getString(R.string.alarm_name_key)

			return instance.getString(key, "") ?: ""
		}

	/**
	 * Name color.
	 */
	val nameColor: Int
		get()
		{
			val key = resources.getString(R.string.name_color_key)
			val defaultValue = resources.getInteger(R.integer.default_name_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Whether the display next alarm should show time remaining for the next alarm.
	 */
	val nextAlarmFormat: Int
		get()
		{
			val key = resources.getString(R.string.next_alarm_format_key)
			val defaultValue = resources.getInteger(R.integer.default_next_alarm_format_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * PM color.
	 */
	val pmColor: Int
		get()
		{
			val key = resources.getString(R.string.pm_color_key)
			val defaultValue = resources.getInteger(R.integer.default_pm_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * The previous version of the app.
	 *
	 * Normally, this should be the same as the current version, but when an
	 * install occurs, these values will differ.
	 */
	val previousAppVersion: String
		get()
		{
			val key = resources.getString(R.string.previous_app_version)

			return instance.getString(key, "") ?: ""
		}

	/**
	 * The previous system volume, before an alarm goes off.
	 */
	val previousVolume: Int
		get()
		{
			val key = resources.getString(R.string.sys_previous_volume)
			val defaultValue = resources.getInteger(R.integer.default_previous_volume)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * The app's rating counter.
	 */
	val rateMyAppCounter: Int
		get()
		{
			val key = resources.getString(R.string.app_rating_counter)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Whether to recursively play the media in a directory.
	 */
	val recursivelyPlayMedia: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_recursively_play_media_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Frequency at which to show the reminder, in units of minutes.
	 */
	val reminderFrequency: Int
		get()
		{
			val key = resources.getString(R.string.reminder_frequency_key)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Whether the alarm should be repeated or not.
	 */
	val repeat: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_repeat_key)
			val defaultValue = resources.getBoolean(R.bool.default_repeat)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether volume should be gradually increased or not.
	 */
	val shouldGraduallyIncreaseVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_should_gradually_increase_volume_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether the main activity should be refreshed or not.
	 */
	val shouldRefreshMainActivity: Boolean
		get()
		{
			val key = resources.getString(R.string.app_should_refresh_main_activity)
			val defaultValue = resources.getBoolean(R.bool.default_app_should_refresh_main_activity)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether volume should be restricted or not.
	 */
	val shouldRestrictVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_should_restrict_volume_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to say the alarm name or not via text-to-speech.
	 */
	val shouldSayAlarmName: Boolean
		get()
		{
			val key = resources.getString(R.string.should_say_alarm_name_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to say the current time or not via text-to-speech.
	 */
	val shouldSayCurrentTime: Boolean
		get()
		{
			val key = resources.getString(R.string.should_say_current_time_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to show a reminder or not.
	 */
	val shouldShowReminder: Boolean
		get()
		{
			val key = resources.getString(R.string.should_show_reminder_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to use text-to-speech for the reminder or not.
	 */
	val shouldUseTtsForReminder: Boolean
		get()
		{
			val key = resources.getString(R.string.should_use_tts_for_reminder_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether the alarm information should be shown or not.
	 */
	val showAlarmInfo: Boolean
		get()
		{
			val key = resources.getString(R.string.show_alarm_info_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to shuffle media.
	 */
	val shuffleMedia: Boolean
		get()
		{
			val key = resources.getString(R.string.shuffle_playlist_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Index for the snooze duration.
	 */
	private val snoozeDurationIndex: Int
		get()
		{
			val key = resources.getString(R.string.snooze_duration_key)
			val defaultValue = resources.getInteger(R.integer.default_snooze_duration_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Snooze duration.
	 */
	val snoozeDurationValue: Int
		get()
		{
			return if (snoozeDurationIndex < 9)
			{
				snoozeDurationIndex + 1
			}
			else
			{
				(snoozeDurationIndex - 7) * 5
			}
		}

	/**
	 * The speak frequency value.
	 */
	val speakFrequency: Int
		get()
		{
			val key = resources.getString(R.string.speak_frequency_key)
			val defaultValue = resources.getInteger(R.integer.default_speak_frequency_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Value indicating which day to start on.
	 */
	val startWeekOn: Int
		get()
		{
			val key = resources.getString(R.string.start_week_on_key)
			val defaultValue = resources.getInteger(R.integer.default_start_week_on_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Theme color.
	 */
	val themeColor: Int
		get()
		{
			val key = resources.getString(R.string.theme_color_key)
			val defaultValue = resources.getInteger(R.integer.default_theme_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Time color.
	 */
	val timeColor: Int
		get()
		{
			val key = resources.getString(R.string.time_color_key)
			val defaultValue = resources.getInteger(R.integer.default_time_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * The time to start showing a reminder.
	 */
	val timeToShowReminder: Int
		get()
		{
			val key = resources.getString(R.string.time_to_show_reminder_key)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Whether dismiss early should be used or not.
	 */
	val useDismissEarly: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_use_dismiss_early_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether NFC is required or not.
	 */
	val useNfc: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_use_nfc_key)
			val defaultValue = resources.getBoolean(R.bool.default_use_nfc)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether the alarm should vibrate the phone or not.
	 */
	val vibrate: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_vibrate_key)
			val defaultValue = resources.getBoolean(R.bool.default_vibrate)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Alarm volume level.
	 */
	val volume: Int
		get()
		{
			val key = resources.getString(R.string.alarm_volume_key)
			val defaultValue = resources.getInteger(R.integer.default_volume)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Whether the app was supported or not.
	 */
	val wasAppSupported: Boolean
		get()
		{
			val key = resources.getString(R.string.key_app_supported)
			val defaultValue = resources.getBoolean(R.bool.default_was_app_supported)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether the permission to ignore battery optimization was requested.
	 */
	val wasIgnoreBatteryOptimizationPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_ignore_battery_optimization_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_ignore_battery_optimization_permission_requested)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether the POST_NOTIFICATIONS permission was requested.
	 */
	val wasPostNotificationsPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_post_notifications_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_post_notifications_permission_requested)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether the SCHEDULE_EXACT_ALARM permission was requested.
	 */
	val wasScheduleExactAlarmPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_schedule_exact_alarm_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_schedule_exact_alarm_permission_requested)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Edit whether this is the app's first run or not.
	 */
	fun editAppFirstRun(context: Context, first: Boolean)
	{
		val key = context.getString(R.string.app_first_run)

		saveBoolean(key, first)
	}

	/**
	 * Edit whether statistics should start to be collected or not.
	 *
	 * @param  shouldStart  Whether statistics should start to be collected or
	 * not.
	 */
	fun editAppStartStatistics(shouldStart: Boolean)
	{
		val key = resources.getString(R.string.app_start_statistics)

		saveBoolean(key, shouldStart)
	}

	/**
	 * Edit the default audio source to use when a new alarm card is created.
	 *
	 *
	 * This can be changed for an alarm by clicking the audio settings button.
	 */
	fun editAudioSource(source: String?)
	{
		val key = resources.getString(R.string.alarm_audio_source_key)

		saveString(key, source)
	}

	/**
	 * Edit the height of the alarm card when it is collapsed.
	 */
	fun editCardHeightCollapsed(height: Int)
	{
		val key = resources.getString(R.string.card_height_collapsed)

		saveInt(key, height)
	}

	/**
	 * Edit the height of the alarm card height it is collapsed, but the dismiss
	 * button is showing.
	 */
	fun editCardHeightCollapsedDismiss(height: Int)
	{
		val key = resources.getString(R.string.card_height_collapsed_dismiss)

		saveInt(key, height)
	}

	/**
	 * Edit the height of the alarm card when it is expanded.
	 */
	fun editCardHeightExpanded(height: Int)
	{
		val key = resources.getString(R.string.card_height_expanded)

		saveInt(key, height)
	}

	/**
	 * Edit the flag indicating if the alarm card has been measured or not.
	 */
	fun editCardIsMeasured(isMeasured: Boolean)
	{
		val key = resources.getString(R.string.card_is_measured)

		saveBoolean(key, isMeasured)
	}

	/**
	 * Edit the counter to delay showing the What's New dialog.
	 */
	fun editDelayShowingWhatsNewDialogCounter(count: Int)
	{
		val key = resources.getString(R.string.key_delay_showing_whats_new_dialog_counter)

		saveInt(key, count)
	}

	/**
	 * Edit the default dismiss early time when an alarm is created.
	 */
	fun editDismissEarlyTime(dismissEarly: Int)
	{
		val key = resources.getString(R.string.alarm_dismiss_early_time_key)

		saveInt(key, dismissEarly)
	}

	/**
	 * Edit the default value of a newly created alarm for wait time between
	 * gradually increasing the volume another step when an alarm is active.
	 */
	fun editGraduallyIncreaseVolumeWaitTime(waitTime: Int)
	{
		val key = resources.getString(R.string.alarm_gradually_increase_volume_wait_time_key)

		saveInt(key, waitTime)
	}

	/**
	 * Edit the previous version that this app was using.
	 *
	 *
	 * Normally, this should be the same as the current version, but when an
	 * install occurs, these values will differ.
	 */
	fun editPreviousAppVersion(version: String?)
	{
		val key = resources.getString(R.string.previous_app_version)

		saveString(key, version)
	}

	/**
	 * Edit the previous system volume, before an alarm goes off.
	 */
	fun editPreviousVolume(previous: Int)
	{
		val key = resources.getString(R.string.sys_previous_volume)

		saveInt(key, previous)
	}

	/**
	 * Edit the counter that will indicate whether it is time to show the
	 * dialog to Rate My App.
	 */
	fun editRateMyAppCounter(counter: Int)
	{
		val key = resources.getString(R.string.app_rating_counter)

		saveInt(key, counter)
	}

	/**
	 * Whether to recursively play the media in a directory.
	 */
	fun editRecursivelyPlayMedia(recurse: Boolean)
	{
		val key = resources.getString(R.string.alarm_recursively_play_media_key)

		saveBoolean(key, recurse)
	}

	/**
	 * Frequency at which to show the reminder, in units of minutes.
	 */
	fun editReminderFrequency(freq: Int)
	{
		val key = resources.getString(R.string.reminder_frequency_key)

		return saveInt(key, freq)
	}

	/**
	 * Edit the default value of a newly created alarm for whether the volume should
	 * gradually be increased when an alarm is active.
	 */
	fun editShouldGraduallyIncreaseVolume(shouldIncrease: Boolean)
	{
		val key = resources.getString(R.string.alarm_should_gradually_increase_volume_key)

		saveBoolean(key, shouldIncrease)
	}

	/**
	 * Edit the default should restrict volume value when an alarm is created.
	 */
	fun editShouldRestrictVolume(shouldRestrict: Boolean)
	{
		val key = resources.getString(R.string.alarm_should_restrict_volume_key)

		saveBoolean(key, shouldRestrict)
	}

	/**
	 * Edit the value indicating whether the main activity should be refreshed or
	 * not.
	 */
	fun editShouldRefreshMainActivity(shouldRefresh: Boolean)
	{
		val key = resources.getString(R.string.app_should_refresh_main_activity)

		saveBoolean(key, shouldRefresh)
	}

	/**
	 * Edit whether the alarm name will be said or not via text-to-speech.
	 */
	fun editShouldSayAlarmName(speak: Boolean)
	{
		val key = resources.getString(R.string.should_say_alarm_name_key)

		saveBoolean(key, speak)
	}

	/**
	 * Edit whether the current time will be said or not via text-to-speech.
	 */
	fun editShouldSayCurrentTime(speak: Boolean)
	{
		val key = resources.getString(R.string.should_say_current_time_key)

		saveBoolean(key, speak)
	}

	/**
	 * Whether to show a reminder or not.
	 */
	fun editShouldShowReminder(showReminder: Boolean)
	{
		val key = resources.getString(R.string.should_show_reminder_key)

		return saveBoolean(key, showReminder)
	}

	/**
	 * Whether to use text-to-speech for the reminder or not.
	 */
	fun editShouldUseTtsForReminder(shouldUseTts: Boolean)
	{
		val key = resources.getString(R.string.should_use_tts_for_reminder_key)

		return saveBoolean(key, shouldUseTts)
	}

	/**
	 * Whether to shuffle media.
	 */
	fun editShuffleMedia(shuffle: Boolean)
	{
		val key = resources.getString(R.string.shuffle_playlist_key)

		saveBoolean(key, shuffle)
	}

	/**
	 * Edit the frequency at which the text-to-speech should go off when an alarm
	 * is going off.
	 */
	fun editSpeakFrequency(freq: Int)
	{
		val key = resources.getString(R.string.speak_frequency_key)

		saveInt(key, freq)
	}

	/**
	 * The time to start showing a reminder.
	 */
	fun editTimeToShowReminder(timeToShow: Int)
	{
		val key = resources.getString(R.string.time_to_show_reminder_key)

		return saveInt(key, timeToShow)
	}

	/**
	 * Edit the default use dismiss early when an alarm is created.
	 */
	fun editUseDismissEarly(useDismissEarly: Boolean)
	{
		val key = resources.getString(R.string.alarm_use_dismiss_early_key)

		saveBoolean(key, useDismissEarly)
	}

	/**
	 * Edit whether the app was supported or not.
	 */
	fun editWasAppSupported(wasSupported: Boolean)
	{
		val key = resources.getString(R.string.key_app_supported)

		saveBoolean(key, wasSupported)
	}

	/**
	 * Edit whether the permission to ignore battery optimization was requested.
	 */
	fun editWasIgnoreBatteryOptimizationPermissionRequested(requested: Boolean)
	{
		val key = resources.getString(R.string.key_permission_ignore_battery_optimization_requested)

		saveBoolean(key, requested)
	}

	/**
	 * Edit whether the POST_NOTIFICATIONS permission was requested.
	 */
	fun editWasPostNotificationsPermissionRequested(requested: Boolean)
	{
		val key = resources.getString(R.string.key_permission_post_notifications_requested)

		saveBoolean(key, requested)
	}

	/**
	 * Edit whether the SCHEDULE_EXACT_ALARM permission was requested.
	 */
	fun editWasScheduleExactAlarmPermissionRequested(requested: Boolean)
	{
		val key = resources.getString(R.string.key_permission_schedule_exact_alarm_requested)

		saveBoolean(key, requested)
	}

	/**
	 * Save a boolean to the shared preference.
	 */
	private fun saveBoolean(key: String, value: Boolean)
	{
		instance.edit()
			.putBoolean(key, value)
			.apply()
	}

	/**
	 * Save an int to the shared preference.
	 */
	private fun saveInt(key: String, value: Int)
	{
		instance.edit()
			.putInt(key, value)
			.apply()
	}

	/**
	 * Save a string to the shared preference.
	 */
	private fun saveString(key: String, value: String?)
	{
		instance.edit()
			.putString(key, value)
			.apply()
	}

}