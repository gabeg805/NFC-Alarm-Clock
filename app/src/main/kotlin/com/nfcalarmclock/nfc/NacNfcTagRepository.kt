package com.nfcalarmclock.nfc

import androidx.lifecycle.LiveData
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.nfc.db.NacNfcTagDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Inject

class NacNfcTagRepository @Inject constructor(

	/**
	 * Data access object for NFC tags.
	 */
	private val nfcTagDao: NacNfcTagDao

)
{

	/**
	 * Live data list of all NFC tags.
	 */
	val allNfcTags: LiveData<List<NacNfcTag>>
		get() = nfcTagDao.allNfcTags

	/**
	 * Count the number of NFC tags.
	 *
	 * @return The number of NFC tags.
	 */
	suspend fun count(): Long = nfcTagDao.count()

	/**
	 * Delete an NFC tag, asynchronously, from the database.
	 *
	 * @return The number of rows deleted.
	 */
	suspend fun delete(nfcTag: NacNfcTag): Int = nfcTagDao.delete(nfcTag)

	/**
	 * Delete an NFC tag, asynchronously, from the database.
	 *
	 * @return The number of rows deleted.
	 */
	suspend fun deleteAll(): Int = nfcTagDao.deleteAll()

	/**
	 * Get an NFC tag with the given ID.
	 *
	 * @return An NFC tag with the given ID.
	 */
	suspend fun findNfcTag(nfcId: String): NacNfcTag? = nfcTagDao.findNfcTag(nfcId)

	/**
	 * All NFC tags in the database.
	 */
	suspend fun getAllNfcTags(): List<NacNfcTag> = nfcTagDao.getAllNfcTags()

	/**
	 * Insert an NFC tag, asynchronously, into the database.
	 *
	 * @param  nfcTag  NFC tag to insert.
	 *
	 * @return The row ID of the inserted NFC tag.
	 */
	suspend fun insert(nfcTag: NacNfcTag): Long = nfcTagDao.insert(nfcTag)

	/**
	 * Update an NFC tag, asynchronously, in the database.
	 *
	 * @param  nfcTag  NFC tag to update.
	 *
	 * @return The number of NFC tags updated.
	 */
	suspend fun update(nfcTag: NacNfcTag): Int = nfcTagDao.update(nfcTag)

}

/**
 * Hilt module to provide an instance of the repository.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacNfcTagRepositoryModule
{

	/**
	 * Provide an instance of the repository.
	 */
	@Provides
	fun provideNfcTagRepository(nfcTagDao: NacNfcTagDao) : NacNfcTagRepository
	{
		return NacNfcTagRepository(nfcTagDao)
	}

}
