package com.nfcalarmclock.nfc

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import com.nfcalarmclock.R
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Adapter for a ListView to show NFC tag information.
 */
class NacNfcTagAdapter(

	/**
	 * Shared preferences.
	 */
	val sharedPreferences: NacSharedPreferences

	// Constructor
) : ListAdapter<NacNfcTag, NacNfcTagViewHolder>(DIFF_CALLBACK)
{

	/**
	 * Listener for when a user wants to modify an NFC tag.
	 */
	interface OnModifyRequestListener
	{
		fun onDelete(position: Int, nfcTag: NacNfcTag)
		fun onRename(position: Int, nfcTag: NacNfcTag)
	}

	/**
	 * Listener when requesting to modify an NFC tag.
	 */
	var onModifyRequestListener: OnModifyRequestListener? = null

	/**
	 * Bind the view holder.
	 */
	override fun onBindViewHolder(holder: NacNfcTagViewHolder, position: Int)
	{
		// Get the NFC tag at the index
		val nfcTag = getItem(position)

		// Populate the view with the NFC tag information
		holder.nameTextView.text = nfcTag?.name
		holder.nfcIdTextView.text = nfcTag?.nfcId

		// Set the color of the name
		holder.nameTextView.setTextColor(sharedPreferences.themeColor)

		// Set click listeners on the buttons
		holder.deleteButton.setOnClickListener {

			// Check if the NFC tag is not null
			if (nfcTag != null)
			{
				// Call the listener
				onModifyRequestListener?.onDelete(position, nfcTag)
			}

		}

		holder.renameButton.setOnClickListener {

			// Check if the NFC tag is not null
			if (nfcTag != null)
			{
				// Call the listener
				onModifyRequestListener?.onRename(position, nfcTag)
			}

		}
	}

	/**
	 * Create the view holder.
	 */
	override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NacNfcTagViewHolder
	{
		// Inflate the NFC tag
		val inflater = LayoutInflater.from(parent.context)
		val root = inflater.inflate(R.layout.nac_nfc_tag, parent, false)

		// Return the view holder
		return NacNfcTagViewHolder(root)
	}

	companion object
	{

		/**
		 * Callback to diff two items.
		 */
		val DIFF_CALLBACK: DiffUtil.ItemCallback<NacNfcTag> =
			object : DiffUtil.ItemCallback<NacNfcTag>()
			{

				/**
				 * Check if items are the same.
				 */
				override fun areItemsTheSame(
					oldNfcTag: NacNfcTag,
					newNfcTag: NacNfcTag
				): Boolean
				{
					return oldNfcTag.nfcId == newNfcTag.nfcId
				}

				/**
				 * Check if the contents of the items are the same.
				 */
				override fun areContentsTheSame(
					oldNfcTag: NacNfcTag,
					newNfcTag: NacNfcTag
				): Boolean
				{
					// NOTE: if you use equals, your object must properly override Object#equals()
					// Incorrectly returning false here will result in too many animations.
					return (oldNfcTag.name == newNfcTag.name)
						&& (oldNfcTag.nfcId == newNfcTag.nfcId)
				}

			}

	}

}