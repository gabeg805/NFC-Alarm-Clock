package com.nfcalarmclock.statistics;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Statistics for when an alarm is snoozed.
 */
@Entity(tableName="alarm_snoozed_statistic")
public class NacAlarmSnoozedStatistic
	extends NacAlarmStatistic
{

}
