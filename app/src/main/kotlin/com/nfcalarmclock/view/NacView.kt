package com.nfcalarmclock.view

import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.graphics.PorterDuff
import android.graphics.PorterDuffColorFilter
import android.os.Build
import android.text.Html
import android.text.Spanned
import android.view.HapticFeedbackConstants
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.SeekBar
import androidx.appcompat.widget.SwitchCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.ColorUtils
import androidx.core.graphics.Insets
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updateLayoutParams
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Determine the correct alpha to use, depending on the state.
 */
fun calcAlpha(state: Boolean): Float
{
	return if (state) 1.0f else 0.4f
}

/**
 * Determine contrast color, such as to use for text, based on a color.
 */
fun calcContrastColor(color: Int): Int
{
	// Get RGB components from color
	val r = Color.red(color)
	val g = Color.green(color)
	val b = Color.blue(color)

	// Counting the perceptive luminance - human eye favors green color...
	val luminance = (0.299*r + 0.587*g + 0.114*b) / 255f

	// Determine which contrast color to use
	return if (luminance > 0.5)
	{
		Color.BLACK
	}
	else
	{
		Color.WHITE
	}
}

/**
 * Convert the given string to a spanned string.
 */
fun String.toSpannedString(): Spanned
{
	return Html.fromHtml(this, Html.FROM_HTML_MODE_LEGACY)
}

/**
 * Convert a bold string to a themed bold string.
 */
fun String.toThemedBold(themeColor: Int): String
{
	return this.replace("<b>", "<b><font color='${themeColor}'>")
		.replace("</b>", "</font></b>")
}

/**
 * Get the text of the selected radio button.
 */
fun RadioGroup.getCheckedText(): String
{
	// Get the view ID of the currently selected radio button
	val viewId = this.checkedRadioButtonId

	// Get the radio button
	val radioButton: RadioButton = this.findViewById(viewId)

	// Get the text of the radio button
	return radioButton.text.toString()
}

/**
 * Perform haptic feedback for the View.
 */
fun View.performHapticFeedback()
{
	this.performHapticFeedback(HapticFeedbackConstants.VIRTUAL_KEY)
}

/**
 * Set the text of from an index.
 */
fun MaterialAutoCompleteTextView.setTextFromIndex(index: Int, fallback: Int = 1)
{
	// Create a new index just in case
	var newIndex = index

	// Check if the index is invalid
	if ((index < 0) || (this.adapter.count == 0))
	{
		return
	}
	// Index exceeds number of items in adapter
	else if (index >= this.adapter.count)
	{
		// Check the fallback value
		newIndex = if (fallback >= this.adapter.count)
		{
			// Set to the 0th index
			0
		}
		else
		{
			// Set to the fallback index
			fallback
		}
	}

	// Get the text
	val text = this.adapter.getItem(newIndex) as String

	// Set the text
	this.setText(text, false)
}

/**
 * Setup the background color of a view.
 */
fun View.setupBackgroundColor(sharedPreferences: NacSharedPreferences)
{
	this.backgroundTintList = ColorStateList.valueOf(sharedPreferences.themeColor)
}

/**
 * Setup any views that need changing due to API 35+ edge-to-edge.
 */
fun View.setupEdgeToEdge(callback: (Insets) -> Unit = {})
{
	// Check if API < 35, then edge-to-edge is not enforced and do not need to do
	// anything
	if (Build.VERSION.SDK_INT < Build.VERSION_CODES.VANILLA_ICE_CREAM)
	{
		return
	}

	// Set the inset listener
	ViewCompat.setOnApplyWindowInsetsListener(this) { v, windowInsets ->

		// Get the insets
		val insets = windowInsets.getInsets(WindowInsetsCompat.Type.systemBars())

		// Apply the insets as a margin to the view. This solution sets only the
		// bottom, left, and right dimensions, but can also apply whichever insets
		// are appropriate to the layout. Can also update the view padding if that is
		// more appropriate
		v.updateLayoutParams<ViewGroup.MarginLayoutParams> {
			topMargin = insets.top
			bottomMargin = insets.bottom

			// Call the callback
			callback(insets)
		}

		// Return CONSUMED so that the window insets do not keep passing down to
		// descendant views
		WindowInsetsCompat.CONSUMED
	}
}

/**
 * Setup the foreground color of an ImageView.
 */
fun ImageView.setupForegroundColor(color: Int)
{
	this.colorFilter = PorterDuffColorFilter(color, PorterDuff.Mode.SRC)
}

/**
 * Setup the color of a check box.
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
 * Setup the progress and thumb color of a SeekBar.
 */
fun SeekBar.setupProgressAndThumbColor(sharedPreferences: NacSharedPreferences)
{
	// Get the theme color
	val color = ColorStateList.valueOf(sharedPreferences.themeColor)

	// Set the color
	this.progressTintList = color
	this.thumbTintList = color
}

/**
 * Setup the progress and thumb color of a Slider.
 */
fun Slider.setupProgressAndThumbColor(sharedPreferences: NacSharedPreferences)
{
	// Get the theme color
	val themeColor = sharedPreferences.themeColor

	// Get the contrast color and blend it with the theme
	val contrastColor = calcContrastColor(themeColor)
	val blendedColor = ColorUtils.blendARGB(themeColor, contrastColor, 0.7f)

	// Determine the active and inactive colors
	val activeColor = ColorStateList.valueOf(themeColor)
	val inactiveColor = ColorStateList.valueOf(blendedColor)

	// Set the colors
	this.trackActiveTintList = activeColor
	this.trackInactiveTintList = inactiveColor
	this.thumbTintList = activeColor
	this.tickActiveTintList = inactiveColor
	this.tickInactiveTintList = activeColor
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
 * Setup the theme color of a MaterialButton.
 */
fun MaterialButton.setupThemeColor(sharedPreferences: NacSharedPreferences, themeColor: Int = sharedPreferences.themeColor)
{
	// Setup main color
	this.setBackgroundColor(themeColor)
	this.setupRippleColor(sharedPreferences, themeColor=themeColor)

	// Setup text color
	val contrastColor = calcContrastColor(themeColor)

	this.setTextColor(contrastColor)
}

/**
 * Setup the stroke color of a MaterialButton.
 */
//fun MaterialButton.setupStrokeColor(sharedPreferences: NacSharedPreferences)
//{
//	this.strokeColor = ColorStateList.valueOf(sharedPreferences.themeColor)
//}

/**
 * Setup the background color of a FloatingActionButton.
 */
fun FloatingActionButton.setupThemeColor(sharedPreferences: NacSharedPreferences)
{
	// Get the theme color
	val themeColor = sharedPreferences.themeColor
	val contrastColor = calcContrastColor(themeColor)

	// Set the color
	this.backgroundTintList = ColorStateList.valueOf(themeColor)
	this.imageTintList = ColorStateList.valueOf(contrastColor)
}

/**
 * Setup the color of a switch.
 */
fun SwitchCompat.setupSwitchColor(sharedPreferences: NacSharedPreferences)
{
	// Get the colors of the two states
	val themeDark = ColorUtils.blendARGB(sharedPreferences.themeColor, Color.BLACK, 0.6f)
	val thumbColors = intArrayOf(sharedPreferences.themeColor, Color.GRAY)
	val trackColors = intArrayOf(themeDark, Color.DKGRAY)

	// Get the IDs of the two states
	val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

	// Create the color state lists
	val thumbStateList = ColorStateList(states, thumbColors)
	val trackStateList = ColorStateList(states, trackColors)

	// Set the new thumb color
	this.thumbTintList = thumbStateList

	// Set the new track color
	this.trackTintList = trackStateList
}

/**
 * Setup the color of an input layout.
 */
fun TextInputLayout.setupInputLayoutColor(
	context: Context,
	sharedPreferences: NacSharedPreferences
)
{
	// Get the theme color
	val themeColor = ColorStateList.valueOf(sharedPreferences.themeColor)

	// Set the colors
	this.boxBackgroundColor = ContextCompat.getColor(context, R.color.gray_light)
	this.boxStrokeColor = sharedPreferences.themeColor
	this.hintTextColor = themeColor
	this.setEndIconTintList(themeColor)
}

/**
 * Setup the height of the scroll view, with respect to the full height of the screen.
 *
 */
//fun ScrollView.setupHeight(resources: Resources, ratio: Float)
//{
//	// Set the height of the scroll view
//	val height = resources.displayMetrics.heightPixels * ratio
//	val layoutParams = LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height.toInt())
//
//	// Update the layout parameters
//	this.layoutParams = layoutParams
//}
