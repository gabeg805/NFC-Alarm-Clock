package com.nfcalarmclock.alarm.options.repeat

import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.view.View
import android.widget.AdapterView
import android.widget.TextView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.dateandtime.NacDateAndTimePickerDialog
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.system.NacCalendar
import com.nfcalarmclock.system.NacCalendar.Day
import com.nfcalarmclock.system.excludeDateTimesToCalendars
import com.nfcalarmclock.system.toBestDateTimeString
import com.nfcalarmclock.system.toFullTime
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek.OnWeekChangedListener
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupRippleColor
import java.util.Calendar
import java.util.EnumSet
import java.util.Locale

/**
 * Repeat options.
 */
class NacRepeatOptionsDialog
	: NacGenericAlarmOptionsDialog()
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_repeat

	/**
	 * Days to run title.
	 */
	private lateinit var daysToRunTitle: TextView

	/**
	 * Days to run description.
	 */
	private lateinit var daysToRunDescription: TextView

	/**
	 * Exclude start range edit text.
	 */
	private lateinit var excludeStartEditText: TextInputEditText

	/**
	 * Exclude end range edit text.
	 */
	private lateinit var excludeEndEditText: TextInputEditText

	/**
	 * Exclude note if current alarm is in exclude range.
	 */
	private lateinit var excludeNoteTextView: TextView

	/**
	 * Exclude note alarm. This is the same as the input alarm, but without any exclude datetime
	 * attributes set.
	 */
	private lateinit var excludeNoteAlarm: NacAlarm

	/**
	 * Exclude start range alarm. This is the same as the input alarm, but the hour, minute, and
	 * date represent the exclude date and time, as a means to show a correct date/time dialog.
	 */
	private lateinit var excludeStartAlarm: NacAlarm

	/**
	 * Exclude end range alarm. This is the same as the input alarm, but the hour, minute, and
	 * date represent the exclude date and time, as a means to show a correct date/time dialog.
	 */
	private lateinit var excludeEndAlarm: NacAlarm

	/**
	 * Days to run, the actual days view.
	 */
	private lateinit var daysToRunDayOfWeek: NacDayOfWeek

	/**
	 * Selected repeat frequency value.
	 */
	private var selectedRepeatFrequencyValue: Int = 1

	/**
	 * Selected repeat frequency units.
	 */
	private var selectedRepeatFrequencyUnits: Int = 4

	/**
	 * Selected days to run before starting the frequency.
	 */
	private var selectedDaysToRunBeforeFrequency: EnumSet<Day> = Day.WEEK

	/**
	 * Build a human readable date/time string using the best format given a locale.
	 *
	 * The output of this should be used in a TextInputEditText.
	 */
	private fun buildDateTimeFromAlarm(alarm: NacAlarm): String
	{
		// Initialize the date/time with the date
		var dateTime = alarm.date

		// Set the time
		if (alarm.hour >= 0)
		{
			// Add space if date is present
			if (dateTime.isNotEmpty())
			{
				dateTime += " "
			}

			dateTime += "${alarm.hour}:${alarm.minute}"
		}

		return dateTime
	}

	/**
	 * Get the correct units list based on a given value and units.
	 *
	 * @return The correct units list based on a given value and units.
	 */
	private fun getCorrectUnitsList(value: Int): Array<String>
	{
		// Get the locale
		val locale = Locale.getDefault()

		// Build list of all units in the form: %d <unit>
		// The %d will need to be removed and the first character will have to be capitalized
		return listOf(
			resources.getQuantityString(R.plurals.unit_minute, value),
			resources.getQuantityString(R.plurals.unit_hour,   value),
			resources.getQuantityString(R.plurals.unit_day,    value),
			resources.getQuantityString(R.plurals.unit_week,   value),
			resources.getQuantityString(R.plurals.unit_month,  value),
		).map {
			it.replace("%d", "").trim()
				.replaceFirstChar { firstChar ->
					firstChar.titlecase(locale)
				}
		}.toTypedArray()
	}

	/**
	 * Get the repeat frequency values from the repeat frequency units index.
	 */
	fun getRepeatFrequencyValuesFromUnitsIndex(index: Int): Array<String>
	{
		return when (index)
		{
			// Minute (max 4 hours)
			0 -> (15..240)

			// Hour (max 1 week)
			1 -> (1..168)

			// Day
			2 -> (1..365)

			// Week
			3 -> (1..52)

			// Month
			4 -> (1..12)

			// Week
			else -> (1..52)
		}.map { it.toString() }.toTypedArray()
	}

	/**
	 * Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm)
	{
		// Get the exclude start and end text
		val context = requireContext()
		val excludeStartText = excludeStartEditText.text.toString()
		val excludeEndText = excludeEndEditText.text.toString()

		// Error: Exclude start datetime is missing
		if (excludeStartText.isEmpty() && excludeEndText.isNotEmpty())
		{
			quickToast(context, R.string.error_message_exclude_start_datetime_missing)
			throw IllegalStateException()
		}
		// Error: Exclude end datetime is missing
		else if (excludeStartText.isNotEmpty() && excludeEndText.isEmpty())
		{
			quickToast(context, R.string.error_message_exclude_end_datetime_missing)
			throw IllegalStateException()
		}

		// Build the start and end datetimes and calendar
		var startDateTime = buildDateTimeFromAlarm(excludeStartAlarm)
		var endDateTime = buildDateTimeFromAlarm(excludeEndAlarm)
		val startHasDate = startDateTime.contains(' ')
		val endHasDate = endDateTime.contains(' ')
		val (startCal, endCal) = excludeStartAlarm.copy()
			.apply {
				excludeStartDateTime = startDateTime
				excludeEndDateTime = endDateTime
			}
			.excludeDateTimesToCalendars()

		// Start and end both have dates
		if (startHasDate && endHasDate)
		{
			NacLog.i("Checking that start calendar occurs before end calendar. startCal=${startCal?.toFullTime()} | endCal=${endCal?.toFullTime()}")

			// Error: Start and end times are not in order
			if ((startCal != null) && (endCal != null) && (startCal >= endCal))
			{
				quickToast(context, R.string.error_message_exclude_start_end_calendar_not_in_order)
				throw IllegalStateException()
			}
		}
		// Start has date but end is a time. Convert end to date
		else if (startHasDate)
		{
			NacLog.i("Found start date but no end date")

			val year = endCal!![Calendar.YEAR]
			val month = endCal[Calendar.MONTH]
			val day = endCal[Calendar.DAY_OF_MONTH]
			endDateTime = "$year-${month+1}-$day $endDateTime"
		}
		// End has date but start is a time. Convert start to date
		else if (endHasDate)
		{
			NacLog.i("Found end date but no start date")

			val year = startCal!![Calendar.YEAR]
			val month = startCal[Calendar.MONTH]
			val day = startCal[Calendar.DAY_OF_MONTH]
			startDateTime = "$year-${month+1}-$day $startDateTime"
		}

		NacLog.i("Saving start datetime=$startDateTime")
		NacLog.i("Saving   end datetime=$endDateTime")

		// Update the alarm
		alarm.shouldRepeat = true
		alarm.shouldSkipNextAlarm = false
		alarm.repeatFrequency = selectedRepeatFrequencyValue
		alarm.repeatFrequencyUnits = selectedRepeatFrequencyUnits
		alarm.excludeStartDateTime = startDateTime
		alarm.excludeEndDateTime = endDateTime
		alarm.repeatFrequencyDaysToRunBeforeStarting = selectedDaysToRunBeforeFrequency

		// Weekly frequency unit
		if (selectedRepeatFrequencyUnits == 4)
		{
			// Days are empty
			if (alarm.days.isEmpty())
			{
				// Every 1 week or no days to run before starting were selected
				if ((selectedRepeatFrequencyValue == 1) || selectedDaysToRunBeforeFrequency.isEmpty())
				{
					// Set the days and days to run before starting to the entire week
					alarm.days = Day.WEEK
					alarm.repeatFrequencyDaysToRunBeforeStarting = alarm.days
				}
				// Every 2+ weeks and days to run before starting has at least 1 day selected
				else
				{
					alarm.days = selectedDaysToRunBeforeFrequency
				}
			}
		}
		// Every other frequency unit
		else
		{
			// Clear various alarm attributes
			alarm.repeatFrequencyDaysToRunBeforeStarting = Day.NONE
			alarm.setDays(0)
		}
	}

	/**
	 * Setup the views for the days to run before starting the frequency.
	 */
	private fun setDaysToRunUsability()
	{
		// Get the state and alpha
		val state = (selectedRepeatFrequencyUnits == 4) && (selectedRepeatFrequencyValue != 1)
		val alpha = calcAlpha(state)

		// Set the usability
		daysToRunTitle.alpha = alpha
		daysToRunDescription.alpha = alpha
		daysToRunDayOfWeek.dayOfWeekView.alpha = alpha
		daysToRunTitle.isEnabled = state
		daysToRunDescription.isEnabled = state
		daysToRunDayOfWeek.dayOfWeekView.isEnabled = state
		daysToRunDayOfWeek.dayButtons.forEach {
			it.isEnabled = state
			it.button?.isEnabled = state
		}
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm)
	{
		// Set the default selected values
		selectedRepeatFrequencyValue = alarm.repeatFrequency
		selectedRepeatFrequencyUnits = alarm.repeatFrequencyUnits
		selectedDaysToRunBeforeFrequency = alarm.repeatFrequencyDaysToRunBeforeStarting

		// Setup the views
		setupRepeatFrequency(alarm.repeatFrequency, alarm.repeatFrequencyUnits)
		setupExcludeAlarms(alarm)
		setupExcludeDateTimeRange()
		setupDaysToRun(selectedDaysToRunBeforeFrequency)
		setDaysToRunUsability()
	}

	/**
	 * Setup the views for the days to run before starting the frequency.
	 */
	private fun setupDaysToRun(defaultCurrentDays: EnumSet<Day>)
	{
		// Get the views
		daysToRunTitle = dialog!!.findViewById(R.id.repeat_freq_days_to_run_title)
		daysToRunDescription = dialog!!.findViewById(R.id.repeat_freq_days_to_run_description)
		daysToRunDayOfWeek = NacDayOfWeek(dialog!!.findViewById(R.id.repeat_freq_days_to_run))

		// Setup days
		daysToRunDayOfWeek.dayButtons.forEach { it.button?.setupRippleColor(sharedPreferences) }
		daysToRunDayOfWeek.setStartWeekOn(sharedPreferences.startWeekOn)
		daysToRunDayOfWeek.setDays(defaultCurrentDays)

		// Set the listener
		daysToRunDayOfWeek.onWeekChangedListener = OnWeekChangedListener { button, day ->

			// Check if day is contained in the set, if so remove it
			if (selectedDaysToRunBeforeFrequency.contains(day))
			{
				selectedDaysToRunBeforeFrequency.remove(day)
			}
			// Day is not present, add it
			else
			{
				selectedDaysToRunBeforeFrequency.add(day)
			}

			// Haptic feedback
			button.performHapticFeedback()

		}
	}

	/**
	 * Setup the exclude start and end alarms.
	 */
	private fun setupExcludeAlarms(alarm: NacAlarm)
	{
		NacLog.i("Original start datetime=${alarm.excludeStartDateTime}")
		NacLog.i("Original end datetime=${alarm.excludeEndDateTime}")

		// Set the exclude start and end alarms. They are copies so the original does not get modified
		excludeNoteAlarm = alarm.copy()
			.apply {
				excludeStartDateTime = ""
				excludeEndDateTime = ""
			}
		excludeStartAlarm = alarm.copy()
		excludeEndAlarm = alarm.copy()

		// Convert the datetime strings to Calendars
		val (startCal, endCal) = alarm.excludeDateTimesToCalendars()

		// Unable to convert the datetimes to calendars. Invalidate the hour and minute
		if ((startCal == null) || (endCal == null))
		{
			excludeStartAlarm.date = ""
			excludeStartAlarm.hour = -1
			excludeStartAlarm.minute = -1
			excludeEndAlarm.date = ""
			excludeEndAlarm.hour = -1
			excludeEndAlarm.minute = -1
		}
		// Set the hour, minute and date
		else
		{
			excludeStartAlarm.date = ""
			excludeStartAlarm.hour = startCal[Calendar.HOUR_OF_DAY]
			excludeStartAlarm.minute = startCal[Calendar.MINUTE]
			excludeEndAlarm.date = ""
			excludeEndAlarm.hour = endCal[Calendar.HOUR_OF_DAY]
			excludeEndAlarm.minute = endCal[Calendar.MINUTE]

			// Start date
			if (excludeStartAlarm.excludeStartDateTime.contains(' '))
			{
				excludeStartAlarm.date = excludeStartAlarm.excludeStartDateTime.split(' ')[0]
				NacLog.i("Start date : ${excludeStartAlarm.date}")
			}

			// End date
			if (excludeEndAlarm.excludeEndDateTime.contains(' '))
			{
				excludeEndAlarm.date = excludeEndAlarm.excludeEndDateTime.split(' ')[0]
				NacLog.i("End date : ${excludeEndAlarm.date}")
			}
		}

		NacLog.i("Start hour   : ${excludeStartAlarm.hour}")
		NacLog.i("Start minute : ${excludeStartAlarm.minute}")
		NacLog.i("End hour   : ${excludeEndAlarm.hour}")
		NacLog.i("End minute : ${excludeEndAlarm.minute}")

	}

	/**
	 * Setup the exclude start and end datetimes in the TextInputEditTexts.
	 */
	private fun setupExcludeDateTimeRange()
	{
		// Get the views
		val excludeStartInputLayout: TextInputLayout = dialog!!.findViewById(R.id.repeat_freq_exclude_start_range_input_layout)
		val excludeEndInputLayout: TextInputLayout = dialog!!.findViewById(R.id.repeat_freq_exclude_end_range_input_layout)
		excludeStartEditText = dialog!!.findViewById(R.id.repeat_freq_exclude_start_range_edit_text)
		excludeEndEditText = dialog!!.findViewById(R.id.repeat_freq_exclude_end_range_edit_text)
		excludeNoteTextView = dialog!!.findViewById(R.id.repeat_freq_exclude_note)

		// Setup the color
		val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)
		excludeStartInputLayout.setStartIconTintList(themeColor)
		excludeEndInputLayout.setStartIconTintList(themeColor)

		// Setup the initial text
		updateExcludeStartAndEndEditTexts()
		updateExcludeNoteVisibility()

		// Show the time dialog on click
		excludeStartEditText.setOnClickListener {
			// TODO: Warning message can be one of: current alarm is in exclude range
			showTimeDialog(
				excludeStartAlarm,
				dateTitle = R.string.title_select_start_date,
				timeTitle = R.string.title_select_start_time)
		}

		excludeEndEditText.setOnClickListener {
			showTimeDialog(
				excludeEndAlarm,
				dateTitle = R.string.title_select_end_date,
				timeTitle = R.string.title_select_end_time)
		}
	}

	/**
	 * Setup the repeat frequency views.
	 */
	private fun setupRepeatFrequency(defaultValue: Int, defaultUnits: Int)
	{
		// Get the context
		val context = requireContext()

		// Get the views
		val valueAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.repeat_freq_value_dropdown_menu)
		val unitsAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.repeat_freq_units_dropdown_menu)
		val valueInputLayout: TextInputLayout = dialog!!.findViewById(R.id.repeat_freq_value_input_layout)
		val unitsInputLayout: TextInputLayout = dialog!!.findViewById(R.id.repeat_freq_units_input_layout)

		// Get the indices to use
		var valueIndex = NacAlarm.calcRepeatFrequencyIndex(defaultValue, defaultUnits)
		var unitsIndex = NacAlarm.calcRepeatFrequencyUnitsIndex(defaultUnits)
		println("defaultValue=$defaultValue | defaultUnits=$defaultUnits")
		println("valueIndex=$valueIndex | unitsIndex=$unitsIndex")

		// Setup the dropdowns
		var valuesList = getRepeatFrequencyValuesFromUnitsIndex(unitsIndex)
		var unitsList = getCorrectUnitsList(defaultValue)

		// Setup the input layouts
		valueInputLayout.setupInputLayoutColor(context, sharedPreferences)
		unitsInputLayout.setupInputLayoutColor(context, sharedPreferences)

		// Setup the textviews
		valueAutoCompleteTextView.setSimpleItems(valuesList)
		unitsAutoCompleteTextView.setSimpleItems(unitsList)
		valueAutoCompleteTextView.setTextFromIndex(valueIndex, fallback = 0)
		unitsAutoCompleteTextView.setTextFromIndex(unitsIndex)

		// Setup the listeners
		valueAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the repeat frequency
			selectedRepeatFrequencyValue = valuesList[position].toInt()

			// Get the corrected units list and the current unit selected
			unitsList = getCorrectUnitsList(selectedRepeatFrequencyValue)
			val text = unitsAutoCompleteTextView.adapter.getItem(unitsIndex) as String

			// Selected unit does not match any in the list. Should change the units list because
			// it has changed from singular to plural or vice versa
			if (text !in unitsList)
			{
				unitsAutoCompleteTextView.setSimpleItems(unitsList)
				unitsAutoCompleteTextView.setTextFromIndex(unitsIndex)
			}

			// Set the usability of the days view
			setDaysToRunUsability()

		}

		unitsAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the repeat frequency units
			selectedRepeatFrequencyUnits = NacAlarm.calcRepeatFrequencyUnitsFromIndex(position)
			unitsIndex = position

			// Recompute a new list of values and the value index
			valuesList = getRepeatFrequencyValuesFromUnitsIndex(position)
			valueIndex = valuesList.indexOfFirst{ selectedRepeatFrequencyValue.toString() == it }

			// Value index was not able to be found. This may only happen when selecting minutes,
			// as the minimum value is 15
			if (valueIndex < 0)
			{
				// Reset the index to 0 and update the selected repeat frequency value
				valueIndex = 0
				selectedRepeatFrequencyValue = valuesList[0].toInt()

				// Update the units list
				unitsList = getCorrectUnitsList(selectedRepeatFrequencyValue)
				unitsAutoCompleteTextView.setSimpleItems(unitsList)
				unitsAutoCompleteTextView.setTextFromIndex(unitsIndex)
			}

			// Update the repeat frequency values
			valueAutoCompleteTextView.setSimpleItems(valuesList)
			valueAutoCompleteTextView.setTextFromIndex(valueIndex)

			// Set the usability of the days view
			setDaysToRunUsability()

		}
	}

	/**
	 * Show the time picker dialog.
	 */
	private fun showTimeDialog(alarm: NacAlarm, dateTitle: Int, timeTitle: Int)
	{
		// Show the dialog
		NacDateAndTimePickerDialog.create(
			alarm,
			onShowDateTitleListener = { it.setText(dateTitle) },
			onShowTimeTitleListener = { it.setText(timeTitle) },
			onDateClearedListener = {
				NacLog.i("Repeat options date cleared. Only using the time", offsetIndex = 1)

				// Clear date
				alarm.date = ""

				// Update the edit texts
				updateExcludeStartAndEndEditTexts()
				updateExcludeNoteVisibility()

				NacLog.i("startEditText=${excludeStartEditText.text}")
				NacLog.i("  endEditText=${excludeEndEditText.text}")
			},
			onTimeClearedListener = {
				NacLog.i("Repeat options time cleared. Clearing everything", offsetIndex = 1)

				// Clear everything
				alarm.date = ""
				alarm.hour = -1
				alarm.minute = -1

				// Update the edit texts
				updateExcludeStartAndEndEditTexts()
				updateExcludeNoteVisibility()

				NacLog.i("startEditText=${excludeStartEditText.text}")
				NacLog.i("  endEditText=${excludeEndEditText.text}")
			},
			onDateAndTimeSelectedListener = { _, _, year, month, day, hour, min ->

				// Set the date and time
				alarm.date = "$year-${month+1}-$day"
				alarm.hour = hour
				alarm.minute = min

				NacLog.i("Repeat options date and time selected: Date=${alarm.date} | Time=${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}", offsetIndex = 1)

				// Update the edit texts
				updateExcludeStartAndEndEditTexts()
				updateExcludeNoteVisibility()

				NacLog.i("startEditText=${excludeStartEditText.text}")
				NacLog.i("  endEditText=${excludeEndEditText.text}")
			},
			onTimeSelectedListener = { _, hour, min ->
				NacLog.i("Repeat options time selected=${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}", offsetIndex = 1)

				// Set the time
				alarm.hour = hour
				alarm.minute = min

				// Update the edit texts
				updateExcludeStartAndEndEditTexts()
				updateExcludeNoteVisibility()

				NacLog.i("startEditText=${excludeStartEditText.text}")
				NacLog.i("  endEditText=${excludeEndEditText.text}")
			})
			.apply {
				shouldShowTitle = true
				shouldAlwaysShowClearButton = true
			}
			.show(childFragmentManager, NacDateAndTimePickerDialog.TAG)
	}

	/**
	 * Update the exclude start and end TextInputEditTexts.
	 */
	private fun updateExcludeStartAndEndEditTexts()
	{
		// Whether exclude alarms have a start/end
		val hasStart = (excludeStartAlarm.hour >= 0)
		val hasEnd = (excludeEndAlarm.hour >= 0)

		// Start/end times
		var startTime = if (hasStart) excludeStartAlarm.toBestDateTimeString() else ""
		var endTime = if (hasEnd) excludeEndAlarm.toBestDateTimeString() else ""

		// Start/end calendars
		val startCal = if (hasStart) NacCalendar.alarmToCalendar(excludeStartAlarm) else null
		val endCal = if (hasEnd) NacCalendar.alarmToCalendar(excludeEndAlarm) else null
		val now = Calendar.getInstance()

		// Helper descriptor strings
		val everyday = getString(R.string.dow_everyday)
		val today = getString(R.string.dow_today)
		val tomorrow = getString(R.string.dow_tomorrow)

		// Everyday
		if (excludeStartAlarm.date.isEmpty() && excludeEndAlarm.date.isEmpty())
		{
			NacLog.i("Both start and end dates are empty. hasStart=$hasStart | hasEnd=$hasEnd")

			if (hasStart)
			{
				startTime = "$everyday $startTime"
			}

			if (hasEnd)
			{
				endTime = "$everyday $endTime"
			}
		}
		// Today start time
		else if (hasStart && excludeStartAlarm.date.isEmpty() && excludeEndAlarm.date.isNotEmpty())
		{
			NacLog.i("Start date is empty, but end date is not. after=${startCal!!.after(now)}")
			startTime = "$today $startTime"
		}
		// Today end time
		else if (hasEnd && excludeStartAlarm.date.isNotEmpty() && excludeEndAlarm.date.isEmpty())
		{
			NacLog.i("End date is empty, but start date is not. after=${endCal!!.after(now)}")
			endTime = "${if (endCal.after(now)) today else tomorrow} $endTime"
		}

		NacLog.i("Updating start edit text. text='$startTime'")
		NacLog.i("Updating end edit text.   text='$endTime'")

		excludeStartEditText.text = SpannableStringBuilder(startTime)
		excludeEndEditText.text = SpannableStringBuilder(endTime)
	}

	/**
	 * Update the visibility of the exclude note, in the event that the exclude range includes
	 * the current alarm.
	 */
	private fun updateExcludeNoteVisibility()
	{
		// No need to show the note when one or both of the exclude ranges is not set
		if (!excludeNoteAlarm.isEnabled
			|| excludeStartEditText.text.toString().isEmpty()
			|| excludeEndEditText.text.toString().isEmpty())
		{
			excludeNoteTextView.visibility = View.GONE
			return
		}

		// Get the next time the alarm will run
		val c = NacCalendar.getNextAlarmDay(excludeNoteAlarm)!!

		// Get the exclude range calendars
		val tmpAlarm = excludeStartAlarm.copy()
			.apply {
				excludeStartDateTime = buildDateTimeFromAlarm(excludeStartAlarm)
				excludeEndDateTime = buildDateTimeFromAlarm(excludeEndAlarm)
			}
		val (startCal, endCal) = tmpAlarm.excludeDateTimesToCalendars()

		// Alarm is within exclude range. Show the note
		excludeNoteTextView.visibility = if ((c >= startCal!!) && (c < endCal!!))
		{
			View.VISIBLE
		}
		// Do not show the note
		else
		{
			View.GONE
		}
	}

}