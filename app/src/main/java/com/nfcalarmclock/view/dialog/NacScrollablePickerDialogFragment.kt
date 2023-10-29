package com.nfcalarmclock.view.dialog

import android.widget.NumberPicker
import com.nfcalarmclock.R

/**
 * Helper class for dialogs that have scrollable pickers.
 */
abstract class NacScrollablePickerDialogFragment
	: NacDialogFragment()
{

	/**
	 * Listener for when the scrollable picker option is selected.
	 */
	fun interface OnScrollablePickerOptionSelectedListener
	{
		fun onScrollablePickerOptionSelected(index: Int)
	}

	/**
	 * The list of values for the scrollable picker.
	 */
	abstract val scrollablePickerValues: List<String>

	/**
	 * Default index for the scrollable picker.
	 */
	var defaultScrollablePickerIndex = 0

	/**
	 * Scrollable picker.
	 */
	private var scrollablePicker: NumberPicker? = null

	/**
	 * Listener for when the scrollable picker option is selected.
	 */
	var onScrollablePickerOptionSelectedListener: OnScrollablePickerOptionSelectedListener? = null

	/**
	 * Call the OnScrollablePickerOptionSelectedListener object, if it has been set.
	 */
	fun callOnScrollablePickerOptionSelectedListener()
	{
		// Get the current index of thee scrollable picker
		val index = scrollablePicker!!.value

		// Call the listener
		onScrollablePickerOptionSelectedListener?.onScrollablePickerOptionSelected(
			index)
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Set the scrollable picker
		scrollablePicker = dialog!!.findViewById(R.id.scrollable_picker)

		// Setup the scrollable picker
		setupScrollablePicker()
	}

	/**
	 * Setup the scrollable picker.
	 */
	private fun setupScrollablePicker()
	{
		// Get the picker values
		val values = scrollablePickerValues

		// Setup the scrollable picker
		scrollablePicker!!.minValue = 0
		scrollablePicker!!.maxValue = values.size - 1
		scrollablePicker!!.displayedValues = values.toTypedArray()
		scrollablePicker!!.value = defaultScrollablePickerIndex
	}

}