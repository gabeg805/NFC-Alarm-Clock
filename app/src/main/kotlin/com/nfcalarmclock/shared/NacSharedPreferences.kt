package com.nfcalarmclock.shared

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.os.Build
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.util.getDeviceProtectedStorageContext
import com.nfcalarmclock.util.media.NacMedia
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
	val instance: SharedPreferences = PreferenceManager.getDefaultSharedPreferences(
		getDeviceProtectedStorageContext(context))

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
			val key = resources.getString(R.string.key_app_first_run)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_first_run)

			saveBoolean(key, value)
		}

	/**
	 * Whether statistics should start to be collected or not.
	 */
	var appStartStatistics: Boolean
		get()
		{
			val key = resources.getString(R.string.key_app_start_statistics)
			val defaultValue = resources.getBoolean(R.bool.default_app_start_statistics)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_start_statistics)

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
			val defaultValue = 900

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
			val defaultValue = 300

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
			val key = resources.getString(R.string.key_main_card_height_collapsed)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_main_card_height_collapsed)

			saveInt(key, value)
		}

	/**
	 * Alarm card height when it is collapsed, with dismiss showing.
	 */
	var cardHeightCollapsedDismiss: Int
		get()
		{
			val key = resources.getString(R.string.key_main_card_height_collapsed_dismiss)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_main_card_height_collapsed_dismiss)

			saveInt(key, value)
		}

	/**
	 * Alarm card height when it is expanded.
	 */
	var cardHeightExpanded: Int
		get()
		{
			val key = resources.getString(R.string.key_main_card_height_expanded)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_main_card_height_expanded)

			saveInt(key, value)
		}

	/**
	 * Check if the alarm card has been measured.
	 */
	var cardIsMeasured: Boolean
		get()
		{
			val key = resources.getString(R.string.key_main_card_is_measured)
			val defaultValue = resources.getBoolean(R.bool.default_card_is_measured)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_main_card_is_measured)

			saveBoolean(key, value)
		}

	/**
	 * Alarm icon color in the clock widget.
	 */
	var clockWidgetAlarmIconColor: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_color_alarm_icon)
			val defaultValue = resources.getInteger(R.integer.default_clock_widget_color_alarm_icon)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_color_alarm_icon)

			saveInt(key, value)
		}

	/**
	 * Alarm time color in the clock widget.
	 */
	var clockWidgetAlarmTimeColor: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_color_alarm_time)
			val defaultValue = resources.getInteger(R.integer.default_clock_widget_color_alarm_time)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_color_alarm_time)

			saveInt(key, value)
		}

	/**
	 * Text size of the alarm time in the clock widget.
	 */
	var clockWidgetAlarmTimeTextSize: Float
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_alarm_time)
			val defaultValue = 14f

			return instance.getFloat(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_alarm_time)

			saveFloat(key, value)
		}

	/**
	 * Color of AM/PM in the clock widget.
	 */
	var clockWidgetAmPmColor: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_color_am_pm)
			val defaultValue = resources.getInteger(R.integer.default_clock_widget_color_am_pm)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_color_am_pm)

			saveInt(key, value)
		}

	/**
	 * Text size of AM/PM in the clock widget.
	 */
	var clockWidgetAmPmTextSize: Float
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_am_pm)
			val defaultValue = 18f

			return instance.getFloat(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_am_pm)

			saveFloat(key, value)
		}

	/**
	 * Background color of the clock widget.
	 */
	var clockWidgetBackgroundColor: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_background_color)
			val defaultValue = resources.getInteger(R.integer.default_clock_widget_color_background)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_background_color)

			saveInt(key, value)
		}

	/**
	 * Background transparency of the clock widget.
	 */
	var clockWidgetBackgroundTransparency: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_background_transparency)
			val defaultValue = 100

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_background_transparency)

			saveInt(key, value)
		}

	/**
	 * Color of the date in the clock widget.
	 */
	var clockWidgetDateColor: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_color_date)
			val defaultValue = resources.getInteger(R.integer.default_clock_widget_color_date)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_color_date)

			saveInt(key, value)
		}

	/**
	 * Text size of the date in the clock widget.
	 */
	var clockWidgetDateTextSize: Float
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_date)
			val defaultValue = 14f

			return instance.getFloat(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_date)

			saveFloat(key, value)
		}

	/**
	 * Color of the hour in the clock widget.
	 */
	var clockWidgetHourColor: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_color_hour)
			val defaultValue = resources.getInteger(R.integer.default_clock_widget_color_hour)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_color_hour)

			saveInt(key, value)
		}

	/**
	 * Color of the minutes in the clock widget.
	 */
	var clockWidgetMinuteColor: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_color_minute)
			val defaultValue = resources.getInteger(R.integer.default_clock_widget_color_minute)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_color_minute)

			saveInt(key, value)
		}

	/**
	 * Text size of the time in the clock widget.
	 */
	var clockWidgetTimeTextSize: Float
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_time)
			val defaultValue = 78f

			return instance.getFloat(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_text_size_time)

			saveFloat(key, value)
		}

	/**
	 * Whether an alarm should be auto dismissed or not.
	 */
	var shouldAutoDismiss: Boolean
		get()
		{
			val key = resources.getString(R.string.should_auto_dismiss_key)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_auto_dismiss_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether an alarm should be auto snoozed or not.
	 */
	var shouldAutoSnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.should_auto_snooze_key)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.should_auto_snooze_key)

			saveBoolean(key, value)
		}

	/**
	 * Whether the alarm time should be bold or not in the clock widget.
	 */
	var shouldClockWidgetBoldAlarmTime: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_bold_alarm_time)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_bold_alarm_time)

			saveBoolean(key, value)
		}

	/**
	 * Whether AM/PM should be bold or not in the clock widget.
	 */
	var shouldClockWidgetBoldAmPm: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_bold_am_pm)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_bold_am_pm)

			saveBoolean(key, value)
		}

	/**
	 * Whether the date should be bold or not in the clock widget.
	 */
	var shouldClockWidgetBoldDate: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_bold_date)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_bold_date)

			saveBoolean(key, value)
		}

	/**
	 * Whether the hour should be bold or not in the clock widget.
	 */
	var shouldClockWidgetBoldHour: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_bold_hour)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_bold_hour)

			saveBoolean(key, value)
		}

	/**
	 * Whether the minutes should be bold or not in the clock widget.
	 */
	var shouldClockWidgetBoldMinute: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_bold_minute)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_bold_minute)

			saveBoolean(key, value)
		}

	/**
	 * Whether the alarm icon and time should be shown or not in the clock widget.
	 */
	var shouldClockWidgetShowAlarm: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_show_alarm)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_show_alarm)

			saveBoolean(key, value)
		}

	/**
	 * Whether the date should be shown or not in the clock widget.
	 */
	var shouldClockWidgetShowDate: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_show_date)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_show_date)

			saveBoolean(key, value)
		}

	/**
	 * Whether the time should be shown or not in the clock widget.
	 */
	var shouldClockWidgetShowTime: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_show_time)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_show_time)

			saveBoolean(key, value)
		}

	/**
	 * Get the current playing alarm media.
	 */
	var currentPlayingAlarmMedia: String
		get()
		{
			val key = resources.getString(R.string.key_media_current_playing_alarm)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_media_current_playing_alarm)

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
			val key = resources.getString(R.string.key_main_delay_showing_whats_new_dialog_counter)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_main_delay_showing_whats_new_dialog_counter)

			saveInt(key, value)
		}

	/**
	 * Delete after dismissed color.
	 */
	val deleteAfterDismissedColor: Int
		get()
		{
			val key = resources.getString(R.string.delete_after_dismissed_color_key)
			val defaultValue = resources.getInteger(R.integer.default_delete_alarm_after_dismissed_color)

			return instance.getInt(key, defaultValue)
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
	 * Event to fix any auto dismiss, auto snooze, or snooze duration values that are set
	 * to 0 in alarms.
	 */
	var eventFixZeroAutoDismissAndSnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.key_event_fix_zero_auto_dismiss_and_snooze)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_event_fix_zero_auto_dismiss_and_snooze)

			saveBoolean(key, value)
		}

	/**
	 * Event to update and backup media information in alarms, starting at database
	 * version 31.
	 */
	var eventUpdateAndBackupMediaInfoInAlarmsDbV31: Boolean
		get()
		{
			val key = resources.getString(R.string.key_event_update_and_backup_media_info_in_alarms_db_v31)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_event_update_and_backup_media_info_in_alarms_db_v31)

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
	 * Check if the selected media for an alarm is not available.
	 */
	var isSelectedMediaForAlarmNotAvailable: Boolean
		get()
		{
			val key = resources.getString(R.string.key_media_is_selected_for_alarm_not_available)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_media_is_selected_for_alarm_not_available)

			saveBoolean(key, value)
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
	 * Local media path.
	 */
	var localMediaPath: String
		get()
		{
			val key = resources.getString(R.string.key_general_default_alarm_local_media_path)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_general_default_alarm_local_media_path)

			saveString(key, value)
		}

	/**
	 * Media artist.
	 */
	var mediaArtist: String
		get()
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_artist)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_artist)

			saveString(key, value)
		}

	/**
	 * Media path.
	 */
	val mediaPath: String
		get()
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_path)

			return instance.getString(key, "") ?: ""
		}

	/**
	 * Media title.
	 */
	var mediaTitle: String
		get()
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_title)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_title)

			saveString(key, value)
		}

	/**
	 * Media type.
	 */
	var mediaType: Int
		get()
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_type)
			val defaultValue = NacMedia.TYPE_NONE

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_type)

			saveInt(key, value)
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
			val key = resources.getString(R.string.key_app_previous_version)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_previous_version)

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
			val key = resources.getString(R.string.key_app_rating_counter)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_rating_counter)

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
			val key = resources.getString(R.string.key_main_should_refresh_activity)
			val defaultValue = resources.getBoolean(R.bool.default_app_should_refresh_main_activity)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_main_should_refresh_activity)

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
			val key = resources.getString(R.string.key_settings_should_show_manage_nfc_tags)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_settings_should_show_manage_nfc_tags)

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
	 * Skip next alarm color.
	 */
	val skipNextAlarmColor: Int
		get()
		{
			val key = resources.getString(R.string.skip_next_alarm_color_key)
			val defaultValue = resources.getInteger(R.integer.default_skip_next_alarm_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Snooze duration.
	 */
	var snoozeDuration: Int
		get()
		{
			val key = resources.getString(R.string.snooze_duration_key)
			val defaultValue = 300

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
		// List of keys to ignore
		val ignoreList = getCsvKeysToIgnore()

		// Open the file for reading
		context.openFileInput(file.name).use { input ->

			// Change the reading mechanism so it reads a character stream instead of a
			// byte stream
			BufferedReader(InputStreamReader(input)).use { reader ->

				// Read each line in the file
				while (true)
				{
					// Read the key, type, and value from the line
					val line = reader.readLine() ?: break
					val (key, type, value) = line.split(",")

					// Check if key is in the ignore list
					if (key in ignoreList)
					{
						continue
					}

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
	 * Get a list of keys to ignore when reading to/writing from a CSV file.
	 */
	private fun getCsvKeysToIgnore(): List<String>
	{
		return listOf(
			resources.getString(R.string.key_app_first_run),
			resources.getString(R.string.key_main_should_refresh_activity),
			resources.getString(R.string.key_main_card_height_collapsed),
			resources.getString(R.string.key_main_card_height_collapsed_dismiss),
			resources.getString(R.string.key_main_card_height_expanded),
			resources.getString(R.string.key_main_card_is_measured),
			resources.getString(R.string.key_media_current_playing_alarm),
			resources.getString(R.string.key_media_is_selected_for_alarm_not_available),
			resources.getString(R.string.key_main_delay_showing_whats_new_dialog_counter),
			resources.getString(R.string.key_permission_ignore_battery_optimization_requested),
			resources.getString(R.string.key_permission_post_notifications_requested),
			resources.getString(R.string.key_permission_schedule_exact_alarm_requested),
			resources.getString(R.string.key_app_previous_version),
			resources.getString(R.string.sys_previous_volume),
			resources.getString(R.string.old_auto_dismiss_key),
			resources.getString(R.string.old_max_snooze_key),
			resources.getString(R.string.old_snooze_duration_key),
		)
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
	 * Save a float to the shared preference.
	 */
	private fun saveFloat(key: String, value: Float)
	{
		instance.edit()
			.putFloat(key, value)
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
		val ignoreList = getCsvKeysToIgnore()

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

	companion object
	{

		/**
		 * Moved the shared preference to device protected storage.
		 */
		fun moveToDeviceProtectedStorage(context: Context): Boolean
		{
			// Check if shared preferences should be moved to device protected storage
			return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
			{
				// Get default shared preferences file name
				val name = "${context.packageName}_preferences"
				val file = File("${context.dataDir}/shared_prefs/${name}.xml")

				// Check if the file exists
				if (file.exists())
				{
					// Get device protected storage context
					val deviceContext = context.createDeviceProtectedStorageContext()

					// Move shared preferences to device encrypted storage
					deviceContext.moveSharedPreferencesFrom(context, name)
				}
				else
				{
					// No need to move shared preferences because it has already been moved
					false
				}
			}
			else
			{
				// Device does not support direct boot
				false
			}
		}

	}

}