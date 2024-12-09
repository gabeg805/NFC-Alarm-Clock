package com.nfcalarmclock.statistics.db

import androidx.room.Entity
import com.nfcalarmclock.alarm.db.NacAlarm
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Statistics for when an alarm is missed.
 */
@Entity(tableName = "alarm_missed_statistic")
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
