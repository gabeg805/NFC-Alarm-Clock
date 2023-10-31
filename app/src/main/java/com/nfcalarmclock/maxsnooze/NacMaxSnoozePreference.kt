package com.nfcalarmclock.maxsnooze

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment.OnScrollablePickerOptionSelectedListener

/**
 * Preference that displays the max number of snoozes for an alarm.
 */
class NacMaxSnoozePreference @JvmOverloads constructor(

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
	OnScrollablePickerOptionSelectedListener
{

	/**
	 * Max snooze index.
	 */
	private var maxSnoozeIndex = 0

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
		val cons = NacSharedConstants(context)

		return NacSharedPreferences.getMaxSnoozeSummary(cons, maxSnoozeIndex)
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		val defaultValue = context.resources.getInteger(R.integer.default_max_snooze_index)

		return a.getInteger(index, defaultValue)
	}

	/**
	 * Save the selected value from the scrollable picker.
	 */
	override fun onScrollablePickerOptionSelected(index: Int)
	{
		// Set the new max snooze index
		maxSnoozeIndex = index

		// Persist the index
		persistInt(index)

		// Notify of a change
		notifyChanged()
	}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			maxSnoozeIndex = getPersistedInt(maxSnoozeIndex)
		}
		// Convert the default value
		else
		{
			maxSnoozeIndex = defaultValue as Int

			persistInt(maxSnoozeIndex)
		}
	}

	/**
	 * Show the auto dismiss dialog.
	 */
	fun showDialog(manager: FragmentManager)
	{
		// Create the dialog
		val dialog = NacMaxSnoozeDialog()

		// Setup the dialog
		dialog.defaultScrollablePickerIndex = maxSnoozeIndex
		dialog.onScrollablePickerOptionSelectedListener = this

		// Show the dialog
		dialog.show(manager, NacMaxSnoozeDialog.TAG)
	}

}