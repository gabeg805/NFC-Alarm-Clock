package com.nfcalarmclock.shared

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader

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
	var appFirstRun: Boolean
		get()
		{
			val key = resources.getString(R.string.app_first_run)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.app_first_run)

			saveBoolean(key, value)
		}

	/**
	 * Whether statistics should start to be collected or not.
	 */
	var appStartStatistics: Boolean
		get()
		{
			val key = resources.getString(R.string.app_start_statistics)
			val defaultValue = resources.getBoolean(R.bool.default_app_start_statistics)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.app_start_statistics)

			saveBoolean(key, value)
		}

	/**
	 * Audio source.
	 */
	var audioSource: String
		get()
		{
			val key = resources.getString(R.string.alarm_audio_source_key)
			val audioSources = resources.getStringArray(R.array.audio_sources)
			val defaultValue = audioSources[2]

			return instance.getString(key, defaultValue) ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_audio_source_key)

			saveString(key, value)
		}

	/**
	 * Auto dismiss time.
	 */
	var autoDismissTime: Int
		get()
		{
			val key = resources.getString(R.string.auto_dismiss_key)
			val defaultValue = 15

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.auto_dismiss_key)

			saveInt(key, value)
		}

	/**
	 * Old auto dismiss index.
	 */
	private val oldAutoDismissIndex: Int
		get()
		{
			val key = resources.getString(R.string.old_auto_dismiss_key)
			val defaultValue = resources.getInteger(R.integer.default_auto_dismiss_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Old auto dismiss time.
	 *
	 * This is used when updating database versions.
	 *
	 * @see .getAutoDismissTime
	 */
	val oldAutoDismissTime: Int
		get()
		{
			return if (oldAutoDismissIndex < 5)
			{
				oldAutoDismissIndex
			}
			else
			{
				(oldAutoDismissIndex - 4) * 5
			}
		}

	/**
	 * Auto snooze time.
	 */
	var autoSnoozeTime: Int
		get()
		{
			val key = resources.getString(R.string.auto_snooze_key)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.auto_snooze_key)

			saveInt(key, value)
		}

	/**
	 * Whether an alarm can be dismissed early or not.
	 */
	var canDismissEarly: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_use_dismiss_early_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_use_dismiss_early_key)

			saveBoolean(key, value)
		}

	/**
	 * Alarm card height when it is collapsed.
	 */
	var cardHeightCollapsed: Int
		get()
		{
			val key = resources.getString(R.string.card_height_collapsed)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.card_height_collapsed)

			saveInt(key, value)
		}

	/**
	 * Alarm card height when it is collapsed, with dismiss showing.
	 */
	var cardHeightCollapsedDismiss: Int
		get()
		{
			val key = resources.getString(R.string.card_height_collapsed_dismiss)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.card_height_collapsed_dismiss)

			saveInt(key, value)
		}

	/**
	 * Alarm card height when it is expanded.
	 */
	var cardHeightExpanded: Int
		get()
		{
			val key = resources.getString(R.string.card_height_expanded)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.card_height_expanded)

			saveInt(key, value)
		}

	/**
	 * Check if the alarm card has been measured.
	 */
	var cardIsMeasured: Boolean
		get()
		{
			val key = resources.getString(R.string.card_is_measured)
			val defaultValue = resources.getBoolean(R.bool.default_card_is_measured)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.card_is_measured)

			saveBoolean(key, value)
		}

	/**
	 * Get the current playing alarm media.
	 */
	var currentPlayingAlarmMedia: String
		get()
		{
			val key = resources.getString(R.string.key_current_playing_alarm_media)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_current_playing_alarm_media)

			saveString(key, value)
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
	var delayShowingWhatsNewDialogCounter: Int
		get()
		{
			val key = resources.getString(R.string.key_delay_showing_whats_new_dialog_counter)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_delay_showing_whats_new_dialog_counter)

			saveInt(key, value)
		}

	/**
	 * The time before an alarm goes off to start showing the dismiss early button by.
	 */
	var dismissEarlyTime: Int
		get()
		{
			val key = resources.getString(R.string.alarm_dismiss_early_time_key)
			val defaultValue = 30

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_dismiss_early_time_key)

			saveInt(key, value)
		}

	/**
	 * Whether easy snooze is enabled or not.
	 */
	var easySnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.easy_snooze_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.easy_snooze_key)

			saveBoolean(key, value)
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
	 * Number of seconds to turn off the flashlight.
	 */
	var flashlightOffDuration: String
		get()
		{
			val key = resources.getString(R.string.alarm_flashlight_off_duration_key)
			val defaultValue = "1"

			return instance.getString(key, defaultValue) ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_flashlight_off_duration_key)

			saveString(key, value)
		}

	/**
	 * Number of seconds to turn on the flashlight.
	 */
	var flashlightOnDuration: String
		get()
		{
			val key = resources.getString(R.string.alarm_flashlight_on_duration_key)
			val defaultValue = "1"

			return instance.getString(key, defaultValue) ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_flashlight_on_duration_key)

			saveString(key, value)
		}

	/**
	 * Strength level of the flashlight.
	 */
	var flashlightStrengthLevel: Int
		get()
		{
			val key = resources.getString(R.string.alarm_flashlight_strength_level_key)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_flashlight_strength_level_key)

			saveInt(key, value)
		}

	/**
	 * Amount of time to wait before gradually increasing the flashlight strength level
	 * another step.
	 */
	var graduallyIncreaseFlashlightStrengthLevelWaitTime: Int
		get()
		{
			val key = resources.getString(R.string.alarm_flashlight_gradually_increase_flashlight_strength_level_wait_time_key)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_flashlight_gradually_increase_flashlight_strength_level_wait_time_key)

			saveInt(key, value)
		}

	/**
	 * Amount of time to wait before gradually increasing the volume another step.
	 */
	var graduallyIncreaseVolumeWaitTime: Int
		get()
		{
			val key = resources.getString(R.string.alarm_gradually_increase_volume_wait_time_key)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_gradually_increase_volume_wait_time_key)

			saveInt(key, value)
		}

	/**
	 * Check if the app has reached the counter limit.
	 *
	 * Note: This is used in the Google Play version of NacRateMyApp.
	 */
	@Suppress("unused")
	val isRateMyAppLimit: Boolean
		get()
		{
			return rateMyAppCounter >= 50
		}

	/**
	 * Check if the app has been rated.
	 *
	 * Note: This is used in the Google Play version of NacRateMyApp.
	 */
	@Suppress("unused")
	val isRateMyAppRated: Boolean
		get()
		{
			val rated = resources.getInteger(R.integer.default_rate_my_app_rated)

			return rateMyAppCounter == rated
		}

	/**
	 * Max number of snoozes.
	 */
	var maxSnooze: Int
		get()
		{
			val key = resources.getString(R.string.max_snooze_key)
			val defaultValue = resources.getInteger(R.integer.default_max_snooze_index)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.max_snooze_key)

			saveInt(key, value)
		}

	/**
	 * Old index for the max number of snoozes.
	 */
	private val oldMaxSnoozeIndex: Int
		get()
		{
			val key = resources.getString(R.string.old_max_snooze_key)
			val defaultValue = resources.getInteger(R.integer.default_max_snooze_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Old max number of snoozes.
	 *
	 * This is used when updating database versions.
	 */
	val oldMaxSnoozeValue: Int
		get()
		{
			return if (oldMaxSnoozeIndex == 11)
			{
				-1
			}
			else
			{
				oldMaxSnoozeIndex
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
	var previousAppVersion: String
		get()
		{
			val key = resources.getString(R.string.previous_app_version)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.previous_app_version)

			saveString(key, value)
		}

	/**
	 * The previous system volume, before an alarm goes off.
	 */
	var previousVolume: Int
		get()
		{
			val key = resources.getString(R.string.sys_previous_volume)
			val defaultValue = resources.getInteger(R.integer.default_previous_volume)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.sys_previous_volume)

			saveInt(key, value)
		}

	/**
	 * The app's rating counter.
	 *
	 * Note: This is used in the Google Play version of NacRateMyApp.
	 */
	@Suppress("MemberVisibilityCanBePrivate")
	var rateMyAppCounter: Int
		get()
		{
			val key = resources.getString(R.string.app_rating_counter)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.app_rating_counter)

			saveInt(key, value)
		}

	/**
	 * Whether to recursively play the media in a directory.
	 */
	var recursivelyPlayMedia: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_recursively_play_media_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_recursively_play_media_key)

			saveBoolean(key, value)
		}

	/**
	 * Frequency at which to show the reminder, in units of minutes.
	 */
	var reminderFrequency: Int
		get()
		{
			val key = resources.getString(R.string.reminder_frequency_key)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.reminder_frequency_key)

			saveInt(key,  value)
		}

	/**
	 * Whether the flashlight should be blinked or not.
	 */
	var shouldBlinkFlashlight: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_flashlight_should_blink_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_flashlight_should_blink_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether to use delete the alarm after it is dismissed or not.
	 */
	var shouldDeleteAlarmAfterDismissed: Boolean
		get()
		{
			val key = resources.getString(R.string.should_delete_alarm_after_dismissed_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_delete_alarm_after_dismissed_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether volume should be gradually increased or not.
	 */
	var shouldGraduallyIncreaseVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_should_gradually_increase_volume_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_should_gradually_increase_volume_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether the main activity should be refreshed or not.
	 */
	var shouldRefreshMainActivity: Boolean
		get()
		{
			val key = resources.getString(R.string.app_should_refresh_main_activity)
			val defaultValue = resources.getBoolean(R.bool.default_app_should_refresh_main_activity)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.app_should_refresh_main_activity)

			saveBoolean(key, value)
		}

	/**
	 * Whether the alarm should be repeated or not.
	 */
	val shouldRepeat: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_repeat_key)
			val defaultValue = resources.getBoolean(R.bool.default_repeat)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether volume should be restricted or not.
	 */
	var shouldRestrictVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_should_restrict_volume_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_should_restrict_volume_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether to say the alarm name or not via text-to-speech.
	 */
	var shouldSayAlarmName: Boolean
		get()
		{
			val key = resources.getString(R.string.should_say_alarm_name_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_say_alarm_name_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether to say the current time or not via text-to-speech.
	 */
	var shouldSayCurrentTime: Boolean
		get()
		{
			val key = resources.getString(R.string.should_say_current_time_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_say_current_time_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show the alarm name or not.
	 */
	var shouldShowAlarmName: Boolean
		get()
		{
			val key = resources.getString(R.string.key_show_alarm_name)
			val defaultValue = resources.getBoolean(R.bool.default_show_alarm_name)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_show_alarm_name)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show the current date and time or not.
	 */
	var shouldShowCurrentDateAndTime: Boolean
		get()
		{
			val key = resources.getString(R.string.key_show_current_date_and_time)
			val defaultValue = resources.getBoolean(R.bool.default_show_current_date_and_time)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_show_current_date_and_time)

			saveBoolean(key, value)
		}

	/**
	 * Whether the Manage NFC Tags preference should be visible or not.
	 */
	var shouldShowManageNfcTagsPreference: Boolean
		get()
		{
			val key = resources.getString(R.string.should_show_manage_nfc_tags_setting)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_show_manage_nfc_tags_setting)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show music information or not.
	 */
	var shouldShowMusicInfo: Boolean
		get()
		{
			val key = resources.getString(R.string.key_show_music_info)
			val defaultValue = resources.getBoolean(R.bool.default_show_music_info)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_show_music_info)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show a reminder or not.
	 */
	var shouldShowReminder: Boolean
		get()
		{
			val key = resources.getString(R.string.should_show_reminder_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_show_reminder_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show or hide the flashlight button.
	 */
	val shouldShowFlashlightButton: Boolean
		get()
		{
			val key = resources.getString(R.string.show_hide_flashlight_button_key)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to show or hide the NFC button.
	 */
	val shouldShowNfcButton: Boolean
		get()
		{
			val key = resources.getString(R.string.show_hide_nfc_button_key)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to show or hide the vibrate button.
	 */
	val shouldShowVibrateButton: Boolean
		get()
		{
			val key = resources.getString(R.string.show_hide_vibrate_button_key)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to shuffle media or not.
	 */
	var shouldShuffleMedia: Boolean
		get()
		{
			val key = resources.getString(R.string.shuffle_playlist_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.shuffle_playlist_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether the flashlight should be used or not.
	 */
	var shouldUseFlashlight: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_use_flashlight_key)
			val defaultValue = resources.getBoolean(R.bool.default_use_flashlight)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.alarm_use_flashlight_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether to use the new alarm screen or not.
	 */
	var shouldUseNewAlarmScreen: Boolean
		get()
		{
			val key = resources.getString(R.string.key_use_new_alarm_screen)
			val defaultValue = resources.getBoolean(R.bool.default_use_new_alarm_screen)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_use_new_alarm_screen)

			saveBoolean(key, value)
		}

	/**
	 * Whether NFC is required or not.
	 */
	val shouldUseNfc: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_use_nfc_key)
			val defaultValue = resources.getBoolean(R.bool.default_use_nfc)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to use text-to-speech for the reminder or not.
	 */
	var shouldUseTtsForReminder: Boolean
		get()
		{
			val key = resources.getString(R.string.should_use_tts_for_reminder_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_use_tts_for_reminder_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether the alarm should vibrate the phone or not.
	 */
	val shouldVibrate: Boolean
		get()
		{
			val key = resources.getString(R.string.alarm_vibrate_key)
			val defaultValue = resources.getBoolean(R.bool.default_vibrate)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Snooze duration.
	 */
	var snoozeDuration: Int
		get()
		{
			val key = resources.getString(R.string.snooze_duration_key)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.snooze_duration_key)

			saveInt(key, value)
		}

	/**
	 * Old index for the snooze duration.
	 */
	private val oldSnoozeDurationIndex: Int
		get()
		{
			val key = resources.getString(R.string.old_snooze_duration_key)
			val defaultValue = resources.getInteger(R.integer.default_snooze_duration_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Snooze duration.
	 *
	 * This is used when updating database versions.
	 */
	val oldSnoozeDurationValue: Int
		get()
		{
			return if (oldSnoozeDurationIndex < 9)
			{
				oldSnoozeDurationIndex + 1
			}
			else
			{
				(oldSnoozeDurationIndex - 7) * 5
			}
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
	var timeToShowReminder: Int
		get()
		{
			val key = resources.getString(R.string.time_to_show_reminder_key)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.time_to_show_reminder_key)

			saveInt(key, value)
		}

	/**
	 * Text-to-speech frequency at which it will speak.
	 */
	var ttsFrequency: Int
		get()
		{
			val key = resources.getString(R.string.speak_frequency_key)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.speak_frequency_key)

			saveInt(key, value)
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
	var wasAppSupported: Boolean
		get()
		{
			val key = resources.getString(R.string.key_app_supported)
			val defaultValue = resources.getBoolean(R.bool.default_was_app_supported)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_supported)

			saveBoolean(key, value)
		}

	/**
	 * Whether the permission to ignore battery optimization was requested.
	 */
	var wasIgnoreBatteryOptimizationPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_ignore_battery_optimization_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_ignore_battery_optimization_permission_requested)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_permission_ignore_battery_optimization_requested)

			saveBoolean(key, value)
		}

	/**
	 * Whether the POST_NOTIFICATIONS permission was requested.
	 */
	var wasPostNotificationsPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_post_notifications_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_post_notifications_permission_requested)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_permission_post_notifications_requested)

			saveBoolean(key, value)
		}

	/**
	 * Whether the SCHEDULE_EXACT_ALARM permission was requested.
	 */
	var wasScheduleExactAlarmPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_schedule_exact_alarm_requested)
			val defaultValue = resources.getBoolean(R.bool.default_was_schedule_exact_alarm_permission_requested)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_permission_schedule_exact_alarm_requested)

			saveBoolean(key, value)
		}

	/**
	 * Copy shared preferences from a CSV file.
	 */
	fun copyFromCsv(context: Context, file: File)
	{
		// Open the file for reading
		context.openFileInput(file.name).use { input ->

			// Change the reading mechanism so it reads a character stream instead of a
			// byte stream
			BufferedReader(InputStreamReader(input)).use { reader ->

				// Read each line in the file
				while (true)
				{
					val line = reader.readLine() ?: break
					val (key, type, value) = line.split(",")

					// Save the value, depending on the type
					when (type)
					{
						"Boolean" -> saveBoolean(key, value.toBoolean())
						"Float"   -> {}
						"Int"     -> saveInt(key, value.toInt())
						"Long"    -> {}
						"String"  -> saveString(key, value)
						else      -> continue
					}

				}

			}

		}
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

	/**
	 * Write the all the shared preferences to a CSV file.
	 */
	fun writeToCsv(context: Context, file: File)
	{
		// List of keys to ignore
		val ignoreList = listOf(
			resources.getString(R.string.app_first_run),
			resources.getString(R.string.card_height_collapsed),
			resources.getString(R.string.card_height_collapsed_dismiss),
			resources.getString(R.string.card_height_expanded),
			resources.getString(R.string.card_is_measured),
			resources.getString(R.string.key_permission_ignore_battery_optimization_requested),
			resources.getString(R.string.key_permission_post_notifications_requested),
			resources.getString(R.string.key_permission_schedule_exact_alarm_requested)
		)

		// Save shared preferences
		context.openFileOutput(file.name, Context.MODE_PRIVATE).use { output ->

			// Get all shared preferences
			instance.all.forEach {

				// Key value pair
				val key = it.key
				var value = it.value

				// Check if key is in the ignore list
				if (key in ignoreList)
				{
					return@forEach
				}

				// Determine the type of the value
				val type = when (value)
				{
					is Boolean -> "Boolean"
					is Float   -> "Float"
					is Int     -> "Int"
					is Long    -> "Long"
					is String  -> "String"
					else       -> return@forEach
				}

				// Check if the value has any newlines and convert them to spaces
				if ((value is String) && value.contains("\n"))
				{
					value = value.replace("\n", " ")
				}

				// Build the line that will be written to the file
				val line = "${key},${type},${value}\n"

				// Write to the file
				output.write(line.toByteArray())
			}

		}
	}

}