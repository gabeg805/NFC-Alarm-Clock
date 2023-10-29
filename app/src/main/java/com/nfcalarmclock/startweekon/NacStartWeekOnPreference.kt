package com.nfcalarmclock.startweekon

import android.app.AlertDialog
import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import android.view.LayoutInflater
import android.view.View
import android.widget.RadioButton
import android.widget.RadioGroup
import androidx.preference.Preference
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.shared.NacSharedDefaults
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.dialog.NacDialog
import com.nfcalarmclock.view.dialog.NacDialog.OnBuildListener

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

	// Interfaces
	Preference.OnPreferenceClickListener,
	OnBuildListener,
	NacDialog.OnShowListener,
	NacDialog.OnDismissListener
{
	/**
	 * Index of the day to start on.
	 */
	private var startDayIndex = 0

	/**
	 * Constructor.
	 */
	init
	{
		// Set the layout
		layoutResource = R.layout.nac_preference

		// Set the on click listener
		onPreferenceClickListener = this
	}

	/**
	 * Get the index of the radio button that is currently checked.
	 *
	 * @return The index of the radio button that is currently checked.
	 */
	private fun getCheckedIndex(root: View): Int
	{
		val group = getRadioGroup(root)
		val count = group.childCount

		// Iterate over each day that can be started on
		for (i in 0 until count)
		{
			// Get the radio button
			val button = group.getChildAt(i) as RadioButton

			// Return the one that is checked
			if (button.isChecked)
			{
				return i
			}
		}
		return -1
	}

	/**
	 * Get the radio button at the given index.
	 *
	 * @return The radio button at the given index.
	 */
	fun getRadioButton(root: View, index: Int): RadioButton
	{
		// Get the group of radio buttons
		val group = getRadioGroup(root)

		// Find the radio button at the given index
		return group.getChildAt(index) as RadioButton
	}

	/**
	 * Get the radio group.
	 *
	 * @return The radio group.
	 */
	fun getRadioGroup(root: View): RadioGroup
	{
		return root.findViewById(R.id.start_week_on)
	}

	/**
	 * Get the summary text which is the name of the day to start on.
	 *
	 * @return The summary text which is the name of the day to start on.
	 */
	override fun getSummary(): CharSequence?
	{
		val cons = NacSharedConstants(context)
		val week = cons.daysOfWeek
		var index = startDayIndex

		// Get the name of the day by the index
		return when (index)
			{
				1, 0 -> week[index]
				else ->
				{
					index = 0
					week[index]
				}
			}
	}

	/**
	 * Inflate the radio buttons.
	 */
	private fun inflateRadioButtons(root: View, cons: NacSharedConstants)
	{
		val inflater = LayoutInflater.from(context)
		val group = getRadioGroup(root)
		val week = cons.daysOfWeek

		// Iterate over each day that can be started on
		// TODO: Should this be the same as getCheckedIndex()?
		for (i in 0..1)
		{
			// Inflate the root of the radio button
			val view = inflater.inflate(R.layout.radio_button, group, true)

			// Get the button
			val button = view.findViewById<RadioButton>(R.id.radio_button)

			// Generate and set the ID and day of the button
			val id = View.generateViewId()
			val day = week[i]

			button.id = id
			button.text = day
		}
	}

	/**
	 * Build a dialog.
	 */
	override fun onBuildDialog(dialog: NacDialog, builder: AlertDialog.Builder)
	{
		val cons = NacSharedConstants(context)

		// Title of the dialog
		builder.setTitle(cons.startWeekOnTitle)

		// Buttons
		dialog.setPositiveButton(cons.actionOk)
		dialog.setNegativeButton(cons.actionCancel)

		// Inflate the radio buttons
		inflateRadioButtons(dialog.root, cons)
	}

	/**
	 * Save the spinner index value.
	 */
	override fun onDismissDialog(dialog: NacDialog): Boolean
	{
		val shared = NacSharedPreferences(context)

		// Get the index of the day to start on
		startDayIndex = getCheckedIndex(dialog.root)

		// Persist this index
		persistInt(startDayIndex)

		// Set flag to refresh the main activity so that the days are redrawn
		shared.editShouldRefreshMainActivity(true)

		// Notify that a change occurred
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

		return a.getInteger(index, defs.startWeekOnIndex)
	}

	/**
	 * Called when the preference is clicked.
	 */
	override fun onPreferenceClick(preference: Preference): Boolean
	{
		// Create a dialog
		val dialog = NacDialog()

		// Set the starting index of which day to start on
		dialog.saveData(startDayIndex)

		// Set the listeners
		dialog.setOnBuildListener(this)
		dialog.addOnDismissListener(this)
		dialog.addOnShowListener(this)

		// Build and show the dialog
		dialog.build(context, R.layout.dlg_start_week_on)
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
			startDayIndex = getPersistedInt(startDayIndex)
		}
		// Convert the default value
		else
		{
			startDayIndex = defaultValue as Int
			persistInt(startDayIndex)
		}
	}

	/**
	 * Show the dialog.
	 */
	override fun onShowDialog(dialog: NacDialog, root: View)
	{
		setCheckedRadioButton(root)
		dialog.scale(0.6, 0.7, false, true)
	}

	/**
	 * Set checked radio button.
	 */
	protected fun setCheckedRadioButton(root: View)
	{
		val button = getRadioButton(root, startDayIndex)

		button.isChecked = true
	}

}