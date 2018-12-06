package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;

/**
 * Container for the values of each preference.
 */
public class NacSharedPreferences
{

	/**
	 * Auto dismiss.
	 */
	public int autoDismiss;

	/**
	 * Max snoozes.
	 */
	public int maxSnoozes;

	/**
	 * Snooze duration.
	 */
	public int snoozeDuration;

	/**
	 * Repeat.
	 */
	public boolean repeat;

	/**
	 * Day of week.
	 */
	public int days;

	/**
	 * Vibrate.
	 */
	public boolean vibrate;

	/**
	 * Sound path.
	 */
	public String sound;

	/**
	 * Name of the alarm.
	 */
	public String name;

	/**
	 */
	public NacSharedPreferences(Context context)
	{
		SharedPreferences shared =
			PreferenceManager.getDefaultSharedPreferences(context);
		NacPreferenceKeys keys = new NacPreferenceKeys(context);

		int dismissdef = 15;
		int snoozesdef = -1;
		int durationdef = 5;
		boolean repeatdef = Alarm.getRepeatDefault();
		int daysdef = Alarm.getDaysDefault();
		boolean vibratedef = Alarm.getVibrateDefault();
		String sounddef = "";
		String namedef = "";

		this.autoDismiss = shared.getInt(keys.autoDismiss, dismissdef);
		this.maxSnoozes = shared.getInt(keys.maxSnoozes, snoozesdef);
		this.snoozeDuration = shared.getInt(keys.snoozeDuration, durationdef);
		this.repeat = shared.getBoolean(keys.repeat, repeatdef);
		this.days = shared.getInt(keys.days, daysdef);
		this.vibrate = shared.getBoolean(keys.vibrate, vibratedef);
		this.sound = shared.getString(keys.sound, sounddef);
		this.name = shared.getString(keys.name, namedef);
	}

}
