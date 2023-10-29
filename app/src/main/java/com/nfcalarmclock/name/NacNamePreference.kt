package com.nfcalarmclock.name

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Preference that displays the name of the alarm.
 */
class NacNamePreference @JvmOverloads constructor(

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
	NacNameDialog.OnNameEnteredListener
{

	/**
	 * Name of the alarm.
	 */
	private var alarmName: String = ""

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

		return NacSharedPreferences.getNameSummary(cons, alarmName)
	}

	/**
	 * Persist the summary string and set the new summary when the dialog is
	 * dismissed.
	 */
	override fun onNameEntered(name: String)
	{
		// Set the new alarm name
		alarmName = name

		// Set the new summary (calls set/get summary)
		summary = this.summary

		// Persist the alarm name
		persistString(alarmName)
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
			alarmName = defaultValue as String

			persistString(alarmName)
		}
	}

	/**
	 * Show the name dialog.
	 */
	fun showDialog(manager: FragmentManager)
	{
		// Create the dialog
		val dialog = NacNameDialog()

		// Setup the dialog
		dialog.defaultName = alarmName
		dialog.onNameEnteredListener = this

		// Show the dialog
		dialog.show(manager, NacNameDialog.TAG)
	}

}