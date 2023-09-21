package com.nfcalarmclock.alarm.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
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
	 * @param  alarm  Alarm to delete.
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
	 * Find an alarm.
	 *
	 * @param  id  The ID of the alarm to find.
	 *
	 * @return The alarm with the ID.
	 */
	@Query("SELECT * FROM alarm WHERE id=:id")
	NacAlarm findAlarm(long id);

	/**
	 * Get an active alarm.
	 *
	 * @return An active alarm.
	 */
	@Query("SELECT * FROM alarm WHERE is_active=1 LIMIT 1")
	LiveData<NacAlarm> getActiveAlarm();

	/**
	 * Get a list of all active alarms.
	 *
	 * @return List of all active alarms.
	 */
	@Query("SELECT * FROM alarm WHERE is_active=1")
	LiveData<List<NacAlarm>> getActiveAlarms();

	/**
	 * Get a list of all active alarms.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return List of all active alarms.
	 */
	@Query("SELECT * FROM alarm WHERE is_active=1")
	List<NacAlarm> getActiveAlarmsNow();

	/**
	 * Get all alarms.
	 *
	 * @return All alarms.
	 */
	@Query("SELECT * FROM alarm")
	LiveData<List<NacAlarm>> getAllAlarms();

	/**
	 * Get all alarms.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return All alarms.
	 */
	@Query("SELECT * FROM alarm")
	List<NacAlarm> getAllAlarmsNow();

	/**
	 * Insert an alarm.
	 *
	 * @param  alarm  The alarm to insert.
	 *
	 * @return The row ID of the alarm that was inserted.
	 */
	@Insert()
	long insert(NacAlarm alarm);

	/**
	 * Update an existing alarm
	 *
	 * @param  alarm  The alarm to update.
	 *
	 * @return The number of alarms updated.
	 */
	@Update
	int update(NacAlarm alarm);

}
