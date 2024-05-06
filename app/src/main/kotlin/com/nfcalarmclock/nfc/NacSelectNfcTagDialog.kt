package com.nfcalarmclock.nfc

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textview.MaterialTextView
import com.nfcalarmclock.R

class NacSelectNfcTagDialog

	// Constructor
	: BottomSheetDialogFragment()

{

	/**
	 * Listener for using any NFC tag.
	 */
	interface OnSelectNfcTagListener
	{
		fun onCancelNfcTagScan()
		fun onSelectNfcTag(nfcId: String)
	}

	/**
	 * Name of the selected NFC tag.
	 */
	var selectedNfcTag: String = ""

	/**
	 * Listener for when the name is entered.
	 */
	var onSelectNfcTagListener: OnSelectNfcTagListener? = null

	/**
	 * Called when the dialog view is created.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?)
		: View?
	{
		return inflater.inflate(R.layout.dlg_select_nfc_tag, container, false)
	}

	/**
	 * Called when the dialog is canceled.
	 */
	override fun onCancel(dialog: DialogInterface)
	{
		super.onCancel(dialog)

		// Call the listener
		onSelectNfcTagListener?.onCancelNfcTagScan()
	}

	/**
	 * Called when the dialog view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		super.onViewCreated(view, savedInstanceState)

		// Get the views
		val textView = view.findViewById(R.id.nfc_tag_dropdown_menu) as MaterialAutoCompleteTextView
		val doneButton = view.findViewById(R.id.select_nfc_tag) as MaterialButton

		// Setup the text view
		textView.onItemClickListener = AdapterView.OnItemClickListener { _, v, _, _ ->

			// Set the selected NFC tag name
			selectedNfcTag = (v as MaterialTextView).text.toString()

		}

		// Setup the button
		doneButton.setOnClickListener {

			// Call the listener
			onSelectNfcTagListener?.onSelectNfcTag(selectedNfcTag)

		}
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSelectNfcTagDialog"

	}

}