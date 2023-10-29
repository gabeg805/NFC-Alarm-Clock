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
	 * Whether volume should be restricted or not.
	 */
	private val shouldRestrictVolume: Boolean
		get() = restrictVolumeCheckBox!!.isChecked

	/**
	 * Check box to restrict/unrestrict the volume.
	 */
	private var restrictVolumeCheckBox: MaterialCheckBox? = null

	/**
	 * Summary text for whether volume should be restricted or not.
	 */
	private var restrictVolumeSummary: TextView? = null

	/**
	 * Listener for when the volume is restricted/unrestricted.
	 */
	var onRestrictVolumeListener: OnRestrictVolumeListener? = null

	/**
	 * Call the OnRestrictVolumeListener object, if it has been set.
	 */
	private fun callOnRestrictVolumeListener()
	{
		// Get the checked status
		val isChecked = restrictVolumeCheckBox?.isChecked

		 // Call the listener
		 onRestrictVolumeListener?.onRestrictVolume(isChecked!!)
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(sharedConstants.titleRestrictVolume)
			.setPositiveButton(sharedConstants.actionOk) { _, _ ->

				// Call the listener
				callOnRestrictVolumeListener()

			}
			.setNegativeButton(sharedConstants.actionCancel) { _, _ ->
			}
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

		// Get the dialog container
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_restrict_volume)

		// Set the checkbox and summary views
		restrictVolumeCheckBox = dialog!!.findViewById(R.id.should_restrict_volume_checkbox)
		restrictVolumeSummary = dialog!!.findViewById(R.id.should_restrict_volume_summary)

		// Set the listener
		container.setOnClickListener {

			// Toggle the checkbox
			toggleShouldRestrictVolume()

			// Setup the summary
			setupShouldRestrictVolumeSummary()

		}

		// Setup the views
		setupShouldRestrictVolume()
		setupShouldRestrictVolumeColor()
	}

	/**
	 * Setup the check box and summary text for whether volume should be
	 * restricted or not.
	 */
	private fun setupShouldRestrictVolume()
	{
		// Set the checked status
		restrictVolumeCheckBox!!.isChecked = defaultShouldRestrictVolume

		// Setup the summary
		setupShouldRestrictVolumeSummary()
	}

	/**
	 * Setup the color of the restrict volume check box.
	 */
	private fun setupShouldRestrictVolumeColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		restrictVolumeCheckBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the summary text for whether volume should be restricted or not.
	 */
	private fun setupShouldRestrictVolumeSummary()
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
		restrictVolumeSummary!!.setText(textId)
	}

	/**
	 * Toggle whether volume should be restricted or not.
	 */
	private fun toggleShouldRestrictVolume()
	{
		restrictVolumeCheckBox!!.isChecked = !shouldRestrictVolume
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacRestrictVolumeDialog"

	}

}