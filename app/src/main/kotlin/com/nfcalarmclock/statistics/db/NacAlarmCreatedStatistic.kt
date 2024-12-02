package com.nfcalarmclock.statistics.db

import androidx.room.Entity
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent

/**
 * Statistics for when an alarm is created.
 */
@Entity(tableName = "alarm_created_statistic",
	ignoredColumns = ["alarm_id", "hour", "minute", "name"])
class NacAlarmCreatedStatistic
	: NacAlarmStatistic()
{

	/**
	 * Check if two stats are equal, except for the ID.
	 */
	override fun equalsExceptId(stat: NacAlarmStatistic): Boolean
	{
		return (timestamp == stat.timestamp)
	}

	/**
	 * Convert the data to a csv format so that it can be used to write to an
	 * output file.
	 */
	override fun toCsvFormat(): String
	{
		return "$timestamp"
	}

}

/**
 * Hilt module to provide an instance of a created statistic.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacAlarmCreatedStatisticModule
{

	/**
	 * Provide an instance of a created statistic.
	 */
	@Provides
	fun provideCreatedStatistic() : NacAlarmCreatedStatistic
	{
		return NacAlarmCreatedStatistic()
	}

}
