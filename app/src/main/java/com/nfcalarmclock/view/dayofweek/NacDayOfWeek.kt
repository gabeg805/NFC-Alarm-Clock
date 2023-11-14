package com.nfcalarmclock.view.dayofweek

import android.widget.LinearLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacCalendar.Day
import com.nfcalarmclock.view.dayofweek.NacDayButton.OnDayChangedListener
import java.util.EnumSet

/**
 * A button that consists of an image to the left, and text to the right of it.
 */
class NacDayOfWeek(

	/**
	 * Day of week view.
	 */
	private val dayOfWeekView: LinearLayout

) : OnDayChangedListener
{

	/**
	 * Listen for when the day of week is changed.
	 *
	 * Returning True means that the listener handled the event, and False means
	 * that the NacDayOfWeek class will handle the click event, and execute the
	 * default action.
	 */
	fun interface OnWeekChangedListener
	{
		fun onWeekChanged(button: NacDayButton, day: Day)
	}

	/**
	 * A list of all the day buttons.
	 */
	val dayButtons: List<NacDayButton>
		get()
		{
			// List of buttons
			val buttons: MutableList<NacDayButton> = ArrayList()

			// Count the number of views in the container
			val count = dayOfWeekView.childCount

			// Iterate over each view
			for (i in 0 until count)
			{
				// Get the button
				val b = dayOfWeekView.getChildAt(i) as NacDayButton

				// Add it to the list
				buttons.add(b)
			}

			// Return the list of buttons
			return buttons
		}

	/**
	 * The alarm days.
	 */
	val days: EnumSet<Day>
		get()
		{
			val days = Day.NONE

			// Iterate over each day in the week
			for (d in Day.WEEK)
			{
				// Get the button
				val button = getDayButton(d)

				// Check if the day is enabled
				if (button.button!!.isChecked)
				{
					// Add the day
					days.add(d)
				}
			}

			// Return the set of days
			return days
		}

	/**
	 * Day of week changed listener.
	 */
	var onWeekChangedListener: OnWeekChangedListener? = null

	/**
	 * Constructor
	 */
	init
	{
		// Setup buttons
		setupDayButtons()
	}

	/**
	 * Convert a particular day to its corresponding view ID.
	 */
	private fun dayToId(day: Day): Int
	{
		return when (day)
		{
			Day.SUNDAY -> R.id.dow_sun
			Day.MONDAY -> R.id.dow_mon
			Day.TUESDAY -> R.id.dow_tue
			Day.WEDNESDAY -> R.id.dow_wed
			Day.THURSDAY -> R.id.dow_thu
			Day.FRIDAY -> R.id.dow_fri
			Day.SATURDAY -> R.id.dow_sat
		}
	}

	/**
	 * Get the day button given a particular day.
	 *
	 * @return The day button given a particular day.
	 */
	private fun getDayButton(day: Day): NacDayButton
	{
		// Get the ID
		val id = dayToId(day)

		// Find the view
		return dayOfWeekView.findViewById(id)
	}

	/**
	 * Convert an view ID to its corresponding day.
	 */
	private fun idToDay(id: Int): Day?
	{
		return when (id)
		{
			R.id.dow_sun -> Day.SUNDAY
			R.id.dow_mon -> Day.MONDAY
			R.id.dow_tue -> Day.TUESDAY
			R.id.dow_wed -> Day.WEDNESDAY
			R.id.dow_thu -> Day.THURSDAY
			R.id.dow_fri -> Day.FRIDAY
			R.id.dow_sat -> Day.SATURDAY
			else -> null
		}
	}

	/**
	 * Called when a day is changed
	 */
	override fun onDayChanged(button: NacDayButton)
	{
		// Get the day or return if it is null
		val day = idToDay(button.id) ?: return

		// Call the listener
		onWeekChangedListener?.onWeekChanged(button, day)
	}

	/**
	 * Set the days that will be enabled/disabled.
	 *
	 * TODO This is doing more animating than necessary.
	 * Only enable/disable if needs to be done.
	 *
	 * @param  days  The button days that will be enabled.
	 */
	fun setDays(days: EnumSet<Day>)
	{
		// Iterate over each day in the week
		for (d in Day.WEEK)
		{
			// Get the button
			val button = getDayButton(d)

			// Clear the listener
			button.onDayChangedListener = null

			// Change the button's state depending on the days
			if (days.contains(d))
			{
				button.enable()
			}
			else
			{
				button.disable()
			}

			// Set the listener once again
			button.onDayChangedListener = this
		}
	}

	/**
	 * @see .setDays
	 */
	fun setDays(value: Int)
	{
		// Determine the days
		val days = Day.valueToDays(value)

		// Set the days
		this.setDays(days)
	}

	/**
	 * Set the day to start week on.
	 *
	 * TODO: Add more days
	 */
	fun setStartWeekOn(start: Int)
	{
		// Get the buttons that can be started on
		val sunday = getDayButton(Day.SUNDAY)
		val monday = getDayButton(Day.MONDAY)

		// Get the first and last children
		val firstChild = dayOfWeekView.getChildAt(0)
		val lastChild = dayOfWeekView.getChildAt(6)

		// Check which days will be started on
		if (start == 1)
		{
			if (firstChild.id != monday.id)
			{
				dayOfWeekView.removeView(firstChild)
				dayOfWeekView.addView(firstChild, 6)
			}
		}
		else
		{
			if (firstChild.id != sunday.id)
			{
				dayOfWeekView.removeView(lastChild)
				dayOfWeekView.addView(lastChild, 0)
			}
		}
	}

	/**
	 * Setup the day buttons.
	 */
	private fun setupDayButtons()
	{
		val context = dayOfWeekView.context

		// Get the days of week
		val week = context.resources.getStringArray(R.array.days_of_week)

		// Count the number of children (should equate to the number of days
		// in the week)
		val count = dayOfWeekView.childCount

		// Iterate over each child
		for (i in 0 until count)
		{
			// Get the button
			val button = dayOfWeekView.getChildAt(i) as NacDayButton

			// Set the day
			button.setText(week[i])

			// Set the listener
			button.onDayChangedListener = this
		}
	}

}