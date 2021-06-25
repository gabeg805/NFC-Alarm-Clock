package com.nfcalarmclock.statistics;

import androidx.room.Entity;
import androidx.room.ForeignKey;

import com.nfcalarmclock.alarm.NacAlarm;

/**
 * Statistics for when an alarm is missed.
 */
@Entity(tableName="alarm_missed_statistic",
	foreignKeys={
		@ForeignKey(entity=NacAlarm.class,
			parentColumns={"id"},
			childColumns={"alarm_id"},
			onDelete=ForeignKey.SET_NULL)
		})
public class NacAlarmMissedStatistic
	extends NacAlarmStatistic
{

	/**
	 */
	public NacAlarmMissedStatistic()
	{
		super();
	}

	/**
	 */
	public NacAlarmMissedStatistic(NacAlarm alarm)
	{
		super(alarm);
	}

}
