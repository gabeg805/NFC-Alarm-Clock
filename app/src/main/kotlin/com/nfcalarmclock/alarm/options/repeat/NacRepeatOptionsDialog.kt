package com.nfcalarmclock.alarm.options.repeat

import android.content.res.ColorStateList
import android.text.SpannableStringBuilder
import android.text.format.DateFormat
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
import com.nfcalarmclock.system.toBestDateTimeString
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek.OnWeekChangedListener
import com.nfcalarmclock.view.performHapticFeedback
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
	 * Exclude start range alarm.
	 */
	private lateinit var excludeStartAlarm: NacAlarm

	/**
	 * Exclude end range alarm.
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
	 * List of units in singular form.
	 */
	private lateinit var singularUnits: Array<String>

	/**
	 * List of units in plurarl form.
	 */
	private lateinit var pluralUnits: Array<String>

	/**
	 * Build a human readable date/time string using the best format given a locale.
	 *
	 * The output of this should be used in a TextInputEditText.
	 */
	private fun buildBestDateTime(dateTime: String): CharSequence
	{
		// Nothing to convert
		if (dateTime.isEmpty())
		{
			return ""
		}

		// Convert the datetime string to a Calendar
		val cal = NacCalendar.dateTimeToCalendar(dateTime)

		// Convert the Calendar to the best human readable datetime string
		val locale = Locale.getDefault()
		val skeletonFormat = if (dateTime.contains(' '))
		{
			"MMM d, h:mm a"
		}
		else
		{
			"h:mm a"
		}
		val betterFormat = DateFormat.getBestDateTimePattern(locale, skeletonFormat)

		return DateFormat.format(betterFormat, cal)
	}

	/**
	 * Get the correct units list based on a given value and units.
	 *
	 * @return The correct units list based on a given value and units.
	 */
	private fun getCorrectUnitsList(value: Int, units: Int): Array<String>
	{
		// Determine the resource ID of the given unit
		val unitsId = when (units)
		{
			1 -> R.plurals.standalone_unit_minute
			2 -> R.plurals.standalone_unit_hour
			3 -> R.plurals.standalone_unit_day
			4 -> R.plurals.standalone_unit_week
			5 -> R.plurals.standalone_unit_month
			else -> R.plurals.standalone_unit_week
		}

		// Get a test unit string based on the ID above the given value
		val testUnit = requireContext().resources.getQuantityString(unitsId, value)

		// Check which list has the test unit and return that list
		return if (testUnit in singularUnits) singularUnits else pluralUnits
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
		// Update the alarm
		alarm.shouldRepeat = true
		alarm.shouldSkipNextAlarm = false
		alarm.repeatFrequency = selectedRepeatFrequencyValue
		alarm.repeatFrequencyUnits = selectedRepeatFrequencyUnits
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

		// Initialize the start and end date/time with the dates
		var startDateTime = excludeStartAlarm.date
		var endDateTime = excludeEndAlarm.date

		// Set the start time
		if (excludeStartAlarm.hour >= 0)
		{
			// Add space if date is present
			if (startDateTime.isNotEmpty())
			{
				startDateTime += " "
			}

			startDateTime += "${excludeStartAlarm.hour}:${excludeStartAlarm.minute}"
		}

		// Set the end time
		if (excludeEndAlarm.hour >= 0)
		{
			// Add space if date is present
			if (endDateTime.isNotEmpty())
			{
				endDateTime += " "
			}

			endDateTime += "${excludeEndAlarm.hour}:${excludeEndAlarm.minute}"
		}

		NacLog.i("Saving start datetime=$startDateTime")
		NacLog.i("Saving   end datetime=$endDateTime")

		// Save the start and end date/times
		alarm.excludeStartDateTime = startDateTime
		alarm.excludeEndDateTime = endDateTime
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
		// Get the context
		val context = requireContext()

		// Get singular and plural form of the units
		singularUnits = listOf(
			context.resources.getQuantityString(R.plurals.standalone_unit_minute, 1),
			context.resources.getQuantityString(R.plurals.standalone_unit_hour, 1),
			context.resources.getQuantityString(R.plurals.standalone_unit_day, 1),
			context.resources.getQuantityString(R.plurals.standalone_unit_week, 1),
			context.resources.getQuantityString(R.plurals.standalone_unit_month, 1),
		).toTypedArray()

		pluralUnits = listOf(
			context.resources.getQuantityString(R.plurals.standalone_unit_minute, 5),
			context.resources.getQuantityString(R.plurals.standalone_unit_hour, 5),
			context.resources.getQuantityString(R.plurals.standalone_unit_day, 5),
			context.resources.getQuantityString(R.plurals.standalone_unit_week, 5),
			context.resources.getQuantityString(R.plurals.standalone_unit_month, 5),
		).toTypedArray()

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
		// Set the exclude start and end alarms. They are copies so the original does not get modified
		excludeStartAlarm = alarm.copy()
		excludeEndAlarm = alarm.copy()

		// Convert the datetime string to a Calendar
		val startCal = NacCalendar.dateTimeToCalendar(excludeStartAlarm.excludeStartDateTime)
		val endCal = NacCalendar.dateTimeToCalendar(excludeEndAlarm.excludeEndDateTime)

		// Set the start hour, minute, and date
		if (startCal != null)
		{
			NacLog.i("Original start datetime=${excludeStartAlarm.excludeStartDateTime}")

			excludeStartAlarm.hour = startCal[Calendar.HOUR_OF_DAY]
			excludeStartAlarm.minute = startCal[Calendar.MINUTE]
			NacLog.i("Hour   : ${excludeStartAlarm.hour}")
			NacLog.i("Minute : ${excludeStartAlarm.minute}")

			if (excludeStartAlarm.excludeStartDateTime.contains(' '))
			{
				excludeStartAlarm.date = excludeStartAlarm.excludeStartDateTime.split(' ')[0]
				NacLog.i("Date : ${excludeStartAlarm.date}")
			}
		}

		// Set the end hour, minute, and date
		if (endCal != null)
		{
			NacLog.i("Original end datetime=${excludeEndAlarm.excludeEndDateTime}")

			excludeEndAlarm.hour = endCal[Calendar.HOUR_OF_DAY]
			excludeEndAlarm.minute = endCal[Calendar.MINUTE]
			NacLog.i("Hour   : ${excludeEndAlarm.hour}")
			NacLog.i("Minute : ${excludeEndAlarm.minute}")

			if (excludeEndAlarm.excludeEndDateTime.contains(' '))
			{
				excludeEndAlarm.date = excludeEndAlarm.excludeEndDateTime.split(' ')[0]
				NacLog.i("Date : ${excludeEndAlarm.date}")
			}
		}
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

		// Setup the color
		val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)
		excludeStartInputLayout.setStartIconTintList(themeColor)
		excludeEndInputLayout.setStartIconTintList(themeColor)

		// Setup the initial text
		val startTime = buildBestDateTime(excludeStartAlarm.excludeStartDateTime)
		val endTime = buildBestDateTime(excludeEndAlarm.excludeEndDateTime)
		excludeStartEditText.text = SpannableStringBuilder(startTime)
		excludeEndEditText.text = SpannableStringBuilder(endTime)

		// Show the time dialog on click
		excludeStartEditText.setOnClickListener {
			showTimeDialog(excludeStartEditText, excludeStartAlarm)
		}

		excludeEndEditText.setOnClickListener {
			showTimeDialog(excludeEndEditText, excludeEndAlarm)
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

		// Setup the dropdowns
		var valuesList = getRepeatFrequencyValuesFromUnitsIndex(unitsIndex)
		var unitsList = getCorrectUnitsList(defaultValue, defaultUnits)

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

			// Get the current unit selected, and the corrected units list
			unitsList = getCorrectUnitsList(selectedRepeatFrequencyValue, selectedRepeatFrequencyUnits)
			val text = unitsAutoCompleteTextView.adapter.getItem(unitsIndex) as String

			// Check if the current unit is in the corrected units list
			if (text !in unitsList)
			{
				// The unit is not in the list, so the dropdown list being shown needs to
				// be updated
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

			// Check if the value index was not able to be found
			if (valueIndex < 0)
			{
				// Reset the index to 0 and update the selected repeat frequency value
				valueIndex = 0
				selectedRepeatFrequencyValue = valuesList[0].toInt()
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
	private fun showTimeDialog(editText: TextInputEditText, alarm: NacAlarm)
	{
		// Show the dialog
		NacDateAndTimePickerDialog.create(
			alarm,
			onShowDateTitleListener = {
				it.text = "Select start date"
			},
			onShowTimeTitleListener = {
				it.text = "Select start time"
			},
			onDateClearedListener = {

				NacLog.i("Repeat options date cleared. Only using the time", offsetIndex = 1)

				// Clear date
				alarm.date = ""

				// Only use the time
				val bestDateTime = alarm.toBestDateTimeString()
				editText.text = SpannableStringBuilder(bestDateTime)

				NacLog.i("Display text=${editText.text}")

			},
			onTimeClearedListener = {

				NacLog.i("Repeat options time cleared. Clearing everything", offsetIndex = 1)

				// Clear everything
				alarm.date = ""
				alarm.hour = -1
				alarm.minute = -1

				// Only use the time
				editText.text = SpannableStringBuilder("")

			},
			onDateAndTimeSelectedListener = { _, _, year, month, day, hour, min ->

				// Set the date and time
				alarm.date = "$year-${month+1}-$day"
				alarm.hour = hour
				alarm.minute = min

				NacLog.i("Repeat options date and time selected: Date=${alarm.date} | Time=${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}", offsetIndex = 1)

				// Format and use the date and time
				val bestDateTime = alarm.toBestDateTimeString()
				editText.text = SpannableStringBuilder(bestDateTime)

				NacLog.i("Display text=${editText.text}")

			},
			onTimeSelectedListener = { _, hour, min ->

				NacLog.i("Repeat options time selected=${hour.toString().padStart(2, '0')}:${min.toString().padStart(2, '0')}", offsetIndex = 1)

				// Set the time
				alarm.hour = hour
				alarm.minute = min

				// Format and use the date and/or time
				val bestDateTime = alarm.toBestDateTimeString()
				editText.text = SpannableStringBuilder(bestDateTime)

				NacLog.i("Display text=${editText.text}")

			})
			.apply {
				shouldShowTitle = true
				shouldAlwaysShowClearButton = true
			}
			.show(childFragmentManager, NacDateAndTimePickerDialog.TAG)
	}

}