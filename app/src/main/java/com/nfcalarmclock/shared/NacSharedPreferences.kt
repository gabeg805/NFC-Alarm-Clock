package com.nfcalarmclock.shared

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.media.NacMedia.getTitle

/**
 * Container for the values of each preference.
 */
class NacSharedPreferences(

	/**
	 * The context application.
	 */
	context: Context

)
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

			return getInt(key, defaultValue)
		}

	/**
	 * App's first run value.
	 */
	val appFirstRun: Boolean
		get()
		{
			val key = resources.getString(R.string.app_first_run)
			val defaultValue = true

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether statistics should start to be collected or not.
	 */
	val appStartStatistics: Boolean
		get()
		{
			val key = resources.getString(R.string.app_start_statistics)
			val defaultValue = resources.getBoolean(R.bool.default_app_start_statistics)

			return getBoolean(key, defaultValue)
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

			return getString(key, defaultValue) ?: ""
		}

	/**
	 * Auto dismiss duration.
	 */
	val autoDismiss: Int
		get()
		{
			val key = resources.getString(R.string.auto_dismiss_key)
			val defaultValue = resources.getInteger(R.integer.default_auto_dismiss_index)

			return getInt(key, defaultValue)
		}

	/**
	 * @see .getAutoDismissTime
	 */
	val autoDismissTime: Int
		get() = getAutoDismissTime(autoDismiss)

	/**
	 * Alarm card height when it is collapsed.
	 */
	val cardHeightCollapsed: Int
		get()
		{
			val key = resources.getString(R.string.card_height_collapsed)
			val defaultValue = resources.getInteger(R.integer.default_card_height_collapsed)

			return getInt(key, defaultValue)
		}

	/**
	 * Alarm card height when it is collapsed, with dismiss showing.
	 */
	val cardHeightCollapsedDismiss: Int
		get()
		{
			val key = resources.getString(R.string.card_height_collapsed_dismiss)
			val defaultValue = resources.getInteger(R.integer.default_card_height_collapsed_dismiss)

			return getInt(key, defaultValue)
		}

	/**
	 * Alarm card height when it is expanded.
	 */
	val cardHeightExpanded: Int
		get()
		{
			val key = resources.getString(R.string.card_height_expanded)
			val defaultValue = resources.getInteger(R.integer.default_card_height_expanded)

			return getInt(key, defaultValue)
		}

	/**
	 * Check if the alarm card has been measured.
	 */
	val cardIsMeasured: Boolean
		get()
		{
			val key = resources.getString(R.string.card_is_measured)
			val defaultValue = resources.getBoolean(R.bool.default_card_is_measured)

			return getBoolean(key, defaultValue)
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

			return getInt(key, defaultValue)
		}

	/**
	 * Alarm days.
	 */
	val days: Int
		get()
		{
			val key = resources.getString(R.string.alarm_days_key)
			val defaultValue = resources.getInteger(R.integer.default_days)

			return getInt(key, defaultValue)
		}

	/**
	 * Days color.
	 */
	val daysColor: Int
		get()
		{
			val key = resources.getString(R.string.days_color_key)
			val defaultValue = resources.getInteger(R.integer.default_days_color)

			return getInt(key, defaultValue)
		}

	/**
	 * The index that corresponds to the time before an alarm goes off to start showing
	 * the dismiss early button by.
	 */
	val dismissEarlyIndex: Int
		get() = getDismissEarlyTimeToIndex(dismissEarlyTime)

	/**
	 * The time before an alarm goes off to start showing the dismiss early button by.
	 */
	val dismissEarlyTime: Int
		get()
		{
			val key = resources.getString(R.string.alarm_dismiss_early_time_key)
			val defaultValue = resources.getInteger(R.integer.default_dismiss_early_time)

			return getInt(key, defaultValue)
		}

	/**
	 * Whether easy snooze is enabled or not.
	 */
	val easySnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.easy_snooze_key)
			val defaultValue = resources.getBoolean(R.bool.default_easy_snooze)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether a new alarm card should be expanded or not.
	 */
	val expandNewAlarm: Boolean
		get()
		{
			val key = resources.getString(R.string.expand_new_alarm_key)
			val defaultValue = resources.getBoolean(R.bool.default_expand_new_alarm)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Check if the app has reached the counter limit.
	 */
	val isRateMyAppLimit: Boolean
		get()
		{
			val limit = resources.getInteger(R.integer.default_rate_my_app_limit)

			return rateMyAppCounter >= limit
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
	 * Max number of snoozes.
	 */
	val maxSnooze: Int
		get()
		{
			val key = resources.getString(R.string.max_snooze_key)
			val defaultValue = resources.getInteger(R.integer.default_max_snooze_index)

			return getInt(key, defaultValue)
		}

	/**
	 * @see .getMaxSnoozeValue
	 */
	val maxSnoozeValue: Int
		get() = getMaxSnoozeValue(maxSnooze)

	/**
	 * Media path.
	 */
	val mediaPath: String
		get()
		{
			val key = resources.getString(R.string.alarm_sound_key)

			return getString(key, "") ?: ""
		}

	/**
	 * Whether the missed alarm notifications should be displayed.
	 */
	val missedAlarmNotification: Boolean
		get()
		{
			val key = resources.getString(R.string.missed_alarm_key)
			val defaultValue = resources.getBoolean(R.bool.default_missed_alarm)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Name of the alarm.
	 */
	val name: String
		get()
		{
			val key = resources.getString(R.string.alarm_name_key)

			return getString(key, "") ?: ""
		}

	/**
	 * Name color.
	 */
	val nameColor: Int
		get()
		{
			val key = resources.getString(R.string.name_color_key)
			val defaultValue = resources.getInteger(R.integer.default_name_color)

			return getInt(key, defaultValue)
		}

	/**
	 * Whether the display next alarm should show time remaining for the next alarm.
	 */
	val nextAlarmFormat: Int
		get()
		{
			val key = resources.getString(R.string.next_alarm_format_key)
			val defaultValue = resources.getInteger(R.integer.default_next_alarm_format_index)

			return getInt(key, defaultValue)
		}

	/**
	 * PM color.
	 */
	val pmColor: Int
		get()
		{
			val key = resources.getString(R.string.pm_color_key)
			val defaultValue = resources.getInteger(R.integer.default_pm_color)

			return getInt(key, defaultValue)
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

			return getString(key, "") ?: ""
		}

	/**
	 * The previous system volume, before an alarm goes off.
	 */
	val previousVolume: Int
		get()
		{
			val key = resources.getString(R.string.sys_previous_volume)
			val defaultValue = resources.getInteger(R.integer.default_previous_volume)

			return getInt(key, defaultValue)
		}

	/**
	 * The app's rating counter.
	 */
	val rateMyAppCounter: Int
		get()
		{
			val key = resources.getString(R.string.app_rating_counter)
			val defaultValue = resources.getInteger(R.integer.default_rate_my_app_counter)

			return getInt(key, defaultValue)
		}

	/**
	 * Whether the alarm should be repeated or not.
	 */
	val repeat: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_repeat_key)
			val defaultValue = resources.getBoolean(R.bool.default_repeat)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether volume should be gradually increased or not.
	 */
	val shouldGraduallyIncreaseVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_should_gradually_increase_volume_key)
			val defaultValue = resources.getBoolean(R.bool.default_should_gradually_increase_volume)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether the main activity should be refreshed or not.
	 */
	val shouldRefreshMainActivity: Boolean
		get()
		{
			val key = resources.getString(R.string.app_should_refresh_main_activity)
			val defaultValue = resources.getBoolean(R.bool.default_app_should_refresh_main_activity)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether volume should be restricted or not.
	 */
	val shouldRestrictVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_should_restrict_volume_key)
			val defaultValue = resources.getBoolean(R.bool.default_should_restrict_volume)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether the alarm information should be shown or not.
	 */
	val showAlarmInfo: Boolean
		get()
		{
			val key = resources.getString(R.string.show_alarm_info_key)
			val defaultValue = resources.getBoolean(R.bool.default_show_alarm_info)

			return getBoolean(key, defaultValue)
		}

	/**
	 * The shuffle status.
	 */
	val shuffle: Boolean
		get()
		{
			val key = resources.getString(R.string.shuffle_playlist_key)
			val defaultValue = resources.getBoolean(R.bool.default_shuffle_playlist)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Nnooze duration.
	 */
	val snoozeDuration: Int
		get()
		{
			val key = resources.getString(R.string.snooze_duration_key)
			val defaultValue = resources.getInteger(R.integer.default_snooze_duration_index)

			return getInt(key, defaultValue)
		}

	/**
	 * @see .getSnoozeDurationValue
	 */
	val snoozeDurationValue: Int
		get() = getSnoozeDurationValue(snoozeDuration)

	/**
	 * The speak frequency value.
	 */
	val speakFrequency: Int
		get()
		{
			val key = resources.getString(R.string.speak_frequency_key)
			val defaultValue = resources.getInteger(R.integer.default_speak_frequency_index)

			return getInt(key, defaultValue)
		}

	/**
	 * The speak to me value.
	 */
	val speakToMe: Boolean
		get()
		{
			val key = resources.getString(R.string.speak_to_me_key)
			val defaultValue = resources.getBoolean(R.bool.default_speak_to_me)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Value indicating which day to start on.
	 */
	val startWeekOn: Int
		get()
		{
			val key = resources.getString(R.string.start_week_on_key)
			val defaultValue = resources.getInteger(R.integer.default_start_week_on_index)

			return getInt(key, defaultValue)
		}

	/**
	 * Theme color.
	 */
	val themeColor: Int
		get()
		{
			val key = resources.getString(R.string.theme_color_key)
			val defaultValue = resources.getInteger(R.integer.default_theme_color)

			return getInt(key, defaultValue)
		}

	/**
	 * Time color.
	 */
	val timeColor: Int
		get()
		{
			val key = resources.getString(R.string.time_color_key)
			val defaultValue = resources.getInteger(R.integer.default_time_color)

			return getInt(key, defaultValue)
		}

	/**
	 * Whether the upcoming alarm notifications should be displayed.
	 */
	val upcomingAlarmNotification: Boolean
		get()
		{
			val key = resources.getString(R.string.upcoming_alarm_key)
			val defaultValue = resources.getBoolean(R.bool.default_upcoming_alarm)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether dismiss early should be used or not.
	 */
	val useDismissEarly: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_use_dismiss_early_key)
			val defaultValue = resources.getBoolean(R.bool.default_use_dismiss_early)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether NFC is required or not.
	 */
	val useNfc: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_use_nfc_key)
			val defaultValue = resources.getBoolean(R.bool.default_use_nfc)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether the alarm should vibrate the phone or not.
	 */
	val vibrate: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_vibrate_key)
			val defaultValue = resources.getBoolean(R.bool.default_vibrate)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Alarm volume level.
	 */
	val volume: Int
		get()
		{
			val key = resources.getString(R.string.alarm_volume_key)
			val defaultValue = resources.getInteger(R.integer.default_volume)

			return getInt(key, defaultValue)
		}

	/**
	 * Whether the app was supported or not.
	 */
	val wasAppSupported: Boolean
		get()
		{
			val key = resources.getString(R.string.key_app_supported)
			val defaultValue = resources.getBoolean(R.bool.default_was_app_supported)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether the permission to ignore battery optimization was requested.
	 */
	val wasIgnoreBatteryOptimizationPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_ignore_battery_optimization_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_ignore_battery_optimization_permission_requested)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether the POST_NOTIFICATIONS permission was requested.
	 */
	val wasPostNotificationsPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_post_notifications_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_post_notifications_permission_requested)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Whether the SCHEDULE_EXACT_ALARM permission was requested.
	 */
	val wasScheduleExactAlarmPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_schedule_exact_alarm_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_schedule_exact_alarm_permission_requested)

			return getBoolean(key, defaultValue)
		}

	/**
	 * Edit whether this is the app's first run or not.
	 */
	fun editAppFirstRun(context: Context, first: Boolean)
	{
		val key = context.getString(R.string.app_first_run)
		saveBoolean(key, first, false)
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
		saveBoolean(key, shouldStart, false)
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
		saveString(key, source, false)
	}

	/**
	 * Edit the height of the alarm card when it is collapsed.
	 */
	fun editCardHeightCollapsed(height: Int)
	{
		val key = resources.getString(R.string.card_height_collapsed)
		saveInt(key, height, false)
	}

	/**
	 * Edit the height of the alarm card height it is collapsed, but the dismiss
	 * button is showing.
	 */
	fun editCardHeightCollapsedDismiss(height: Int)
	{
		val key = resources.getString(R.string.card_height_collapsed_dismiss)
		saveInt(key, height, false)
	}

	/**
	 * Edit the height of the alarm card when it is expanded.
	 */
	fun editCardHeightExpanded(height: Int)
	{
		val key = resources.getString(R.string.card_height_expanded)
		saveInt(key, height, false)
	}

	/**
	 * Edit the flag indicating if the alarm card has been measured or not.
	 */
	fun editCardIsMeasured(isMeasured: Boolean)
	{
		val key = resources.getString(R.string.card_is_measured)
		saveBoolean(key, isMeasured, false)
	}

	/**
	 * Edit the default dismiss early time when an alarm is created.
	 */
	fun editDismissEarlyTime(dismissEarly: Int)
	{
		val key = resources.getString(R.string.alarm_dismiss_early_time_key)
		saveInt(key, dismissEarly, false)
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
		saveString(key, version, false)
	}

	/**
	 * Edit the previous system volume, before an alarm goes off.
	 */
	fun editPreviousVolume(previous: Int)
	{
		val key = resources.getString(R.string.sys_previous_volume)
		saveInt(key, previous, false)
	}

	/**
	 * Edit the counter that will indicate whether it is time to show dialog to
	 * Rate My App.
	 */
	fun editRateMyAppCounter(counter: Int)
	{
		val key = resources.getString(R.string.app_rating_counter)
		saveInt(key, counter, false)
	}

	/**
	 * Edit the default value of a newly created alarm for if the volume should
	 * gradually be increased when an alarm is active.
	 */
	fun editShouldGraduallyIncreaseVolume(shouldIncrease: Boolean)
	{
		val key = resources.getString(R.string.alarm_should_gradually_increase_volume_key)
		saveBoolean(key, shouldIncrease, false)
	}

	/**
	 * Edit the default should restrict volume value when an alarm is created.
	 */
	fun editShouldRestrictVolume(shouldRestrict: Boolean)
	{
		val key = resources.getString(R.string.alarm_should_restrict_volume_key)
		saveBoolean(key, shouldRestrict, false)
	}

	/**
	 * Edit the value indicating whether the main activity should be refreshed or
	 * not.
	 */
	fun editShouldRefreshMainActivity(shouldRefresh: Boolean)
	{
		val key = resources.getString(R.string.app_should_refresh_main_activity)
		saveBoolean(key, shouldRefresh, false)
	}

	/**
	 * Edit the frequency at which the text-to-speech should go off when an alarm
	 * is going off.
	 */
	fun editSpeakFrequency(freq: Int)
	{
		val key = resources.getString(R.string.speak_frequency_key)
		saveInt(key, freq, false)
	}

	/**
	 * Edit the flag indicating whether text-to-speech should be used when an
	 * alarm goes off.
	 */
	fun editSpeakToMe(speak: Boolean)
	{
		val key = resources.getString(R.string.speak_to_me_key)
		saveBoolean(key, speak, false)
	}

	/**
	 * Edit the default use dismiss early when an alarm is created.
	 */
	fun editUseDismissEarly(useDismissEarly: Boolean)
	{
		val key = resources.getString(R.string.alarm_use_dismiss_early_key)
		saveBoolean(key, useDismissEarly, false)
	}

	/**
	 * Edit whether the app was supported or not.
	 */
	fun editWasAppSupported(wasSupported: Boolean)
	{
		val key = resources.getString(R.string.key_app_supported)
		saveBoolean(key, wasSupported, false)
	}

	/**
	 * Edit whether the permission to ignore battery optimization was requested.
	 */
	fun editWasIgnoreBatteryOptimizationPermissionRequested(requested: Boolean)
	{
		val key = resources.getString(R.string.key_permission_ignore_battery_optimization_requested)
		saveBoolean(key, requested, false)
	}

	/**
	 * Edit whether the POST_NOTIFICATIONS permission was requested.
	 */
	fun editWasPostNotificationsPermissionRequested(requested: Boolean)
	{
		val key = resources.getString(R.string.key_permission_post_notifications_requested)
		saveBoolean(key, requested, false)
	}

	/**
	 * Edit whether the SCHEDULE_EXACT_ALARM permission was requested.
	 */
	fun editWasScheduleExactAlarmPermissionRequested(requested: Boolean)
	{
		val key = resources.getString(R.string.key_permission_schedule_exact_alarm_requested)
		saveBoolean(key, requested, false)
	}


	/**
	 * Get a boolean value from the SharedPreferences instance.
	 *
	 * @return A boolean value from the SharedPreferences instance.
	 */
	private fun getBoolean(key: String?, defValue: Boolean): Boolean
	{
		return instance.getBoolean(key, defValue)
	}

	/**
	 * Get an integer value from the SharedPreferences instance.
	 *
	 * @return An integer value from the SharedPreferences instance.
	 */
	private fun getInt(key: String?, defValue: Int): Int
	{
		return instance.getInt(key, defValue)
	}

	/**
	 * Get a string value from the SharedPreferences instance.
	 *
	 * @return A string value from the SharedPreferences instance.
	 */
	fun getString(key: String?, defValue: String?): String?
	{
		return instance.getString(key, defValue)
	}

	/**
	 * Increment the rate my app counter.
	 */
	fun incrementRateMyApp()
	{
		editRateMyAppCounter(rateMyAppCounter + 1)
	}

	/**
	 * Set the rate my app counter to the rated value.
	 */
	fun ratedRateMyApp()
	{
		val rated = resources.getInteger(R.integer.default_rate_my_app_rated)
		editRateMyAppCounter(rated)
	}

	/**
	 * Save the changes that were made to the shared preference.
	 */
	fun save(editor: SharedPreferences.Editor, commit: Boolean = false)
	{
		if (commit)
		{
			editor.commit()
		}
		else
		{
			editor.apply()
		}
	}

	/**
	 * Save a boolean to the shared preference.
	 */
	private fun saveBoolean(key: String?, value: Boolean, commit: Boolean)
	{
		val editor = instance.edit()
			.putBoolean(key, value)

		save(editor, commit)
	}

	/**
	 * Save an int to the shared preference.
	 */
	private fun saveInt(key: String?, value: Int, commit: Boolean)
	{
		val editor = instance.edit()
			.putInt(key, value)

		save(editor, commit)
	}

	/**
	 * Save a string to the shared preference.
	 */
	private fun saveString(key: String?, value: String?, commit: Boolean)
	{
		val editor = instance.edit()
			.putString(key, value)

		save(editor, commit)
	}

	companion object
	{

		/**
		 * Get the summary text to use when displaying the auto dismiss widget.
		 *
		 * @return The summary text to use when displaying the auto dismiss widget.
		 */
		fun getAutoDismissSummary(res: Resources, index: Int): String
		{
			val summaries = res.getStringArray(R.array.auto_dismiss_summaries)

			return summaries[index]
		}

		/**
		 * Calculate the auto dismiss duration from an index value, corresponding to a
		 * location in the spainner widget.
		 *
		 * @return Calculate the auto dismiss duration from an index value, corresponding
		 *         to a location in the spainner widget.
		 */
		fun getAutoDismissTime(index: Int): Int
		{
			return if (index < 5) index else (index - 4) * 5
		}

		/**
		 * Get the time before an alarm goes off to start showing the dismiss early button by.
		 *
		 * @param  index  The index that corresponds to the time.
		 */
		fun getDismissEarlyIndexToTime(index: Int): Int
		{
			return if (index < 5) index + 1 else (index - 3) * 5
		}

		/**
		 * Get the index that corresponds to the time before an alarm goes off to start
		 * showing the dismiss early button by.
		 *
		 * @return The index that corresponds to the time before an alarm goes off to
		 *         start showing the dismiss early button by.
		 */
		fun getDismissEarlyTimeToIndex(time: Int): Int
		{
			return if (time <= 5) time - 1 else time / 5 + 3
		}

		/**
		 * Get the summary text to use when displaying the max snooze widget.
		 *
		 * @return The summary text to use when displaying the max snooze widget.
		 */
		fun getMaxSnoozeSummary(res: Resources, index: Int): String
		{
			val summaries = res.getStringArray(R.array.max_snooze_summaries)

			return summaries[index]
		}

		/**
		 * Calculate the max snooze duration from an index corresponding to a location
		 * in the spinner widget.
		 *
		 * @return Calculate the max snooze duration from an index corresponding to a
		 *         location in the spinner widget.
		 */
		fun getMaxSnoozeValue(index: Int): Int
		{
			return if (index == 11) -1 else index
		}

		/**
		 * Get the sound message.
		 *
		 * @return The sound message.
		 */
		fun getMediaMessage(context: Context, path: String?): String
		{
			return if (!path.isNullOrEmpty())
			{
				getTitle(context, path)
			}
			else
			{
				context.resources.getString(R.string.description_media)
			}
		}

		/**
		 * Get the name message.
		 *
		 * @return The name message.
		 */
		fun getNameMessage(res: Resources, name: String): String
		{
			// Get the empty alarm name
			val emptyName = res.getString(R.string.alarm_name)

			return name.ifEmpty { emptyName }
		}

		/**
		 * Get the summary text for the snooze duration widget.
		 *
		 * @return The summary text for the snooze duration widget.
		 */
		fun getSnoozeDurationSummary(res: Resources, index: Int): String
		{
			val summaries = res.getStringArray(R.array.snooze_duration_summaries)

			return summaries[index]
		}

		/**
		 * Calculate the snooze duration from an index value, corresponding to a location
		 * location in the spainner widget.
		 *
		 * @return Calculate the snooze duration from an index value, corresponding to a
		 *         location in the spainner widget.
		 */
		fun getSnoozeDurationValue(index: Int): Int
		{
			return if (index < 4) index + 1 else (index - 3) * 5
		}

	}

}