package com.nfcalarmclock.alarm.card

import androidx.lifecycle.MutableLiveData
import com.nfcalarmclock.alarm.db.NacAlarm

class NacAlarmCardAdapterLiveData
	: MutableLiveData<List<NacAlarm>>()
{

	/**
	 * Calculate the merge between two lists of alarms.
	 *
	 * @return The merged alarm list.
	 */
	private fun calculateMerge(
		oAlarms: List<NacAlarm>?,
		nAlarms: List<NacAlarm>?
	): List<NacAlarm>
	{
		val oldAlarms = oAlarms ?: ArrayList()
		val newAlarms = nAlarms ?: ArrayList()

		// Initialize list of merged alarms
		val mergedAlarms: MutableList<NacAlarm> = ArrayList(oldAlarms)

		// Check if the current list is empty
		if (oldAlarms.isEmpty())
		{
			// Add all the new alarms to the merged list
			mergedAlarms.addAll(newAlarms)
		}
		// Current list is not empty
		else
		{
			// Initialize list of indices of alarms that are not found
			val notFoundIndices: MutableList<Int> = ArrayList()

			// Add the indices to the list
			for (j in oldAlarms.indices)
			{
				notFoundIndices.add(j)
			}

			// Iterate over the new alarm list
			for (a in newAlarms)
			{
				var foundAlarm: NacAlarm? = null

				// Iterate over the indices of the old alarm list
				for (j in oldAlarms.indices)
				{
					// Get the alarm
					val b = mergedAlarms[j]

					// Check if the old alarm ID equals the new alarm
					if (b.equalsId(a))
					{
						// Remove not found index
						//notFoundIndices.remove(Integer.valueOf(j))
						notFoundIndices.remove(j)

						// Both alarms are equal
						if (b == a)
						{
							// NOMINAL: do nothing
						}
						// Alarms are not equal
						else
						{
							// UPDATE
							mergedAlarms[j] = a
						}

						// Set the found alarm equal to the current alarm
						foundAlarm = b
						break
					}
				}

				// Alarm was not found
				if (foundAlarm == null)
				{
					// ADD
					mergedAlarms.add(a)
				}

			}

			// Iterate over the indices in reverse
			//for (i in notFoundIndices.indices.reversed())
			for (index in notFoundIndices.reversed())
			{
				// Remove the indices that were not found
				mergedAlarms.removeAt(index)
			}
		}

		return mergedAlarms
	}

	/**
	 * Merge the current alarms with a new set of alarms.
	 */
	fun merge(alarms: List<NacAlarm>?, copiedIds: Pair<Long, Long>? = null)
	{
		// Get the current alarms
		val currentAlarms = value

		// Merge the current alarms with the new alarms
		var mergedAlarms = calculateMerge(currentAlarms, alarms)

		// Check if an alarm was copied
		if (copiedIds != null)
		{
			// Get the indices of the original and output alarm
			val origIndex = mergedAlarms.indexOfFirst { it.id == copiedIds.first }
			val outputIndex = mergedAlarms.indexOfFirst { it.id == copiedIds.second }

			// Check if the indices are valid
			if ((origIndex > 0) && (outputIndex > 0))
			{
				// Change the index of the copied alarm from the end of the list, to the
				// index right after the original alarm
				val newList = mergedAlarms.toMutableList()
				val outputAlarm = newList.removeAt(outputIndex)

				newList.add(origIndex+1, outputAlarm)
				mergedAlarms = newList
			}
		}

		// Set the merged alarms as the current alarms
		value = mergedAlarms
	}

	/**
	 * Merge the current alarms with a new set of alarms, and sort the merge.
	 */
	fun mergeSort(alarms: List<NacAlarm>?, order: List<Long>? = null)
	{
		// Get the current alarms
		val currentAlarms = value

		// Merge the current alarms with the new alarms
		var mergedAlarms = calculateMerge(currentAlarms, alarms).toMutableList()

		// Normal sort of the merged alarms. There was no order specified or mismatching sizes of lists
		if ((order == null) || (order.size != mergedAlarms.size))
		{
			mergedAlarms.sort()
		}
		// Use the ordered list as the sort order for the alarms
		else
		{
			val alarmIdMap = mergedAlarms.associateBy { it.id }
			mergedAlarms = order.mapNotNull { alarmIdMap[it] }.toMutableList()
		}

		// Set the merged alarms as the current alarms
		value = mergedAlarms
	}

	/**
	 * Sort the current values.
	 */
	fun sort(order: List<Long>? = null)
	{
		// Create a list of alarms using the current alarm list
		var newAlarms: MutableList<NacAlarm> = value?.toMutableList()
			?: mutableListOf()

		// Sort the alarms normally
		if ((order == null) || (order.size != newAlarms.size))
		{
			newAlarms.sort()
		}
		// Use the ordered list as the sort order for the alarms
		else
		{
			val alarmIdMap = newAlarms.associateBy { it.id }
			newAlarms = order.mapNotNull { alarmIdMap[it] }.toMutableList()
		}

		// Set the sorted alarm list as the current alarms
		value = newAlarms
	}

}