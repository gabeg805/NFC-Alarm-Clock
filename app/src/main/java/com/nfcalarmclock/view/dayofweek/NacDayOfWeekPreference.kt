package com.nfcalarmclock.view.dayofweek

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar
import com.nfcalarmclock.util.NacCalendar.Day
import java.util.EnumSet

/**
 * Preference that displays the day of week dialog.
 */
class NacDayOfWeekPreference @JvmOverloads constructor(

	/**
	 * Context.
	 */
	context: Context,

	/**
	 * Attribute set.
	 */
	attrs: AttributeSet? = null,

	/**
	 * Default style.
	 */
	style: Int = 0

	// Constructor
) : Preference(context, attrs, style)

	// Interface
{

	/**
	 * Value of days.
	 */
	private var dayOfWeekValue = 0

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference
	}

	/**
	 * Get the summary text.
	 *
	 * @return The summary text.
	 */
	override fun getSummary(): CharSequence?
	{
		return sharedPreferences.daysSummary
	}

	/**
	 * Save the selected days when the dialog is dismissed.
	 */
	//override fun onDismissDialog(dialog: NacDialog): Boolean
	//{
	//	// Get the day of week
	//	val dow = (dialog as NacDayOfWeekDialog).dayOfWeek

	//	// Set the day of week value
	//	mValue = NacCalendar.Days.daysToValue(dow.days)

	//	// Reevaluate the summary
	//	summary = this.summary

	//	// Persist the value
	//	persistInt(mValue)
	//}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		// Calculate the default value
		val default = NacCalendar.Days.daysToValue(
			EnumSet.of(Day.MONDAY, Day.TUESDAY,
				Day.WEDNESDAY, Day.THURSDAY,
				Day.FRIDAY))

		// Get the default value
		return a.getInteger(index, default)
	}

	/**
	 * Display the dialog when the preference is selected.
	 */
	//override fun onPreferenceClick(preference: Preference): Boolean
	//{
	//	// Create the dialog
	//	val dialog = NacDayOfWeekDialog()

	//	// Setup the dialog
	//	dialog.build(context)
	//	dialog.addOnShowListener(this)
	//	dialog.addOnDismissListener(this)

	//	// Show the dialog
	//	dialog.show()
	//	return true
	//}

	/**
	 * Set the days in the dialog.
	 */
	//override fun onShowDialog(dialog: NacDialog, root: View)
	//{
	//	val dow = (dialog as NacDayOfWeekDialog).dayOfWeek

	//	dow.setDays(mValue)
	//}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			dayOfWeekValue = getPersistedInt(dayOfWeekValue)
		}
		// Convert the default value
		else
		{
			dayOfWeekValue = defaultValue as Int
			persistInt(dayOfWeekValue)
		}
	}

	/**
	 * Show the start week on dialog.
	 */
	fun showDialog(manager: FragmentManager)
	{
		// Create the dialog
		val dialog = NacDayOfWeekDialog()

		// Setup the dialog
		//dialog.defaultStartWeekOnIndex = startWeekOnIndex
		//dialog.onStartWeekSelectedListener = this

		// Show the dialog
		//dialog.show(manager, NacAutoDismissDialog.TAG)
	}

}