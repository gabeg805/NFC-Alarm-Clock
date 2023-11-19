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

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacGraduallyIncreaseVolumeDialog"

	}

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
		get() = checkBox!!.isChecked

	/**
	 * Check box for whether the volume should be gradually increased or not.
	 */
	private var checkBox: MaterialCheckBox? = null

	/**
	 * Listener for when the volume is gradually increased or not.
	 */
	var onGraduallyIncreaseVolumeListener: OnGraduallyIncreaseVolumeListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Creat ethe dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_gradually_increase_volume)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Get the checked status
				val isChecked = checkBox?.isChecked

				// Call the listener
				onGraduallyIncreaseVolumeListener?.onGraduallyIncreaseVolume(isChecked!!)

			}
			.setNegativeButton(R.string.action_cancel, null)
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

		// Get the container of the dialog and the text view
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_gradually_increase_volume)
		val textView: TextView = dialog!!.findViewById(R.id.should_gradually_increase_volume_summary)

		// Set the member variable
		checkBox = dialog!!.findViewById(R.id.should_gradually_increase_volume_checkbox)

		// Set the status of the checkbox
		checkBox!!.isChecked = defaultShouldGraduallyIncreaseVolume

		// Setup the views
		setupOnClickListener(container, textView)
		setupCheckBoxColor()
		setupTextView(textView)
	}

	/**
	 * Setup the color of the gradually increase volume check box.
	 */
	private fun setupCheckBoxColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		checkBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the on click listener of the container.
	 */
	private fun setupOnClickListener(container: RelativeLayout, textView: TextView)
	{
		// Set the listener
		container.setOnClickListener {

			// Toggle the checkbox
			checkBox!!.isChecked = !shouldGraduallyIncreaseVolume

			// Setup the summary
			setupTextView(textView)

		}
	}

	/**
	 * Setup the summary text for whether volume should be gradually increased or not.
	 */
	private fun setupTextView(textView: TextView)
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
		textView.setText(textId)
	}

}