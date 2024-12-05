package com.nfcalarmclock.view.colorpicker

import android.content.Context
import android.content.res.TypedArray
import android.graphics.Color
import android.util.AttributeSet
import android.widget.ImageView
import androidx.fragment.app.FragmentManager
import androidx.preference.Preference
import androidx.preference.PreferenceViewHolder
import com.nfcalarmclock.R
import com.nfcalarmclock.view.colorpicker.NacColorPickerDialog.OnColorSelectedListener
import com.nfcalarmclock.view.colorpicker.NacColorPickerDialog.OnDefaultColorSelectedListener
import com.nfcalarmclock.view.setupForegroundColor

/**
 * Preference that allows a user to select a color.
 */
class NacColorPickerPreference @JvmOverloads constructor(

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
	OnColorSelectedListener,
	OnDefaultColorSelectedListener
{

	/**
	 * Color image view.
	 */
	private var exampleColor: ImageView? = null

	/**
	 * Color value.
	 */
	private var colorValue = 0

	/**
	 * Default color value for the object.
	 */
	private var defaultColorValue = 0

	/**
	 * Constructor.
	 */
	init
	{
		layoutResource = R.layout.nac_preference_color
	}

	/**
	 * Called when the view holder is bound.
	 */
	override fun onBindViewHolder(holder: PreferenceViewHolder)
	{
		// Super
		super.onBindViewHolder(holder)

		// Set the member variable
		exampleColor = holder.findViewById(R.id.widget) as ImageView

		// Setup the example color
		exampleColor!!.setupForegroundColor(colorValue)
	}

	/**
	 * Called when the color is selected.
	 */
	override fun onColorSelected(color: Int)
	{
		colorValue = color

		// Setup the example color
		exampleColor!!.setupForegroundColor(colorValue)

		// Persist the value
		persistInt(colorValue)

		// Call the change listeners
		callChangeListener(colorValue)
	}

	/**
	 * Called when the default (neutral) button is clicked.
	 *
	 * This means that the default color should be used.
	 */
	override fun onDefaultColorSelected(dialog: NacColorPickerDialog)
	{
		// Set the color of the color picker, example color, and edit text
		dialog.color = defaultColorValue
	}

	/**
	 * Get the default value.
	 *
	 * @return The default value.
	 */
	override fun onGetDefaultValue(a: TypedArray, index: Int): Any
	{
		defaultColorValue = a.getInteger(index, Color.WHITE)

		return defaultColorValue
	}

	/**
	 * Set the initial preference value.
	 */
	override fun onSetInitialValue(defaultValue: Any?)
	{
		// Check if the default value is null
		if (defaultValue == null)
		{
			colorValue = getPersistedInt(colorValue)
		}
		// Convert the default value
		else
		{
			colorValue = defaultValue as Int

			persistInt(colorValue)
		}
	}

	/**
	 * Show the dialog.
	 */
	fun showDialog(manager: FragmentManager?)
	{
		// Create the dialog
		val dialog = NacColorPickerDialog()

		// Setup the dialog
		dialog.initialColor = colorValue
		dialog.onColorSelectedListener = this
		dialog.onDefaultColorSelectedListener = this

		// Show the dialog
		dialog.show(manager!!, NacColorPickerDialog.TAG)
	}
}