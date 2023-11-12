package com.nfcalarmclock.view.dayofweek

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.LinearLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacCalendar.Day
import com.nfcalarmclock.view.dialog.NacDialogFragment
import java.util.EnumSet

/**
 */
class NacDayOfWeekDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when days of week are selected.
	 */
	fun interface OnDaysOfWeekSelectedListener
	{
		fun onDaysOfWeekSelected(selectedDays: EnumSet<NacCalendar.Day>)
	}

	/**
	 * Default days selected.
	 *
	 * This will be set externally before the dialog is shown.
	 */
	var defaultDayOfWeekValues: Int = 0
		set(value) {

			// Set the current selected days
			currentDayOfWeekSelected = NacCalendar.Day.valueToDays(value)

			// Set the backing field
			field = value
		}

	/**
	 * Current selected days.
	 */
	private var currentDayOfWeekSelected: EnumSet<Day> = NacCalendar.Day.NONE

	/**
	 * Listener for when the volume is restricted/unrestricted.
	 */
	var onDaysOfWeekSelectedListener: OnDaysOfWeekSelectedListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_days)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				onDaysOfWeekSelectedListener?.onDaysOfWeekSelected(currentDayOfWeekSelected)

			}
			.setNegativeButton(R.string.action_cancel) { _, _ ->
			}
			.setView(R.layout.dlg_alarm_days)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the dialog container
		val container = dialog!!.findViewById<LinearLayout>(R.id.days)

		// Create the day of week view
		val dayOfWeek = NacDayOfWeek(container)

		// Setup the view
		dayOfWeek.setDays(defaultDayOfWeekValues)
		dayOfWeek.setStartWeekOn(sharedPreferences!!.startWeekOn)
		setupOnClickListener(dayOfWeek)
	}

	/**
	 * Setup the on click listener when a day is selected.
	 */
	private fun setupOnClickListener(dayOfWeek: NacDayOfWeek)
	{
		// Set the listener
		dayOfWeek.onWeekChangedListener = NacDayOfWeek.OnWeekChangedListener { _, day ->

			// Toggle the day that was selected
			if (currentDayOfWeekSelected.contains(day))
			{
				currentDayOfWeekSelected.remove(day)
			}
			else
			{
				currentDayOfWeekSelected.add(day)
			}

		}
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacDayOfWeekDialog"

	}

}
