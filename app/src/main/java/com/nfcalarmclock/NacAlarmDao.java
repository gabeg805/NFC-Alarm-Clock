package com.nfcalarmclock;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;
import java.util.List;

/**
 * Data access object for alarms.
 */
@Dao
public interface NacAlarmDao
{

	/**
	 * Delete an alarm.
	 *
	 * @param  alarm  An alarm.
	 *
	 * @return The number of rows deleted.
	 */
	@Delete
	int delete(NacAlarm alarm);

	/**
	 * Delete all alarms.
	 *
	 * @return The number of rows deleted.
	 */
	@Query("DELETE FROM alarm")
	int deleteAll();

	/**
	 * @return The alarm with the ID.
	 */
	@Query("SELECT * FROM alarm WHERE id=:id")
	NacAlarm findAlarm(long id);

	/**
	 * @return An active alarm.
	 */
	@Query("SELECT * FROM alarm WHERE is_active=1 LIMIT 1")
	LiveData<NacAlarm> getActiveAlarm();

	/**
	 * @return List of active alarms.
	 */
	@Query("SELECT * FROM alarm WHERE is_active=1")
	LiveData<List<NacAlarm>> getActiveAlarms();

	/**
	 * @return All alarms.
	 */
	@Query("SELECT * FROM alarm")
	LiveData<List<NacAlarm>> getAllAlarms();

	/**
	 * @return All alarms. This will wait until all alarms are selected.
	 */
	@Query("SELECT * FROM alarm")
	List<NacAlarm> getAllAlarmsNow();

	/**
	 * Insert an alarm.
	 *
	 * @param  alarm  An alarm.
	 *
	 * @return The row ID of the alarm that was inserted.
	 */
	@Insert(onConflict=OnConflictStrategy.ABORT)
	long insert(NacAlarm alarm);

	/**
	 * Update an existing alarm
	 *
	 * @param  alarm  An alarm.
	 *
	 * @return The number of alarms updated.
	 */
	@Update
	int update(NacAlarm alarm);

}
