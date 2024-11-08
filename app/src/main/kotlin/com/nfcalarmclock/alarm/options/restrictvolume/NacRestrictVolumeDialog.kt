package com.nfcalarmclock.alarm.options.restrictvolume

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setupCheckBoxColor

/**
 * Enable or disable the option to restrict the volume of an alarm that goes
 * off.
 */
class NacRestrictVolumeDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Checkbox indicating whether to restrict volume or not.
	 */
	private lateinit var checkBox: MaterialCheckBox

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_restrict_volume, container, false)
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the bundle
		val alarm = NacBundle.getAlarm(arguments)

		// Get the views
		val okButton = dialog!!.findViewById(R.id.ok_button) as MaterialButton
		val cancelButton = dialog!!.findViewById(R.id.cancel_button) as MaterialButton

		// Get the default value
		val defaultRestrictVolume= alarm?.shouldRestrictVolume ?: false

		// Setup the views
		setupShouldRestrictVolume(defaultRestrictVolume)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attribute
			alarm?.shouldRestrictVolume = checkBox.isChecked

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Setup whether to restrict volume or not.
	 */
	private fun setupShouldRestrictVolume(default: Boolean)
	{
		// Get the views
		val relativeLayout = dialog!!.findViewById(R.id.should_restrict_volume) as RelativeLayout
		val description = dialog!!.findViewById(R.id.should_restrict_volume_summary) as TextView
		checkBox = dialog!!.findViewById(R.id.should_restrict_volume_checkbox) as MaterialCheckBox

		// Set the default value
		checkBox.isChecked = default

		// Setup the views
		setupDescription(description)
		checkBox.setupCheckBoxColor(sharedPreferences)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			checkBox.isChecked = !checkBox.isChecked

			// Setup the description
			setupDescription(description)

		}
	}

	/**
	 * Setup the text view for whether volume should be restricted or not.
	 */
	private fun setupDescription(textView: TextView)
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (checkBox.isChecked)
		{
			R.string.restrict_volume_true
		}
		else
		{
			R.string.restrict_volume_false
		}

		// Set the text
		textView.setText(textId)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacRestrictVolumeDialog"

	}

}