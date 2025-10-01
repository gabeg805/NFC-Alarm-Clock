package com.nfcalarmclock.card

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Alarm card adapter.
 */
class NacCardAdapter
	: ListAdapter<NacAlarm, NacCardHolder>(DIFF_CALLBACK)
{

	companion object
	{

		/**
		 * Callback to diff two items.
		 */
		val DIFF_CALLBACK: DiffUtil.ItemCallback<NacAlarm> =
			object : DiffUtil.ItemCallback<NacAlarm>()
			{

				/**
				 * Check if items are the same.
				 */
				override fun areItemsTheSame(
					oldAlarm: NacAlarm,
					newAlarm: NacAlarm
				): Boolean
				{
					return oldAlarm.equalsId(newAlarm)
				}

				/**
				 * Check if the contents of the items are the same.
				 */
				override fun areContentsTheSame(
					oldAlarm: NacAlarm,
					newAlarm: NacAlarm
				): Boolean
				{
					// NOTE: if you use equals, your object must properly override Object#equals()
					// Incorrectly returning false here will result in too many animations.
					return oldAlarm.equals(newAlarm)
				}

			}

	}

	/**
	 * Listener for when an alarm card is bound.
	 */
	interface OnViewHolderBoundListener
	{
		fun onViewHolderBound(card: NacCardHolder, index: Int)
	}

	/**
	 * Listener for when an alarm card is created.
	 */
	interface OnViewHolderCreatedListener
	{
		fun onViewHolderCreated(card: NacCardHolder)
	}

	/**
	 * Listener for when an alarm card is bound.
	 */
	var onViewHolderBoundListener: OnViewHolderBoundListener? = null

	/**
	 * Listener for when an alarm card is created.
	 */
	var onViewHolderCreatedListener: OnViewHolderCreatedListener? = null

	/**
	 * Constructor.
	 */
	init
	{
		setHasStableIds(true)
	}

	/**
	 * Get the alarm at the given index.
	 *
	 * @return The alarm at the given index.
	 */
	fun getAlarmAt(index: Int): NacAlarm
	{
		return getItem(index)
	}

	/**
	 * Get the unique ID of an alarm. Used alongside setHasStableIds().
	 *
	 * @return The unique ID of an alarm. Used alongside setHasStableIds().
	 */
	override fun getItemId(index: Int): Long
	{
		// Get the alarm at the index
		val alarm = getItem(index)

		// Return the alarm ID or NO_ID
		return alarm?.id ?: RecyclerView.NO_ID
	}

	/**
	 * Setup the alarm card.
	 *
	 * @param  card  The alarm card.
	 * @param  index  The position of the alarm card.
	 */
	override fun onBindViewHolder(card: NacCardHolder, index: Int)
	{
		// Get the alarm at the index
		val alarm = getItem(index)

		// Initialize the card
		card.bind(alarm)

		// Call the listener
		onViewHolderBoundListener?.onViewHolderBound(card, index)
	}

	/**
	 * Create the view holder.
	 *
	 * @param  parent  The parent view.
	 * @param  viewType  The type of view.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NacCardHolder
	{
		// Inflate the card
		val inflater = LayoutInflater.from(parent.context)
		val root = inflater.inflate(R.layout.card_frame, parent, false)
		val card = NacCardHolder(root)

		// Call the listener
		onViewHolderCreatedListener?.onViewHolderCreated(card)

		// Return the card
		return card
	}

}