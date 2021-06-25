package com.nfcalarmclock.statistics;

import androidx.room.Entity;

import com.nfcalarmclock.alarm.NacAlarm;

/**
 * Statistics for when an alarm is created.
 */
@Entity(tableName="alarm_created_statistic",
	ignoredColumns={"alarm_id", "hour", "minute", "name"})
public class NacAlarmCreatedStatistic
	extends NacAlarmStatistic
{

	/**
	 */
	public NacAlarmCreatedStatistic()
	{
		super();
	}

}
