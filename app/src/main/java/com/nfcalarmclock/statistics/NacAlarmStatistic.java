package com.nfcalarmclock.statistics;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Statistics for an alarm.
 */
public abstract class NacAlarmStatistic
{

	/**
	 * Unique ID.
	 */
	@PrimaryKey(autoGenerate=true)
	@ColumnInfo(name="id")
	private long mId;

	/**
	 * Timestamp of when an alarm was snoozed.
	 */
	@ColumnInfo(name="timestamp")
	private Date mTimestamp;

	/**
	 * The hour the alarm ran at.
	 */
	@ColumnInfo(name="hour")
	private int mHour;

	/**
	 * The minute the alarm ran at.
	 */
	@ColumnInfo(name="minute")
	private int mMinute;

	/**
	 * The name of the alarm.
	 */
	@ColumnInfo(name="name")
	private String mName;

	/**
	 * @return The hour the alarm ran at.
	 */
	public int getHour()
	{
		return this.mHour;
	}

	/**
	 * @return The unique ID.
	 */
	public long getId()
	{
		return this.mId;
	}

	/**
	 * @return The minute the alarm ran at.
	 */
	public int getMinute()
	{
		return this.mMinute;
	}

	/**
	 * @return The name of the alarm.
	 */
	public String getName()
	{
		return this.mName;
	}

	/**
	 * @return The timestamp.
	 */
	public Date getTimestamp()
	{
		return this.mTimestamp;
	}

	/**
	 * Set the hour the alarm ran at.
	 *
	 * @param  hour  The hour.
	 */
	public void setHour(int hour)
	{
		this.mHour = hour;
	}

	/**
	 * Set the unique ID.
	 *
	 * @param  id  The unique ID.
	 */
	public void setId(long id)
	{
		this.mId = id;
	}

	/**
	 * Set the minute the alarm ran at.
	 *
	 * @param  minute  The minute.
	 */
	public void setMinute(int minute)
	{
		this.mMinute = minute;
	}

	/**
	 * Set the name of the alarm.
	 *
	 * @param  name  The name of the alarm.
	 */
	public void setName(String name)
	{
		this.mName = name;
	}

	/**
	 * Set the timestamp.
	 *
	 * @param timestamp  The timestamp.
	 */
	public void setTimestamp(Date timestamp)
	{
		this.mTimestamp = timestamp;
	}

}
