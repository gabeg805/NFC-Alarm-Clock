package com.nfcalarmclock;

import androidx.lifecycle.MutableLiveData;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class NacAlarmCardAdapterLiveData
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
			for (NacAlarm a : newAlarms)
			{
				mergedAlarms.add(a);
			}
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

//
//	public void mergeLiveDataWithAlarmCardAdapter(List<NacAlarm> alarms)
//	{
//		NacAlarmCardAdapter adapter = this.getAlarmCardAdapter();
//		List<NacAlarm> adapterAlarms = adapter.getCurrentList();
//		List<NacAlarm> newAlarms = new ArrayList<>(adapterAlarms);
//
//		int size = alarms.size();
//		int adapterSize = newAlarms.size();
//
//		if (adapterSize == 0)
//		{
//			NacUtility.printf("AHHHHHH! Will this be triggered if everything gets deleted?");
//			for (NacAlarm a : alarms)
//			{
//				NacUtility.printf("Adding alarm to adapter list: %d", a.getId());
//				newAlarms.add(a);
//			}
//		}
//		else
//		{
//			List<Integer> notFoundIndices = new ArrayList<>();
//			for (int j=0; j < adapterSize; j++)
//			{
//				notFoundIndices.add(j);
//			}
//
//			for (int i=0; i < size; i++)
//			{
//				NacAlarm a = alarms.get(i);
//				NacAlarm foundAlarm = null;
//
//				for (int j=0; j < adapterSize; j++)
//				{
//					NacAlarm b = newAlarms.get(j);
//					if (b.equalsId(a))
//					{
//						// Remove not found indices
//						notFoundIndices.remove(Integer.valueOf(j));
//
//						if (b.equals(a))
//						{
//							// NOMINAL: do nothing
//							NacUtility.printf("Do nothing to alarm: %d", b.getId());
//						}
//						else
//						{
//							// UPDATE
//							NacUtility.printf("Update alarm: %d", b.getId());
//							newAlarms.set(j, a);
//						}
//
//						foundAlarm = b;
//						break;
//					}
//				}
//
//				if (foundAlarm == null)
//				{
//					// ADD
//					NacUtility.printf("Add alarm: %d", a.getId());
//					newAlarms.add(a);
//				}
//			}
//
//			for (int i=notFoundIndices.size()-1; i >= 0; i--)
//			{
//				int index = notFoundIndices.get(i);
//				NacUtility.printf("Delete alarm: %d", newAlarms.get(index).getId());
//				newAlarms.remove(index);
//			}
//		}
//
//		RecyclerView rv = this.getRecyclerView();
//		if (adapter.getCardsExpandedCount(rv) == 0)
//		{
//			NacUtility.printf("Sorting alarms!");
//			Collections.sort(newAlarms);
//		}
//
//		this.getAlarmCardAdapterLiveData().setValue(newAlarms);
//	}
