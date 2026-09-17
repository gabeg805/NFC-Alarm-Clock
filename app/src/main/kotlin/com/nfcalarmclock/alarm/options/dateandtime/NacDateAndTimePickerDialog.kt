package com.nfcalarmclock.alarm.options.dateandtime

import android.os.Bundle
import android.text.format.DateFormat
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.DatePicker
import android.widget.TextView
import android.widget.TimePicker
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.isVisible
import androidx.core.view.updateLayoutParams
import androidx.fragment.app.DialogFragment
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.getAlarm
import com.nfcalarmclock.system.toBundle
import java.util.Calendar

/**
 * Dialog that allows picking the date and time.
 */
class NacDateAndTimePickerDialog
	: DialogFragment()
{

	/**
	 * Listener for when the date/time title should be shown.
	 */
	interface OnShowTitleListener
	{
		fun onShowDateTitle(textView: TextView)
		fun onShowTimeTitle(textView: TextView)
	}

	/**
	 * Listener for when the date is cleared.
	 */
	fun interface OnDateClearedListener
	{
		fun onDateCleared(datePicker: DatePicker)
	}

	/**
	 * Listener for when the time is cleared.
	 */
	fun interface OnTimeClearedListener
	{
		fun onTimeCleared(timePicker: TimePicker)
	}

	/**
	 * Listener for when the date and time is selected.
	 */
	fun interface OnDateAndTimeSelectedListener
	{
		fun onDateAndTimeSelected(
			datePicker: DatePicker,
			timePicker: TimePicker,
			year: Int,
			month: Int,
			dayOfMonth: Int,
			hour: Int,
			minute: Int)
	}

	/**
	 * Listener for when the time is selected.
	 */
	fun interface OnTimeSelectedListener
	{
		fun onTimeSelected(
			timePicker: TimePicker,
			hour: Int,
			minute: Int)
	}

	/**
	 * Shared preferences.
	 */
	private lateinit var sharedPreferences: NacSharedPreferences

	/**
	 * Date/time title.
	 */
	private lateinit var dateTimeTitle: TextView

	/**
	 * Date picker.
	 */
	private lateinit var datePicker: DatePicker

	/**
	 * Shared preferences.
	 */
	private lateinit var timePicker: TimePicker

	/**
	 * Show date/time title listener.
	 */
	var onShowTitleListener: OnShowTitleListener? = null

	/**
	 * Date cleared listener.
	 */
	var onDateClearedListener: OnDateClearedListener? = null

	/**
	 * Time cleared listener.
	 */
	var onTimeClearedListener: OnTimeClearedListener? = null

	/**
	 * Date selected listener.
	 */
	var onDateAndTimeSelectedListener: OnDateAndTimeSelectedListener? = null

	/**
	 * Time selected listener.
	 */
	var onTimeSelectedListener: OnTimeSelectedListener? = null

	/**
	 * Whether to show the title or not.
	 */
	var shouldShowTitle: Boolean = false

	/**
	 * Whether to always show the clear button or not.
	 */
	var shouldAlwaysShowClearButton: Boolean = false

	/**
	 * View is created.
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
	 * Fragment is resumed.
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
	 * View has been created.
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
		dateTimeTitle = dialog!!.findViewById(R.id.date_time_title)
		timePicker = dialog!!.findViewById(R.id.time_picker)
		datePicker = dialog!!.findViewById(R.id.date_picker)
		val dateButton: MaterialButton = dialog!!.findViewById(R.id.set_date)
		val timeButton: MaterialButton = dialog!!.findViewById(R.id.set_time)
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)
		val clearButton: MaterialButton = dialog!!.findViewById(R.id.clear_button)

		// Setup the views
		dateTimeTitle.visibility = if (shouldShowTitle) View.VISIBLE else View.GONE
		setupDatePicker(alarm)
		setupTimePicker(alarm)

		// Initialize the clear button visibility
		clearButton.visibility = if (shouldAlwaysShowClearButton) View.VISIBLE else View.GONE

		// Call the listener on initial showing
		if (dateTimeTitle.isVisible)
		{
			onShowTitleListener?.onShowTimeTitle(dateTimeTitle)
		}

		// Setup the date button
		dateButton.setOnClickListener {

			// Call the listener
			if (dateTimeTitle.isVisible)
			{
				onShowTitleListener?.onShowDateTitle(dateTimeTitle)
			}

			// Show the date picker
			timePicker.visibility = View.GONE
			datePicker.visibility = View.VISIBLE

			// Show the time button
			timeButton.visibility = View.VISIBLE
			dateButton.visibility = View.GONE

			// Show the clear button
			clearButton.visibility = View.VISIBLE

			// Constrain the ok button to be beneath the date picker
			okButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
				topToBottom = R.id.set_time
			}

		}

		// Setup the time button
		timeButton.setOnClickListener {

			// Call the listener
			if (dateTimeTitle.isVisible)
			{
				onShowTitleListener?.onShowTimeTitle(dateTimeTitle)
			}

			// Show the time picker
			timePicker.visibility = View.VISIBLE
			datePicker.visibility = View.GONE

			// Show the date button
			timeButton.visibility = View.GONE
			dateButton.visibility = View.VISIBLE

			// Hide the clear button
			clearButton.visibility = if (shouldAlwaysShowClearButton) View.VISIBLE else View.GONE

			// Constrain the ok button to be beneath the time picker
			okButton.updateLayoutParams<ConstraintLayout.LayoutParams> {
				topToBottom = R.id.set_date
			}

		}

		// Setup the color of the ok and cancel button
		okButton.setTextColor(sharedPreferences.themeColor)
		cancelButton.setTextColor(sharedPreferences.themeColor)
		clearButton.setTextColor(sharedPreferences.themeColor)

		// Setup the ok button
		okButton.setOnClickListener {

			// Time picker is visible
			if (timePicker.isVisible)
			{
				// Call the listener
				onTimeSelectedListener?.onTimeSelected(
					timePicker,
					timePicker.hour,
					timePicker.minute)
			}
			// Date picker is visible
			else
			{
				// Call the listener
				onDateAndTimeSelectedListener?.onDateAndTimeSelected(
					datePicker,
					timePicker,
					datePicker.year,
					datePicker.month,
					datePicker.dayOfMonth,
					timePicker.hour,
					timePicker.minute)
			}

			// Dismiss the dialog
			dismiss()

		}

		// Setup the cancel button
		cancelButton.setOnClickListener {
			dismiss()
		}

		// Setup the clear button
		clearButton.setOnClickListener {

			// Call the listener
			if (shouldAlwaysShowClearButton)
			{
				// Date cleared
				if (datePicker.isVisible)
				{
					onDateClearedListener?.onDateCleared(datePicker)
				}
				// Time cleared
				else
				{
					onTimeClearedListener?.onTimeCleared(timePicker)
				}
			}
			// Date cleared by default
			else
			{
				onDateClearedListener?.onDateCleared(datePicker)
			}

			// Dismiss the dialog
			dismiss()

		}

	}

	/**
	 * Setup the date picker.
	 */
	private fun setupDatePicker(alarm: NacAlarm)
	{
		// Save off the alarm's date before clearing it to not affect the
		// alarmToCalendar() conversion
		val saveDate = alarm.date

		alarm.date = ""

		// Get the next time the alarm will go off
		val now = Calendar.getInstance()
		val alarmCal = NacCalendar.alarmToCalendar(alarm)

		// Restore the alarm's date
		alarm.date = saveDate

		// Starting date
		if (alarm.date.isNotEmpty())
		{
			// Get the year/month/day
			val (year, month, day) = alarm.date.split("-")

			// Update the starting date
			datePicker.updateDate(year.toInt(), month.toInt()-1, day.toInt())
		}

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
			onShowDateTitleListener: (TextView) -> Unit = {},
			onShowTimeTitleListener: (TextView) -> Unit = {},
			onDateClearedListener: (DatePicker) -> Unit = {},
			onTimeClearedListener: (TimePicker) -> Unit = {},
			onDateAndTimeSelectedListener: (DatePicker, TimePicker, Int, Int, Int, Int, Int) -> Unit = { _, _, _, _, _, _, _ -> },
			onTimeSelectedListener: (TimePicker, Int, Int) -> Unit = { _, _, _ -> },
		): NacDateAndTimePickerDialog
		{

			// Create the dialog
			val dialog = NacDateAndTimePickerDialog()

			// Add the alarm to the dialog
			dialog.arguments = alarm.toBundle()

			// Set the show title listener
			dialog.onShowTitleListener = object: OnShowTitleListener {
				override fun onShowDateTitle(textView: TextView)
				{
					onShowDateTitleListener(textView)
				}

				override fun onShowTimeTitle(textView: TextView)
				{
					onShowTimeTitleListener(textView)
				}
			}

			// Set the date cleared listener
			dialog.onDateClearedListener = OnDateClearedListener { datePicker ->
				onDateClearedListener(datePicker)
			}

			// Set the time cleared listener
			dialog.onTimeClearedListener = OnTimeClearedListener { timePicker ->
				onTimeClearedListener(timePicker)
			}

			// Set the date set listener
			dialog.onDateAndTimeSelectedListener = OnDateAndTimeSelectedListener { datePicker, timePicker, year, month, day, hour, min ->
				onDateAndTimeSelectedListener(datePicker, timePicker, year, month, day, hour, min)
			}

			// Set the time listener
			dialog.onTimeSelectedListener = OnTimeSelectedListener { timePicker, hour, min ->
				onTimeSelectedListener(timePicker, hour, min)
			}

			return dialog
		}

	}

}