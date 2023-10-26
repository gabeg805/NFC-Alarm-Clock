package com.nfcalarmclock.statistics.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.SET_NULL
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Statistics for when an alarm is dismissed.
 */
@Entity(tableName = "alarm_dismissed_statistic",
	foreignKeys = [ForeignKey(entity = NacAlarm::class,
		parentColumns = ["id"],
		childColumns = ["alarm_id"],
		onDelete = SET_NULL)],
	inheritSuperIndices = true)
class NacAlarmDismissedStatistic : NacAlarmStatistic
{

	/**
	 * Whether the alarm used NFC to dismiss or not.
	 */
	@ColumnInfo(name = "used_nfc")
	var usedNfc = false

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
	constructor(alarm: NacAlarm?, usedNfc: Boolean) : this(alarm)
	{
		this.usedNfc = usedNfc
	}

}