package com.nfcalarmclock.nfc

import androidx.lifecycle.LiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.nfcalarmclock.nfc.db.NacNfcTag
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * NFC tag view model.
 */
@HiltViewModel
class NacNfcTagViewModel @Inject constructor(

	/**
	 * NFC tag repository.
	 */
	private val nfcTagRepository: NacNfcTagRepository

) : ViewModel()
{

	/**
	 * Live data list of all NFC tags.
	 */
	val allNfcTags: LiveData<List<NacNfcTag>> = nfcTagRepository.allNfcTags

	/**
	 * Delete an NFC tag from the database.
	 *
	 * @param  nfcTag  NFC tag to delete.
	 *
	 * @return The number of rows deleted.
	 */
	fun delete(nfcTag: NacNfcTag)
	{
		viewModelScope.launch {
			nfcTagRepository.delete(nfcTag)
		}
	}

	/**
	 * Count the number of NFC tags.
	 *
	 * @return The number of NFC tags.
	 */
	suspend fun count(): Long = nfcTagRepository.count()

	/**
	 * Delete an NFC tag, asynchronously, from the database.
	 *
	 * @return The number of rows deleted.
	 */
	suspend fun deleteAll(): Int = nfcTagRepository.deleteAll()

	/**
	 * Find an NFC tag.
	 *
	 * @param nfcId The ID of the NFC tag to find.
	 *
	 * @return The nfcTag with the ID.
	 */
	suspend fun findNfcTag(nfcId: String): NacNfcTag? = nfcTagRepository.findNfcTag(nfcId)

	/**
	 * All NFC tags in the database.
	 */
	suspend fun getAllNfcTags(): List<NacNfcTag> = nfcTagRepository.getAllNfcTags()

	/**
	 * Insert an NFC tag into the database.
	 *
	 * @param  nfcTag  The NFC tag to insert.
	 *
	 * @return The row ID of the NFC tag that was inserted.
	 */
	fun insert(nfcTag: NacNfcTag)
	{
		viewModelScope.launch {

			// Get the row ID
			val rowId = nfcTagRepository.insert(nfcTag)

			// NFC tag was inserted successfully
			if (rowId > 0)
			{
				// Alarm ID has not been set yet
				if (nfcTag.id == 0L)
				{
					nfcTag.id = rowId
				}
			}
		}
	}

	/**
	 * Update an NFC tag in the database.
	 *
	 * @param  nfcTag  The NFC tag to update.
	 *
	 * @return The number of rows updated.
	 */
	fun update(nfcTag: NacNfcTag)
	{
		viewModelScope.launch {
			nfcTagRepository.update(nfcTag)
		}
	}

}