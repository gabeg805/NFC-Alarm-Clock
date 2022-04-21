package com.nfcalarmclock.card;

import androidx.lifecycle.MutableLiveData;

import com.nfcalarmclock.alarm.NacAlarm;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NacCardAdapterLiveData
	extends MutableLiveData<List<NacAlarm>>
{

	/**
	 * Calculate the merge between two lists of alarms.
	 *
	 * @return The merged alarm list.
	 */
	public List<NacAlarm> calculateMerge(List<NacAlarm> oldAlarms,
		List<NacAlarm> newAlarms)
	{
		if (oldAlarms == null)
		{
			oldAlarms = new ArrayList<>();
		}

		if (newAlarms == null)
		{
			newAlarms = new ArrayList<>();
		}

		List<NacAlarm> mergedAlarms = new ArrayList<>(oldAlarms);
		int oldSize = oldAlarms.size();
		int newSize = newAlarms.size();

		if (oldSize == 0)
		{
			mergedAlarms.addAll(newAlarms);
		}
		else
		{
			List<Integer> notFoundIndices = new ArrayList<>();
			for (int j=0; j < oldSize; j++)
			{
				notFoundIndices.add(j);
			}

			for (int i=0; i < newSize; i++)
			{
				NacAlarm a = newAlarms.get(i);
				NacAlarm foundAlarm = null;

				for (int j=0; j < oldSize; j++)
				{
					NacAlarm b = mergedAlarms.get(j);
					if (b.equalsId(a))
					{
						// Remove not found indices
						notFoundIndices.remove(Integer.valueOf(j));

						if (b.equals(a))
						{
							// NOMINAL: do nothing
						}
						else
						{
							// UPDATE
							mergedAlarms.set(j, a);
						}

						foundAlarm = b;
						break;
					}
				}

				if (foundAlarm == null)
				{
					// ADD
					mergedAlarms.add(a);
				}
			}

			for (int i=notFoundIndices.size()-1; i >= 0; i--)
			{
				int index = notFoundIndices.get(i);
				mergedAlarms.remove(index);
			}
		}

		return mergedAlarms;
	}

	/**
	 * Merge the current alarms with a new set of alarms.
	 */
	public void merge(List<NacAlarm> alarms)
	{
		List<NacAlarm> currentAlarms = getValue();
		List<NacAlarm> mergedAlarms = this.calculateMerge(currentAlarms, alarms);

		setValue(mergedAlarms);
	}

	/**
	 * Merge the current alarms with a new set of alarms, and sort the merge.
	 */
	public void mergeSort(List<NacAlarm> alarms)
	{
		List<NacAlarm> currentAlarms = getValue();
		List<NacAlarm> mergedAlarms = this.calculateMerge(currentAlarms, alarms);

		Collections.sort(mergedAlarms);
		setValue(mergedAlarms);
	}

	/**
	 * Sort the current values.
	 */
	public void sort()
	{
		List<NacAlarm> alarms = getValue();
		List<NacAlarm> newAlarms = new ArrayList<>(alarms);

		Collections.sort(newAlarms);
		setValue(newAlarms);
	}

}
