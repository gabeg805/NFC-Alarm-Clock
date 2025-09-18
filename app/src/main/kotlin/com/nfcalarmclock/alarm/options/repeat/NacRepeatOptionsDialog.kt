package com.nfcalarmclock.alarm.options.repeat

import android.widget.AdapterView
import android.widget.TextView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.system.NacCalendar.Day
import com.nfcalarmclock.system.NacCalendar.alarmToCalendar
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek.OnWeekChangedListener
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupRippleColor
import java.util.Calendar
import java.util.EnumSet

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
			// Minute (max 8 hours)
			0 -> (15..480)

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
	 * Get next alarm day.
	 */
	private fun getNextAlarmDay(alarm: NacAlarm): Day
	{
		// Get the current time
		val now = Calendar.getInstance()

		// Build the alarm calendar instance
		val alarmCal = alarmToCalendar(alarm)
		alarmCal[Calendar.DAY_OF_WEEK] = Day.dayToCalendarDay(Day.TODAY)

		// Alarm will occur in the future
		return if (alarmCal.after(now))
		{
			Day.TODAY
		}
		// Alarm is in the past
		else
		{
			// Increment today to tomorrow
			now.add(Calendar.DAY_OF_MONTH, 1)

			// Get tomorrow as a day
			val tomorrow = now.get(Calendar.DAY_OF_WEEK)

			return Day.calendarDayToDay(tomorrow)
		}
	}

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.shouldRepeat = true
		alarm?.shouldSkipNextAlarm = false
		alarm?.repeatFrequency = selectedRepeatFrequencyValue
		alarm?.repeatFrequencyUnits = selectedRepeatFrequencyUnits
		alarm?.repeatFrequencyDaysToRunBeforeStarting = selectedDaysToRunBeforeFrequency

		// Weekly frequency unit
		if (selectedRepeatFrequencyUnits == 4)
		{
			// Days are empty
			if (alarm?.days?.isEmpty() == true)
			{
				// Get the next alarm day
				val nextDay = getNextAlarmDay(alarm)

				// Toggle the day
				alarm.toggleDay(nextDay)
			}
		}
		// Every other frequency unit
		else
		{
			// Clear various alarm attributes
			alarm?.repeatFrequencyDaysToRunBeforeStarting = Day.NONE
			alarm?.setDays(0)
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
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
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

		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build(sharedPreferences)

		// Set the default selected values
		selectedRepeatFrequencyValue = a.repeatFrequency
		selectedRepeatFrequencyUnits = a.repeatFrequencyUnits
		selectedDaysToRunBeforeFrequency = a.days.ifEmpty {
				EnumSet.of(getNextAlarmDay(a))
		}

		// Setup the views
		setupRepeatFrequency(a.repeatFrequency, a.repeatFrequencyUnits)
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

}