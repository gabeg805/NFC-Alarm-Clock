package com.nfcalarmclock.snoozeduration

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
 * Preference that displays how long to snooze for.
 */
class NacSnoozeDurationPreference @JvmOverloads constructor(

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
	 * Preference value.
	 */
	private var snoozeDurationIndex = 0

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
	override fun getSummary(): CharSequence
	{
		val cons = NacSharedConstants(context)

		return NacSharedPreferences.getSnoozeDurationSummary(cons, snoozeDurationIndex)
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		val defaultValue = context.resources.getInteger(R.integer.default_snooze_duration_index)

		return a.getInteger(index, defaultValue)
	}

	/**
	 * Save the selected value from the scrollable picker.
	 */
	override fun onScrollablePickerOptionSelected(index: Int)
	{
		// Set the snooze duration index
		snoozeDurationIndex = index

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
			snoozeDurationIndex = getPersistedInt(snoozeDurationIndex)
		}
		// Convert the default value
		else
		{
			snoozeDurationIndex = defaultValue as Int

			persistInt(snoozeDurationIndex)
		}
	}

	/**
	 * Show the auto dismiss dialog.
	 */
	fun showDialog(manager: FragmentManager)
	{
		// Create the dialog
		val dialog = NacSnoozeDurationDialog()

		// Setup the dialog
		dialog.defaultScrollablePickerIndex = snoozeDurationIndex
		dialog.onScrollablePickerOptionSelectedListener = this

		// Show the dialog
		dialog.show(manager, NacSnoozeDurationDialog.TAG)
	}

}