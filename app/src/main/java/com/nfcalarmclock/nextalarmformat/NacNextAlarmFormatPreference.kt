package com.nfcalarmclock.nextalarmformat

import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.View
import android.widget.RadioGroup
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.shared.NacSharedDefaults
import com.nfcalarmclock.view.dialog.NacDialog
import com.nfcalarmclock.view.dialog.NacDialog.OnBuildListener

/**
 * Preference that prompts the user what format they want to display the next
 * alarm.
 */
class NacNextAlarmFormatPreference @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	style: Int = 0
) : Preference(context, attrs, style),
	Preference.OnPreferenceClickListener,
	OnBuildListener,
	NacDialog.OnShowListener,
	NacDialog.OnDismissListener
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
		onPreferenceClickListener = this
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
	 * Build the dialog.
	 */
	override fun onBuildDialog(dialog: NacDialog, builder: AlertDialog.Builder)
	{
		val context = dialog.context
		val cons = NacSharedConstants(context)

		// Set the title of the dialog
		builder.setTitle(cons.titleNextAlarmFormat)

		// Set the text of the buttons
		dialog.setPositiveButton(cons.actionOk)
		dialog.setNegativeButton(cons.actionCancel)
	}

	/**
	 * Save the spinner index value.
	 */
	override fun onDismissDialog(dialog: NacDialog): Boolean
	{
		val days = dialog.root.findViewById<RadioGroup>(R.id.formats)
		val id = days.checkedRadioButtonId

		// Set the value of the next alarm format
		nextAlarmFormatIndex = if (id == R.id.nexton)
			{
				1
			}
			else if (id == R.id.nextin)
			{
				0
			}
			else
			{
				0
			}

		// Persist the index
		persistInt(nextAlarmFormatIndex)

		// Notify of the change
		notifyChanged()
		return true
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
	 * Called when the preference is clicked.
	 */
	override fun onPreferenceClick(preference: Preference): Boolean
	{
		val dialog = NacDialog()

		// Set the value
		dialog.saveData(nextAlarmFormatIndex)

		// Set the listeners
		dialog.setOnBuildListener(this)
		dialog.addOnDismissListener(this)
		dialog.addOnShowListener(this)

		// Build and show the dialog
		dialog.build(context, R.layout.dlg_next_alarm_format)
		dialog.show()
		return true
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
	 * Show the dialog.
	 */
	override fun onShowDialog(dialog: NacDialog, root: View)
	{
		// Get the radio group
		val days = root.findViewById<RadioGroup>(R.id.formats)

		// Determine which day to have checked
		when (nextAlarmFormatIndex)
		{
			1 -> days.check(R.id.nexton)
			0 -> days.check(R.id.nextin)
			else -> days.check(R.id.nextin)
		}

		// Scale the dialog
		dialog.scale(0.9, 0.7, false, true)
	}

}