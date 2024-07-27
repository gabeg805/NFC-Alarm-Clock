package com.nfcalarmclock.startweekon

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Preference that prompts the user what day to start the week on.
 */
class NacStartWeekOnPreference @JvmOverloads constructor(

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
) : Preference(context, attrs, style),

	// Interface
	NacStartWeekOnDialog.OnStartWeekSelectedListener
{
	/**
	 * Index of the day to start on.
	 */
	private var startWeekOnIndex = 0

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference
	}

	/**
	 * Get the summary text which is the name of the day to start on.
	 *
	 * TODO: Update this for when start week is more flexible.
	 *
	 * @return The summary text which is the name of the day to start on.
	 */
	override fun getSummary(): CharSequence?
	{
		val week = context.resources.getStringArray(R.array.days_of_week)
		var index = startWeekOnIndex

		// Get the name of the day by the index
		return when (index)
			{
				1, 0 -> week[index]
				2 -> week[6]
				else ->
				{
					index = 0
					week[index]
				}
			}
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		val defaultValue = context.resources.getInteger(R.integer.default_start_week_on_index)

		return a.getInteger(index, defaultValue)
	}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			startWeekOnIndex = getPersistedInt(startWeekOnIndex)
		}
		// Convert the default value
		else
		{
			startWeekOnIndex = defaultValue as Int

			persistInt(startWeekOnIndex)
		}
	}

	/**
	 * Called when the start week on day is selected.
	 */
	override fun onStartWeekSelected(which: Int)
	{
		// Set the index of the day to start on
		startWeekOnIndex = which

		// Persist this index
		persistInt(startWeekOnIndex)

		// Set flag to refresh the main activity so that the days are redrawn
		val shared = NacSharedPreferences(context)

		shared.shouldRefreshMainActivity = true

		// Notify that a change occurred
		notifyChanged()
	}

	/**
	 * Show the start week on dialog.
	 */
	fun showDialog(manager: FragmentManager)
	{
		// Create the dialog
		val dialog = NacStartWeekOnDialog()

		// Setup the dialog
		dialog.defaultStartWeekOnIndex = startWeekOnIndex
		dialog.onStartWeekSelectedListener = this

		// Show the dialog
		dialog.show(manager, NacStartWeekOnDialog.TAG)
	}

}