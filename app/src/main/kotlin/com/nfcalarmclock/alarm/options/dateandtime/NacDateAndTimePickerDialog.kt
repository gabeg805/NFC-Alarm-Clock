package com.nfcalarmclock.alarm.options.dateandtime

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TimePicker
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

class NacDateAndTimePickerDialog
	: DialogFragment()
{

	/**
	 * Listener for when the date is selected.
	 */
	fun interface OnDateSelectedListener
	{
		fun onDateSelected(datePicker: DatePicker, year: Int, month: Int, dayOfMonth: Int)
	}

	/**
	 * Listener for when to setup the date picker.
	 */
	fun interface OnSetupDatePickerListener
	{
		fun onSetupDatePicker(datePicker: DatePicker)
	}

	/**
	 * Listener for when to setup the time picker.
	 */
	fun interface OnSetupTimePickerListener
	{
		fun onSetupTimePicker(timePicker: TimePicker)
	}

	/**
	 * Listener for when the time is selected.
	 */
	fun interface OnTimeSelectedListener
	{
		fun onTimeSelected(timePicker: TimePicker, hour: Int, minute: Int)
	}

	/**
	 * Date selected listener.
	 */
	var onDateSelectedListener: OnDateSelectedListener? = null

	/**
	 * Setup date picker listener.
	 */
	var onSetupDatePickerListener: OnSetupDatePickerListener? = null

	/**
	 * Setup time picker listener.
	 */
	var onSetupTimePickerListener: OnSetupTimePickerListener? = null

	/**
	 * Time selected listener.
	 */
	var onTimeSelectedListener: OnTimeSelectedListener? = null

	/**
	 * Called when the view should be created.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_date_and_time_picker, container, false)
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the width of the screen
		val screenWidth = resources.displayMetrics.widthPixels
		val height = ViewGroup.LayoutParams.WRAP_CONTENT
		val width = 0.9f * screenWidth

		// Set the dialog to 90% width and wrap the height
		dialog?.window?.setLayout(width.toInt(), height)
	}

	/**
	 * Called after the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the shared preferences
		val sharedPreferences = NacSharedPreferences(requireContext())

		// Get the views
		val timePicker: TimePicker = dialog!!.findViewById(R.id.time_picker)
		val datePicker: DatePicker = dialog!!.findViewById(R.id.date_picker)
		val dateButton: MaterialButton = dialog!!.findViewById(R.id.set_date)
		val timeButton: MaterialButton = dialog!!.findViewById(R.id.set_time)
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Call any listeners that may have been external set to setup the date and time
		// pickers
		onSetupDatePickerListener?.onSetupDatePicker(datePicker)
		onSetupTimePickerListener?.onSetupTimePicker(timePicker)

		// Setup the date button
		dateButton.setOnClickListener {

			// Show the date picker
			timePicker.visibility = View.GONE
			datePicker.visibility = View.VISIBLE

			// Show the time button
			timeButton.visibility = View.VISIBLE
			dateButton.visibility = View.GONE

			// Constrain the ok button to be beneath the date picker
			okButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
				topToBottom = R.id.set_time
			}

		}

		// Setup the time button
		timeButton.setOnClickListener {

			// Show the date picker
			timePicker.visibility = View.VISIBLE
			datePicker.visibility = View.GONE

			// Show the date button
			timeButton.visibility = View.GONE
			dateButton.visibility = View.VISIBLE

			// Constrain the ok button to be beneath the time picker
			okButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
				topToBottom = R.id.set_date
			}

		}

		// Setup the color of the ok and cancel button
		okButton.setTextColor(sharedPreferences.themeColor)
		cancelButton.setTextColor(sharedPreferences.themeColor)

		// Setup the ok button
		okButton.setOnClickListener {

			// Time picker is visible
			if (timePicker.isVisible)
			{
				onTimeSelectedListener?.onTimeSelected(timePicker, timePicker.hour,
					timePicker.minute)
			}
			// Date picker is visible
			else
			{
				onDateSelectedListener?.onDateSelected(datePicker, datePicker.year,
					datePicker.month, datePicker.dayOfMonth)
			}

			// Dismiss the dialog
			dismiss()
		}

		// Setup the cancel button
		cancelButton.setOnClickListener {
			dismiss()
		}

	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacDateAndTimePickerDialog"

	}

}