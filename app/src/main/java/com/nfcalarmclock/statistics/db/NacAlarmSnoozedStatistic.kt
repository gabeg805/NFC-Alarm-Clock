package com.nfcalarmclock.statistics.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.SET_NULL
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Statistics for when an alarm is snoozed.
 */
@Entity(tableName = "alarm_snoozed_statistic",
	foreignKeys = [ForeignKey(entity = NacAlarm::class,
		parentColumns = ["id"],
		childColumns = ["alarm_id"],
		onDelete = SET_NULL)],
	inheritSuperIndices = true)
class NacAlarmSnoozedStatistic : NacAlarmStatistic
{

	/**
	 * Duration of the snooze.
	 */
	@ColumnInfo(name = "duration", defaultValue = "0")
	var duration: Long = 0

	/**
	 * Constructor.
	 */
	constructor() : super()

	/**
	 * Constructor.
	 */
	constructor(alarm: NacAlarm?) : super(alarm)

	/**
	 * Constructor.
	 */
	constructor(alarm: NacAlarm?, duration: Long) : this(alarm)
	{
		this.duration = duration
	}

}