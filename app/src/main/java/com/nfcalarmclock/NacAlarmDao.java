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
	 */
	@Delete
	void delete(NacAlarm alarm);

	/**
	 * @see SsProblemDao#deleteAll
	 */
	@Query("DELETE FROM alarm")
	void deleteAll();

	/**
	 * @return All alarms.
	 */
	@Query("SELECT * FROM alarm")
	LiveData<List<NacAlarm>> getAll();

	/**
	 * Insert an alarm.
	 *
	 * @param  alarm  An alarm.
	 */
	@Insert(onConflict=OnConflictStrategy.ABORT)
	void insert(NacAlarm alarm);

	/**
	 * Update an existing alarm
	 *
	 * @param  alarm  An alarm.
	 */
	@Update
	void update(NacAlarm alarm);

}
