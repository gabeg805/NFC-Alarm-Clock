package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;

/**
 * Container, or Keeper, if you will, of keys...of each preference.
 */
public class NacPreferenceKeys
{

	/**
	 * General screen.
	 */
	private final String mGeneralScreen;

	/**
	 * Default alarm screen.
	 */
	private final String mDefaultAlarmScreen;

	/**
	 * Color screen.
	 */
	private final String mColorScreen;

	/**
	 * Miscellaneous screen.
	 */
	private final String mMiscellaneousScreen;

	/**
	 * General screen title.
	 */
	private final String mGeneralScreenTitle;

	/**
	 * Default alarm screen title.
	 */
	private final String mDefaultAlarmScreenTitle;

	/**
	 * Color screen title.
	 */
	private final String mColorScreenTitle;

	/**
	 * Miscellaneous screen title.
	 */
	private final String mMiscellaneousTitle;

	/**
	 * Auto dismiss.
	 */
	private final String mAutoDismiss;

	/**
	 * Max snoozes.
	 */
	private final String mMaxSnooze;

	/**
	 * Snooze duration.
	 */
	private final String mSnoozeDuration;

	/**
	 * Require NFC to dismiss an alarm.
	 */
	private final String mRequireNfc;

	/**
	 * Easy snoozing of the alarm.
	 */
	private final String mEasySnooze;

	/**
	 * Shuffle the alarm playlist.
	 */
	private final String mShuffle;

	/**
	 * Monday as the first day-of-week.
	 */
	private final String mMondayFirst;

	/**
	 * Repeat.
	 */
	private final String mRepeat;

	/**
	 * Day of week.
	 */
	private final String mDays;

	/**
	 * Vibrate.
	 */
	private final String mVibrate;

	/**
	 * Sound path.
	 */
	private final String mSound;

	/**
	 * Name of the alarm.
	 */
	private final String mName;

	/**
	 * Theme color.
	 */
	private final String mThemeColor;

	/**
	 * Color of the name of the alarm.
	 */
	private final String mNameColor;

	/**
	 * Color of the text that shows the days that the alarm runs.
	 */
	private final String mDaysColor;

	/**
	 * Time color.
	 */
	private final String mTimeColor;

	/**
	 * AM color.
	 */
	private final String mAmColor;

	/**
	 * PM color.
	 */
	private final String mPmColor;

	/**
	 */
	public NacPreferenceKeys(Context context)
	{
		Resources res = context.getResources();

		this.mGeneralScreen = res.getString(R.string.pref_screen_general_key);
		this.mDefaultAlarmScreen = res.getString(R.string.pref_screen_default_alarm_key);
		this.mColorScreen = res.getString(R.string.pref_screen_color_key);
		this.mMiscellaneousScreen = res.getString(R.string.pref_screen_misc_key);
		this.mGeneralScreenTitle = res.getString(R.string.pref_screen_general_title);
		this.mDefaultAlarmScreenTitle = res.getString(R.string.pref_screen_default_alarm_title);
		this.mColorScreenTitle = res.getString(R.string.pref_screen_color_title);
		this.mMiscellaneousTitle = res.getString(R.string.pref_screen_misc_title);
		this.mAutoDismiss = res.getString(R.string.pref_auto_dismiss_key);
		this.mMaxSnooze = res.getString(R.string.pref_max_snooze_key);
		this.mSnoozeDuration = res.getString(R.string.pref_snooze_duration_key);
		this.mRequireNfc = res.getString(R.string.pref_dismiss_button_key);
		this.mEasySnooze = res.getString(R.string.pref_easy_snooze_key);
		this.mShuffle = res.getString(R.string.pref_shuffle_playlist_key);
		this.mMondayFirst = res.getString(R.string.pref_monday_first_key);
		this.mRepeat = res.getString(R.string.pref_repeat_key);
		this.mDays = res.getString(R.string.pref_days_key);
		this.mVibrate = res.getString(R.string.pref_vibrate_key);
		this.mSound = res.getString(R.string.pref_sound_key);
		this.mName = res.getString(R.string.pref_name_key);
		this.mThemeColor = res.getString(R.string.pref_theme_color_key);
		this.mNameColor = res.getString(R.string.pref_name_color_key);
		this.mDaysColor = res.getString(R.string.pref_days_color_key);
		this.mTimeColor = res.getString(R.string.pref_time_color_key);
		this.mAmColor = res.getString(R.string.pref_am_color_key);
		this.mPmColor = res.getString(R.string.pref_pm_color_key);
	}

	/**
	 * @return The AM color key.
	 */
	public String getAmColor()
	{
		return this.mAmColor;
	}

	/**
	 * @return The auto dismiss key.
	 */
	public String getAutoDismiss()
	{
		return this.mAutoDismiss;
	}

	/**
	 * @return The color screen key.
	 */
	public String getColorScreen()
	{
		return this.mColorScreen;
	}

	/**
	 * @return The color screen title.
	 */
	public String getColorScreenTitle()
	{
		return this.mColorScreenTitle;
	}

	/**
	 * @return The days key.
	 */
	public String getDays()
	{
		return this.mDays;
	}

	/**
	 * @return The days color key.
	 */
	public String getDaysColor()
	{
		return this.mDaysColor;
	}

	/**
	 * @return The default alarm screen key.
	 */
	public String getDefaultAlarmScreen()
	{
		return this.mDefaultAlarmScreen;
	}

	/**
	 * @return The default alarm screen title.
	 */
	public String getDefaultAlarmScreenTitle()
	{
		return this.mDefaultAlarmScreenTitle;
	}

	/**
	 * @return The easy snooze key.
	 */
	public String getEasySnooze()
	{
		return this.mEasySnooze;
	}

	/**
	 * @return The general screen key.
	 */
	public String getGeneralScreen()
	{
		return this.mGeneralScreen;
	}

	/**
	 * @return The general screen title.
	 */
	public String getGeneralScreenTitle()
	{
		return this.mGeneralScreenTitle;
	}

	/**
	 * @return The max snooze key.
	 */
	public String getMaxSnooze()
	{
		return this.mMaxSnooze;
	}

	/**
	 * @return The miscellaneous screen key.
	 */
	public String getMiscellaneousScreen()
	{
		return this.mMiscellaneousScreen;
	}

	/**
	 * @return The miscellaneous screen title.
	 */
	public String getMiscellaneousScreenTitle()
	{
		return this.mMiscellaneousTitle;
	}

	/**
	 * @return The monday first key.
	 */
	public String getMondayFirst()
	{
		return this.mMondayFirst;
	}

	/**
	 * @return The name key.
	 */
	public String getName()
	{
		return this.mName;
	}

	/**
	 * @return The name color key.
	 */
	public String getNameColor()
	{
		return this.mNameColor;
	}

	/**
	 * @return The PM color key.
	 */
	public String getPmColor()
	{
		return this.mPmColor;
	}

	/**
	 * @return The repeat key.
	 */
	public String getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return The NFC required key.
	 */
	public String getRequireNfc()
	{
		return this.mRequireNfc;
	}

	/**
	 * @return The shuffle key.
	 */
	public String getShuffle()
	{
		return this.mShuffle;
	}

	/**
	 * @return The snooze counter.
	 */
	public String getSnoozeCount(int id)
	{
		return "snoozeCount" + String.valueOf(id);
	}

	/**
	 * @return The snooze duration key.
	 */
	public String getSnoozeDuration()
	{
		return this.mSnoozeDuration;
	}

	/**
	 * @return The sound key.
	 */
	public String getSound()
	{
		return this.mSound;
	}

	/**
	 * @return The theme color key.
	 */
	public String getThemeColor()
	{
		return this.mThemeColor;
	}

	/**
	 * @return The time color key.
	 */
	public String getTimeColor()
	{
		return this.mTimeColor;
	}

	/**
	 * @return The vibrate key.
	 */
	public String getVibrate()
	{
		return this.mVibrate;
	}

}
