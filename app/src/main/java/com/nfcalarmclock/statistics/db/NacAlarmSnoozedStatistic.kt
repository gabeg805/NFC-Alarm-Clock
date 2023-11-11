package com.nfcalarmclock.statistics.db

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.SET_NULL
import com.nfcalarmclock.alarm.db.NacAlarm
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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

/**
 * Hilt module to provide an instance of a snoozed statistic.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacAlarmSnoozedStatisticModule
{

	/**
	 * Provide an instance of a snoozed statistic.
	 */
	@Provides
	fun provideSnoozedStatistic() : NacAlarmSnoozedStatistic
	{
		return NacAlarmSnoozedStatistic()
	}

}
