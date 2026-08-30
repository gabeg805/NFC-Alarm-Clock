package com.nfcalarmclock.settings.daybuttonstyle

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.view.dayofweek.NacDayButton

/**
 * Preference that allows a user to select a style for the day buttons.
 *
 * @param context Context.
 * @param attrs Attribute set.
 * @param style Style.
 */
class NacDayButtonStylePreference @JvmOverloads constructor(
	context: Context,
	attrs: AttributeSet? = null,
	style: Int = 0
) : Preference(context, attrs, style),
	Preference.OnPreferenceClickListener
{

	/**
	 * Shared preferences.
	 */
	private val sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)

	/**
	 * Style value.
	 */
	private var styleValue: Int = 0

	/**
	 * Example button.
	 */
	private lateinit var exampleButton: NacDayButton

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference_day_button
		onPreferenceClickListener = this
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
		exampleButton = (holder.findViewById(R.id.widget) as NacDayButton)

		// Setup the view
		setupExampleButton()
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		// Default value
		val def = context.resources.getInteger(R.integer.default_day_button_style)

		// Get the value
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

		// Set the new style. Note: If the size of the button is increased, when the style
		// changes, the view will increase to fill that space, making the circle more like a
		// rounded square
		exampleButton.setStyle(styleValue)

		//// Setup the view
		//setupDayButton()

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
	 * Setup the example day button.
	 */
	private fun setupExampleButton()
	{
		// Get an example day
		val daysOfWeek = context.resources.getStringArray(R.array.days_of_week_full)
		val exampleDay = daysOfWeek[1]

		// Setup the button
		exampleButton.setText(exampleDay)
		exampleButton.enable()
		exampleButton.button!!.isEnabled = false
	}

}
