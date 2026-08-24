package com.nfcalarmclock.settings.cardbuttonlabels

import android.content.Context
import android.content.res.TypedArray
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Preference for selecting whether card button's should have labels or not.
 */
class NacCardButtonLabelsPreference @JvmOverloads constructor(
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
	 * Should show card button labels.
	 */
	private var shouldShowCardButtonLabels: Boolean = true

	/**
	 * Example button.
	 */
	private lateinit var exampleButton: MaterialButton

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference_card_button_label
		onPreferenceClickListener = this
	}

	/**
	 * Get the summary text to use for the preference.
	 *
	 * @return The summary text to use for the preference.
	 */
	override fun getSummary(): CharSequence
	{
		return if (shouldShowCardButtonLabels)
		{
			context.getString(R.string.description_should_show_card_button_labels_true)
		}
		else
		{
			context.getString(R.string.description_should_show_card_button_labels_false)
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
		exampleButton = holder.findViewById(R.id.widget) as MaterialButton

		// Setup the view
		setupExampleCardButton()
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		// Default value
		val def = context.resources.getBoolean(R.bool.default_style_should_show_card_button_labels)

		// Get the value
		return a.getBoolean(index, def)
	}

	/**
	 * Allow users to select the whole preference to change the state.
	 */
	override fun onPreferenceClick(pref: Preference): Boolean
	{
		// Get the current preference
		val shouldShow = sharedPreferences.shouldShowCardButtonLabels

		// Toggle the preference
		shouldShowCardButtonLabels = !shouldShow

		// Persist the value
		persistBoolean(shouldShowCardButtonLabels)

		// Notify of a change
		notifyChanged()

		// Call listener
		callChangeListener(shouldShowCardButtonLabels)

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
			shouldShowCardButtonLabels = getPersistedBoolean(shouldShowCardButtonLabels)
		}
		// Convert the default value
		else
		{
			shouldShowCardButtonLabels = defaultValue as Boolean

			persistBoolean(shouldShowCardButtonLabels)
		}
	}

	/**
	 * Setup the example card button.
	 */
	private fun setupExampleCardButton()
	{
		exampleButton.text = if (shouldShowCardButtonLabels)
		{
			context.resources.getString(R.string.action_alarm_snooze)
		}
		else
		{
			""
		}
	}

}