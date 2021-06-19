package com.nfcalarmclock.statistics;

import androidx.room.Embedded;
import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Statistics for when an alarm is snoozed.
 */
@Entity(tableName="alarm_snoozed_statistic")
public class NacAlarmSnoozedStatistic
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
