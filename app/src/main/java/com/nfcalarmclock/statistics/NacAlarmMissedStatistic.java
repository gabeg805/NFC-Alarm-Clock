package com.nfcalarmclock.statistics;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Statistics for when an alarm is missed.
 */
@Entity(tableName="alarm_missed_statistic")
public class NacAlarmMissedStatistic
{

	/**
	 * Embded the ID into this class.
	 */
	@PrimaryKey(autoGenerate=true)
	@Embedded
	NacAlarmStatisticId statisticId;

	/**
	 * Embded the columns from the statistic class into this class.
	 */
	@Embedded
	NacAlarmStatistic statistic;

}
