package com.nfcalarmclock.statistics.db

import androidx.room.Entity

/**
 * Statistics for when an alarm is created.
 */
@Entity(tableName = "alarm_created_statistic",
	ignoredColumns = ["alarm_id", "hour", "minute", "name"])
class NacAlarmCreatedStatistic
	: NacAlarmStatistic()