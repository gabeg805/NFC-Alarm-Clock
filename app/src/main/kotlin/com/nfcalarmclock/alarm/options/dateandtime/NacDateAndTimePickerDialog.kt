package com.nfcalarmclock.alarm.options.dateandtime

import android.os.Bundle
import android.text.format.DateFormat
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
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.getAlarm
import com.nfcalarmclock.util.toBundle
import java.util.Calendar

/**
 * Dialog that allows picking the date and time.
 */
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
	 * Listener for when the time is selected.
	 */
	fun interface OnTimeSelectedListener
	{
		fun onTimeSelected(timePicker: TimePicker, hour: Int, minute: Int)
	}

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Date picker.
	 */
	private lateinit var datePicker: DatePicker

	/**
	 * Shared preferences.
	 */
	private lateinit var timePicker: TimePicker

	/**
	 * Date selected listener.
	 */
	var onDateSelectedListener: OnDateSelectedListener? = null

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

		// Get the bundle
		val alarm = arguments?.getAlarm()!!

		// Get the shared preferences
		sharedPreferences = NacSharedPreferences(requireContext())

		// Get the views
		timePicker = dialog!!.findViewById(R.id.time_picker)
		datePicker = dialog!!.findViewById(R.id.date_picker)
		val dateButton: MaterialButton = dialog!!.findViewById(R.id.set_date)
		val timeButton: MaterialButton = dialog!!.findViewById(R.id.set_time)
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Setup the date and time pickers
		setupDatePicker(alarm)
		setupTimePicker(alarm)

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

	/**
	 * Setup the date picker.
	 */
	private fun setupDatePicker(alarm: NacAlarm)
	{
		// Get the next time the alarm will go off
		val now = Calendar.getInstance()
		val alarmCal = NacCalendar.alarmToCalendar(alarm, skipDate = true)

		// Min date
		datePicker.minDate = if (alarmCal.before(now))
		{
			now.add(Calendar.DAY_OF_MONTH, 1)
			now.timeInMillis
		}
		else
		{
			System.currentTimeMillis() - 1000
		}

		// First day of week
		datePicker.firstDayOfWeek = if (sharedPreferences.startWeekOn == 1) Calendar.MONDAY else Calendar.SUNDAY
	}

	/**
	 * Setup the time picker.
	 */
	@Suppress("UsePropertyAccessSyntax")
	private fun setupTimePicker(alarm: NacAlarm)
	{
		// Get whether the time is 24 hour format or not
		val is24HourFormat = DateFormat.is24HourFormat(context)

		// Set the time attributes
		timePicker.hour = alarm.hour
		timePicker.minute = alarm.minute
		timePicker.setIs24HourView(is24HourFormat)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacDateAndTimePickerDialog"

		/**
		 * Create a dialog that can be shown easily.
		 */
		fun create(
			alarm: NacAlarm,
			onDateSelectedListener: (DatePicker, Int, Int, Int) -> Unit = { _, _, _, _ -> },
			onTimeSelectedListener: (TimePicker, Int, Int) -> Unit = { _, _, _ -> },
		): NacDateAndTimePickerDialog
		{

			// Create the dialog
			val dialog = NacDateAndTimePickerDialog()

			// Add the alarm to the dialog
			dialog.arguments = alarm.toBundle()

			// Set the date listener
			dialog.onDateSelectedListener = OnDateSelectedListener { datePicker, year, month, day ->
				onDateSelectedListener(datePicker, year, month, day)
			}

			// Set the time listener
			dialog.onTimeSelectedListener = OnTimeSelectedListener { timePicker, hr, min ->
				onTimeSelectedListener(timePicker, hr, min)
			}

			return dialog
		}

	}

}