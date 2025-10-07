package com.nfcalarmclock.card

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Generic card adapter.
 *
 * @param viewHolderConstructor Constructor to create the view holder easily in
 *                              onCreateViewHolder().
 */
abstract class NacBaseCardAdapter<T: NacAlarm, VH: NacBaseCardHolder<T>>(
	val viewHolderConstructor: (View) -> VH
)
	: ListAdapter<T, VH>(NacBaseDiffItemCallback<T>())
{

	/**
	 * Listener for when a view holder is bound.
	 */
	fun interface OnViewHolderBoundListener<VH>
	{
		fun onViewHolderBound(card: VH, index: Int)
	}

	/**
	 * Listener for when a view holder is created.
	 */
	fun interface OnViewHolderCreatedListener<VH>
	{
		fun onViewHolderCreated(card: VH)
	}

	/**
	 * Layout resource ID.
	 */
	abstract val layoutId: Int

	/**
	 * Listener for when an alarm card is bound.
	 */
	var onViewHolderBoundListener: OnViewHolderBoundListener<VH>? = null

	/**
	 * Listener for when an alarm card is created.
	 */
	var onViewHolderCreatedListener: OnViewHolderCreatedListener<VH>? = null

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
	fun getItemAt(index: Int): T
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
		// Get the item at the index
		val item = getItem(index)

		// Return the alarm ID or NO_ID
		return item?.id ?: RecyclerView.NO_ID
	}

	/**
	 * Setup the card view holder.
	 *
	 * @param card The card view holder.
	 * @param index The position of the card.
	 */
	override fun onBindViewHolder(card: VH, index: Int)
	{
		// Get the item at the index
		val item = getItem(index)

		// Initialize the card
		card.bind(item)

		// Call the listener
		onViewHolderBoundListener?.onViewHolderBound(card, index)
	}

	/**
	 * Create the view holder.
	 *
	 * @param parent The parent view.
	 * @param viewType The type of view.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH
	{
		// Inflate the card
		val inflater = LayoutInflater.from(parent.context)
		val root = inflater.inflate(layoutId, parent, false)
		val card = viewHolderConstructor(root)

		// Call the listener
		onViewHolderCreatedListener?.onViewHolderCreated(card)

		// Return the card
		return card
	}

}