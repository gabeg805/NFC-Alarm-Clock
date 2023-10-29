package com.nfcalarmclock.graduallyincreasevolume

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
 * Ask user if they would like to gradually increase the volume when an alarm
 * goes off.
 */
class NacGraduallyIncreaseVolumeDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when an audio source is selected.
	 */
	fun interface OnGraduallyIncreaseVolumeListener
	{
		fun onGraduallyIncreaseVolume(shouldIncrease: Boolean)
	}

	/**
	 * Default volume restriction.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultShouldGraduallyIncreaseVolume = false

	/**
	 * Whether volume should be gradually increased or not.
	 */
	private val shouldGraduallyIncreaseVolume: Boolean
		get() = graduallyIncreaseVolumeCheckBox!!.isChecked

	/**
	 * Check box for whether the volume should be gradually increased or not.
	 */
	private var graduallyIncreaseVolumeCheckBox: MaterialCheckBox? = null

	/**
	 * Summary text for whether volume should be gradually increase or not.
	 */
	private var graduallyIncreaseVolumeSummary: TextView? = null

	/**
	 * Listener for when the volume is gradually increased or not.
	 */
	var onGraduallyIncreaseVolumeListener: OnGraduallyIncreaseVolumeListener? = null

	/**
	 * Call the OnGraduallyIncreaseVolumeListener object, if it has been set.
	 */
	private fun callOnGraduallyIncreaseVolumeListener()
	{
		// Get the checked status
		val isChecked = graduallyIncreaseVolumeCheckBox?.isChecked

		// Call the listener
		onGraduallyIncreaseVolumeListener?.onGraduallyIncreaseVolume(isChecked!!)
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Get the name of the title
		val title = getString(R.string.title_gradually_increase_volume)

		// Get the name of the actions
		val ok = getString(R.string.action_ok)
		val cancel = getString(R.string.action_cancel)

		// Creat ethe dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(title)
			.setPositiveButton(ok) { _, _ ->

				// Call the listener
				callOnGraduallyIncreaseVolumeListener()

			}
			.setNegativeButton(cancel) { _, _ ->
			}
			.setView(R.layout.dlg_alarm_gradually_increase_volume)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the container of the dialog
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_gradually_increase_volume)

		// Set the checkbox and summary views
		graduallyIncreaseVolumeCheckBox = dialog!!.findViewById(R.id.should_gradually_increase_volume_checkbox)
		graduallyIncreaseVolumeSummary = dialog!!.findViewById(R.id.should_gradually_increase_volume_summary)

		// Set the listener
		container.setOnClickListener {

			// Toggle the checkbox
			toggleShouldGraduallyIncreaseVolume()

			// Setup the summary
			setupShouldGraduallyIncreaseVolumeSummary()

		}

		// Setup the views
		setupShouldGraduallyIncreaseVolume()
		setupShouldGraduallyIncreaseVolumeColor()
	}

	/**
	 * Setup the check box and summary text for whether volume should be
	 * gradually increased or not.
	 */
	private fun setupShouldGraduallyIncreaseVolume()
	{
		// Set the status of the checkbox
		graduallyIncreaseVolumeCheckBox!!.isChecked =
			defaultShouldGraduallyIncreaseVolume

		// Setup the summary
		setupShouldGraduallyIncreaseVolumeSummary()
	}

	/**
	 * Setup the color of the gradually increase volume check box.
	 */
	private fun setupShouldGraduallyIncreaseVolumeColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		graduallyIncreaseVolumeCheckBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the summary text for whether volume should be gradually increased or not.
	 */
	private fun setupShouldGraduallyIncreaseVolumeSummary()
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (shouldGraduallyIncreaseVolume)
		{
			R.string.gradually_increase_volume_true
		}
		else
		{
			R.string.gradually_increase_volume_false
		}

		// Set the text
		graduallyIncreaseVolumeSummary!!.setText(textId)
	}

	/**
	 * Toggle whether volume should be gradually increased or not.
	 */
	private fun toggleShouldGraduallyIncreaseVolume()
	{
		graduallyIncreaseVolumeCheckBox!!.isChecked =
			!shouldGraduallyIncreaseVolume
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacGraduallyIncreaseVolumeDialog"

	}

}