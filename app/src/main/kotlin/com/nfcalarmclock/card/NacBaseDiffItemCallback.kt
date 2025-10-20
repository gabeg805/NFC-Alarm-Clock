package com.nfcalarmclock.card

import androidx.recyclerview.widget.DiffUtil
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Generic diff item callback object.
 */
class NacBaseDiffItemCallback<T: NacAlarm>
	: DiffUtil.ItemCallback<T>()
{

	/**
	 * Check if items are the same.
	 */
	override fun areItemsTheSame(
		oldItem: T,
		newItem: T
	): Boolean
	{
		return oldItem.equalsId(newItem)
	}

	/**
	 * Check if the contents of the items are the same.
	 */
	override fun areContentsTheSame(
		oldItem: T,
		newItem: T
	): Boolean
	{
		// NOTE: if you use equals, your object must properly override Object#equals()
		// Incorrectly returning false here will result in too many animations.
		// This will call the Object.equals() method.
		return oldItem == newItem
	}

}