package com.nfcalarmclock.view

import android.content.Context
import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Set the text of from an index.
 */
fun MaterialAutoCompleteTextView.setTextFromIndex(index: Int)
{
	// Get the text
	val text = this.adapter.getItem(index) as String

	// Set the text
	this.setText(text, false)
}

/**
 * Setup the ripple color of a MaterialButton.
 */
fun MaterialButton.setupRippleColor(sharedPreferences: NacSharedPreferences, themeColor: Int = sharedPreferences.themeColor)
{
	// Blend the theme color
	val blendedColor = ColorUtils.blendARGB(themeColor, Color.TRANSPARENT, 0.6f)

	// Set the ripple color
	this.rippleColor = ColorStateList.valueOf(blendedColor)
}

/**
 * Setup the stroke color of a MaterialButton.
 */
fun MaterialButton.setupStrokeColor(sharedPreferences: NacSharedPreferences)
{
	this.strokeColor = ColorStateList.valueOf(sharedPreferences.themeColor)
}

/**
 * Setup the color of the check box.
 */
fun MaterialCheckBox.setupCheckBoxColor(sharedPreferences: NacSharedPreferences)
{
	// Get the colors for the boolean states
	val colors = intArrayOf(sharedPreferences.themeColor, Color.GRAY)

	// Get the IDs of the two states
	val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

	// Set the state list of the checkbox
	this.buttonTintList = ColorStateList(states, colors)
}

/**
 * Get the text of the selected radio button.
 */
fun RadioGroup.getCheckedText(): String
{
	// Get the view ID of the currently selected radio button
	val viewId = this.checkedRadioButtonId

	// Get the radio button
	val radioButton = this.findViewById(viewId) as RadioButton

	// Get the text of the radio button
	return radioButton.text.toString()
}

/**
 * Setup the color of an input layout.
 */
fun TextInputLayout.setupInputLayoutColor(
	context: Context,
	sharedPreferences: NacSharedPreferences
)
{
	this.boxBackgroundColor = ContextCompat.getColor(context, R.color.gray_light)
	this.boxStrokeColor = sharedPreferences.themeColor
	this.setEndIconTintList(ColorStateList.valueOf(sharedPreferences.themeColor))
}

/**
 * Setup the background color of a view.
 */
fun View.setupBackgroundColor(sharedPreferences: NacSharedPreferences)
{
	this.backgroundTintList = ColorStateList.valueOf(sharedPreferences.themeColor)
}

/**
 * Setup the height of the scroll view, with respect to the full height of the screen.
 *
 */
fun ScrollView.setupHeight(resources: Resources, ratio: Float)
{
	// Set the height of the scroll view
	val height = resources.displayMetrics.heightPixels * ratio
	val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height.toInt())

	// Update the layout parameters
	this.layoutParams = layoutParams
}
