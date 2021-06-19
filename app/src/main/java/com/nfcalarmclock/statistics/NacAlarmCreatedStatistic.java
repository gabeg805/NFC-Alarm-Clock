package com.nfcalarmclock.statistics;

import java.util.Date;

/**
 * Statistics for when an alarm is created.
 */
@Entity(tableName="alarm_create_statistic")
public class NacAlarmCreatedStatistic
{

	/**
	 * Unique ID.
	 */
	@PrimaryKey(autoGenerate=true)
	@ColumnInfo(name="id")
	private long mId;

	/**
	 * Timestamp of when an alarm was created.
	 */
	@ColumnInfo(name="timestamp")
	private Date mTimestamp;

	/**
	 * @return The unique ID.
	 */
	public long getId()
	{
		return this.mId;
	}

	/**
	 * @return The timestamp.
	 */
	public Date getTimestamp()
	{
		return this.mTimestamp;
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
	 * Set the timestamp.
	 *
	 * @param timestamp  The timestamp.
	 */
	public void setTimestamp(Date timestamp)
	{
		this.mTimestamp = timestamp;
	}

}
