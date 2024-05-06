package com.nfcalarmclock.nfc

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.nfcalarmclock.R
import com.nfcalarmclock.nfc.db.NacNfcTag

class NacSaveNfcTagDialog

	// Constructor
	: BottomSheetDialogFragment()

{

	/**
	 * Listener for saving an NFC tag.
	 */
	interface OnSaveNfcTagListener
	{
		fun onCancelNfcTag()
		fun onSaveNfcTag(nfcTag: NacNfcTag)
	}

	/**
	 * NFC tag ID.
	 */
	var nfcId: String = ""

	/**
	 * Listener for when the NFC tag is saved.
	 */
	var onSaveNfcTagListener: OnSaveNfcTagListener? = null

	/**
	 * Called when the dialog view is created.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?)
		: View?
	{
		return inflater.inflate(R.layout.dlg_save_nfc_tag, container, false)
	}

	/**
	 * Called when the dialog is canceled.
	 */
	override fun onCancel(dialog: DialogInterface)
	{
		super.onCancel(dialog)

		// Call the listener
		onSaveNfcTagListener?.onCancelNfcTag()
	}

	/**
	 * Called when the dialog view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		// Get the views
		val editText = view.findViewById(R.id.nfc_tag_name) as TextInputEditText
		val saveButton = view.findViewById(R.id.save_nfc_tag) as MaterialButton
		val skipButton = view.findViewById(R.id.skip_nfc_tag) as MaterialButton

		// Setup the edit view
		editText.addTextChangedListener {
			println("HELLO")
		}

		// Setup the save button
		// TODO: Make this unusable if no words are entered
		saveButton.setOnClickListener {

			// Get the name
			val nfcName = editText.text.toString()

			// Create an NFC tag
			val nfcTag = NacNfcTag(nfcName, nfcId)
			println("SAVE : $nfcName | $nfcId")

			// Call the listener
			onSaveNfcTagListener?.onSaveNfcTag(nfcTag)

		}

		// Setup the skip button
		skipButton.setOnClickListener {

			// Call the listener
			onSaveNfcTagListener?.onCancelNfcTag()

		}

	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSaveNfcTagDialog"

	}

}