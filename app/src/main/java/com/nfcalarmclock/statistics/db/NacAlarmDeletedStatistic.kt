package com.nfcalarmclock.statistics.db

import androidx.room.Entity
import com.nfcalarmclock.alarm.db.NacAlarm
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

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

/**
 * Hilt module to provide an instance of a deleted statistic.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacAlarmDeletedStatisticModule
{

	/**
	 * Provide an instance of a deleted statistic.
	 */
	@Provides
	fun provideDeletedStatistic() : NacAlarmDeletedStatistic
	{
		return NacAlarmDeletedStatistic()
	}

}
