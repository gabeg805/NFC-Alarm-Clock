package com.nfcalarmclock.alarm.options

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
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
	 * Layout resource ID.
	 */
	abstract val layoutId: Int

	/**
	 * Setup all alarm options.
	 */
	abstract fun setupAlarmOptions(alarm: NacAlarm?)

	/**
	 * Setup any extra buttons.
	 */
	open fun setupExtraButtons(alarm: NacAlarm?)
	{
	}

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
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the bundle
		val alarm = arguments?.getAlarm()

		// Get the ok and cancel buttons
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Setup any alarm options
		setupAlarmOptions(alarm)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Call the ok clicked function
			onOkClicked(alarm)

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton, listener = {

			// Call the cancel clicked function
			onCancelClicked(alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup any extra buttons
		setupExtraButtons(alarm)
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

}