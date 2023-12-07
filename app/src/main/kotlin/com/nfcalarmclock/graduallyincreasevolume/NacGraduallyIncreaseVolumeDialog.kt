package com.nfcalarmclock.graduallyincreasevolume

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
		fun onGraduallyIncreaseVolume(shouldIncrease: Boolean, index: Int, waitTime: Int)
	}

	/**
	 * Default should increase volume.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultShouldGraduallyIncreaseVolume = false

	/**
	 * Default should increase volume wait time index.
	 */
	var defaultShouldGraduallyIncreaseVolumeIndex = 0

	/**
	 * Check box for whether the volume should be gradually increased or not.
	 */
	private lateinit var checkBox: MaterialCheckBox

	/**
	 * Title above the picker to choose the gradually increase volume wait time.
	 */
	private lateinit var pickerTitle: TextView

	/**
	 * Scrollable picker to choose the gradually increase volume wait time.
	 */
	private lateinit var picker: NumberPicker

	/**
	 * Listener for when the volume is gradually increased or not.
	 */
	var onGraduallyIncreaseVolumeListener: OnGraduallyIncreaseVolumeListener? = null

	/**
	 * Whether volume should be gradually increased or not.
	 */
	private val shouldGraduallyIncreaseVolume: Boolean
		get() = checkBox.isChecked

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (shouldGraduallyIncreaseVolume) 1.0f else 0.25f

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

				// Get the index value
				val index = picker.value

				// Calculate the time based on the index
				val waitTime = if (index < 10)
				{
					index + 1
				}
				else
				{
					(index - 7) * 5
				}

				// Call the listener
				onGraduallyIncreaseVolumeListener?.onGraduallyIncreaseVolume(
					shouldGraduallyIncreaseVolume, index, waitTime)

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
		pickerTitle = dialog!!.findViewById(R.id.title_gradually_increase_volume_wait_time)
		picker = dialog!!.findViewById(R.id.gradually_increase_volume_wait_time_picker)

		// Set the status of the checkbox
		checkBox.isChecked = defaultShouldGraduallyIncreaseVolume

		// Setup the views
		setupOnClickListener(container, textView)
		setupCheckBoxColor()
		setupTextView(textView)
		setupTimePickerValues()
		setupTimePickerUsable()
	}

	/**
	 * Set the default gradually increase volume index from a wait time.
	 */
	fun setDefaultIndexFromWaitTime(waitTime: Int)
	{
		defaultShouldGraduallyIncreaseVolumeIndex = if (waitTime <= 10)
		{
			waitTime - 1
		}
		else
		{
			waitTime / 5 + 7
		}
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
		checkBox.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the on click listener of the container.
	 */
	private fun setupOnClickListener(container: RelativeLayout, textView: TextView)
	{
		// Set the listener
		container.setOnClickListener {

			// Toggle the checkbox
			checkBox.isChecked = !shouldGraduallyIncreaseVolume

			// Setup the views
			setupTextView(textView)
			setupTimePickerUsable()

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

	/**
	 * Setup whether the gradually increase volume wait time container can be
	 * used or not.
	 */
	private fun setupTimePickerUsable()
	{
		// Set the alpha for the views
		pickerTitle.alpha = alpha
		picker.alpha = alpha

		// Set whether it can be used or not
		picker.isEnabled = shouldGraduallyIncreaseVolume
	}

	/**
	 * Setup scrollable picker for the gradually increase volume wait time.
	 */
	private fun setupTimePickerValues()
	{
		// Get the wait times
		val values = requireContext().resources.getStringArray(R.array.gradually_increase_volume_wait_times).toList()

		// Setup the time picker
		picker.minValue = 0
		picker.maxValue = values.size - 1
		picker.displayedValues = values.toTypedArray()
		picker.value = defaultShouldGraduallyIncreaseVolumeIndex
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacGraduallyIncreaseVolumeDialog"

	}

}