package com.nfcalarmclock.nextalarmformat

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.autodismiss.NacAutoDismissDialog
import com.nfcalarmclock.shared.NacSharedDefaults

/**
 * Preference that prompts the user what format they want to display the next
 * alarm.
 */
class NacNextAlarmFormatPreference @JvmOverloads constructor(

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
	NacNextAlarmFormatDialog.OnNextAlarmFormatSelectedListener
{

	/**
	 * Preference value.
	 */
	private var nextAlarmFormatIndex = 0

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference
	}

	/**
	 * Get the summary text of which alarm format to use.
	 *
	 * @return The summary text of which alarm format to use.
	 */
	override fun getSummary(): CharSequence
	{
		return when (nextAlarmFormatIndex)
			{
				1 -> context.getString(R.string.next_alarm_format_time_on)
				0 -> context.getString(R.string.next_alarm_format_time_in)
				else -> context.getString(R.string.next_alarm_format_time_in)
			}
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		val defs = NacSharedDefaults(context)

		return a.getInteger(index, defs.nextAlarmFormatIndex)
	}

	/**
	 * Save the spinner index value.
	 */
	override fun onNextAlarmFormatSelected(which: Int)
	{
		// Set the value of the next alarm format
		nextAlarmFormatIndex = which

		// Persist the index
		persistInt(nextAlarmFormatIndex)

		// Notify of the change
		notifyChanged()
	}

	/**
	 * Set the initial preference value.
	 */
	public override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			nextAlarmFormatIndex = getPersistedInt(nextAlarmFormatIndex)
		}
		// Convert the default value
		else
		{
			nextAlarmFormatIndex = defaultValue as Int

			persistInt(nextAlarmFormatIndex)
		}
	}

	/**
	 * Show the next alarm format dialog.
	 */
	fun showDialog(manager: FragmentManager)
	{
		// Create the dialog
		val dialog = NacNextAlarmFormatDialog()

		// Setup the dialog
		dialog.defaultNextAlarmFormatIndex = nextAlarmFormatIndex
		dialog.onNextAlarmFormatListener = this

		// Show the dialog
		dialog.show(manager, NacAutoDismissDialog.TAG)
	}

}