package com.nfcalarmclock.dismissearly

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

class NacDismissEarlyDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when an audio source is selected.
	 */
	fun interface OnDismissEarlyOptionSelectedListener
	{
		fun onDismissEarlyOptionSelected(useDismissEarly: Boolean, index: Int)
	}

	/**
	 * Default dismiss early.
	 */
	var defaultShouldDismissEarly = false

	/**
	 * Default dismiss early index.
	 */
	var defaultShouldDismissEarlyIndex = 0

	/**
	 * Whether an alarm should be able to be dismissed early or not.
	 */
	private val shouldDismissEarly: Boolean
		get() = dismissEarlyCheckBox!!.isChecked

	/**
	 * Check box to dismiss early or not.
	 */
	private var dismissEarlyCheckBox: MaterialCheckBox? = null

	/**
	 * Summary text for whether dismiss early should be used or not.
	 */
	private var dismissEarlySummary: TextView? = null

	/**
	 * Scrollable picker to choose the dismiss early time.
	 */
	private var dismissEarlyTimePicker: NumberPicker? = null

	/**
	 * Listener for when the dismiss early option is clicked.
	 */
	var onDismissEarlyOptionSelectedListener: OnDismissEarlyOptionSelectedListener? = null

	/**
	 * Call the OnDismissEarlyListener object, if it has been set.
	 */
	private fun callOnDismissEarlyOptionSelectedListener()
	{
		// Get the index value
		val index = dismissEarlyTimePicker!!.value

		// Call the listener
		onDismissEarlyOptionSelectedListener?.onDismissEarlyOptionSelected(
			shouldDismissEarly, index)
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Get the name of the title
		val title = getString(R.string.title_dismiss_early)

		// Get the name of the actions
		val ok = getString(R.string.action_ok)
		val cancel = getString(R.string.action_cancel)

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(title)
			.setPositiveButton(ok) { _, _ ->

				// Call the listener
				callOnDismissEarlyOptionSelectedListener()

			}
			.setNegativeButton(cancel) { _, _ ->
			}
			.setView(R.layout.dlg_alarm_dismiss_early)
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
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_use_dismiss_early)

		// Set the views
		dismissEarlyCheckBox = dialog!!.findViewById(R.id.should_use_dismiss_early_checkbox)
		dismissEarlySummary = dialog!!.findViewById(R.id.should_use_dismiss_early_summary)
		dismissEarlyTimePicker = dialog!!.findViewById(R.id.dismiss_early_time_picker)

		// Setup the widgets
		container.setOnClickListener {

			// Toggle the checkbox
			toggleShouldDismissEarly()

			// Setup the summary
			setupDismissEarlySummary()

			// Not sure
			setupDismissEarlyTimeEnabled()

		}

		// Setup
		setupShouldDismissEarly()
		setupDismissEarlyTimePicker()
		setupDismissEarlyTimeEnabled()
		setupDismissEarlyColor()
	}

	/**
	 * Setup the color of the dismiss early check box.
	 */
	private fun setupDismissEarlyColor()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		dismissEarlyCheckBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the summary text for whether a user should be able to dismiss an
	 * alarm early or not.
	 */
	private fun setupDismissEarlySummary()
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (shouldDismissEarly)
		{
			R.string.dismiss_early_true
		}
		else
		{
			R.string.dismiss_early_false
		}

		// Set the text
		dismissEarlySummary!!.setText(textId)
	}

	/**
	 * Setup whether the dismiss early time container can be used or not.
	 */
	private fun setupDismissEarlyTimeEnabled()
	{
		// Set the alpha
		dismissEarlyTimePicker!!.alpha = if (shouldDismissEarly) 1.0f else 0.25f

		// Set whether it can be used or not
		dismissEarlyTimePicker!!.isEnabled = shouldDismissEarly
	}

	/**
	 * Setup scrollable picker for the dismiss early time.
	 */
	private fun setupDismissEarlyTimePicker()
	{
		// Get the dismiss early times
		val values = sharedConstants.dismissEarlyTimes

		// Setup the time picker
		dismissEarlyTimePicker!!.minValue = 0
		dismissEarlyTimePicker!!.maxValue = values.size - 1
		dismissEarlyTimePicker!!.displayedValues = values.toTypedArray()
		dismissEarlyTimePicker!!.value = defaultShouldDismissEarlyIndex
	}

	/**
	 * Setup the check box and summary text for whether a user should be able
	 * to dismiss an alarm early or not.
	 */
	private fun setupShouldDismissEarly()
	{
		// Set the status of the checkbox
		dismissEarlyCheckBox!!.isChecked = defaultShouldDismissEarly

		// Setup the summary
		setupDismissEarlySummary()
	}

	/**
	 * Toggle whether an alarm should be able to be dismissed early or not.
	 */
	private fun toggleShouldDismissEarly()
	{
		dismissEarlyCheckBox!!.isChecked = !shouldDismissEarly
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacDismissEarlyDialog"

	}

}