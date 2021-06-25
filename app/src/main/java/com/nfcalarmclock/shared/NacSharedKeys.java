package com.nfcalarmclock.shared;

import android.content.Context;
import android.content.res.Resources;

import com.nfcalarmclock.R;

import java.util.ArrayList;
import java.util.List;

/**
 * Keys of each preference.
 */
public class NacSharedKeys
	extends NacSharedResource
{

	/**
	 */
	public NacSharedKeys(Context context)
	{
		super(context);
	}

	/**
	 */
	public NacSharedKeys(Resources res)
	{
		super(res);
	}

	/**
	 * @return The about setting.
	 */
	public String getAbout()
	{
		return this.getString(R.string.about_setting_key);
	}

	/**
	 * @return The about setting title.
	 */
	public String getAboutTitle()
	{
		return this.getString(R.string.about_setting);
	}

	/**
	 * @return The AM color key.
	 */
	public String getAmColor()
	{
		return this.getString(R.string.am_color_key);
	}

	/**
	 * @return The appearance setting key.
	 */
	public String getAppearance()
	{
		return this.getString(R.string.appearance_setting_key);
	}

	/**
	 * @return The appearance setting title.
	 */
	public String getAppearanceTitle()
	{
		return this.getString(R.string.appearance_setting);
	}

	/**
	 * @return The post install, first run key.
	 */
	public String getAppFirstRun()
	{
		return this.getString(R.string.app_first_run);
	}

	/**
	 * @return Start statistics key.
	 */
	public String getAppStartStatistics()
	{
		return this.getString(R.string.app_start_statistics);
	}

	/**
	 * @return The audio source key.
	 */
	public String getAudioSource()
	{
		return this.getString(R.string.alarm_audio_source_key);
	}

	/**
	 * @return The auto dismiss key.
	 */
	public String getAutoDismiss()
	{
		return this.getString(R.string.auto_dismiss_key);
	}

	/**
	 * @return The card height collapsed key.
	 */
	public String getCardHeightCollapsed()
	{
		return this.getString(R.string.card_height_collapsed);
	}

	/**
	 * @return The card height collapsed (with dismiss showing) key.
	 */
	public String getCardHeightCollapsedDismiss()
	{
		return this.getString(R.string.card_height_collapsed_dismiss);
	}

	/**
	 * @return The card height expanded key.
	 */
	public String getCardHeightExpanded()
	{
		return this.getString(R.string.card_height_expanded);
	}

	/**
	 * @return The card is measured key.
	 */
	public String getCardIsMeasured()
	{
		return this.getString(R.string.card_is_measured);
	}

	/**
	 * @return List of keys for color preferences.
	 */
	public List<String> getColorKeys()
	{
		List<String> keys = new ArrayList<>();

		keys.add(this.getAmColor());
		keys.add(this.getDaysColor());
		keys.add(this.getNameColor());
		keys.add(this.getPmColor());
		keys.add(this.getThemeColor());
		keys.add(this.getTimeColor());

		return keys;
	}

	/**
	 * @return The day button style key.
	 */
	public String getDayButtonStyle()
	{
		return this.getString(R.string.day_button_style_key);
	}

	/**
	 * @return The days key.
	 */
	public String getDays()
	{
		return this.getString(R.string.alarm_days_key);
	}

	/**
	 * @return The days color key.
	 */
	public String getDaysColor()
	{
		return this.getString(R.string.days_color_key);
	}

	/**
	 * @return The easy snooze key.
	 */
	public String getEasySnooze()
	{
		return this.getString(R.string.easy_snooze_key);
	}

	/**
	 * @return The expand new alarm card key.
	 */
	public String getExpandNewAlarm()
	{
		return this.getString(R.string.expand_new_alarm_key);
	}

	/**
	 * @return The general setting key.
	 */
	public String getGeneral()
	{
		return this.getString(R.string.general_setting_key);
	}

	/**
	 * @return The general setting title.
	 */
	public String getGeneralTitle()
	{
		return this.getString(R.string.general_setting);
	}

	/**
	 * @return The max snooze key.
	 */
	public String getMaxSnooze()
	{
		return this.getString(R.string.max_snooze_key);
	}

	/**
	 * @return The media path key.
	 */
	public String getMediaPath()
	{
		return this.getString(R.string.alarm_sound_key);
	}

	/**
	 * @return The miscellaneous setting key.
	 */
	public String getMiscellaneous()
	{
		return this.getString(R.string.misc_setting_key);
	}

	/**
	 * @return The miscellaneous setting title.
	 */
	public String getMiscellaneousTitle()
	{
		return this.getString(R.string.misc_setting);
	}

	/**
	 * @return The missed alarm notification key.
	 */
	public String getMissedAlarmNotification()
	{
		return this.getString(R.string.missed_alarm_key);
	}

	/**
	 * @return The name key.
	 */
	public String getName()
	{
		return this.getString(R.string.alarm_name_key);
	}

	/**
	 * @return The name color key.
	 */
	public String getNameColor()
	{
		return this.getString(R.string.name_color_key);
	}

	/**
	 * @return The display time remaining key.
	 */
	public String getNextAlarmFormat()
	{
		return this.getString(R.string.next_alarm_format_key);
	}

	/**
	 * @return The PM color key.
	 */
	public String getPmColor()
	{
		return this.getString(R.string.pm_color_key);
	}

	/**
	 * @return The previous system volume, before an alarm goes off.
	 */
	public String getPreviousVolume()
	{
		return this.getString(R.string.sys_previous_volume);
	}

	/**
	 * @return The counter used to check when the rate-my-app dialog should be
	 *         shown.
	 */
	public String getRateMyAppCounter()
	{
		return this.getString(R.string.app_rating_counter);
	}

	/**
	 * @return The repeat key.
	 */
	public String getRepeat()
	{
		return this.getString(R.string.alarm_repeat_key);
	}

	/**
	 * @return The should refresh main activity key.
	 */
	public String getShouldRefreshMainActivity()
	{
		return this.getString(R.string.app_should_refresh_main_activity);
	}

	/**
	 * @return The alarm information key.
	 */
	public String getShowAlarmInfo()
	{
		return this.getString(R.string.show_alarm_info_key);
	}

	/**
	 * @return The shuffle key.
	 */
	public String getShuffle()
	{
		return this.getString(R.string.shuffle_playlist_key);
	}

	/**
	 * @return The snooze counter.
	 */
	public static String getSnoozeCount(long id)
	{
		return "snoozeCount" + id;
	}

	/**
	 * @return The snooze duration key.
	 */
	public String getSnoozeDuration()
	{
		return this.getString(R.string.snooze_duration_key);
	}

	/**
	 * @return The speak frequency key.
	 */
	public String getSpeakFrequency()
	{
		return this.getString(R.string.speak_frequency_key);
	}

	/**
	 * @return The speak to me key.
	 */
	public String getSpeakToMe()
	{
		return this.getString(R.string.speak_to_me_key);
	}

	/**
	 * @return The start week on key.
	 */
	public String getStartWeekOn()
	{
		return this.getString(R.string.start_week_on_key);
	}

	/**
	 * @return The statistics setting.
	 */
	public String getStatistics()
	{
		return this.getString(R.string.stats_setting_key);
	}

	/**
	 * @return The statistics setting title.
	 */
	public String getStatisticsTitle()
	{
		return this.getString(R.string.stats_setting);
	}

	/**
	 * @return The theme color key.
	 */
	public String getThemeColor()
	{
		return this.getString(R.string.theme_color_key);
	}

	/**
	 * @return The time color key.
	 */
	public String getTimeColor()
	{
		return this.getString(R.string.time_color_key);
	}

	/**
	 * @return The upcoming alarm notification key.
	 */
	public String getUpcomingAlarmNotification()
	{
		return this.getString(R.string.upcoming_alarm_key);
	}

	/**
	 * @return The use NFC key.
	 */
	public String getUseNfc()
	{
		return this.getString(R.string.alarm_use_nfc_key);
	}

	/**
	 * @return The vibrate key.
	 */
	public String getVibrate()
	{
		return this.getString(R.string.alarm_vibrate_key);
	}

	/**
	 * @return The volume key.
	 */
	public String getVolume()
	{
		return this.getString(R.string.alarm_volume_key);
	}

}
