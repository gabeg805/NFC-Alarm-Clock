package com.nfcalarmclock.name

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.dialog.NacDialog

/**
 * Preference that displays the name of the alarm.
 */
class NacNamePreference @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	style: Int = 0
) : Preference(context, attrs, style),
	Preference.OnPreferenceClickListener,
	NacDialog.OnDismissListener
{

	/**
	 * Name of the alarm.
	 */
	private var alarmName: String? = null

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference
		onPreferenceClickListener = this
	}

	/**
	 * Get the summary text.
	 *
	 * @return The summary text.
	 */
	override fun getSummary(): CharSequence?
	{
		val cons = NacSharedConstants(context)

		return NacSharedPreferences.getNameSummary(cons, alarmName)
	}

	/**
	 * Persist the summary string and set the new summary when the dialog is
	 * dismissed.
	 */
	override fun onDismissDialog(dialog: NacDialog): Boolean
	{
		// Set the new alarm name
		alarmName = dialog.dataString

		// Set the new summary (calls set/get summary)
		summary = this.summary

		// Persist the alarm name
		persistString(alarmName)
		return true
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any?
	{
		return a.getString(index)
	}

	/**
	 * Display the dialog when the preference is clicked.
	 */
	override fun onPreferenceClick(pref: Preference): Boolean
	{
		// Create the dialog
		val dialog = NacNameDialog()

		// Build the dialog
		dialog.build(context)

		// Setup the dialog
		dialog.addOnDismissListener(this)
		dialog.saveData(alarmName)

		// Show the dialog
		dialog.show()
		return true
	}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			alarmName = getPersistedString(alarmName)
		}
		// Convert the default value
		else
		{
			alarmName = defaultValue as String?
			persistString(alarmName)
		}
	}

}