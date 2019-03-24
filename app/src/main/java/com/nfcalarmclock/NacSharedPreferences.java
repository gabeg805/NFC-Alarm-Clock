package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.preference.PreferenceManager;
import java.util.EnumSet;

/**
 * Container for the values of each preference.
 */
public class NacSharedPreferences
{

	/**
	 * Shared preferences instance.
	 */
	public final SharedPreferences mInstance;

	/**
	 * Auto dismiss.
	 */
	public final int mAutoDismiss;

	/**
	 * Max snoozes.
	 */
	public final int mMaxSnooze;

	/**
	 * Snooze duration.
	 */
	public final int mSnoozeDuration;

	/**
	 * Day of week.
	 */
	public final int mDays;

	/**
	 * Repeat.
	 */
	public final boolean mRepeat;

	/**
	 * Vibrate.
	 */
	public final boolean mVibrate;

	/**
	 * Sound path.
	 */
	public final String mSound;

	/**
	 * Name of the alarm.
	 */
	public final String mName;

	/**
	 * Theme color.
	 */
	public final int mThemeColor;

	/**
	 * Color of the name of the alarm.
	 */
	public final int mNameColor;

	/**
	 * Color of the text that shows which days the alarm runs.
	 */
	public final int mDaysColor;

	/**
	 * Time color.
	 */
	public final int mTimeColor;

	/**
	 * AM color.
	 */
	public final int mAmColor;

	/**
	 * PM color.
	 */
	public final int mPmColor;

	/**
	 * Default auto dismiss duration.
	 */
	public static final int DEFAULT_AUTO_DISMISS = 15;

	/**
	 * Default max snooze count.
	 */
	public static final int DEFAULT_MAX_SNOOZE = -1;

	/**
	 * Default snooze duration.
	 */
	public static final int DEFAULT_SNOOZE_DURATION = 5;

	/**
	 * Default auto dismiss index value.
	 */
	public static final int DEFAULT_AUTO_DISMISS_INDEX = 7;

	/**
	 * Default max snooze index value.
	 */
	public static final int DEFAULT_MAX_SNOOZE_INDEX = 11;

	/**
	 * Default snooze duration index value.
	 */
	public static final int DEFAULT_SNOOZE_DURATION_INDEX = 4;

	/**
	 * Default maximum number of alarms.
	 */
	public static final int DEFAULT_MAX_ALARMS = 50;

	/**
	 * Default days.
	 */
	public static final int DEFAULT_DAYS = NacCalendar.daysToValue(
		EnumSet.of(NacCalendar.Day.MONDAY, NacCalendar.Day.TUESDAY,
			NacCalendar.Day.WEDNESDAY, NacCalendar.Day.THURSDAY,
			NacCalendar.Day.FRIDAY));

	/**
	 * Default repeat status.
	 */
	public static final boolean DEFAULT_REPEAT = true;

	/**
	 * Default vibrate.
	 */
	public static final boolean DEFAULT_VIBRATE = true;

	/**
	 * Default sound path.
	 */
	public static final String DEFAULT_SOUND = "";

	/**
	 * Default alarm name.
	 */
	public static final String DEFAULT_NAME = "";

	/**
	 * Default theme color.
	 */
	public static final int DEFAULT_THEME_COLOR = 0xfffb8c00;

	/**
	 * Default name color.
	 */
	public static final int DEFAULT_NAME_COLOR = 0xff00c0fb;

	/**
	 * Default days color.
	 */
	public static final int DEFAULT_DAYS_COLOR = 0xfffb8c00;

	/**
	 * Default time color.
	 */
	public static final int DEFAULT_TIME_COLOR = Color.WHITE;

	/**
	 * Default AM color.
	 */
	public static final int DEFAULT_AM_COLOR = Color.WHITE;

	/**
	 * Default PM color.
	 */
	public static final int DEFAULT_PM_COLOR = Color.WHITE;

	/**
	 * Default sound summary text.
	 */
	public static final String DEFAULT_SOUND_SUMMARY = "None";

	/**
	 * Default sound message.
	 */
	public static final String DEFAULT_SOUND_MESSAGE = "Song or ringtone";

	/**
	 * Default name summary text.
	 */
	public static final String DEFAULT_NAME_SUMMARY = "None";

	/**
	 * Default name message.
	 */
	public static final String DEFAULT_NAME_MESSAGE = "Alarm name";

	/**
	 * Default days summary text.
	 */
	public static final String DEFAULT_DAYS_SUMMARY = "None";

	/**
	 * Default days message.
	 */
	public static final String DEFAULT_DAYS_MESSAGE = "Today";

	/**
	 */
	public NacSharedPreferences(Context context)
	{
		SharedPreferences shared =
			PreferenceManager.getDefaultSharedPreferences(context);
		NacPreferenceKeys keys = new NacPreferenceKeys(context);

		this.mInstance = shared;
		this.mDays = shared.getInt(keys.getDays(), DEFAULT_DAYS);
		this.mRepeat = shared.getBoolean(keys.getRepeat(), DEFAULT_REPEAT);
		this.mVibrate = shared.getBoolean(keys.getVibrate(), DEFAULT_VIBRATE);
		this.mSound = shared.getString(keys.getSound(), DEFAULT_SOUND);
		this.mName = shared.getString(keys.getName(), DEFAULT_NAME);
		this.mThemeColor = shared.getInt(keys.getThemeColor(), DEFAULT_THEME_COLOR);
		this.mNameColor = shared.getInt(keys.getNameColor(), DEFAULT_NAME_COLOR);
		this.mDaysColor = shared.getInt(keys.getDaysColor(), DEFAULT_DAYS_COLOR);
		this.mTimeColor = shared.getInt(keys.getTimeColor(), DEFAULT_TIME_COLOR);
		this.mAmColor = shared.getInt(keys.getAmColor(), DEFAULT_AM_COLOR);
		this.mPmColor = shared.getInt(keys.getPmColor(), DEFAULT_PM_COLOR);

		int autoDismiss = shared.getInt(keys.getAutoDismiss(), DEFAULT_AUTO_DISMISS);
		int maxSnooze = shared.getInt(keys.getMaxSnooze(), DEFAULT_MAX_SNOOZE);
		int snoozeDuration = shared.getInt(keys.getSnoozeDuration(), DEFAULT_SNOOZE_DURATION);

		this.mAutoDismiss = (autoDismiss == 1) ? 1 : 5*(autoDismiss-1);
		this.mMaxSnooze = (maxSnooze == 11) ? -1 : maxSnooze;
		this.mSnoozeDuration = (snoozeDuration == 0) ? 1 : 5*snoozeDuration;
	}

	/**
	 * @return The SharedPreferences instance.
	 */
	public SharedPreferences getInstance()
	{
		return this.mInstance;
	}

	/**
	 * @return Auto dismiss duration.
	 */
	public int getAutoDismiss()
	{
		return this.mAutoDismiss;
	}

	/**
	 * @return The max number of snoozes.
	 */
	public int getMaxSnooze()
	{
		return this.mMaxSnooze;
	}

	/**
	 * @return The snooze duration.
	 */
	public int getSnoozeDuration()
	{
		return this.mSnoozeDuration;
	}

	/**
	 * @return The alarm days.
	 */
	public int getDays()
	{
		return this.mDays;
	}

	/**
	 * @return Whether the alarm should be repeated or not.
	 */
	public boolean getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return Whether the alarm should vibrate the phone or not.
	 */
	public boolean getVibrate()
	{
		return this.mVibrate;
	}

	/**
	 * @return The sound path.
	 */
	public String getSound()
	{
		return this.mSound;
	}

	/**
	 * @return The name of the alarm.
	 */
	public String getName()
	{
		return this.mName;
	}

	/**
	 * @return The theme color.
	 */
	public int getThemeColor()
	{
		return this.mThemeColor;
	}

	/**
	 * @return The name color.
	 */
	public int getNameColor()
	{
		return this.mNameColor;
	}

	/**
	 * @return The days color.
	 */
	public int getDaysColor()
	{
		return this.mDaysColor;
	}

	/**
	 * @return The time color.
	 */
	public int getTimeColor()
	{
		return this.mTimeColor;
	}

	/**
	 * @return The AM color.
	 */
	public int getAmColor()
	{
		return this.mAmColor;
	}

	/**
	 * @return The PM color.
	 */
	public int getPmColor()
	{
		return this.mPmColor;
	}

}
