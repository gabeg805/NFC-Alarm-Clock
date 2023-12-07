package com.nfcalarmclock.restrictvolume

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Enable or disable the option to restrict the volume of an alarm that goes
 * off.
 */
class NacRestrictVolumeDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when an audio source is selected.
	 */
	fun interface OnRestrictVolumeListener
	{
		fun onRestrictVolume(shouldRestrict: Boolean)
	}

	/**
	 * Default volume restriction.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultShouldRestrictVolume = false

	/**
	 * Check box to restrict/unrestrict the volume.
	 */
	private lateinit var checkBox: MaterialCheckBox

	/**
	 * Listener for when the volume is restricted/unrestricted.
	 */
	var onRestrictVolumeListener: OnRestrictVolumeListener? = null

	/**
	 * Whether volume should be restricted or not.
	 */
	private val shouldRestrictVolume: Boolean
		get() = checkBox.isChecked

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Get the checked status
				val isChecked = checkBox.isChecked

				// Call the listener
				onRestrictVolumeListener?.onRestrictVolume(isChecked)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_alarm_restrict_volume)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the dialog container and text view
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_restrict_volume)
		val textView: TextView = dialog!!.findViewById(R.id.should_restrict_volume_summary)

		// Set the member variable
		checkBox = dialog!!.findViewById(R.id.should_restrict_volume_checkbox)

		// Set the default checked status
		checkBox.isChecked = defaultShouldRestrictVolume

		// Setup the views
		setupOnClickListener(container, textView)
		setupCheckBoxColor()
		setupTextView(textView)
	}

	/**
	 * Setup the color of the restrict volume check box.
	 */
	private fun setupCheckBoxColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		checkBox.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the on click listener for the restrict volume.
	 */
	private fun setupOnClickListener(container: RelativeLayout, textView: TextView)
	{
		// Set the listener
		container.setOnClickListener {

			// Toggle the checkbox
			checkBox.isChecked = !shouldRestrictVolume

			// Setup the summary
			setupTextView(textView)

		}
	}

	/**
	 * Setup the text view for whether volume should be restricted or not.
	 */
	private fun setupTextView(textView: TextView)
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (shouldRestrictVolume)
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