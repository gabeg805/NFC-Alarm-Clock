package com.nfcalarmclock.alarm.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.LinearLayout
import androidx.core.widget.NestedScrollView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.getAlarm
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupInputLayoutColor


/**
 * Generic alarm options dialog.
 */
abstract class NacGenericAlarmOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Interface to use to save the alarm, if the NavController is not used.
	 */
	fun interface OnSaveAlarmListener
	{
		fun onSaveAlarm(alarm: NacAlarm)
	}

	/**
	 * Layout resource ID.
	 */
	abstract val layoutId: Int

	/**
	 * Save alarm listener. Should be used if the NavController is not used.
	 */
	var onSaveAlarmListener: OnSaveAlarmListener? = null

	/**
	 * Setup all alarm options.
	 */
	abstract fun setupAlarmOptions(alarm: NacAlarm?)

	/**
	 * Called when the Ok button is clicked.
	 */
	abstract fun onOkClicked(alarm: NacAlarm?)

	/**
	 * Called when the cancel button is clicked.
	 */
	open fun onCancelClicked(alarm: NacAlarm?)
	{
	}

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(layoutId, container, false)
	}

	/**
	 * Called when the primary button is clicked.
	 */
	open fun onPrimaryButtonClicked(alarm: NacAlarm?)
	{
		try
		{
			// Call the ok clicked function
			onOkClicked(alarm)
		}
		catch (_: IllegalStateException)
		{
			// Stop on exception
			return
		}

		// Save the alarm
		onSaveAlarm(alarm)

		// Dismiss the dialog
		dismiss()
	}

	/**
	 * Called when the alarm should be saved.
	 */
	open fun onSaveAlarm(alarm: NacAlarm?)
	{
		// Save the change so that it is accessible in the previous dialog
		findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)
	}

	/**
	 * Called when the secondary button is clicked.
	 */
	open fun onSecondaryButtonClicked(alarm: NacAlarm?)
	{
		try
		{
			// Call the cancel clicked function
			onCancelClicked(alarm)
		}
		catch (_: IllegalStateException)
		{
			// Stop on exception
			return
		}

		// Dismiss the dialog
		dismiss()
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the bundle
		val alarm = arguments?.getAlarm()

		// Setup any alarm options
		// TODO: Can this be moved after setting up the views below?
		setupAlarmOptions(alarm)

		// Setup the views
		setupOkButton(alarm)
		setupCancelButton(alarm)
		setupExtraButtons(alarm)
		setupScrollView(alarm)
	}

	/**
	 * Setup the Cancel button.
	 */
	open fun setupCancelButton(alarm: NacAlarm?)
	{
		// Get the cancel button
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Setup the cancel button
		setupSecondaryButton(cancelButton, listener = { onSecondaryButtonClicked(alarm) })
	}

	/**
	 * Setup any extra buttons.
	 */
	open fun setupExtraButtons(alarm: NacAlarm?)
	{
	}

	/**
	 * Setup the minutes and second dropdown views of an option.
	 */
	fun setupMinutesAndSecondsOption(
		minutesInputLayout: TextInputLayout,
		secondsInputLayout: TextInputLayout,
		minutesAutoCompleteTextView: MaterialAutoCompleteTextView,
		secondsAutoCompleteTextView: MaterialAutoCompleteTextView,
		startIndices: Pair<Int, Int>,
		onTimeChanged: (minIndex: Int, secIndex: Int) -> Unit
	)
	{
		// Get the list of seconds, without 60 seconds. Only need 0 to 59 seconds
		val seconds = resources.getStringArray(R.array.general_seconds_summaries).dropLast(1).toTypedArray()

		// Get the index of the default selected items in the textviews
		var (minutesIndex, secondsIndex) = startIndices

		// Setup the input layout
		minutesInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		secondsInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)
		minutesAutoCompleteTextView.setTextFromIndex(minutesIndex)
		secondsAutoCompleteTextView.setTextFromIndex(secondsIndex)
		secondsAutoCompleteTextView.setSimpleItems(seconds)

		// Set the minutes input layout end icon click listener
		minutesInputLayout.setEndIconOnClickListener {
			minutesAutoCompleteTextView.showDropDown()
			minutesAutoCompleteTextView.listSelection = minutesIndex
		}

		// Set the seconds input layout end icon click listener
		secondsInputLayout.setEndIconOnClickListener {
			secondsAutoCompleteTextView.showDropDown()
			secondsAutoCompleteTextView.listSelection = secondsIndex
		}

		// Set the minutes textview click listener
		minutesAutoCompleteTextView.setOnClickListener {
			minutesAutoCompleteTextView.listSelection = minutesIndex
		}

		// Set the seconds textview click listener
		secondsAutoCompleteTextView.setOnClickListener {
			secondsAutoCompleteTextView.listSelection = secondsIndex
		}

		// Set the minutes textview listener
		minutesAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the current index
			minutesIndex = position

			// Change the seconds dropdown if minutes are set to 0
			if ((minutesIndex == 0) && (secondsIndex == 0))
			{
				secondsIndex = 1
				secondsAutoCompleteTextView.setTextFromIndex(secondsIndex)
			}

			// Call the callback when the time is changed
			onTimeChanged(minutesIndex, secondsIndex)

		}

		// Set the seconds textview listener
		secondsAutoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->

			// Set the current index
			secondsIndex = position

			// Change the minutes dropdown if seconds are set to 0
			if ((minutesIndex == 0) && (secondsIndex == 0))
			{
				minutesIndex = 1
				minutesAutoCompleteTextView.setTextFromIndex(minutesIndex)
			}

			// Call the callback when the time is changed
			onTimeChanged(minutesIndex, secondsIndex)

		}
	}

	/**
	 * Setup the OK button.
	 */
	open fun setupOkButton(alarm: NacAlarm?)
	{
		// Get the ok button
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = { onPrimaryButtonClicked(alarm) })
	}

	/**
	 * Setup the scroll view.
	 */
	open fun setupScrollView(alarm: NacAlarm?)
	{
		// Get the views
		val container: LinearLayout = dialog!!.findViewById(R.id.alarm_option_container)
		val scrollView: NestedScrollView = dialog!!.findViewById(R.id.options_scroll_view)
		val nbuttons = container.childCount - 1

		// Set the max height that the scroll view can take up
		setupScrollableViewHeight(scrollView, 85, nbuttons = nbuttons)
	}

}