package com.nfcalarmclock.shared

import android.content.Context
import android.content.SharedPreferences
import android.content.res.Resources
import android.view.Gravity
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.util.media.NacMedia
import java.io.BufferedReader
import java.io.File
import java.io.InputStreamReader
import java.util.Calendar
import androidx.core.content.edit
import com.nfcalarmclock.system.daysToValue

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
			val key = resources.getString(R.string.key_color_am)
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
	 * App's next alarm time in milliseconds.
	 */
	var appNextAlarmTimeMillis: Long
		get()
		{
			val key = resources.getString(R.string.key_app_next_alarm_time_millis)
			val defaultValue = 0L

			return instance.getLong(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_next_alarm_time_millis)

			saveLong(key, value)
		}

	/**
	 * App's next alarm timezone ID.
	 */
	var appNextAlarmTimezoneId: String
		get()
		{
			val key = resources.getString(R.string.key_app_next_alarm_timezone_id)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_next_alarm_timezone_id)

			saveString(key, value)
		}

	/**
	 * App's next alarm time in milliseconds.
	 */
	var appShouldSaveNextAlarm: Boolean
		get()
		{
			val key = resources.getString(R.string.key_app_next_alarm_should_save_app_alarm)

			return instance.getBoolean(key, true)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_app_next_alarm_should_save_app_alarm)

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
			val key = resources.getString(R.string.key_default_alarm_audio_source)
			val audioSources = resources.getStringArray(R.array.audio_sources)
			val defaultValue = audioSources[2]

			return instance.getString(key, defaultValue) ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_audio_source)

			saveString(key, value)
		}

	/**
	 * Auto dismiss time.
	 */
	var autoDismissTime: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_auto_dismiss_time)
			val defaultValue = 900

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_auto_dismiss_time)

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
			val key = resources.getString(R.string.key_default_alarm_snooze_auto_snooze_time)
			val defaultValue = 300

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_auto_snooze_time)

			saveInt(key, value)
		}

	/**
	 * Whether an alarm can be dismissed early or not.
	 */
	var canDismissEarly: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_can_dismiss_early)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_can_dismiss_early)

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
	 * Position of the alarm time above the date in the clock widget.
	 */
	var clockWidgetAlarmTimePositionAboveDate: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_position_alarm_time_above_date)

			return instance.getBoolean(key, false)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_position_alarm_time_above_date)

			saveBoolean(key, value)
		}

	/**
	 * Position of the alarm time below the date in the clock widget.
	 */
	var clockWidgetAlarmTimePositionBelowDate: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_position_alarm_time_below_date)

			return instance.getBoolean(key, false)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_position_alarm_time_below_date)

			saveBoolean(key, value)
		}

	/**
	 * Position of the alarm time same line as the date in the clock widget.
	 */
	var clockWidgetAlarmTimePositionSameLineAsDate: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_position_alarm_time_same_line_as_date)

			return instance.getBoolean(key, true)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_position_alarm_time_same_line_as_date)

			saveBoolean(key, value)
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
	 * General alignment of the views in the clock widget.
	 */
	var clockWidgetGeneralAlignment: Int
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_general_alignment)

			return instance.getInt(key, Gravity.CENTER_HORIZONTAL)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_general_alignment)

			saveInt(key, value)
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
			val key = resources.getString(R.string.key_default_alarm_dismiss_should_auto_dismiss)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_should_auto_dismiss)

			saveBoolean(key, value)
		}

	/**
	 * Whether an alarm should be auto snoozed or not.
	 */
	var shouldAutoSnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_should_auto_snooze)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_should_auto_snooze)

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
	 * Whether to show app specific alarms in the clock widget.
	 */
	var shouldClockWidgetShowAppSpecificAlarms: Boolean
		get()
		{
			val key = resources.getString(R.string.key_clock_widget_show_app_specific_alarms)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_clock_widget_show_app_specific_alarms)

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
	 * Alarm date.
	 */
	var date: String
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_date)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_date)

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
			val key = resources.getString(R.string.key_style_day_button)
			val defaultValue = resources.getInteger(R.integer.default_day_button_style)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Alarm days.
	 */
	var days: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_days)
			val defaultValue = resources.getInteger(R.integer.default_days)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_days)

			saveInt(key, value)
		}

	/**
	 * Days color.
	 */
	val daysColor: Int
		get()
		{
			val key = resources.getString(R.string.key_color_days)
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
			val key = resources.getString(R.string.key_color_delete_after_dismissed)
			val defaultValue = resources.getInteger(R.integer.default_delete_alarm_after_dismissed_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * The time before an alarm goes off to start showing the dismiss early button by.
	 */
	var dismissEarlyTime: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_early_time)
			val defaultValue = 30

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_early_time)

			saveInt(key, value)
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
			val key = resources.getString(R.string.key_tweak_expand_new_alarm)
			val defaultValue = resources.getBoolean(R.bool.default_expand_new_alarm)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Number of seconds to turn off the flashlight.
	 */
	var flashlightOffDuration: String
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_off_duration)
			val defaultValue = "1"

			return instance.getString(key, defaultValue) ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_off_duration)

			saveString(key, value)
		}

	/**
	 * Number of seconds to turn on the flashlight.
	 */
	var flashlightOnDuration: String
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_on_duration)
			val defaultValue = "1"

			return instance.getString(key, defaultValue) ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_on_duration)

			saveString(key, value)
		}

	/**
	 * Strength level of the flashlight.
	 */
	var flashlightStrengthLevel: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_strength_level)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_strength_level)

			saveInt(key, value)
		}

	/**
	 * Amount of time to wait before gradually increasing the flashlight strength level
	 * another step.
	 */
	var graduallyIncreaseFlashlightStrengthLevelWaitTime: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_gradually_increase_flashlight_strength_level_wait_time)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_gradually_increase_flashlight_strength_level_wait_time)

			saveInt(key, value)
		}

	/**
	 * Amount of time to wait before gradually increasing the volume another step.
	 */
	var graduallyIncreaseVolumeWaitTime: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_volume_gradually_increase_volume_wait_time)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_volume_gradually_increase_volume_wait_time)

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
			val key = resources.getString(R.string.key_default_alarm_snooze_max_snooze)
			val defaultValue = resources.getInteger(R.integer.default_max_snooze_index)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_max_snooze)

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
	var mediaPath: String
		get()
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_path)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_general_default_alarm_media_path)

			saveString(key, value)
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
			val key = resources.getString(R.string.key_missed_alarm)
			val defaultValue = resources.getBoolean(R.bool.default_missed_alarm)

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Name of the alarm.
	 */
	var name: String
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_name)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_name)

			saveString(key, value)
		}

	/**
	 * Name color.
	 */
	val nameColor: Int
		get()
		{
			val key = resources.getString(R.string.key_color_name)
			val defaultValue = resources.getInteger(R.integer.default_name_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Whether the display next alarm should show time remaining for the next alarm.
	 */
	val nextAlarmFormat: Int
		get()
		{
			val key = resources.getString(R.string.key_tweak_next_alarm_format)
			val defaultValue = resources.getInteger(R.integer.default_next_alarm_format_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Order in which to dismiss NFC tags when multiple are selected.
	 */
	var nfcTagDismissOrder: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_nfc_tag_dismiss_order)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_nfc_tag_dismiss_order)

			saveInt(key, value)
		}

	/**
	 * ID of the NFC tag that needs to be used to dismiss the alarm.
	 */
	var nfcTagId: String
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_nfc_tag_id)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_nfc_tag_id)

			saveString(key, value)
		}

	/**
	 * PM color.
	 */
	val pmColor: Int
		get()
		{
			val key = resources.getString(R.string.key_color_pm)
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
			val key = resources.getString(R.string.key_default_alarm_media_should_recursively_play_media)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_media_should_recursively_play_media)

			saveBoolean(key, value)
		}

	/**
	 * Frequency at which to show the reminder, in units of minutes.
	 */
	var reminderFrequency: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_frequency)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_frequency)

			saveInt(key,  value)
		}

	/**
	 * Frequency at which to repeat the alarm.
	 */
	var repeatFrequency: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_frequency)
			val defaultValue = 1

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_frequency)

			saveInt(key,  value)
		}

	/**
	 * Days to run before starting the frequency at which to repeat the alarm.
	 */
	var repeatFrequencyDaysToRunBeforeStarting: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_frequency_days_to_run_before_starting)
			val defaultValue = NacCalendar.Day.WEEK.daysToValue()

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_frequency_days_to_run_before_starting)

			saveInt(key,  value)
		}

	/**
	 * Units for the frequency at which to repeat the alarm.
	 */
	var repeatFrequencyUnits: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_frequency_units)
			val defaultValue = 4

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_frequency_units)

			saveInt(key,  value)
		}

	/**
	 * Whether the flashlight should be blinked or not.
	 */
	var shouldBlinkFlashlight: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_should_blink)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_should_blink)

			saveBoolean(key, value)
		}

	/**
	 * Whether to use delete the alarm after it is dismissed or not.
	 */
	var shouldDeleteAlarmAfterDismissed: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_should_delete_alarm_after_dismissed)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_should_delete_alarm_after_dismissed)

			saveBoolean(key, value)
		}

	/**
	 * Whether easy snooze is enabled or not.
	 */
	var shouldEasySnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_should_use_easy_snooze)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_should_use_easy_snooze)

			saveBoolean(key, value)
		}

	/**
	 * Whether volume should be gradually increased or not.
	 */
	var shouldGraduallyIncreaseVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_volume_should_gradually_increase_volume)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_volume_should_gradually_increase_volume)

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
	var shouldRepeat: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_should_repeat)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_repeat_should_repeat)

			saveBoolean(key, value)
		}

	/**
	 * Whether volume should be restricted or not.
	 */
	var shouldRestrictVolume: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_volume_should_restrict_volume)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_volume_should_restrict_volume)

			saveBoolean(key, value)
		}

	/**
	 * Whether to save battery when an alarm is active or not.
	 */
	var shouldSaveBatteryInAlarmScreen: Boolean
		get()
		{
			val key = resources.getString(R.string.key_alarm_screen_battery_saver)
			val defaultValue = resources.getBoolean(R.bool.default_alarm_screen_battery_saver)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_alarm_screen_battery_saver)

			saveBoolean(key, value)
		}

	/**
	 * Whether to say the alarm name or not via text-to-speech.
	 */
	var shouldSayAlarmName: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_tts_should_say_alarm_name)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_tts_should_say_alarm_name)

			saveBoolean(key, value)
		}

	/**
	 * Whether to say the current time or not via text-to-speech.
	 */
	var shouldSayCurrentTime: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_tts_should_say_current_time)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_tts_should_say_current_time)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show the alarm name or not.
	 */
	var shouldShowAlarmName: Boolean
		get()
		{
			val key = resources.getString(R.string.key_alarm_screen_show_alarm_name)
			val defaultValue = resources.getBoolean(R.bool.default_alarm_screen_show_alarm_name)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_alarm_screen_show_alarm_name)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show the current date and time or not.
	 */
	var shouldShowCurrentDateAndTime: Boolean
		get()
		{
			val key = resources.getString(R.string.key_alarm_screen_show_current_date_and_time)
			val defaultValue = resources.getBoolean(R.bool.default_alarm_screen_show_current_date_and_time)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_alarm_screen_show_current_date_and_time)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show a notification for dismiss early or not.
	 */
	var shouldShowDismissEarlyNotification: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_should_show_dismiss_early_notification)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_dismiss_should_show_dismiss_early_notification)

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
			val key = resources.getString(R.string.key_alarm_screen_show_music_info)
			val defaultValue = resources.getBoolean(R.bool.default_alarm_screen_show_music_info)

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_alarm_screen_show_music_info)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show a reminder or not.
	 */
	var shouldShowReminder: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_should_show_reminder)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_should_show_reminder)

			saveBoolean(key, value)
		}

	/**
	 * Whether to show or hide the flashlight button.
	 */
	val shouldShowFlashlightButton: Boolean
		get()
		{
			val key = resources.getString(R.string.key_show_hide_flashlight_button)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to show or hide the NFC button.
	 */
	val shouldShowNfcButton: Boolean
		get()
		{
			val key = resources.getString(R.string.key_show_hide_nfc_button)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to show or hide the vibrate button.
	 */
	val shouldShowVibrateButton: Boolean
		get()
		{
			val key = resources.getString(R.string.key_show_hide_vibrate_button)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}

	/**
	 * Whether to shuffle media or not.
	 */
	var shouldShuffleMedia: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_media_should_shuffle_media)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_media_should_shuffle_media)

			saveBoolean(key, value)
		}

	/**
	 * Whether volume snooze is enabled or not.
	 */
	var shouldVolumeSnooze: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_should_use_volume_snooze)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_should_use_volume_snooze)

			saveBoolean(key, value)
		}

	/**
	 * Whether the flashlight should be used or not.
	 */
	var shouldUseFlashlight: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_should_use_flashlight)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_flashlight_should_use_flashlight)

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
	var shouldUseNfc: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_nfc_should_use_nfc)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_nfc_should_use_nfc)

			saveBoolean(key, value)
		}

	/**
	 * Whether to use text-to-speech for the reminder or not.
	 */
	var shouldUseTtsForReminder: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_should_use_tts_for_reminder)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_should_use_tts_for_reminder)

			saveBoolean(key, value)
		}

	/**
	 * Whether the alarm should vibrate the phone or not.
	 */
	var shouldVibrate: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_should_vibrate)
			val defaultValue = true

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_should_vibrate)

			saveBoolean(key, value)
		}

	/**
	 * Whether to vibrate using a pattern or not.
	 */
	var shouldVibratePattern: Boolean
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_should_vibrate_pattern)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_should_vibrate_pattern)

			saveBoolean(key, value)
		}

	/**
	 * Skip next alarm color.
	 */
	val skipNextAlarmColor: Int
		get()
		{
			val key = resources.getString(R.string.key_color_skip_next_alarm)
			val defaultValue = resources.getInteger(R.integer.default_skip_next_alarm_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Snooze duration.
	 */
	var snoozeDuration: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_duration)
			val defaultValue = 300

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_snooze_duration)

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
			val key = resources.getString(R.string.key_style_start_week_on)
			val defaultValue = resources.getInteger(R.integer.default_start_week_on_index)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Theme color.
	 */
	val themeColor: Int
		get()
		{
			val key = resources.getString(R.string.key_color_theme)
			val defaultValue = resources.getInteger(R.integer.default_theme_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * Time color.
	 */
	val timeColor: Int
		get()
		{
			val key = resources.getString(R.string.key_color_time)
			val defaultValue = resources.getInteger(R.integer.default_time_color)

			return instance.getInt(key, defaultValue)
		}

	/**
	 * The time to start showing a reminder.
	 */
	var timeToShowReminder: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_time_to_show_reminder)
			val defaultValue = 5

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_reminder_time_to_show_reminder)

			saveInt(key, value)
		}

	/**
	 * Text-to-speech frequency at which it will speak.
	 */
	var ttsFrequency: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_tts_speak_frequency)
			val defaultValue = 0

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_tts_speak_frequency)

			saveInt(key, value)
		}

	/**
	 * Text-to-speech speech rate.
	 */
	var ttsSpeechRate: Float
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_tts_speech_rate)

			return instance.getFloat(key, 0.7f)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_tts_speech_rate)

			saveFloat(key, value)
		}

	/**
	 * Text-to-speech voice name.
	 */
	var ttsVoice: String
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_tts_voice)

			return instance.getString(key, "") ?: ""
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_tts_voice)

			saveString(key, value)
		}

	/**
	 * Duration to vibrate the device for.
	 */
	var vibrateDuration: Long
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_duration)
			val defaultValue = 500L

			return instance.getLong(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_duration)

			saveLong(key, value)
		}

	/**
	 * Number of times to repeat the vibration.
	 */
	var vibrateRepeatPattern: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_repeat_pattern)
			val defaultValue = 3

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_repeat_pattern)

			saveInt(key, value)
		}

	/**
	 * Amount of time to wait in between vibrations.
	 */
	var vibrateWaitTime: Long
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_wait_time)
			val defaultValue = 1000L

			return instance.getLong(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_wait_time)

			saveLong(key, value)
		}

	/**
	 * Amount of time to wait after the vibration has been repeated the set number of
	 * times.
	 */
	var vibrateWaitTimeAfterPattern: Long
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_wait_time_after_pattern)
			val defaultValue = 2000L

			return instance.getLong(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_vibrate_wait_time_after_pattern)

			saveLong(key, value)
		}

	/**
	 * Alarm volume level.
	 */
	var volume: Int
		get()
		{
			val key = resources.getString(R.string.key_default_alarm_volume)
			val defaultValue = resources.getInteger(R.integer.default_volume)

			return instance.getInt(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_default_alarm_volume)

			saveInt(key, value)
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
			val defaultValue = false

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
			val defaultValue = false

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
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_permission_schedule_exact_alarm_requested)

			saveBoolean(key, value)
		}

	/**
	 * Whether the SYSTEM_ALERT_WINDOW permission was requested.
	 */
	var wasSystemAlertWindowPermissionRequested: Boolean
		get()
		{
			val key = resources.getString(R.string.key_permission_system_alert_window_requested)
			val defaultValue = false

			return instance.getBoolean(key, defaultValue)
		}
		set(value)
		{
			val key = resources.getString(R.string.key_permission_system_alert_window_requested)

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
	 * Run the event to fix any auto dismiss, auto snooze, or snooze duration values
	 * that are set to 0 in alarms.
	 */
	suspend fun runEventFixZeroAutoDismissAndSnooze(
		allAlarms: List<NacAlarm>,
		onAlarmChanged: suspend (NacAlarm) -> Unit = {})
	{
		// Set the default values
		val defaultAutoDismissTime = 900
		val defaultAutoSnoozeTime = 300
		val defaultSnoozeDuration = 300

		// Auto dismiss for the shared preferences
		if (autoDismissTime == 0)
		{
			autoDismissTime = defaultAutoDismissTime
		}

		// Auto snooze for the shared preferences
		if (autoSnoozeTime == 0)
		{
			autoSnoozeTime = defaultAutoSnoozeTime
		}

		// Snooze duration for the shared preferences
		if (snoozeDuration == 0)
		{
			snoozeDuration = defaultSnoozeDuration
		}

		// Iterate over each alarm that has the auto dismiss, auto snooze, or snooze
		// duration set incorrectly
		allAlarms
			.filter {
				(it.autoDismissTime == 0) || (it.autoSnoozeTime == 0) || (it.snoozeDuration == 0)
			}
			.forEach { alarm ->

				// Auto dismiss
				if (alarm.autoDismissTime == 0)
				{
					alarm.autoDismissTime = defaultAutoDismissTime
				}

				// Auto snooze
				if (alarm.autoSnoozeTime == 0)
				{
					alarm.autoSnoozeTime = defaultAutoSnoozeTime
				}

				// Snooze duration
				if (alarm.snoozeDuration == 0)
				{
					alarm.snoozeDuration = defaultSnoozeDuration
				}

				// Call the listener when the alarm is changed
				onAlarmChanged(alarm)

			}

		// Mark the event as completed
		eventFixZeroAutoDismissAndSnooze = true
	}

	/**
	 * Save a boolean to the shared preference.
	 */
	private fun saveBoolean(key: String, value: Boolean)
	{
		instance.edit {
			putBoolean(key, value)
		}
	}

	/**
	 * Save a float to the shared preference.
	 */
	private fun saveFloat(key: String, value: Float)
	{
		instance.edit {
			putFloat(key, value)
		}
	}

	/**
	 * Save an int to the shared preference.
	 */
	private fun saveInt(key: String, value: Int)
	{
		instance.edit {
			putInt(key, value)
		}
	}

	/**
	 * Save an long to the shared preference.
	 */
	private fun saveLong(key: String, value: Long)
	{
		instance.edit {
			putLong(key, value)
		}
	}

	/**
	 * Find and save the next alarm.
	 */
	fun saveNextAlarm(allAlarms: List<NacAlarm>, snoozeCal: Calendar? = null)
	{
		// Find the next alarm
		val nextAlarm = NacCalendar.getNextAlarm(allAlarms)

		// Determine the time in milliseconds to use
		val millis = nextAlarm?.calendar?.let {

			// Snooze time
			if (snoozeCal?.before(it) == true)
			{
				snoozeCal.timeInMillis
			}
			// Next calendar time
			else
			{
				it.timeInMillis
			}

		} ?: 0L

		// Save the next alarm information
		appNextAlarmTimezoneId = Calendar.getInstance().timeZone.id
		appNextAlarmTimeMillis = millis
	}

	/**
	 * Save a string to the shared preference.
	 */
	private fun saveString(key: String, value: String?)
	{
		instance.edit {
			putString(key, value)
		}
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
			// Get default shared preferences file name
			val name = "${context.packageName}_preferences"
			val file = File("${context.dataDir}/shared_prefs/${name}.xml")

			// Check if the file exists
			return if (file.exists())
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

	}

}