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
	public String autoDismiss;

	/**
	 * Max snoozes.
	 */
	public String maxSnoozes;

	/**
	 * Snooze duration.
	 */
	public String snoozeDuration;

	/**
	 * Repeat.
	 */
	public String repeat;

	/**
	 * Day of week.
	 */
	public String days;

	/**
	 * Vibrate.
	 */
	public String vibrate;

	/**
	 * Sound path.
	 */
	public String sound;

	/**
	 * Name of the alarm.
	 */
	public String name;

	/**
	 * Theme color.
	 */
	public String themeColor;

	/**
	 * Time color.
	 */
	public String timeColor;

	/**
	 * AM color.
	 */
	public String amColor;

	/**
	 * PM color.
	 */
	public String pmColor;

	/**
	 * Color of the name of the alarm.
	 */
	public String nameColor;

	/**
	 * Color of the text that shows the days that the alarm runs.
	 */
	public String daysColor;

	/**
	 */
	public NacPreferenceKeys(Context context)
	{
		Resources res = context.getResources();

		this.autoDismiss = res.getString(R.string.pref_auto_dismiss_key);
		this.maxSnoozes = res.getString(R.string.pref_max_snooze_key);
		this.snoozeDuration = res.getString(R.string.pref_snooze_duration_key);
		this.repeat = res.getString(R.string.pref_repeat_key);
		this.days = res.getString(R.string.pref_days_key);
		this.vibrate = res.getString(R.string.pref_vibrate_key);
		this.sound = res.getString(R.string.pref_sound_key);
		this.name = res.getString(R.string.pref_name_key);
		this.themeColor = res.getString(R.string.pref_theme_color_key);
		this.timeColor = res.getString(R.string.pref_time_color_key);
		this.amColor = res.getString(R.string.pref_am_color_key);
		this.pmColor = res.getString(R.string.pref_pm_color_key);
		this.nameColor = res.getString(R.string.pref_name_color_key);
		this.daysColor = res.getString(R.string.pref_days_color_key);
	}

}
