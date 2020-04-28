package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;

/**
 * Constants container.
 */
public class NacSharedConstants
	extends NacSharedResource
{

	/**
	 */
	public NacSharedConstants(Context context)
	{
		super(context);
	}

	/**
	 */
	public NacSharedConstants(Resources res)
	{
		super(res);
	}

	/**
	 * @return Cancel.
	 */
	public String getCancel()
	{
		return this.getString(R.string.cancel);
	}

	/**
	 * @return Clear.
	 */
	public String getClear()
	{
		return this.getString(R.string.clear);
	}

	/**
	 * @return The color hint.
	 */
	public String getColorHint()
	{
		return this.getString(R.string.color_hint);
	}

	/**
	 * @return The days of week.
	 */
	public List<String> getDaysOfWeek()
	{
		String sun = this.getString(R.string.dow_sunday);
		String mon = this.getString(R.string.dow_monday);
		String tue = this.getString(R.string.dow_tuesday);
		String wed = this.getString(R.string.dow_wednesday);
		String thu = this.getString(R.string.dow_thursday);
		String fri = this.getString(R.string.dow_friday);
		String sat = this.getString(R.string.dow_saturday);

		return Arrays.asList(sun, mon, tue, wed, thu, fri, sat);
	}

	/**
	 * @return The days of week abbreviated.
	 */
	public List<String> getDaysOfWeekAbbr()
	{
		String sun = this.getString(R.string.dow_sun);
		String mon = this.getString(R.string.dow_mon);
		String tue = this.getString(R.string.dow_tue);
		String wed = this.getString(R.string.dow_wed);
		String thu = this.getString(R.string.dow_thu);
		String fri = this.getString(R.string.dow_fri);
		String sat = this.getString(R.string.dow_sat);

		return Arrays.asList(sun, mon, tue, wed, thu, fri, sat);
	}

	/**
	 * @return Everyday.
	 */
	public String getEveryday()
	{
		return this.getString(R.string.dow_everyday);
	}

	/**
	 * @return Frequency interval.
	 */
	public String getFrequencyInterval()
	{
		return this.getString(R.string.frequency_interval);
	}

	/**
	 * @return Frequency once.
	 */
	public String getFrequencyOnce()
	{
		return this.getString(R.string.frequency_once);
	}

	/**
	 * @return The media hint.
	 */
	public String getMediaHint()
	{
		return this.getString(R.string.media_hint);
	}

	/**
	 * @return Monday.
	 */
	public String getMonday()
	{
		return this.getString(R.string.dow_monday);
	}

	/**
	 * @return The alarm name example.
	 */
	public String getNameExample()
	{
		return this.getString(R.string.name_example);
	}

	/**
	 * @return The name hint.
	 */
	public String getNameHint()
	{
		return this.getString(R.string.name_hint);
	}

	/**
	 * @return None.
	 */
	public String getNone()
	{
		return this.getString(R.string.none);
	}

	/**
	 * @return Ok.
	 */
	public String getOk()
	{
		return this.getString(R.string.ok);
	}

	/**
	 * @return Settings.
	 */
	public String getSettings()
	{
		return this.getString(R.string.settings);
	}

	/**
	 * @return Speak frequency.
	 */
	public String getSpeakFrequency()
	{
		return this.getString(R.string.speak_frequency);
	}

	/**
	 * @return Start week on title.
	 */
	public String getStartWeekOnTitle()
	{
		return this.getString(R.string.start_week_on);
	}

	/**
	 * @return Sunday.
	 */
	public String getSunday()
	{
		return this.getString(R.string.dow_sunday);
	}

	/**
	 * @return Today.
	 */
	public String getToday()
	{
		return this.getString(R.string.dow_today);
	}

	/**
	 * @return Tomorrow.
	 */
	public String getTomorrow()
	{
		return this.getString(R.string.dow_tomorrow);
	}

	/**
	 * @return Weekdays.
	 */
	public String getWeekdays()
	{
		return this.getString(R.string.dow_weekdays);
	}

	/**
	 * @return Weekend.
	 */
	public String getWeekend()
	{
		return this.getString(R.string.dow_weekend);
	}

}
