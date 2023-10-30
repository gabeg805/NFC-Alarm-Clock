package com.nfcalarmclock.view.dayofweek

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Preference that allows a user to select a style for the day buttons.
 */
class NacDayButtonStylePreference @JvmOverloads constructor(

	/**
	 * Context.
	 */
	context: Context,

	/**
	 * Context.
	 */
	attrs: AttributeSet? = null,

	/**
	 * Context.
	 */
	style: Int = 0

	// Constructor
) : Preference(context, attrs, style),

	// Interface
	Preference.OnPreferenceClickListener
{

	/**
	 * Day button.
	 */
	private var dayButton: NacDayButton? = null

	/**
	 * Style value.
	 */
	private var styleValue = 0

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference_day_button
		onPreferenceClickListener = this
		sharedPreferences = NacSharedPreferences(context)
	}

	/**
	 * Get the summary text to use for the preference.
	 *
	 * @return The summary text to use for the preference.
	 */
	override fun getSummary(): CharSequence
	{
		return when (styleValue)
		{
			1 -> context.getString(R.string.description_day_button_style_filled)
			2 -> context.getString(R.string.description_day_button_style_outlined)
			else -> context.getString(R.string.description_day_button_style_filled)
		}
	}

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Set the view
		dayButton = holder.findViewById(R.id.widget) as NacDayButton

		// Setup the view
		setupDayButton()
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		// Default style
		val def = context.resources.getInteger(R.integer.default_day_button_style)

		// Get the style
		return a.getInteger(index, def)
	}

	/**
	 * Allow users to select the whole preference to change the checkbox.
	 */
	override fun onPreferenceClick(pref: Preference): Boolean
	{
		// Get the preferred style
		val style = sharedPreferences.dayButtonStyle

		// Toggle the style
		styleValue = style % 2 + 1

		// Set the new style
		dayButton!!.setStyle(styleValue)

		// Setup the view
		setupDayButton()

		// Persist the value
		persistInt(styleValue)

		// Notify of a change
		notifyChanged()

		// Call listener
		callChangeListener(styleValue)

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
			styleValue = getPersistedInt(styleValue)
		}
		// Convert the default value
		else
		{
			styleValue = defaultValue as Int

			persistInt(styleValue)
		}
	}

	/**
	 * Setup the day button.
	 */
	protected fun setupDayButton()
	{
		// Get an example day
		val daysOfWeek = context.resources.getStringArray(R.array.days_of_week)
		val exampleDay = daysOfWeek[1]

		// Setup the button
		dayButton!!.setText(exampleDay)
		dayButton!!.enable()
		dayButton!!.button!!.isEnabled = false
	}

}