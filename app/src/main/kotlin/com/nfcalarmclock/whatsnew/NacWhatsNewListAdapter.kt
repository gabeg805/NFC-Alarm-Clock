package com.nfcalarmclock.whatsnew

import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.R

/**
 * Generic message holder for the adapter.
 */
class MessageHolder(root: View)
	: RecyclerView.ViewHolder(root)

/**
 * List adapter for all the What's New items.
 */
class NacWhatsNewListAdapter
	: ListAdapter<String, MessageHolder>(DIFF_CALLBACK)
{

	/**
	 * Called when creating the view holder.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder
	{
		// Inflate the item
		val inflater = LayoutInflater.from(parent.context)
		val root = inflater.inflate(R.layout.nac_whats_new_entry, parent, false)

		// Create the view holder
		return MessageHolder(root)
	}

	/**
	 * Called when binding the view holder to data that was submitted to the
	 * adapter.
	 */
	@Suppress("deprecation")
	override fun onBindViewHolder(holder: MessageHolder, position: Int)
	{
		// Get the textview
		val textView = holder.itemView.findViewById<TextView>(R.id.whats_new_message)

		// Get the message at the position
		val message = getItem(position)

		// Set the text
		textView.text = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N)
		{
			Html.fromHtml(message, Html.FROM_HTML_MODE_LEGACY)
		}
		else
		{
			Html.fromHtml(message)
		}
	}

	companion object
	{

		/**
		 * Callback to diff the items.
		 */
		val DIFF_CALLBACK: DiffUtil.ItemCallback<String> =
			object : DiffUtil.ItemCallback<String>()
			{

				/**
				 * Check if items are the same.
				 */
				override fun areItemsTheSame(
					oldItem: String,
					newItem: String
				): Boolean
				{
					return (oldItem == newItem)
				}

				/**
				 * Check if the contents of the items are the same.
				 */
				override fun areContentsTheSame(
					oldItem: String,
					newItem: String
				): Boolean
				{
					return areItemsTheSame(oldItem, newItem)
				}

			}

	}

}
