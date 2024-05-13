package com.nfcalarmclock.nfc

import android.view.View
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView.ViewHolder
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
) : ViewHolder(root)
{

	/**
	 * Name.
	 */
	val nameTextView = root.findViewById(R.id.nfc_tag_name) as TextView

	/**
	 * NFC ID.
	 */
	val nfcIdTextView = root.findViewById(R.id.nfc_tag_id) as TextView

	/**
	 * Delete button.
	 */
	val deleteButton = root.findViewById(R.id.delete_button) as MaterialButton

	/**
	 * Rename button.
	 */
	val renameButton = root.findViewById(R.id.rename_button) as MaterialButton

}