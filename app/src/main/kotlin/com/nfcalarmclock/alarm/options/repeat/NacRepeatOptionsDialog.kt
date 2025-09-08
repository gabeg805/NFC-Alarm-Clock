package com.nfcalarmclock.alarm.options.repeat

import android.widget.AdapterView
import android.widget.TextView
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.view.calcAlpha
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek
import com.nfcalarmclock.view.dayofweek.NacDayOfWeek.OnWeekChangedListener
import com.nfcalarmclock.view.performHapticFeedback
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor
import com.nfcalarmclock.view.setupRippleColor
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
	private var selectedDaysToRunBeforeFrequency: EnumSet<NacCalendar.Day> = NacCalendar.Day.WEEK

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
			0 -> R.plurals.standalone_unit_week
			1 -> R.plurals.standalone_unit_day
			2 -> R.plurals.standalone_unit_hour
			else -> R.plurals.standalone_unit_week
		}

		// Get a test unit string based on the ID above the given value
		val testUnit = requireContext().resources.getQuantityString(unitsId, value)

		// Check which list has the test unit and return that list
		return if (testUnit in singularUnits) singularUnits else pluralUnits
	}

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
		// Update the alarm
		alarm?.shouldRepeat = true
		alarm?.repeatFrequency = selectedRepeatFrequencyValue
		alarm?.repeatFrequencyUnits = selectedRepeatFrequencyUnits
		alarm?.repeatFrequencyDaysToRunBeforeStarting = selectedDaysToRunBeforeFrequency

		// Check if the frequency unit is not by week
		if (selectedRepeatFrequencyUnits != 4)
		{
			// Clear various alarm attributes
			alarm?.shouldSkipNextAlarm = false
			alarm?.repeatFrequencyDaysToRunBeforeStarting = NacCalendar.Day.NONE
			alarm?.date = ""
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
			context.resources.getQuantityString(R.plurals.standalone_unit_hour, 1),
			context.resources.getQuantityString(R.plurals.standalone_unit_day, 1),
			context.resources.getQuantityString(R.plurals.standalone_unit_week, 1)
		).toTypedArray()

		pluralUnits = listOf(
			context.resources.getQuantityString(R.plurals.standalone_unit_hour, 5),
			context.resources.getQuantityString(R.plurals.standalone_unit_day, 5),
			context.resources.getQuantityString(R.plurals.standalone_unit_week, 5)
		).toTypedArray()

		// Get the default values
		val defaultRepeatFrequencyValue = alarm?.repeatFrequency ?: 1
		val defaultRepeatFrequencyUnits = alarm?.repeatFrequencyUnits ?: 4
		val defaultCurrentDays = alarm?.days ?: NacCalendar.Day.WEEK
		val defaultDaysToRun = alarm?.repeatFrequencyDaysToRunBeforeStarting ?: NacCalendar.Day.WEEK
		selectedRepeatFrequencyValue = defaultRepeatFrequencyValue
		selectedRepeatFrequencyUnits = defaultRepeatFrequencyUnits
		selectedDaysToRunBeforeFrequency = defaultDaysToRun

		// Setup the views
		setupRepeatFrequency(defaultRepeatFrequencyValue, defaultRepeatFrequencyUnits)
		setupDaysToRun(defaultCurrentDays)
		setDaysToRunUsability()
	}

	/**
	 * Setup the views for the days to run before starting the frequency.
	 */
	private fun setupDaysToRun(defaultCurrentDays: EnumSet<NacCalendar.Day>)
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
	 * Setup the auto snooze views.
	 */
	private fun setupRepeatFrequency(defaultValue: Int, defaultUnits: Int)
	{
		// Get the context
		val context = requireContext()

		// Get the views
		val valueAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_snooze_minutes_dropdown_menu)
		val unitsAutoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.auto_snooze_seconds_dropdown_menu)
		val valueInputLayout: TextInputLayout = dialog!!.findViewById(R.id.auto_snooze_minutes_input_layout)
		val unitsInputLayout: TextInputLayout = dialog!!.findViewById(R.id.auto_snooze_seconds_input_layout)

		// Setup the input layouts
		valueInputLayout.setupInputLayoutColor(context, sharedPreferences)
		unitsInputLayout.setupInputLayoutColor(context, sharedPreferences)

		// Setup the dropdowns
		val valuesList = (1..100).map { it.toString()  }.toTypedArray()
		val unitsList = getCorrectUnitsList(defaultValue, defaultUnits)

		valueAutoCompleteTextView.setSimpleItems(valuesList)
		unitsAutoCompleteTextView.setSimpleItems(unitsList)

		// Set the default value in the textview
		val valueIndex = NacAlarm.calcRepeatFrequencyIndex(defaultValue)
		valueAutoCompleteTextView.setTextFromIndex(valueIndex)

		// Set the default unit in the textview
		var unitsIndex = NacAlarm.calcRepeatFrequencyUnitsIndex(defaultUnits)
		unitsAutoCompleteTextView.setTextFromIndex(unitsIndex)

		// Setup the listeners
		valueAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the repeat frequency
			selectedRepeatFrequencyValue = NacAlarm.calcRepeatFrequencyFromIndex(position)

			// Get the current unit selected, and the corrected units list
			val list = getCorrectUnitsList(selectedRepeatFrequencyValue, selectedRepeatFrequencyUnits)
			val text = unitsAutoCompleteTextView.adapter.getItem(unitsIndex) as String

			// Check if the current unit is in the corrected units list
			if (text !in list)
			{
				// The unit is not in the list, so the dropdown list being shown needs to
				// be updated
				unitsAutoCompleteTextView.setSimpleItems(list)
				unitsAutoCompleteTextView.setTextFromIndex(unitsIndex)
			}

			// Set the usability of the days view
			setDaysToRunUsability()

		}

		unitsAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the repeat frequency units
			selectedRepeatFrequencyUnits = NacAlarm.calcRepeatFrequencyUnitsFromIndex(position)
			unitsIndex = position

			// Set the usability of the days view
			setDaysToRunUsability()

		}
	}

}