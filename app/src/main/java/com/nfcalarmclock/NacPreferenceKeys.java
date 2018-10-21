package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;

/**
 * Container, or Keeper, if you will, of keys...of each preference.
 */
public class NacPreferenceKeys
{

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
	 */
	public NacPreferenceKeys(Context context)
	{
		Resources res = context.getResources();

		this.repeat = res.getString(R.string.pref_repeat_key);
		this.days = res.getString(R.string.pref_days_key);
		this.vibrate = res.getString(R.string.pref_vibrate_key);
		this.sound = res.getString(R.string.pref_sound_key);
		this.name = res.getString(R.string.pref_name_key);
	}

}
