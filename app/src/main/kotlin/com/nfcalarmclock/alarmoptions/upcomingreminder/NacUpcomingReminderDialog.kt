package com.nfcalarmclock.alarmoptions.upcomingreminder

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
 * Upcoming reminder dialog.
 */
class NacUpcomingReminderDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Check box to show the reminder or not.
	 */
	private lateinit var showReminderCheckBox: MaterialCheckBox

	/**
	 * Question above how early to show the reminder.
	 */
	private lateinit var howEarlyQuestion: TextView

	/**
	 * Input layout for how early to show the reminder.
	 */
	private lateinit var howEarlyInputLayout: TextInputLayout

	/**
	 * Question above how frequently to show the reminder.
	 */
	private lateinit var howFreqQuestion: TextView

	/**
	 * Input layout for how frequently to show the reminder.
	 */
	private lateinit var howFreqInputLayout: TextInputLayout

	/**
	 * Parent layout for if if text-to-speech should be used when showing the reminder.
	 */
	private lateinit var useTtsRelativeLayout: RelativeLayout

	/**
	 * Question above the checkbox if text-to-speech should be used when
	 * showing the reminder.
	 */
	private lateinit var useTtsQuestion: TextView

	/**
	 * Check box whether to use text-to-speech when the reminder is shown or not.
	 */
	private lateinit var useTtsCheckBox: MaterialCheckBox

	/**
	 * Selected how early time.
	 */
	private var selectedHowEarlyTime: Int = 0

	/**
	 * Selected how frequent time.
	 */
	private var selectedHowFreqTime: Int = 0

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (showReminderCheckBox.isChecked) 1.0f else 0.2f

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_upcoming_reminder, container, false)
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
		useTtsRelativeLayout = dialog!!.findViewById(R.id.should_use_tts_with_reminder)
		useTtsQuestion = dialog!!.findViewById(R.id.title_should_use_tts_with_reminder)

		// Get the default values
		val defaultShouldShowReminder = alarm?.showReminder ?: false
		val defaultTimeToShowReminder = alarm?.timeToShowReminder ?: 5
		val defaultReminderFrequency = alarm?.reminderFrequency ?: 0
		val defaultShouldUseTts = alarm?.shouldUseTtsForReminder ?: false
		selectedHowEarlyTime = defaultTimeToShowReminder
		selectedHowFreqTime = defaultReminderFrequency

		// Setup the views
		setupShouldShowReminder(defaultShouldShowReminder)
		setupHowEarly(defaultTimeToShowReminder)
		setupHowFreq(defaultReminderFrequency)
		setupShouldUseTts(defaultShouldUseTts)
		setupHowEarlyUsable()
		setupHowFreqUsable()
		setupUseTtsUsable()

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Update the alarm attributes
			alarm?.showReminder = showReminderCheckBox.isChecked
			alarm?.timeToShowReminder = selectedHowEarlyTime
			alarm?.reminderFrequency = selectedHowFreqTime
			alarm?.useTtsForReminder = useTtsCheckBox.isChecked

			// Save the change so that it is accessible in the previous dialog
			findNavController().previousBackStackEntry?.savedStateHandle?.set("YOYOYO", alarm)

			// Dismiss the dialog
			dismiss()

		})

		// Setup the cancel button
		setupSecondaryButton(cancelButton)
	}

	/**
	 * Setup how early the upcoming reminder should be shown.
	 */
	private fun setupHowEarly(default: Int)
	{
		// Get the views
		howEarlyQuestion = dialog!!.findViewById(R.id.title_how_early_to_show_reminder)
		howEarlyInputLayout = dialog!!.findViewById(R.id.how_early_to_show_reminder_input_layout)
		val autoCompleteTextView = dialog!!.findViewById(R.id.how_early_to_show_reminder_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layout
		howEarlyInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		val index = NacAlarm.calcUpcomingReminderTimeToShowIndex(default)
		autoCompleteTextView.setTextFromIndex(index)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedHowEarlyTime = NacAlarm.calcUpcomingReminderTimeToShow(position)
		}
	}

	/**
	 * Setup whether the how early time container can be used or not.
	 */
	private fun setupHowEarlyUsable()
	{
		// Set the alpha
		howEarlyQuestion.alpha = alpha
		howEarlyInputLayout.alpha = alpha

		// Set whether it can be used or not
		howEarlyInputLayout.isEnabled = showReminderCheckBox.isChecked
	}

	/**
	 * Setup how frequent the upcoming reminder should be shown.
	 */
	private fun setupHowFreq(default: Int)
	{
		// Get the views
		howFreqQuestion = dialog!!.findViewById(R.id.title_how_freq_to_show_reminder)
		howFreqInputLayout = dialog!!.findViewById(R.id.how_freq_to_show_reminder_input_layout)
		val autoCompleteTextView = dialog!!.findViewById(R.id.how_freq_to_show_reminder_dropdown_menu) as MaterialAutoCompleteTextView

		// Setup the input layout
		howFreqInputLayout.setupInputLayoutColor(requireContext(), sharedPreferences)

		// Set the default selected items in the text views
		autoCompleteTextView.setTextFromIndex(default)

		// Set the textview listeners
		autoCompleteTextView.onItemClickListener = AdapterView.OnItemClickListener { _, _, position, _ ->
			selectedHowFreqTime = position
		}
	}

	/**
	 * Setup whether the how frequent time container can be used or not.
	 */
	private fun setupHowFreqUsable()
	{
		// Set the alpha
		howFreqQuestion.alpha = alpha
		howFreqInputLayout.alpha = alpha

		// Set whether it can be used or not
		howFreqInputLayout.isEnabled = showReminderCheckBox.isChecked
	}

	/**
	 * Setup the whether an upcoming reminder should be shown or not.
	 */
	private fun setupShouldShowReminder(default: Boolean)
	{
		// Get the views
		val relativeLayout = dialog!!.findViewById(R.id.should_show_reminder) as RelativeLayout
		val textView = dialog!!.findViewById(R.id.should_show_reminder_summary) as TextView
		showReminderCheckBox = dialog!!.findViewById(R.id.should_show_reminder_checkbox)

		// Set the status of the checkbox
		showReminderCheckBox.isChecked = default

		// Setup the checkbox
		showReminderCheckBox.setupCheckBoxColor(sharedPreferences)

		// Setup the description
		setupShouldShowReminderDescription(textView)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			showReminderCheckBox.isChecked = !showReminderCheckBox.isChecked

			// Setup the views
			setupShouldShowReminderDescription(textView)
			setupHowEarlyUsable()
			setupHowFreqUsable()
			setupUseTtsUsable()

		}
	}

	/**
	 * Setup the summary text for whether an upcoming reminder should be shown
	 * or not.
	 */
	private fun setupShouldShowReminderDescription(textView: TextView)
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (showReminderCheckBox.isChecked)
		{
			R.string.upcoming_reminder_true
		}
		else
		{
			R.string.upcoming_reminder_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup the whether text-to-speech should be used for the upcoming reminder or not.
	 */
	private fun setupShouldUseTts(default: Boolean)
	{
		// Get the views
		val relativeLayout = dialog!!.findViewById(R.id.should_use_tts_with_reminder) as RelativeLayout
		val textView = dialog!!.findViewById(R.id.should_use_tts_with_reminder_summary) as TextView
		useTtsCheckBox = dialog!!.findViewById(R.id.should_use_tts_with_reminder_checkbox)

		// Set the status of the checkbox
		useTtsCheckBox.isChecked = default

		// Setup the checkbox
		useTtsCheckBox.setupCheckBoxColor(sharedPreferences)

		// Setup the description
		setupShouldUseTtsDescription(textView)

		// Set the listener
		relativeLayout.setOnClickListener {

			// Toggle the checkbox
			useTtsCheckBox.isChecked = !useTtsCheckBox.isChecked

			// Setup the views
			setupShouldUseTtsDescription(textView)

		}
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used when
	 * showing the upcoming reminder or not.
	 */
	private fun setupShouldUseTtsDescription(textView: TextView)
	{
		// Determine the text ID to use
		val textId = if (useTtsCheckBox.isChecked)
		{
			R.string.upcoming_reminder_use_tts_true
		}
		else
		{
			R.string.upcoming_reminder_use_tts_false
		}

		// Set the text
		textView.setText(textId)
	}

	/**
	 * Setup whether the use text-to-speech container can be used or not.
	 */
	private fun setupUseTtsUsable()
	{
		// Set the alpha
		useTtsQuestion.alpha = alpha
		useTtsRelativeLayout.alpha = alpha

		// Set whether it can be used or not
		useTtsCheckBox.isEnabled = showReminderCheckBox.isChecked
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacUpcomingReminderDialog"

	}

}