package com.nfcalarmclock.nfc

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textview.MaterialTextView
import com.nfcalarmclock.R
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment

/**
 * Select an NFC tag that has been previously saved.
 */
class NacSelectNfcTagDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Listener for using any NFC tag.
	 */
	interface OnSelectNfcTagListener
	{
		fun onCancel()
		fun onSelected(nfcId: String)
	}

	/**
	 * List of NFC tags.
	 */
	var nfcTags: List<NacNfcTag> = ArrayList()

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
		// Super
		super.onCancel(dialog)

		// Call the listener
		onSelectNfcTagListener?.onCancel()
	}

	/**
	 * Called when the dialog view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the views
		val textView = view.findViewById(R.id.nfc_tag_dropdown_menu) as MaterialAutoCompleteTextView
		val doneButton = view.findViewById(R.id.select_nfc_tag) as MaterialButton
		primaryButton = doneButton

		// Get the list of all NFC tag names
		val nfcTagNames = nfcTags.map { it.name }

		// Setup the text view
		textView.setSimpleItems(nfcTagNames.toTypedArray())
		textView.setSelection(0)
		textView.onItemClickListener = AdapterView.OnItemClickListener { _, v, _, _ ->

			// Set the selected NFC tag name
			selectedNfcTag = (v as MaterialTextView).text.toString()

		}

		// Setup the button
		doneButton.setOnClickListener {

			// Call the listener
			onSelectNfcTagListener?.onSelected(selectedNfcTag)

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