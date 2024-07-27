package com.nfcalarmclock.alarmoptions.dismissoptions

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
 * Dismiss options.
 */
class NacDismissOptionsDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Question above the frequency picker.
	 */
	private lateinit var dismissEarlyQuestion: TextView

	/**
	 * Input layout to select the dismiss early wait time.
	 */
	private lateinit var dismissEarlyInputLayout: TextInputLayout

	/**
	 * Checkbox for whether the alarm can be dismissed early or not.
	 */
	private lateinit var dismissEarlyCheckBox: MaterialCheckBox

	/**
	 * Selected auto dismiss time.
	 */
	private var selectedAutoDismissTime: Int = 0

	/**
	 * Selected dismiss early time.
	 */
	private var selectedDismissEarlyTime: Int = 0

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (dismissEarlyCheckBox.isChecked) 1.0f else 0.2f

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_dismiss_options, container, false)
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
		val okButton = dialog!!.findViewById(R.id.ok_button) as MaterialButton
		val cancelButton = dialog!!.findViewById(R.id.cancel_button) as MaterialButton

		// Get the default values
		val defaultAutoDismissTime = alarm?.autoDismissTime ?: 15
		val defaultShouldDismissEarly = alarm?.useDismissEarly ?: false
		val defaultDismissEarlyTime = alarm?.dismissEarlyTime ?: 30
		selectedAutoDismissTime = defaultAutoDismissTime
		selectedDismissEarlyTime = defaultDismissEarlyTime

		// TODO: FIND HOW TO CENTER ITEMS IN MENU AND THEN USE TEXT ALIGNMENT TO CENTER IN TEXTVIEW
		// TODO: FIND HOW TO SET MAX HEIGHT OF EXPANDED MENU

		// Setup the views
		setupAutoDismiss(defaultAutoDismissTime)
		setupShouldDismissEarly(defaultShouldDismissEarly)
		setupHowEarlyToDismiss(defaultDismissEarlyTime)
		setHowEarlyToDismissUsable()

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.autoDismissTime = selectedAutoDismissTime
			alarm?.useDismissEarly = dismissEarlyCheckBox.isChecked
			alarm?.dismissEarlyTime = selectedDismissEarlyTime

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Set if the how early to dismiss views should be usable or not.
	 */
	private fun setHowEarlyToDismissUsable()
	{
		// Set the alpha for the views
		dismissEarlyQuestion.alpha = alpha
		dismissEarlyInputLayout.alpha = alpha

		// Set whether it can be used or not
		dismissEarlyInputLayout.isEnabled = dismissEarlyCheckBox.isChecked
	}

	/**
	 * Setup the description for what type of snoozing should take place.
	 */
	private fun setShouldDismissEarlyDescription(textView: TextView)
	{
		// Determine the text ID to use based on whether easy snooze will be
		// used or not
		val textId = if (dismissEarlyCheckBox.isChecked)
		{
			R.string.dismiss_early_true
		}
		else
		{
			R.string.dismiss_early_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup the auto dismiss views.
	 */
	private fun setupAutoDismiss(default: Int)
	{
		// Get the views
		val inputLayout = dialog!!.findViewById(R.id.auto_dismiss_input_layout) as TextInputLayout
		val autoCompleteTextView = dialog!!.findViewById(R.id.auto_dismiss_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layout
		inputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcAutoDismissIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedAutoDismissTime = NacAlarm.calcAutoDismissTime(position)
		}
	}

	/**
	 * Setup the how early to dismiss views.
	 */
	private fun setupHowEarlyToDismiss(default: Int)
	{
		// Get the views
		dismissEarlyQuestion = dialog!!.findViewById(R.id.how_early_to_dismiss_question)
		dismissEarlyInputLayout = dialog!!.findViewById(R.id.how_early_to_dismiss_input_layout)
		val autoCompleteTextView = dialog!!.findViewById(R.id.how_early_to_dismiss_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layout
		dismissEarlyInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcDismissEarlyIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedDismissEarlyTime = NacAlarm.calcDismissEarlyTime(position)
		}
	}

	/**
	 * Setup the should dismiss early views.
	 */
	private fun setupShouldDismissEarly(default: Boolean)
	{
		// Get the views
		val relativeLayout = dialog!!.findViewById(R.id.should_use_dismiss_early) as RelativeLayout
		val description = dialog!!.findViewById(R.id.should_use_dismiss_early_summary) as TextView
		dismissEarlyCheckBox = dialog!!.findViewById(R.id.should_use_dismiss_early_checkbox) as MaterialCheckBox

		// Set the default checkbox value
		dismissEarlyCheckBox.isChecked = default

		// Setup the checkbox
		dismissEarlyCheckBox.setupCheckBoxColor(sharedPreferences)

		// Should dismiss early description
		setShouldDismissEarlyDescription(description)

		// Easy snooze listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			dismissEarlyCheckBox.isChecked = !dismissEarlyCheckBox.isChecked

			// Set the description
			setShouldDismissEarlyDescription(description)

			// Set the usability of the how early to dismiss views
			setHowEarlyToDismissUsable()

		}
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSnoozeOptionsDialog"

	}

}