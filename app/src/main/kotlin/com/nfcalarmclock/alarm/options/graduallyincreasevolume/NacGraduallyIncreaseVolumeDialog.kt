package com.nfcalarmclock.alarm.options.graduallyincreasevolume

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.AdapterView
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.google.android.material.checkbox.MaterialCheckBox
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.android.material.textfield.TextInputLayout
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.util.NacBundle
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.setTextFromIndex
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupInputLayoutColor

/**
 * Ask user if they would like to gradually increase the volume when an alarm
 * goes off.
 */
class NacGraduallyIncreaseVolumeDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Checkbox indicating whether to gradually increase volume or not.
	 */
	private lateinit var checkBox: MaterialCheckBox

	/**
	 * Question above the input layout to choose the gradually increase volume wait time.
	 */
	private lateinit var waitTimeQuestion: TextView

	/**
	 * Input layout to select the gradually increase volume wait time.
	 */
	private lateinit var waitTimeInputLayout: TextInputLayout

	/**
	 * Selected gradually increase volume wait time.
	 */
	private var selectedWaitTime: Int = 0

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (checkBox.isChecked) 1.0f else 0.2f

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_gradually_increase_volume, container, false)
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the bundle
		val alarm = NacBundle.getAlarm(arguments)

		// Get the ok and cancel buttons
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)

		// Get the default values
		val defaultShouldGraduallyIncreaseVolume = alarm?.shouldGraduallyIncreaseVolume ?: false
		val defaultGraduallyIncreaseVolumeWaitTime = alarm?.graduallyIncreaseVolumeWaitTime ?: 5
		selectedWaitTime = defaultGraduallyIncreaseVolumeWaitTime

		// Setup the views
		setupShouldGraduallyIncreaseVolume(defaultShouldGraduallyIncreaseVolume)
		setupWaitTimes(defaultGraduallyIncreaseVolumeWaitTime)
		setupWaitTimeUsable()

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.shouldGraduallyIncreaseVolume = checkBox.isChecked
			alarm?.graduallyIncreaseVolumeWaitTime = selectedWaitTime

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Setup the summary text for whether volume should be gradually increased or not.
	 */
	private fun setupDescription(textView: TextView)
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (checkBox.isChecked)
		{
			R.string.gradually_increase_volume_true
		}
		else
		{
			R.string.gradually_increase_volume_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup whether the volume should be gradually increased or not.
	 */
	private fun setupShouldGraduallyIncreaseVolume(default: Boolean)
	{
		// Get the views
		val relativeLayout: RelativeLayout = dialog!!.findViewById(R.id.should_gradually_increase_volume)
		val description: TextView = dialog!!.findViewById(R.id.should_gradually_increase_volume_summary)
		checkBox = dialog!!.findViewById(R.id.should_gradually_increase_volume_checkbox)!!

		// Set the status of the checkbox
		checkBox.isChecked = default

		// Setup the checkbox
		checkBox.setupCheckBoxColor(sharedPreferences)

		// Setup the description
		setupDescription(description)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			checkBox.isChecked = !checkBox.isChecked

			// Set the description
			setupDescription(description)

			// Set the usability of the wait time input layout
			setupWaitTimeUsable()

		}
	}

	/**
	 * Setup whether the gradually increase volume wait time container can be
	 * used or not.
	 */
	private fun setupWaitTimeUsable()
	{
		// Set the alpha for the views
		waitTimeQuestion.alpha = alpha
		waitTimeInputLayout.alpha = alpha

		// Set whether it can be used or not
		waitTimeInputLayout.isEnabled = checkBox.isChecked
	}

	/**
	 * Setup whether the volume should be gradually increased or not.
	 */
	private fun setupWaitTimes(default: Int)
	{
		// Get the views
		waitTimeQuestion = dialog!!.findViewById(R.id.title_gradually_increase_volume_wait_time)
		waitTimeInputLayout = dialog!!.findViewById(R.id.gradually_increase_volume_input_layout)
		val autoCompleteTextView: MaterialAutoCompleteTextView = dialog!!.findViewById(R.id.gradually_increase_volume_dropdown_menu)

		// Setup the input layout
		waitTimeInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcGraduallyIncreaseVolumeIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedWaitTime = NacAlarm.calcGraduallyIncreaseVolumeWaitTime(position)
		}
	}

}