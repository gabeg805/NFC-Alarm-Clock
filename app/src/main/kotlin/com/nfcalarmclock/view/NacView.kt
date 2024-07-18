package com.nfcalarmclock.view

import android.content.res.ColorStateList
import android.content.res.Resources
import android.graphics.Color
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ScrollView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.shared.NacSharedPreferences


/**
 * Setup the color of the check box.
 */
fun setupCheckBoxColor(
	checkBox: MaterialCheckBox,
	sharedPreferences: NacSharedPreferences
)
{
	// Get the colors for the boolean states
	val colors = intArrayOf(sharedPreferences.themeColor, Color.GRAY)

	// Get the IDs of the two states
	val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

	// Set the state list of the checkbox
	checkBox.buttonTintList = ColorStateList(states, colors)
}

/**
 * Setup the height of the scroll view in a dialog.
 *
 */
fun setupDialogScrollViewHeight(scrollView: ScrollView, resources: Resources)
{
	// Set the height of the scroll view
	val height = resources.displayMetrics.heightPixels / 2
	val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)

	// Update the layout parameters
	scrollView.layoutParams = layoutParams
}
