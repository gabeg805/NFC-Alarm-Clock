package com.nfcalarmclock.statistics;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Statistics for when an alarm is missed.
 */
@Entity(tableName="alarm_missed_statistic")
public class NacAlarmMissedStatistic
	extends NacAlarmStatistic
{

}
