package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;

/**
 * Container, or Keeper, if you will, of keys...of each preference.
 */
public class NacPreferenceKeys
{

	/**
	 * Auto dismiss.
	 */
	public final String mAutoDismiss;

	/**
	 * Max snoozes.
	 */
	public final String mMaxSnooze;

	/**
	 * Snooze duration.
	 */
	public final String mSnoozeDuration;

	/**
	 * Repeat.
	 */
	public final String mRepeat;

	/**
	 * Day of week.
	 */
	public final String mDays;

	/**
	 * Vibrate.
	 */
	public final String mVibrate;

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
	public final String mThemeColor;

	/**
	 * Color of the name of the alarm.
	 */
	public final String mNameColor;

	/**
	 * Color of the text that shows the days that the alarm runs.
	 */
	public final String mDaysColor;

	/**
	 * Time color.
	 */
	public final String mTimeColor;

	/**
	 * AM color.
	 */
	public final String mAmColor;

	/**
	 * PM color.
	 */
	public final String mPmColor;

	/**
	 */
	public NacPreferenceKeys(Context context)
	{
		Resources res = context.getResources();

		this.mAutoDismiss = res.getString(R.string.pref_auto_dismiss_key);
		this.mMaxSnooze = res.getString(R.string.pref_max_snooze_key);
		this.mSnoozeDuration = res.getString(R.string.pref_snooze_duration_key);
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
	 * @return The auto dismiss key.
	 */
	public String getAutoDismiss()
	{
		return this.mAutoDismiss;
	}

	/**
	 * @return The max snooze key.
	 */
	public String getMaxSnooze()
	{
		return this.mMaxSnooze;
	}

	/**
	 * @return The snooze duration key.
	 */
	public String getSnoozeDuration()
	{
		return this.mSnoozeDuration;
	}

	/**
	 * @return The repeat key.
	 */
	public String getRepeat()
	{
		return this.mRepeat;
	}

	/**
	 * @return The days key.
	 */
	public String getDays()
	{
		return this.mDays;
	}

	/**
	 * @return The vibrate key.
	 */
	public String getVibrate()
	{
		return this.mVibrate;
	}

	/**
	 * @return The sound key.
	 */
	public String getSound()
	{
		return this.mSound;
	}

	/**
	 * @return The name key.
	 */
	public String getName()
	{
		return this.mName;
	}

	/**
	 * @return The theme color key.
	 */
	public String getThemeColor()
	{
		return this.mThemeColor;
	}

	/**
	 * @return The name color key.
	 */
	public String getNameColor()
	{
		return this.mNameColor;
	}

	/**
	 * @return The days color key.
	 */
	public String getDaysColor()
	{
		return this.mDaysColor;
	}

	/**
	 * @return The time color key.
	 */
	public String getTimeColor()
	{
		return this.mTimeColor;
	}

	/**
	 * @return The AM color key.
	 */
	public String getAmColor()
	{
		return this.mAmColor;
	}

	/**
	 * @return The PM color key.
	 */
	public String getPmColor()
	{
		return this.mPmColor;
	}

}
