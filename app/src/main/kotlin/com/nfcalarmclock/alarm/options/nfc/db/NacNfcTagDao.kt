package com.nfcalarmclock.alarm.options.nfc.db

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
import androidx.room.Update

/**
 * Data access object for NFC tags.
 */
@Dao
interface NacNfcTagDao
{

	/**
	 * Get all NFC tags.
	 *
	 * @return All NFC tags.
	 */
	@get:Query("SELECT * FROM nfc_tag ORDER BY name")
	val allNfcTags: LiveData<List<NacNfcTag>>

	/**
	 * Count the number of created alarm statistics.
	 *
	 * @return The number of created alarm statistics.
	 */
	@Query("SELECT COUNT(id) FROM nfc_tag")
	suspend fun count(): Long

	/**
	 * Delete an NFC tag.
	 *
	 * @param  nfcTag  An NFC tag to delete.
	 *
	 * @return The number of rows deleted.
	 */
	@Delete
	suspend fun delete(nfcTag: NacNfcTag): Int

	/**
	 * Find an NFC tag.
	 *
	 * @param  nfcId  The ID of the NFC tag to find.
	 *
	 * @return The NFC tag with the ID.
	 */
	@Query("SELECT * FROM nfc_tag WHERE nfc_id=:nfcId")
	suspend fun findNfcTag(nfcId: String): NacNfcTag?

	/**
	 * Get all NFC tags.
	 * <p>
	 * This will wait until all alarms are selected.
	 *
	 * @return All NFC tags.
	 */
	@Query("SELECT * FROM nfc_tag ORDER BY name")
	suspend fun getAllNfcTags(): List<NacNfcTag>

	/**
	 * Insert an NFC tag.
	 *
	 * @param  nfcTag  The NFC tag to insert.
	 *
	 * @return The row ID of the NFC tag that was inserted.
	 */
	@Insert
	suspend fun insert(nfcTag: NacNfcTag): Long

	/**
	 * Update an existing NFC tag.
	 *
	 * @param  nfcTag  The NFC tag to update.
	 *
	 * @return The number of NFC tags updated.
	 */
	@Update
	suspend fun update(nfcTag: NacNfcTag): Int

}