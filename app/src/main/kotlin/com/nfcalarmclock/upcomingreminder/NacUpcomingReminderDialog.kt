package com.nfcalarmclock.upcomingreminder

import android.app.AlertDialog
import android.app.Dialog
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

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
	 * Default index of time to show reminder.
	 */
	var defaultTimeToShowReminderIndex = 0

	/**
	 * Default index of frequency at which to show the reminder.
	 */
	var defaultReminderFrequencyIndex = 0

	/**
	 * Default whether to use text-to-speech.
	 */
	var defaultShouldUseTts = false

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
	 * Time of how early to show the reminder.
	 */
	private val howEarlyTime: Int
		get()
		{
			// Get the index value
			val index = howEarlyPicker.value

			// Calculate the time based on the index
			return if (index < 10)
			{
				index + 1
			}
			else
			{
				(index-7) * 5
			}
		}


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

		// Get the container of the dialog and the text view
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_show_reminder)
		val otherContainer = dialog!!.findViewById<RelativeLayout>(R.id.should_use_tts_with_reminder)

		// Set the views
		showReminderCheckBox = dialog!!.findViewById(R.id.should_show_reminder_checkbox)
		showReminderTextView = dialog!!.findViewById(R.id.should_show_reminder_summary)
		howEarlyTitle = dialog!!.findViewById(R.id.title_how_early_to_show_reminder)
		howEarlyPicker = dialog!!.findViewById(R.id.time_to_show_reminder_picker)
		howFreqTitle = dialog!!.findViewById(R.id.title_how_freq_to_show_reminder)
		howFreqPicker = dialog!!.findViewById(R.id.freq_to_show_reminder_picker)
		useTtsTitle = dialog!!.findViewById(R.id.title_should_use_tts_with_reminder)
		useTtsCheckBox = dialog!!.findViewById(R.id.should_use_tts_with_reminder_checkbox)
		useTtsTextView = dialog!!.findViewById(R.id.should_use_tts_with_reminder_summary)

		// Set the status of the checkbox
		showReminderCheckBox.isChecked = defaultShouldShowReminder
		useTtsCheckBox.isChecked = defaultShouldUseTts

		// Setup the views
		setupShowReminderOnClickListener(container, otherContainer)
		setupUseTtsOnClickListener(otherContainer)
		setupCheckBoxColors()
		setupShowReminderSummary()
		setupUseTtsSummary()
		setupHowEarlyPickerValues()
		setupHowEarlyPickerUsable()
		setupHowFrequentPickerValues()
		setupHowFrequentPickerUsable()
		setupUseTtsUsable(otherContainer)
		setupScrollViewHeight()
	}

	/**
	 * Set the default time to show index from a time.
	 */
	fun setDefaultIndexFromTime(time: Int)
	{
		defaultTimeToShowReminderIndex = if (time == 0)
		{
			4
		}
		else if (time <= 10)
		{
			time - 1
		}
		else
		{
			time/5 + 7
		}
	}

	/**
	 * Setup the color of the upcoming reminder check boxes.
	 */
	private fun setupCheckBoxColors()
	{
		// Get the colors for the boolean states
		val colors = intArrayOf(sharedPreferences!!.themeColor, Color.GRAY)

		// Get the IDs of the two states
		val states = arrayOf(intArrayOf(android.R.attr.state_checked), intArrayOf(-android.R.attr.state_checked))

		// Set the state list of the checkbox
		showReminderCheckBox.buttonTintList = ColorStateList(states, colors)
		useTtsCheckBox.buttonTintList = ColorStateList(states, colors)
	}

	/**
	 * Setup the on click listener for when the show reminder container is
	 * clicked.
	 */
	private fun setupShowReminderOnClickListener(container: RelativeLayout, otherContainer: RelativeLayout)
	{
		// Setup the listener
		container.setOnClickListener {

			// Toggle the checkbox
			showReminderCheckBox.isChecked = !shouldShowReminder

			// Setup the views
			setupShowReminderSummary()
			setupHowEarlyPickerUsable()
			setupHowFrequentPickerUsable()
			setupUseTtsUsable(otherContainer)

		}
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
	 * Setup scrollable picker for the how early times.
	 */
	private fun setupHowEarlyPickerValues()
	{
		// Get the dismiss early times
		val values = requireContext().resources.getStringArray(R.array.upcoming_reminder_times_to_show).toList()

		// Setup the time picker
		howEarlyPicker.minValue = 0
		howEarlyPicker.maxValue = values.size - 1
		howEarlyPicker.displayedValues = values.toTypedArray()
		howEarlyPicker.value = defaultTimeToShowReminderIndex
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
	 * Setup scrollable picker for the how frequent times.
	 */
	private fun setupHowFrequentPickerValues()
	{
		// Get the dismiss early times
		val values = requireContext().resources.getStringArray(R.array.upcoming_reminder_frequency).toList()

		// Setup the time picker
		howFreqPicker.minValue = 0
		howFreqPicker.maxValue = values.size - 1
		howFreqPicker.displayedValues = values.toTypedArray()
		howFreqPicker.value = defaultReminderFrequencyIndex
	}

	/**
	 * Setup the height of the scroll view.
	 *
	 */
	private fun setupScrollViewHeight()
	{
		// Get the scroll view
		val scrollView = dialog!!.findViewById<ScrollView>(R.id.upcoming_reminder_scrollview)

		// Set the height of the scroll view
		val height = resources.displayMetrics.heightPixels / 2
		val layoutParams = FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, height)

		scrollView.layoutParams = layoutParams
	}

	/**
	 * Setup the on click listener for when the use text-to-speech container is
	 * clicked.
	 */
	private fun setupUseTtsOnClickListener(container: RelativeLayout)
	{
		// Setup the listener
		container.setOnClickListener {

			// Toggle the checkbox
			useTtsCheckBox.isChecked = !shouldUseTts

			// Setup the views
			setupUseTtsSummary()

		}
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

		// Set the visibility of the whole use text-to-speech section
		val visibility = if (defaultShouldUseTts) View.VISIBLE else View.GONE

		useTtsTitle.visibility = visibility
		container.visibility = visibility
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacUpcomingReminderDialog"

	}

}