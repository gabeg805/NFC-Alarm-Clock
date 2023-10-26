package com.nfcalarmclock.statistics.db

import androidx.room.Entity
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Statistics for when an alarm is deleted.
 */
@Entity(tableName = "alarm_deleted_statistic", ignoredColumns = ["alarm_id"])
class NacAlarmDeletedStatistic : NacAlarmStatistic
{

	/**
	 * Constructor.
	 */
	constructor() : super()

	/**
	 * Constructor.
	 */
	constructor(alarm: NacAlarm?) : super(alarm)

}