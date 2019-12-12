package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;

/**
 * Container, or Keeper, if you will, of keys...of each preference.
 */
public class NacSharedKeys
{

	/**
	 * The first run after installing the app.
	 */
	private final String mAppFirstRun;

	/**
	 * Appearance screen.
	 */
	private final String mAppearanceScreen;

	/**
	 * General screen.
	 */
	private final String mGeneralScreen;

	/**
	 * Miscellaneous screen.
	 */
	private final String mMiscellaneousScreen;

	/**
	 * About screen.
	 */
	private final String mAboutScreen;

	/**
	 * Appearance screen title.
	 */
	private final String mAppearanceScreenTitle;

	/**
	 * General screen title.
	 */
	private final String mGeneralScreenTitle;

	/**
	 * Miscellaneous screen title.
	 */
	private final String mMiscellaneousTitle;

	/**
	 * About screen title.
	 */
	private final String mAboutScreenTitle;

	/**
	 * Auto dismiss.
	 */
	private final String mAutoDismiss;

	/**
	 * Prevent app from closing.
	 */
	private final String mPreventAppFromClosing;

	/**
	 * Max snoozes.
	 */
	private final String mMaxSnooze;

	/**
	 * Snooze duration.
	 */
	private final String mSnoozeDuration;

	/**
	 * Easy snoozing of the alarm.
	 */
	private final String mEasySnooze;

	/**
	 * Show missed alarm notifications.
	 */
	private final String mMissedAlarmNotification;

	/**
	 * Show upcoming alarm notifications.
	 */
	private final String mUpcomingAlarmNotification;

	/**
	 * Next alarm display format.
	 */
	private final String mNextAlarmFormat;

	/**
	 * Start week on.
	 */
	private final String mStartWeekOn;

	/**
	 * Show alarm information in alarm activity.
	 */
	private final String mShowAlarmInfo;

	/**
	 * Expand a new alarm card.
	 */
	private final String mExpandNewAlarm;

	/**
	 * Repeat.
	 */
	private final String mRepeat;

	/**
	 * Use NFC to dismiss an alarm.
	 */
	private final String mUseNfc;

	/**
	 * Day of week.
	 */
	private final String mDays;

	/**
	 * Vibrate.
	 */
	private final String mVibrate;

	/**
	 * Sound level.
	 */
	private final String mVolume;

	/**
	 * Audio source.
	 */
	private final String mAudioSource;

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
	 * Shuffle the alarm playlist.
	 */
	private final String mShuffle;

	/**
	 * Speak the current time to the user.
	 */
	private final String mSpeakToMe;

	/**
	 * Frequency at which to speak the current time to the user.
	 */
	private final String mSpeakFrequency;

	/**
	 */
	public NacSharedKeys(Context context)
	{
		Resources res = context.getResources();

		this.mAppFirstRun = res.getString(R.string.app_first_run);
		this.mAppearanceScreen = res.getString(R.string.pref_screen_appearance_key);
		this.mGeneralScreen = res.getString(R.string.pref_screen_general_key);
		this.mMiscellaneousScreen = res.getString(R.string.pref_screen_misc_key);
		this.mAboutScreen = res.getString(R.string.pref_screen_about_key);
		this.mAppearanceScreenTitle = res.getString(R.string.pref_screen_appearance_title);
		this.mGeneralScreenTitle = res.getString(R.string.pref_screen_general_title);
		this.mMiscellaneousTitle = res.getString(R.string.pref_screen_misc_title);
		this.mAboutScreenTitle = res.getString(R.string.pref_screen_about_title);
		this.mAutoDismiss = res.getString(R.string.pref_auto_dismiss_key);
		this.mPreventAppFromClosing = res.getString(R.string.pref_prevent_app_from_closing_key);
		this.mMaxSnooze = res.getString(R.string.pref_max_snooze_key);
		this.mSnoozeDuration = res.getString(R.string.pref_snooze_duration_key);
		this.mEasySnooze = res.getString(R.string.pref_easy_snooze_key);
		this.mMissedAlarmNotification = res.getString(R.string.pref_missed_alarm_notification_key);
		this.mUpcomingAlarmNotification = res.getString(R.string.pref_upcoming_alarm_notification_key);
		this.mNextAlarmFormat = res.getString(R.string.pref_next_alarm_format_key);
		this.mStartWeekOn = res.getString(R.string.pref_start_week_on_key);
		this.mShowAlarmInfo = res.getString(R.string.pref_alarm_info_key);
		this.mExpandNewAlarm = res.getString(R.string.pref_expand_new_alarm_key);
		this.mRepeat = res.getString(R.string.pref_repeat_key);
		this.mUseNfc = res.getString(R.string.pref_use_nfc_key);
		this.mDays = res.getString(R.string.pref_days_key);
		this.mVibrate = res.getString(R.string.pref_vibrate_key);
		this.mVolume = res.getString(R.string.pref_volume_key);
		this.mAudioSource = res.getString(R.string.pref_audio_source_key);
		this.mSound = res.getString(R.string.pref_sound_key);
		this.mName = res.getString(R.string.pref_name_key);
		this.mThemeColor = res.getString(R.string.pref_theme_color_key);
		this.mNameColor = res.getString(R.string.pref_name_color_key);
		this.mDaysColor = res.getString(R.string.pref_days_color_key);
		this.mTimeColor = res.getString(R.string.pref_time_color_key);
		this.mAmColor = res.getString(R.string.pref_am_color_key);
		this.mPmColor = res.getString(R.string.pref_pm_color_key);
		this.mShuffle = res.getString(R.string.pref_shuffle_playlist_key);
		this.mSpeakToMe = res.getString(R.string.pref_speak_to_me_key);
		this.mSpeakFrequency = res.getString(R.string.pref_speak_frequency_key);
	}

	/**
	 * @return The about screen.
	 */
	public String getAboutScreen()
	{
		return this.mAboutScreen;
	}

	/**
	 * @return The about screen title.
	 */
	public String getAboutScreenTitle()
	{
		return this.mAboutScreenTitle;
	}

	/**
	 * @return The AM color key.
	 */
	public String getAmColor()
	{
		return this.mAmColor;
	}

	/**
	 * @return The appearance screen key.
	 */
	public String getAppearanceScreen()
	{
		return this.mAppearanceScreen;
	}

	/**
	 * @return The appearance screen title.
	 */
	public String getAppearanceScreenTitle()
	{
		return this.mAppearanceScreenTitle;
	}

	/**
	 * @return The post install, first run key.
	 */
	public String getAppFirstRun()
	{
		return this.mAppFirstRun;
	}

	/**
	 * @return The audio source key.
	 */
	public String getAudioSource()
	{
		return this.mAudioSource;
	}

	/**
	 * @return The auto dismiss key.
	 */
	public String getAutoDismiss()
	{
		return this.mAutoDismiss;
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
	 * @return The easy snooze key.
	 */
	public String getEasySnooze()
	{
		return this.mEasySnooze;
	}

	/**
	 * @return The expand new alarm card key.
	 */
	public String getExpandNewAlarm()
	{
		return this.mExpandNewAlarm;
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
	 * @return The missed alarm notification key.
	 */
	public String getMissedAlarmNotification()
	{
		return this.mMissedAlarmNotification;
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
	 * @return The display time remaining key.
	 */
	public String getNextAlarmFormat()
	{
		return this.mNextAlarmFormat;
	}

	/**
	 * @return The PM color key.
	 */
	public String getPmColor()
	{
		return this.mPmColor;
	}

	/**
	 * @return The prevent app from closing key.
	 */
	public String getPreventAppFromClosing()
	{
		return this.mPreventAppFromClosing;
	}

	/**
	 * @return The repeat key.
	 */
	public String getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return The alarm information key.
	 */
	public String getShowAlarmInfo()
	{
		return this.mShowAlarmInfo;
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
	 * @return The speak frequency key.
	 */
	public String getSpeakFrequency()
	{
		return this.mSpeakFrequency;
	}

	/**
	 * @return The speak to me key.
	 */
	public String getSpeakToMe()
	{
		return this.mSpeakToMe;
	}

	/**
	 * @return The start week on key.
	 */
	public String getStartWeekOn()
	{
		return this.mStartWeekOn;
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
	 * @return The upcoming alarm notification key.
	 */
	public String getUpcomingAlarmNotification()
	{
		return this.mUpcomingAlarmNotification;
	}

	/**
	 * @return The use NFC key.
	 */
	public String getUseNfc()
	{
		return this.mUseNfc;
	}

	/**
	 * @return The vibrate key.
	 */
	public String getVibrate()
	{
		return this.mVibrate;
	}

	/**
	 * @return The volume key.
	 */
	public String getVolume()
	{
		return this.mVolume;
	}

}
