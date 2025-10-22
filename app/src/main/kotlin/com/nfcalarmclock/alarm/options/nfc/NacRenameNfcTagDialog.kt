package com.nfcalarmclock.alarm.options.nfc

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
import com.nfcalarmclock.util.NacUtility
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setupInputLayoutColor

/**
 * Dialog to rename an NFC tag.
 */
class NacRenameNfcTagDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Listener for when a user wants to rename an NFC tag.
	 */
	fun interface OnRenameNfcTagListener
	{
		fun onRename(name: String)
	}

	/**
	 * List of NFC tags.
	 */
	var allNfcTags: List<NacNfcTag> = ArrayList()

	/**
	 * Listener for when the NFC tag is saved.
	 */
	var onRenameNfcTagListener: OnRenameNfcTagListener? = null

	/**
	 * Called when the dialog view is created.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?)
		: View?
	{
		return inflater.inflate(R.layout.dlg_rename_nfc_tag, container, false)
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
		val doneButton: MaterialButton = view.findViewById(R.id.done_nfc_tag)
		val cancelButton: MaterialButton = view.findViewById(R.id.cancel_nfc_tag)

		// Setup the input layout
		inputLayout.setupInputLayoutColor(context, sharedPreferences)

		// Listener for when the text in the EditText changes
		editText.addTextChangedListener{

			// Make sure the editable is valid and has text
			if (it?.isNotEmpty() == true)
			{
				doneButton.isEnabled = true
				doneButton.alpha = 1.0f
			}
			// Editable is null or does not have a name entered
			else
			{
				doneButton.isEnabled = false
				doneButton.alpha = 0.4f
			}
		}

		// Setup the done button
		setupPrimaryButton(doneButton, listener = {

			// Get the name
			val name = editText.text.toString().trim()

			// Check if the NFC tag name already exists
			if (allNfcTags.any { it.name == name })
			{
				// Show an error toast
				NacUtility.quickToast(requireContext(), R.string.error_message_nfc_name_exists)
				return@setupPrimaryButton
			}

			// Call the listener
			onRenameNfcTagListener?.onRename(name)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacRenameNfcTagDialog"

	}

}