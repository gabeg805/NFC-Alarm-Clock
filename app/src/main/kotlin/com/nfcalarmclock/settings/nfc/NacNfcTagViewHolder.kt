package com.nfcalarmclock.settings.nfc

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R

/**
 * View holder for an NFC tag.
 */
class NacNfcTagViewHolder(

	/**
	 * Root view.
	 */
	val root: View

	// Constructor
) : RecyclerView.ViewHolder(root)
{

	/**
	 * Name.
	 */
	val nameTextView: TextView = root.findViewById(R.id.nfc_tag_name)

	/**
	 * NFC ID.
	 */
	val nfcIdTextView: TextView = root.findViewById(R.id.nfc_tag_id_value)

	/**
	 * Delete button.
	 */
	val deleteButton: MaterialButton = root.findViewById(R.id.delete_button)

	/**
	 * Rename button.
	 */
	val renameButton: MaterialButton = root.findViewById(R.id.rename_button)

}