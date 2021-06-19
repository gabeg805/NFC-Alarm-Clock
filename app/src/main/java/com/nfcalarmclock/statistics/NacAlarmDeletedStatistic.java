package com.nfcalarmclock.statistics;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.PrimaryKey;
import java.util.Date;

/**
 * Statistics for when an alarm is deleted.
 */
@Entity(tableName="alarm_deleted_statistic")
public class NacAlarmDeletedStatistic
	extends NacAlarmStatistic
{

}
