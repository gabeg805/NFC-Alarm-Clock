package com.nfcalarmclock.whatsnew

import android.annotation.SuppressLint
import android.os.Build
import android.text.Html
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.MarginLayoutParams
import android.widget.TextView
import androidx.core.view.marginTop
import androidx.core.view.updateLayoutParams
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
	 * Called to get the item view type.
	 */
	override fun getItemViewType(position: Int): Int
	{
		// Get the item in the list
		val item = currentList[position]

		// Check if the first char is a number
		return if (item[0].isDigit())
		{
			// Version view type
			1
		}
		else
		{
			// Normal message view type
			0
		}
	}

	/**
	 * Called when creating the view holder.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder
	{
		// Get the layout ID
		val layoutId = when (viewType)
		{
			1    -> R.layout.nac_whats_new_entry_version
			else -> R.layout.nac_whats_new_entry
		}

		// Inflate the item
		val inflater = LayoutInflater.from(parent.context)
		val root = inflater.inflate(layoutId, parent, false)

		// Create the view holder
		return MessageHolder(root)
	}

	/**
	 * Called when binding the view holder to data that was submitted to the
	 * adapter.
	 */
	@SuppressLint("SetTextI18n")
	@Suppress("deprecation")
	override fun onBindViewHolder(holder: MessageHolder, position: Int)
	{
		// Get the message at the position
		val message = getItem(position)

		when (holder.itemViewType)
		{
			// Message view type
			0 -> {
				// Get the textview
				val textView = holder.itemView.findViewById<TextView>(R.id.whats_new_message)

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

			// Version view type
			1 -> {
				// Get the textview
				val textView = holder.itemView.findViewById<TextView>(R.id.version)

				// Prepare the version name and number
				val versionName = holder.itemView.resources.getString(R.string.version)

				// Set the text
				textView.text = "$versionName $message"

				// Update the top margin of the first item to 0
				if (position == 0)
				{
					textView.updateLayoutParams<MarginLayoutParams> {
						topMargin = 0
					}
				}
			}
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
