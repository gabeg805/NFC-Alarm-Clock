package com.nfcalarmclock.nfc

import android.content.DialogInterface
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
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
		fun onSelected(nfcTag: NacNfcTag)
	}

	/**
	 * List of NFC tags.
	 */
	var allNfcTags: List<NacNfcTag> = ArrayList()

	/**
	 * Name of the selected NFC tag.
	 */
	var selectedNfcTag: NacNfcTag? = null

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
		val inputLayout = view.findViewById(R.id.nfc_tag_input_layout) as TextInputLayout
		val textView = view.findViewById(R.id.nfc_tag_dropdown_menu) as MaterialAutoCompleteTextView
		val doneButton = view.findViewById(R.id.select_nfc_tag) as MaterialButton
		primaryButton = doneButton

		// Setup the color of the input layout
		inputLayout.boxBackgroundColor = resources.getColor(R.color.gray)
		inputLayout.boxStrokeColor = sharedPreferences.themeColor
		inputLayout.setEndIconTintList(ColorStateList.valueOf(sharedPreferences.themeColor))

		// Get the list of all NFC tag names
		val nfcTagNames = allNfcTags.map { it.name }
		println("All NFC tags : $nfcTagNames")

		// Set the list of items in the textview's dropdown menu
		textView.setSimpleItems(nfcTagNames.toTypedArray())

		// Check if the list of NFC tags contains the selected NFC tag
		if ((selectedNfcTag != null) && allNfcTags.contains(selectedNfcTag))
		{
			textView.setText(selectedNfcTag!!.name, false)
		}
		// Selected NFC tag is not set or is not in the lsit
		else
		{
			textView.setText(nfcTagNames[0], false)
		}

		// Setup the textview listener
		textView.onItemClickListener = AdapterView.OnItemClickListener { _, v, _, _ ->

			// Get the name of the NFC tag
			val name = (v as MaterialTextView).text.toString()

			// Set the selected NFC tag name
			selectedNfcTag = allNfcTags.first { it.name == name }

		}

		// Setup the button
		doneButton.setOnClickListener {

			// Call the listener
			onSelectNfcTagListener?.onSelected(selectedNfcTag!!)

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