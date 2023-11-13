package com.nfcalarmclock.autodismiss

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment.OnScrollablePickerOptionSelectedListener

/**
 * Preference that displays how long before an alarm is auto dismissed.
 */
class NacAutoDismissPreference @JvmOverloads constructor(

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
	private var autoDismissIndex = 0

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
		val summaries = context.resources.getStringArray(R.array.auto_dismiss_summaries)

		return summaries[autoDismissIndex]
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		val defaultValue = context.resources.getInteger(R.integer.default_auto_dismiss_index)

		return a.getInteger(index, defaultValue)
	}

	/**
	 * Save the selected value from the scrollable picker.
	 */
	override fun onScrollablePickerOptionSelected(index: Int)
	{
		// Set the auto dismiss index
		autoDismissIndex = index

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
			autoDismissIndex = getPersistedInt(autoDismissIndex)
		}
		// Convert the default value
		else
		{
			autoDismissIndex = defaultValue as Int

			persistInt(autoDismissIndex)
		}
	}

	/**
	 * Show the auto dismiss dialog.
	 */
	fun showDialog(manager: FragmentManager)
	{
		// Create the dialog
		val dialog = NacAutoDismissDialog()

		// Setup the dialog
		dialog.defaultScrollablePickerIndex = autoDismissIndex
		dialog.onScrollablePickerOptionSelectedListener = this

		// Show the dialog
		dialog.show(manager, NacAutoDismissDialog.TAG)
	}

}