package com.nfcalarmclock.alarm.options.nfc

import android.widget.AdapterView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.view.setupInputLayoutColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Select an NFC tag that has been previously saved.
 */
@AndroidEntryPoint
class NacSelectNfcTagDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_select_nfc_tag

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * List of NFC tags.
	 */
	private var allNfcTags: List<NacNfcTag> = emptyList()

	/**
	 * Name of the selected NFC tag.
	 */
	private var selectedNfcTag: NacNfcTag = NacNfcTag()

	/**
	 * Update the alarm with selected options.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		alarm?.nfcTagId = selectedNfcTag.nfcId
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		lifecycleScope.launch {

			// Get all the NFC tags
			allNfcTags = nfcTagViewModel.getAllNfcTags()

			// Set the selected NFC tag based on the alarm if it is set
			selectedNfcTag = allNfcTags.firstOrNull { it.nfcId == alarm?.nfcTagId }
				?: allNfcTags[0]

			// Verify that the selected NFC tag exists. If it does not, set it to the
			// first tag in the list
			if (allNfcTags.none { it.nfcId == selectedNfcTag.nfcId })
			{
				selectedNfcTag = allNfcTags[0]
			}

			// Set the list of items in the textview dropdown menu
			val nfcTagNames = allNfcTags.map { it.name }.toTypedArray()

			// Setup the input layout and textview
			setupInputLayoutAndTextView(selectedNfcTag.name, nfcTagNames)

		}
	}

	/**
	 * Setup the input layout and textview.
	 */
	private fun setupInputLayoutAndTextView(defaultName: String, nfcTagNames: Array<String>)
	{
		// Get the views
		val inputLayout: TextInputLayout = dialog!!.findViewById(R.id.nfc_tag_input_layout)
		val textView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.nfc_tag_dropdown_menu)

		// Setup the views
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		textView.setSimpleItems(nfcTagNames)
		textView.setText(defaultName, false)

		// Setup the textview listener
		textView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedNfcTag = allNfcTags[position]
		}
	}

}