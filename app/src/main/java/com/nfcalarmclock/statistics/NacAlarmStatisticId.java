package com.nfcalarmclock.statistics;

import androidx.room.ColumnInfo;

/**
 * Statistics ID for an alarm.
 */
public class NacAlarmStatisticId
{

	/**
	 * Unique ID.
	 */
	@ColumnInfo(name="id")
	private long mId;

	/**
	 * @return The unique ID.
	 */
	public long getId()
	{
		return this.mId;
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

}
