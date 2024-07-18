package com.nfcalarmclock.upcomingreminder

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.snoozeoptions.NacSnoozeOptionsDialog
import com.nfcalarmclock.view.dialog.NacDialogFragment
import com.nfcalarmclock.view.setupCheckBoxColor
import com.nfcalarmclock.view.setupDialogScrollViewHeight

/**
 * Upcoming reminder dialog.
 */
class NacUpcomingReminderDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when an upcoming reminder option is selected.
	 */
	fun interface OnUpcomingReminderOptionSelectedListener
	{
		fun onUpcomingReminderOptionSelected(
			shouldShowReminder: Boolean,
			timeToShow: Int,
			reminderFreq: Int,
			shouldUseTts: Boolean)
	}

	/**
	 * Default should show reminder.
	 */
	var defaultShouldShowReminder = false

	/**
	 * Default time when to show the reminder.
	 */
	var defaultTimeToShowReminder = 0

	/**
	 * Default frequency at which to show the reminder.
	 */
	var defaultReminderFrequency = 0

	/**
	 * Default whether to use text-to-speech.
	 */
	var defaultShouldUseTts = false

	/**
	 * Whether text-to-speech should be shown or not.
	 */
	var canShowTts = false

	/**
	 * Check box to show the reminder or not.
	 */
	private lateinit var showReminderCheckBox: MaterialCheckBox

	/**
	 * Text view showing the text of the show reminder check box.
	 */
	private lateinit var showReminderTextView: TextView

	/**
	 * Title above the time picker for when to show the reminder.
	 */
	private lateinit var howEarlyTitle: TextView

	/**
	 * Scrollable picker to choose the time when the reminder should be shown.
	 */
	private lateinit var howEarlyPicker: NumberPicker

	/**
	 * Title above the time picker for how frequently to show the reminder.
	 */
	private lateinit var howFreqTitle: TextView

	/**
	 * Scrollable picker to choose how frequently to show the reminder.
	 */
	private lateinit var howFreqPicker: NumberPicker

	/**
	 * Title above the checkbox for if text-to-speech should be used when
	 * showing the reminder.
	 */
	private lateinit var useTtsTitle: TextView

	/**
	 * Check box whether to use text-to-speech when the reminder is shown or not.
	 */
	private lateinit var useTtsCheckBox: MaterialCheckBox

	/**
	 * Text view showing the text of the use text-to-speech check box.
	 */
	private lateinit var useTtsTextView: TextView

	/**
	 * Listener for when the dismiss early option is clicked.
	 */
	var onUpcomingReminderOptionSelectedListener: OnUpcomingReminderOptionSelectedListener? = null

	/**
	 * Whether to show the reminder or not.
	 */
	private val shouldShowReminder: Boolean
		get() = showReminderCheckBox.isChecked

	/**
	 * Whether to use text-to-speech or not.
	 */
	private val shouldUseTts: Boolean
		get() = useTtsCheckBox.isChecked

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (shouldShowReminder) 1.0f else 0.25f

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Get the time of when to show the reminder
				val howEarlyTime = NacAlarm.calcUpcomingReminderTimeToShow(howEarlyPicker.value)

				// Call the listener
				onUpcomingReminderOptionSelectedListener?.onUpcomingReminderOptionSelected(
					shouldShowReminder, howEarlyTime, howFreqPicker.value, shouldUseTts)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_alarm_upcoming_reminder)
			.create()
	}

	/**
	 * Called when the fragment is resumed.
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the views
		val scrollView = dialog!!.findViewById<ScrollView>(R.id.upcoming_reminder_scrollview)
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_show_reminder)
		val otherContainer = dialog!!.findViewById<RelativeLayout>(R.id.should_use_tts_with_reminder)

		showReminderCheckBox = dialog!!.findViewById(R.id.should_show_reminder_checkbox)
		showReminderTextView = dialog!!.findViewById(R.id.should_show_reminder_summary)
		howEarlyTitle = dialog!!.findViewById(R.id.title_how_early_to_show_reminder)
		howEarlyPicker = dialog!!.findViewById(R.id.time_to_show_reminder_picker)
		howFreqTitle = dialog!!.findViewById(R.id.title_how_freq_to_show_reminder)
		howFreqPicker = dialog!!.findViewById(R.id.freq_to_show_reminder_picker)
		useTtsTitle = dialog!!.findViewById(R.id.title_should_use_tts_with_reminder)
		useTtsCheckBox = dialog!!.findViewById(R.id.should_use_tts_with_reminder_checkbox)
		useTtsTextView = dialog!!.findViewById(R.id.should_use_tts_with_reminder_summary)

		// Set default values
		showReminderCheckBox.isChecked = defaultShouldShowReminder
		useTtsCheckBox.isChecked = defaultShouldUseTts

		// Setup
		setupCheckBoxColor(showReminderCheckBox, sharedPreferences!!)
		setupCheckBoxColor(useTtsCheckBox, sharedPreferences!!)
		setupShowReminderSummary()
		setupUseTtsSummary()
		setupHowEarlyPickerUsable()
		setupHowFrequentPickerUsable()
		setupUseTtsUsable(otherContainer)

		// Show reminder listener
		container.setOnClickListener {

			// Toggle the checkbox
			showReminderCheckBox.isChecked = !shouldShowReminder

			// Setup the views
			setupShowReminderSummary()
			setupHowEarlyPickerUsable()
			setupHowFrequentPickerUsable()
			setupUseTtsUsable(otherContainer)

		}

		// Text to speech listener
		otherContainer.setOnClickListener {

			// Toggle the checkbox
			useTtsCheckBox.isChecked = !shouldUseTts

			// Setup the views
			setupUseTtsSummary()

		}

		// Get the how early and frequent times
		val howEarlyValues = requireContext().resources.getStringArray(R.array.upcoming_reminder_times_to_show).toList()
		val howFreqValues = requireContext().resources.getStringArray(R.array.upcoming_reminder_frequency).toList()

		// How early time picker
		howEarlyPicker.minValue = 0
		howEarlyPicker.maxValue = howEarlyValues.size - 1
		howEarlyPicker.displayedValues = howEarlyValues.toTypedArray()
		howEarlyPicker.value = NacAlarm.calcUpcomingReminderTimeToShowIndex(defaultTimeToShowReminder)

		// How frequent time picker
		howFreqPicker.minValue = 0
		howFreqPicker.maxValue = howFreqValues.size - 1
		howFreqPicker.displayedValues = howFreqValues.toTypedArray()
		howFreqPicker.value = defaultReminderFrequency

		// Scroll view height
		setupDialogScrollViewHeight(scrollView, resources)
	}

	/**
	 * Setup the summary text for whether an upcoming reminder should be shown
	 * or not.
	 */
	private fun setupShowReminderSummary()
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (shouldShowReminder)
		{
			R.string.upcoming_reminder_true
		}
		else
		{
			R.string.upcoming_reminder_false
		}

		// Set the text
		showReminderTextView.setText(textId)
	}

	/**
	 * Setup whether the how early time container can be used or not.
	 */
	private fun setupHowEarlyPickerUsable()
	{
		// Set the alpha
		howEarlyTitle.alpha = alpha
		howEarlyPicker.alpha = alpha

		// Set whether it can be used or not
		howEarlyPicker.isEnabled = shouldShowReminder
	}

	/**
	 * Setup whether the how frequent time container can be used or not.
	 */
	private fun setupHowFrequentPickerUsable()
	{
		// Set the alpha
		howFreqTitle.alpha = alpha
		howFreqPicker.alpha = alpha

		// Set whether it can be used or not
		howFreqPicker.isEnabled = shouldShowReminder
	}

	/**
	 * Setup the summary text for whether text-to-speech should be used when
	 * showing the upcoming reminder or not.
	 */
	private fun setupUseTtsSummary()
	{
		// Determine the text ID to use
		val textId = if (shouldUseTts)
		{
			R.string.upcoming_reminder_use_tts_true
		}
		else
		{
			R.string.upcoming_reminder_use_tts_false
		}

		// Set the text
		useTtsTextView.setText(textId)
	}

	/**
	 * Setup whether the use text-to-speech container can be used or not.
	 */
	private fun setupUseTtsUsable(container: RelativeLayout)
	{
		// Set the alpha
		useTtsTitle.alpha = alpha
		container.alpha = alpha

		// Set whether it can be used or not
		useTtsCheckBox.isEnabled = shouldShowReminder
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacUpcomingReminderDialog"

		/**
		 * Show the dialog.
		 */
		fun show(
			manager: FragmentManager,
			shouldShowReminder: Boolean,
			timeToShow: Int,
			reminderFreq: Int,
			shouldUseTts: Boolean,
			canShowTts: Boolean,
			listener: (Boolean, Int, Int, Boolean) -> Unit = { _, _, _, _ -> })
		{
			// Create the dialog
			val dialog = NacUpcomingReminderDialog()

			// Set the default values
			dialog.defaultShouldShowReminder = shouldShowReminder
			dialog.defaultTimeToShowReminder = timeToShow
			dialog.defaultReminderFrequency = reminderFreq
			dialog.defaultShouldUseTts = shouldUseTts
			dialog.canShowTts = canShowTts

			// Setup the listener
			dialog.onUpcomingReminderOptionSelectedListener = OnUpcomingReminderOptionSelectedListener { shouldShowReminder, timeToShow, reminderFreq, shouldUseTts ->
				listener(shouldShowReminder, timeToShow, reminderFreq,shouldUseTts)
			}

			// Show the dialog
			dialog.show(manager, NacSnoozeOptionsDialog.TAG)
		}

	}

}