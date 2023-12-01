package com.nfcalarmclock.statistics.db

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.ForeignKey.Companion.SET_NULL
import com.nfcalarmclock.alarm.db.NacAlarm
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Statistics for when an alarm is missed.
 */
@Entity(tableName = "alarm_missed_statistic",
	foreignKeys = [ForeignKey(entity = NacAlarm::class,
		parentColumns = ["id"],
		childColumns = ["alarm_id"],
		onDelete = SET_NULL)],
	inheritSuperIndices = true)
class NacAlarmMissedStatistic
	: NacAlarmStatistic
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

/**
 * Hilt module to provide an instance of a missed statistic.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacAlarmMissedStatisticModule
{

	/**
	 * Provide an instance of a missed statistic.
	 */
	@Provides
	fun provideMissedStatistic() : NacAlarmMissedStatistic
	{
		return NacAlarmMissedStatistic()
	}

}
