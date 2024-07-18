package com.nfcalarmclock.snoozeoptions

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.view.dialog.NacDialogFragment
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupDialogScrollViewHeight

class NacSnoozeOptionsDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when the snooze options are changed.
	 */
	fun interface OnSnoozeOptionsChangedListener
	{
		fun onSnoozeOptionsChanged(maxSnooze: Int, snoozeDuration: Int, easySnooze: Boolean)
	}

	/**
	 * Default max snooze count.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultMaxSnooze = 0

	/**
	 * Default snooze duration.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultSnoozeDuration = 0

	/**
	 * Default easy snooze.
	 */
	var defaultEasySnooze = false

	/**
	 * Scrollable picker to choose the max snooze count.
	 */
	private lateinit var maxSnoozePicker: NumberPicker

	/**
	 * Scrollable picker to choose the snooze duration.
	 */
	private lateinit var snoozeDurationPicker: NumberPicker

	/**
	 * Check box for whether snoozing should be easy or not.
	 */
	private lateinit var checkBox: MaterialCheckBox

	/**
	 * Description next to the checkbox indicating what type of snoozing will be used.
	 */
	private lateinit var checkBoxDescription: TextView

	/**
	 * Listener for when the volume is gradually increased or not.
	 */
	var onSnoozeOptionsChangedListener: OnSnoozeOptionsChangedListener? = null

	/**
	 * Whether volume should be gradually increased or not.
	 */
	private val shouldEasySnooze: Boolean
		get() = checkBox.isChecked

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Creat ethe dialog
		return AlertDialog.Builder(requireContext())
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Get the values
				val maxSnooze = NacAlarm.calcMaxSnooze(maxSnoozePicker.value)
				val snoozeDuration = NacAlarm.calcSnoozeDuration(snoozeDurationPicker.value)

				// Call the listener
				onSnoozeOptionsChangedListener?.onSnoozeOptionsChanged(
					maxSnooze, snoozeDuration, shouldEasySnooze)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_alarm_snooze_options)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the views
		val scrollView = dialog!!.findViewById<ScrollView>(R.id.snooze_options_scrollview)
		val easySnoozeContainer = dialog!!.findViewById<RelativeLayout>(R.id.should_easy_snooze)

		maxSnoozePicker = dialog!!.findViewById(R.id.max_snooze_picker)
		snoozeDurationPicker = dialog!!.findViewById(R.id.snooze_duration_picker)
		checkBox = dialog!!.findViewById(R.id.should_easy_snooze_checkbox)
		checkBoxDescription = dialog!!.findViewById(R.id.should_easy_snooze_summary)

		// Set the default value
		checkBox.isChecked = defaultEasySnooze

		// Scroll view height
		setupDialogScrollViewHeight(scrollView, resources)

		// Checkbox
		setupCheckBoxColor(checkBox, sharedPreferences!!)

		// Easy snooze description
		setupEasySnoozeDescription()

		// Easy snooze listener
		easySnoozeContainer.setOnClickListener {

			// Toggle the checkbox
			checkBox.isChecked = !shouldEasySnooze

			// Setup the views
			setupEasySnoozeDescription()

		}

		// Get the scrollable values
		val maxSnoozeValues = requireContext().resources.getStringArray(R.array.max_snooze_summaries).toList()
		val snoozeDurationValues = requireContext().resources.getStringArray(R.array.snooze_duration_summaries).toList()

		// Setup the max snooze picker
		maxSnoozePicker.minValue = 0
		maxSnoozePicker.maxValue = maxSnoozeValues.size - 1
		maxSnoozePicker.displayedValues = maxSnoozeValues.toTypedArray()
		maxSnoozePicker.value = NacAlarm.calcMaxSnoozeIndex(defaultMaxSnooze)

		// Setup the snooze duration picker
		snoozeDurationPicker.minValue = 0
		snoozeDurationPicker.maxValue = snoozeDurationValues.size - 1
		snoozeDurationPicker.displayedValues = snoozeDurationValues.toTypedArray()
		snoozeDurationPicker.value = NacAlarm.calcSnoozeDurationIndex(defaultSnoozeDuration)
	}

	/**
	 * Setup the description for what type of snoozing should take place.
	 */
	private fun setupEasySnoozeDescription()
	{
		// Determine the text ID to use based on whether easy snooze will be
		// used or not
		val textId = if (shouldEasySnooze)
		{
			R.string.easy_snooze_true
		}
		else
		{
			R.string.easy_snooze_false
		}

		// Set the text
		checkBoxDescription.setText(textId)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSnoozeOptionsDialog"

		/**
		 * Show the dialog.
		 */
		fun show(
			manager: FragmentManager,
			maxSnooze: Int,
			snoozeDuration: Int,
			easySnooze: Boolean,
			listener: (Int, Int, Boolean) -> Unit = { _, _, _ -> })
		{
			// Create the dialog
			val dialog = NacSnoozeOptionsDialog()

			// Set the default values
			dialog.defaultMaxSnooze = maxSnooze
			dialog.defaultSnoozeDuration = snoozeDuration
			dialog.defaultEasySnooze = easySnooze

			// Setup the listener
			dialog.onSnoozeOptionsChangedListener = OnSnoozeOptionsChangedListener { maxSnooze, snoozeDuration, easySnooze ->
				listener(maxSnooze, snoozeDuration, easySnooze)
			}

			// Show the dialog
			dialog.show(manager, TAG)
		}

	}

}