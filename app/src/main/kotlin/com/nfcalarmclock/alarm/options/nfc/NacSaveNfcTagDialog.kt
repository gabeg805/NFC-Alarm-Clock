package com.nfcalarmclock.alarm.options.nfc

import android.view.View
import android.view.inputmethod.EditorInfo
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Space
import android.widget.TextView
import androidx.core.view.isVisible
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.nfc.SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME
import com.nfcalarmclock.nfc.SCANNED_NFC_TAG_ID_BUNDLE_NAME
import com.nfcalarmclock.nfc.setNfcTagIds
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.setupInputLayoutColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Save an NFC tag that was scanned.
 */
@AndroidEntryPoint
open class NacSaveNfcTagDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_save_nfc_tag

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Edit text containing the name of the NFC tag to save.
	 */
	private lateinit var editText: TextInputEditText

	/**
	 * List of NFC tags.
	 */
	private var allNfcTags: List<NacNfcTag> = emptyList()

	/**
	 * Called when the cancel button is clicked.
	 */
	override fun onCancelClicked(alarm: NacAlarm?)
	{
		// Save the alarm
		onSaveAlarm(alarm)
	}

	/**
	 * Update the alarm with selected options.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Get the name
		val nfcName = editText.text.toString().trim()

		// The entered name for an NFC tag already exists
		if (allNfcTags.any { it.name == nfcName })
		{
			quickToast(requireContext(), R.string.error_message_nfc_name_exists)
			throw IllegalStateException()
		}

		// Get the NFC tag bundle paramters
		val nfcId = requireArguments().getString(SCANNED_NFC_TAG_ID_BUNDLE_NAME) ?: ""
		val doesNfcTagAlreadyExist = requireArguments().getBoolean(SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME)

		// NFC tag does not already exist so save the NFC tag
		if (!doesNfcTagAlreadyExist)
		{
			lifecycleScope.launch {
				val tag = NacNfcTag(nfcName, alarm!!.nfcTagId)
				nfcTagViewModel.insert(tag)
			}
		}

		// Get the add and replace radio buttons
		val radioGroup: RadioGroup = dialog!!.findViewById(R.id.save_nfc_tag_add_or_replace_radio_group)
		val addButton: RadioButton = dialog!!.findViewById(R.id.save_nfc_tag_add_radio_button)
		val replaceButton: RadioButton = dialog!!.findViewById(R.id.save_nfc_tag_replace_radio_button)

		// Radio buttons are not shown so do nothing
		if (!radioGroup.isVisible || (alarm == null))
		{
			return
		}

		// Add
		if (addButton.isChecked)
		{
			println("ADD")
			// Create a list of NFC tags
			val nfcTags = alarm.nfcTagIdList
				.toMutableList()
				.apply { add(nfcId) }
				.map { NacNfcTag("", it) }

			// Set the NFC tag IDs to the alarm/timer
			alarm.setNfcTagIds(nfcTags)
		}
		// Replace
		else if (replaceButton.isChecked)
		{
			println("REPLACE")
			alarm.nfcTagId = nfcId
		}
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get all the NFC tags
		lifecycleScope.launch {
			allNfcTags = nfcTagViewModel.getAllNfcTags()
		}

		// Setup the views
		setupAddOrReplace(alarm)
		setupEditText(alarm)
	}

	/**
	 * Setup the add or replace views.
	 */
	private fun setupAddOrReplace(alarm: NacAlarm?)
	{
		// Get the views
		val title: TextView = dialog!!.findViewById(R.id.save_nfc_tag_add_or_replace_title)
		val description: TextView = dialog!!.findViewById(R.id.save_nfc_tag_add_or_replace_description)
		val radioGroup: RadioGroup = dialog!!.findViewById(R.id.save_nfc_tag_add_or_replace_radio_group)
		val separator: Space = dialog!!.findViewById(R.id.save_nfc_tag_separator)

		// Get the visibility depending on if the alarm/timer has NFC tags set or not
		val visibility = if (alarm?.nfcTagId?.isNotEmpty() == true) View.VISIBLE else View.GONE

		// Set the visibility
		title.visibility = visibility
		description.visibility = visibility
		radioGroup.visibility = visibility
		separator.visibility = visibility
	}

	/**
	 * Setup the Cancel button.
	 */
	override fun setupCancelButton(alarm: NacAlarm?)
	{
		// Super
		super.setupCancelButton(alarm)

		// Get whether the NFC tag already exists or not
		val doesNfcTagAlreadyExist = requireArguments().getBoolean(SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME)

		// NFC tag does not already exist so need to save it and give it a name
		if (!doesNfcTagAlreadyExist)
		{
			// Get the cancel button
			val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

			// Rename the button
			cancelButton.setText(R.string.action_skip)
		}
	}

	/**
	 * Setup the edit text.
	 */
	private fun setupEditText(alarm: NacAlarm?)
	{
		// Get the views
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val inputLayout: TextInputLayout = dialog!!.findViewById(R.id.nfc_tag_input_layout)
		editText = dialog!!.findViewById(R.id.nfc_tag_name)

		// Setup the input layout
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Text change listener
		editText.addTextChangedListener{

			// Ok button should only be clickable when there is text in the editable
			okButton.isEnabled = (it?.isNotEmpty() == true)
			okButton.alpha = calcAlpha(okButton.isEnabled)

		}

		// Keyboard IME action listener
		editText.setOnEditorActionListener { _, id, _ ->

			// Act as if the Ok button was clicked when the Go button in the keyboard is
			// pressed
			when (id)
			{
				EditorInfo.IME_ACTION_GO -> { onPrimaryButtonClicked(alarm) }
				else -> {}
			}

			return@setOnEditorActionListener false

		}
	}

	/**
	 * Setup OK button.
	 */
	override fun setupOkButton(alarm: NacAlarm?)
	{
		// Super
		super.setupOkButton(alarm)

		// Get whether the NFC tag already exists or not
		val doesNfcTagAlreadyExist = requireArguments().getBoolean(SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME)

		// NFC tag does not already exist so need to save it and give it a name
		if (!doesNfcTagAlreadyExist)
		{
			// Get the ok button
			val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)

			// Rename the button and set its usability
			okButton.setText(R.string.action_save)
			okButton.isEnabled = false
			okButton.alpha = calcAlpha(false)
		}
	}

}