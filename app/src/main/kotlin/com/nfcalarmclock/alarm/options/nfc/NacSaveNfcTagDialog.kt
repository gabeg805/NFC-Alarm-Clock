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
import com.nfcalarmclock.view.setupThemeColor
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
	 * Radio group for the add/replace buttons.
	 */
	private val addOrReplaceRadioGroup: RadioGroup by lazy {
		requireView().findViewById(R.id.save_nfc_tag_add_or_replace_radio_group)
	}

	/**
	 * Add radio button.
	 */
	private val addRadioButton: RadioButton by lazy {
		requireView().findViewById(R.id.save_nfc_tag_add_radio_button)
	}

	/**
	 * Replace radio button.
	 */
	private val replaceRadioButton: RadioButton by lazy {
		requireView().findViewById(R.id.save_nfc_tag_replace_radio_button)
	}

	/**
	 * Edit text containing the name of the NFC tag to save.
	 */
	private val editText: TextInputEditText by lazy {
		requireView().findViewById(R.id.nfc_tag_name)
	}

	/**
	 * List of NFC tags.
	 */
	private var allNfcTags: List<NacNfcTag> = emptyList()

	/**
	 * Scanned NFC tag ID.
	 */
	private var nfcTagId: String = ""

	/**
	 * Whether the NFC tag exists already or not.
	 */
	private var doesNfcTagAlreadyExist: Boolean = false

	/**
	 * Add or replace the NFC tag.
	 */
	private fun addOrReplaceNfcTag(alarm: NacAlarm?)
	{
		// Radio buttons are not shown so do nothing
		if (!addOrReplaceRadioGroup.isVisible || (alarm == null))
		{
			alarm?.nfcTagId = nfcTagId
			println("NO ADD OR REPLACE, SO SIMPLY SET : ${alarm?.nfcTagId} | $nfcTagId")
			return
		}

		// Add
		if (addRadioButton.isChecked)
		{
			println("ADD")
			// Create a list of NFC tags
			val nfcTags = alarm.nfcTagIdList
				.toMutableList()
				.apply { add(nfcTagId) }
				.map { NacNfcTag("", it) }

			// Set the NFC tag IDs to the alarm/timer
			alarm.setNfcTagIds(nfcTags)
		}
		// Replace
		else if (replaceRadioButton.isChecked)
		{
			println("REPLACE")
			alarm.nfcTagId = nfcTagId
		}
	}

	/**
	 * Called when the cancel button is clicked.
	 */
	override fun onCancelClicked(alarm: NacAlarm?)
	{
		println("Cancel clicked!")
		// Add or replace the NFC tag
		addOrReplaceNfcTag(alarm)
		println("Alarm now has NFC ID : ${alarm?.nfcTagId} | $doesNfcTagAlreadyExist")

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
		println("OK CLICKED! $nfcName")

		// The entered name for an NFC tag already exists
		if (allNfcTags.any { it.name == nfcName })
		{
			quickToast(requireContext(), R.string.error_message_nfc_name_exists)
			throw IllegalStateException()
		}

		// NFC tag does not already exist so save the NFC tag
		if (!doesNfcTagAlreadyExist)
		{
			println("SAVING NFC TAG : $nfcName | $nfcTagId")
			lifecycleScope.launch {
				val tag = NacNfcTag(nfcName, nfcTagId)
				nfcTagViewModel.insert(tag)
			}
		}

		// Add or replace the NFC tag
		addOrReplaceNfcTag(alarm)
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

		// Set the NFC tag ID and whether the NFC tag exists already or not
		nfcTagId = requireArguments().getString(SCANNED_NFC_TAG_ID_BUNDLE_NAME) ?: ""
		doesNfcTagAlreadyExist = requireArguments().getBoolean(SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME)
		println("Found NFC tag? $nfcTagId | $doesNfcTagAlreadyExist")

		// Setup the views
		setupAddOrReplace(alarm)
		setupSaveNfcTag(alarm)
	}

	/**
	 * Setup the add or replace views.
	 */
	private fun setupAddOrReplace(alarm: NacAlarm?)
	{
		// Get the views
		val title: TextView = dialog!!.findViewById(R.id.save_nfc_tag_add_or_replace_title)
		val description: TextView = dialog!!.findViewById(R.id.save_nfc_tag_add_or_replace_description)
		val separator: Space = dialog!!.findViewById(R.id.save_nfc_tag_separator)

		// Get the visibility depending on if the alarm/timer has NFC tags set or not
		val visibility = if (alarm?.nfcTagId?.isNotEmpty() == true) View.VISIBLE else View.GONE

		// Set the visibility
		title.visibility = visibility
		description.visibility = visibility
		addOrReplaceRadioGroup.visibility = visibility

		// Separator only needs to change visibility when the add/replace views will be
		// visible and the save NFC tag views will NOT be visible. Otherwise, it should
		// not be shown
		if ((visibility == View.VISIBLE) && doesNfcTagAlreadyExist)
		{
			separator.visibility = View.VISIBLE
		}

		// Do nothing else if the radio group is not shown
		if (visibility == View.GONE)
		{
			return
		}

		// Setup the radio button colors
		addRadioButton.setupThemeColor(sharedPreferences)
		replaceRadioButton.setupThemeColor(sharedPreferences)
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
	 * Setup the save NFC tag views.
	 */
	private fun setupSaveNfcTag(alarm: NacAlarm?)
	{
		// Get the views
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val saveNfcTagTitle: TextView = dialog!!.findViewById(R.id.save_nfc_tag_title)
		val saveNfcTagDescription: TextView = dialog!!.findViewById(R.id.save_nfc_tag_description)
		val saveNfcTagInputLayout: TextInputLayout = dialog!!.findViewById(R.id.save_nfc_tag_input_layout)
		val usingNfcTagTitle: TextView = dialog!!.findViewById(R.id.save_nfc_tag_already_exists_title)
		val usingNfcTagDescription: TextView = dialog!!.findViewById(R.id.save_nfc_tag_already_exists_description)

		// Get the visibility
		val visibility = if (doesNfcTagAlreadyExist) View.GONE else View.VISIBLE
		val oppositeVisibility = if (visibility == View.VISIBLE) View.GONE else View.VISIBLE

		// Set the visibility
		saveNfcTagTitle.visibility = visibility
		saveNfcTagDescription.visibility = visibility
		saveNfcTagInputLayout.visibility = visibility
		usingNfcTagTitle.visibility = oppositeVisibility
		usingNfcTagDescription.visibility = oppositeVisibility

		// Views are not being shown so no need to set any listeners
		if (visibility == View.GONE)
		{
			// Set the text of the using NFC tag description
			lifecycleScope.launch {
				val nfcTag = nfcTagViewModel.findNfcTag(nfcTagId)!!
				println("Setting the text! ${nfcTag.name}")
				usingNfcTagDescription.text = nfcTag.name
			}

			return
		}

		// Setup the input layout
		saveNfcTagInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

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