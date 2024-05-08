package com.nfcalarmclock.nfc

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment

/**
 * Save an NFC tag that was scanned.
 */
class NacSaveNfcTagDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Listener for saving an NFC tag.
	 */
	interface OnSaveNfcTagListener
	{
		fun onCancel()
		fun onSave(nfcTag: NacNfcTag)
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
		// Super
		super.onCancel(dialog)

		// Call the listener
		onSaveNfcTagListener?.onCancel()
	}

	/**
	 * Called when the dialog view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the views
		val inputLayout = view.findViewById(R.id.nfc_tag_input_layout) as TextInputLayout
		val editText = view.findViewById(R.id.nfc_tag_name) as TextInputEditText
		val saveButton = view.findViewById(R.id.save_nfc_tag) as MaterialButton
		val skipButton = view.findViewById(R.id.skip_nfc_tag) as MaterialButton
		primaryButton = saveButton

		// Setup the input layout
		inputLayout.hintTextColor = ColorStateList.valueOf(sharedPreferences.themeColor)
		inputLayout.boxStrokeColor = sharedPreferences.themeColor

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
			onSaveNfcTagListener?.onSave(nfcTag)

			// Dismiss the dialog
			dismiss()

		}

		// Setup the skip button
		skipButton.setOnClickListener {

			// Call the listener
			onSaveNfcTagListener?.onCancel()

			// Dismiss the dialog
			dismiss()

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