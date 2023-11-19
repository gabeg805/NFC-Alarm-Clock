package com.nfcalarmclock.settings.preference

import android.annotation.SuppressLint
import android.content.Context
import android.content.res.ColorStateList
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.CheckBox
import android.widget.CompoundButton
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Preference that indicates repeating the alarm.
 */
@SuppressLint("ResourceType")
class NacCheckboxPreference @JvmOverloads constructor(

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
	CompoundButton.OnCheckedChangeListener
{

	/**
	 * Check box button.
	 */
	private var checkBox: CheckBox? = null

	/**
	 * Check value.
	 */
	var isChecked = false

	/**
	 * Summary text when checkbox is enabled.
	 */
	private var enabledSummary: String? = null

	/**
	 * Summary text when checkbox is disabled.
	 */
	private var disabledSummary: String? = null

	/**
	 * Constructor.
	 */
	init
	{
		// Set the layout
		layoutResource = R.layout.nac_preference_checkbox

		// Set the listener
		onPreferenceClickListener = this

		// Find the summary from the attributes set
		val array = intArrayOf(android.R.attr.summaryOn, android.R.attr.summaryOff)
		val ta = context.obtainStyledAttributes(attrs, array)

		// Set the summaries from the attributes
		try
		{
			enabledSummary = ta.getString(0)
			disabledSummary = ta.getString(1)
		}
		// Recycle the typed array
		finally
		{
			ta.recycle()
		}
	}

	/**
	 * Get the summary text.
	 *
	 * @return The summary text.
	 */
	override fun getSummary(): CharSequence?
	{
		// Enabled summary
		return if (isChecked)
			{
				enabledSummary
			}
			// Disabled summary
			else
			{
				disabledSummary
			}
	}

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Set the checkbox view
		checkBox = holder.findViewById(R.id.widget) as CheckBox

		// Set the checked status and sandwich it by unsetting and resetting
		// the listener so that it does not go off when the status is set
		checkBox!!.setOnCheckedChangeListener(null)
		checkBox!!.isChecked = isChecked
		checkBox!!.setOnCheckedChangeListener(this)

		// Setup color
		setupCheckBoxColor()
	}

	/**
	 * Setup the color of the check box.
	 */
	private fun setupCheckBoxColor()
	{
		// Create a shared preferences
		val shared = NacSharedPreferences(context)

		// Get the colors for the boolean states
		val colors = intArrayOf(shared.themeColor, Color.LTGRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Set the state list
		checkBox!!.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Handle checkbox changes.
	 */
	override fun onCheckedChanged(button: CompoundButton, state: Boolean)
	{
		// Call the listener and check if the call was canceled
		if (!callChangeListener(state))
		{
			return
		}

		// Set the checked status
		isChecked = state
		checkBox!!.isChecked = state

		// Set the summary
		summary = this.summary

		// Persist the state
		persistBoolean(state)

		// Don't know what this does
		notifyDependencyChange(!state)
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		return a.getBoolean(index, DEFAULT_VALUE)
	}

	/**
	 * Allow users to select the whole preference to change the checkbox.
	 */
	override fun onPreferenceClick(pref: Preference): Boolean
	{
		// Perform the click
		checkBox!!.performClick()

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
			isChecked = getPersistedBoolean(isChecked)
		}
		// Convert the default value
		else
		{
			isChecked = defaultValue as Boolean

			persistBoolean(isChecked)
		}
	}

	companion object
	{

		/**
		 * Default value.
		 */
		private const val DEFAULT_VALUE = true

	}

}