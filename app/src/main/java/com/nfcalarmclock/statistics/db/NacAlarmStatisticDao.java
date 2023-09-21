package com.nfcalarmclock.statistics.db;

import androidx.room.Insert;

/**
 * Data access object for storing when alarms were created.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public abstract interface NacAlarmStatisticDao<T>
{

    /**
     * Insert an instance of an alarm statistic.
     *
     * @param  stat  Alarm statistic.
	 *
     * @return The row ID of the row that was inserted.
     */
    @Insert
    long insert(T stat);

}
