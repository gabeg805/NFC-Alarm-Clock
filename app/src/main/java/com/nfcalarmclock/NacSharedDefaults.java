package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import java.util.EnumSet;

/**
 * Default values.
 */
public class NacSharedDefaults
	extends NacSharedResource
{

	/**
	 */
	public NacSharedDefaults(Context context)
	{
		super(context);
	}

	/**
	 */
	public NacSharedDefaults(Resources res)
	{
		super(res);
	}

	/**
	 * @return The AM color.
	 */
	public int getAmColor()
	{
		return this.getInteger(R.integer.default_am_color);
	}

	/**
	 * @return App first run.
	 */
	public boolean getAppFirstRun()
	{
		return this.getBoolean(R.bool.default_app_first_run);
	}

	/**
	 * @return The auto dismiss index.
	 */
	public int getAutoDismissIndex()
	{
		return this.getInteger(R.integer.default_auto_dismiss_index);
	}

	/**
	 * @return Height of the card when it is collapsed.
	 */
	public int getCardHeightCollapsed()
	{
		return this.getInteger(R.integer.default_card_height_collapsed);
	}

	/**
	 * @return Height of the card when it is collapsed (with dismiss showing).
	 */
	public int getCardHeightCollapsedDismiss()
	{
		return this.getInteger(R.integer.default_card_height_collapsed_dismiss);
	}

	/**
	 * @return Height of the card when it is expanded.
	 */
	public int getCardHeightExpanded()
	{
		return this.getInteger(R.integer.default_card_height_expanded);
	}

	/**
	 * @return Has the card been measured.
	 */
	public boolean getCardIsMeasured()
	{
		return this.getBoolean(R.bool.default_card_is_measured);
	}

	/**
	 * @return The color.
	 */
	public int getColor()
	{
		return this.getInteger(R.integer.default_color);
	}

	/**
	 * @return The day button style.
	 *
	 * 1: Filled-in buttons (Default)
	 * 2: Outlined buttons 
	 */
	public int getDayButtonStyle()
	{
		return this.getInteger(R.integer.default_day_button_style);
	}

	/**
	 * @return The days.
	 */
	public int getDays()
	{
		return this.getInteger(R.integer.default_days);
	}

	/**
	 * @return The days color.
	 */
	public int getDaysColor()
	{
		return this.getInteger(R.integer.default_days_color);
	}

	/**
	 * @return The days.
	 */
	public static int getDaysValue()
	{
		return NacCalendar.Days.daysToValue(
			EnumSet.of(NacCalendar.Day.MONDAY, NacCalendar.Day.TUESDAY,
				NacCalendar.Day.WEDNESDAY, NacCalendar.Day.THURSDAY,
				NacCalendar.Day.FRIDAY)
			);
	}

	/**
	 * @return The easy snooze option.
	 */
	public boolean getEasySnooze()
	{
		return this.getBoolean(R.bool.default_easy_snooze);
	}

	/**
	 * @return The expand new alarm.
	 */
	public boolean getExpandNewAlarm()
	{
		return this.getBoolean(R.bool.default_expand_new_alarm);
	}

	/**
	 * @return The max snooze index.
	 */
	public int getMaxSnoozeIndex()
	{
		return this.getInteger(R.integer.default_max_snooze_index);
	}

	/**
	 * @return The missed alarm.
	 */
	public boolean getMissedAlarm()
	{
		return this.getBoolean(R.bool.default_missed_alarm);
	}

	/**
	 * @return The name color.
	 */
	public int getNameColor()
	{
		return this.getInteger(R.integer.default_name_color);
	}

	/**
	 * @return The next alarm format index.
	 *
	 * 0 = Next alarm in 7 hours 30 minutes
	 * 1 = Next alarm on Mon, 7:00 AM
	 */
	public int getNextAlarmFormatIndex()
	{
		return this.getInteger(R.integer.default_next_alarm_format_index);
	}

	/**
	 * @return The PM color.
	 */
	public int getPmColor()
	{
		return this.getInteger(R.integer.default_pm_color);
	}

	/**
	 * @return The prevent app from closing.
	 */
	public boolean getPreventAppFromClosing()
	{
		return this.getBoolean(R.bool.default_prevent_app_from_closing);
	}

	/**
	 * @return Previous volume.
	 */
	public int getPreviousVolume()
	{
		return this.getInteger(R.integer.default_previous_volume);
	}

	/**
	 * @return Rate my app counter.
	 */
	public int getRateMyAppCounter()
	{
		return this.getInteger(R.integer.default_rate_my_app_counter);
	}

	/**
	 * @return Rate my app limit.
	 */
	public int getRateMyAppLimit()
	{
		return this.getInteger(R.integer.default_rate_my_app_limit);
	}

	/**
	 * @return Rate my app rated.
	 */
	public int getRateMyAppRated()
	{
		return this.getInteger(R.integer.default_rate_my_app_rated);
	}

	/**
	 * @return The repeat.
	 */
	public boolean getRepeat()
	{
		return this.getBoolean(R.bool.default_repeat);
	}

	/**
	 * @return The should refresh main activity.
	 */
	public boolean getShouldRefreshMainActivity()
	{
		return this.getBoolean(R.bool.default_app_should_refresh_main_activity);
	}

	/**
	 * @return The show alarm info.
	 */
	public boolean getShowAlarmInfo()
	{
		return this.getBoolean(R.bool.default_show_alarm_info);
	}

	/**
	 * @return Shuffle playlist.
	 */
	public boolean getShufflePlaylist()
	{
		return this.getBoolean(R.bool.default_shuffle_playlist);
	}

	/**
	 * @return Snooze count.
	 */
	public int getSnoozeCount()
	{
		return this.getInteger(R.integer.default_snooze_count);
	}

	/**
	 * @return The snooze duration index.
	 */
	public int getSnoozeDurationIndex()
	{
		return this.getInteger(R.integer.default_snooze_duration_index);
	}

	/**
	 * @return Speak frequency.
	 */
	public int getSpeakFrequencyIndex()
	{
		return this.getInteger(R.integer.default_speak_frequency_index);
	}

	/**
	 * @return Speak to me.
	 */
	public boolean getSpeakToMe()
	{
		return this.getBoolean(R.bool.default_speak_to_me);
	}

	/**
	 * @return The start week on index.
	 *
	 * 0 = Sunday
	 * 1 = Monday
	 * 6 = Saturday
	 */
	public int getStartWeekOnIndex()
	{
		return this.getInteger(R.integer.default_start_week_on_index);
	}

	/**
	 * @return The theme color.
	 */
	public int getThemeColor()
	{
		return this.getInteger(R.integer.default_theme_color);
	}

	/**
	 * @return The time color.
	 */
	public int getTimeColor()
	{
		return this.getInteger(R.integer.default_time_color);
	}

	/**
	 * @return The upcoming alarm.
	 */
	public boolean getUpcomingAlarm()
	{
		return this.getBoolean(R.bool.default_upcoming_alarm);
	}

	/**
	 * @return The use NFC.
	 */
	public boolean getUseNfc()
	{
		return this.getBoolean(R.bool.default_use_nfc);
	}

	/**
	 * @return The vibrate.
	 */
	public boolean getVibrate()
	{
		return this.getBoolean(R.bool.default_vibrate);
	}

	/**
	 * @return The volume.
	 */
	public int getVolume()
	{
		return this.getInteger(R.integer.default_volume);
	}

}
