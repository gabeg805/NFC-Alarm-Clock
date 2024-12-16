package com.nfcalarmclock.alarm.options.nfc

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.widget.addTextChangedListener
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setupInputLayoutColor

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
	 * List of NFC tags.
	 */
	var allNfcTags: List<NacNfcTag> = ArrayList()

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

		// Get the context
		val context = requireContext()

		// Get the views
		val inputLayout: TextInputLayout = view.findViewById(R.id.nfc_tag_input_layout)
		val editText: TextInputEditText = view.findViewById(R.id.nfc_tag_name)
		val saveButton: MaterialButton = view.findViewById(R.id.save_nfc_tag)
		val skipButton: MaterialButton = view.findViewById(R.id.skip_nfc_tag)

		// Setup the input layout
		inputLayout.setupInputLayoutColor(context, sharedPreferences)

		// Setup the edit view
		editText.addTextChangedListener{

			// Make sure the editable is valid and has text
			if (it?.isNotEmpty() == true)
			{
				saveButton.isEnabled = true
				saveButton.alpha = 1.0f
			}
			// Editable is null or does not have a name entered
			else
			{
				saveButton.isEnabled = false
				saveButton.alpha = 0.4f
			}
		}

		// Setup the save button
		setupPrimaryButton(saveButton, listener = {

			// Get the name
			val nfcName = editText.text.toString().trim()

			// Iterate over each NFC tag
			allNfcTags.forEach {

				// Check that the name does not already exist
				if (it.name == nfcName)
				{
					// Show toast
					NacUtility.quickToast(requireContext(), R.string.error_message_nfc_name_exists)
					return@setupPrimaryButton
				}
				// Check that the NFC ID does not already exist
				else if (it.nfcId == nfcId)
				{
					// Get the message
					val msg = getString(R.string.error_message_nfc_id_exists, nfcName)

					// Show toast
					NacUtility.quickToast(requireContext(), msg)
					return@setupPrimaryButton
				}

			}

			// Create an NFC tag
			val nfcTag = NacNfcTag(nfcName, nfcId)

			// Call the listener
			onSaveNfcTagListener?.onSave(nfcTag)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the skip button
		setupSecondaryButton(skipButton, listener = {

			// Call the listener
			onSaveNfcTagListener?.onCancel()

			// Dismiss the dialog
			dismiss()

		})

	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSaveNfcTagDialog"

	}

}