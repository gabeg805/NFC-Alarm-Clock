package com.nfcalarmclock.alarm.options.nfc

import android.view.inputmethod.EditorInfo
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
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.view.calcAlpha
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

		// Iterate over each NFC tag
		allNfcTags.forEach {

			// Check that the name does not already exist
			if (it.name == nfcName)
			{
				// Show toast
				NacUtility.quickToast(requireContext(), R.string.error_message_nfc_name_exists)
				throw IllegalStateException()
			}
			// Check that the NFC ID does not already exist
			else if (it.nfcId == alarm!!.nfcTagId)
			{
				// Get the message
				val msg = getString(R.string.error_message_nfc_id_exists, it.name)

				// Show toast
				NacUtility.quickToast(requireContext(), msg)
				throw IllegalStateException()
			}

		}

		// Save the NFC tag
		lifecycleScope.launch {
			val tag = NacNfcTag(nfcName, alarm!!.nfcTagId)

			nfcTagViewModel.insert(tag)
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
		setupEditText(alarm)
	}

	/**
	 * Setup the Cancel button.
	 */
	override fun setupCancelButton(alarm: NacAlarm?)
	{
		// Super
		super.setupCancelButton(alarm)

		// Get the cancel button
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Rename the button
		cancelButton.setText(R.string.action_skip)
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

			// Make sure the editable is valid and has text
			if (it?.isNotEmpty() == true)
			{
				okButton.isEnabled = true
				okButton.alpha = 1.0f
			}
			// Editable is null or does not have a name entered
			else
			{
				okButton.isEnabled = false
				okButton.alpha = calcAlpha(false)
			}
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

		// Get the ok button
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)

		// Rename the button and set its usability
		okButton.setText(R.string.action_save)
		okButton.isEnabled = false
		okButton.alpha = calcAlpha(false)
	}

}