package com.nfcalarmclock.dismissearly

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.NumberPicker
import android.widget.RelativeLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import com.google.android.material.checkbox.MaterialCheckBox
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.dismissearly.NacDismissEarlyDialog.OnDismissEarlyOptionSelectedListener
import com.nfcalarmclock.view.dialog.NacDialogFragment
import com.nfcalarmclock.view.setupCheckBoxColor

/**
 * Dismiss early dialog.
 */
class NacDismissEarlyDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when a dismiss early option is selected.
	 */
	fun interface OnDismissEarlyOptionSelectedListener
	{
		fun onDismissEarlyOptionSelected(useDismissEarly: Boolean, dismissEarlyTime: Int)
	}

	/**
	 * Default dismiss early.
	 */
	var defaultShouldDismissEarly = false

	/**
	 * Default dismiss early time.
	 */
	var defaultDismissEarlyTime = 0

	/**
	 * Check box to dismiss early or not.
	 */
	private lateinit var checkBox: MaterialCheckBox

	/**
	 * Title above the dismiss early time picker.
	 */
	private lateinit var pickerTitle: TextView

	/**
	 * Scrollable picker to choose the dismiss early time.
	 */
	private lateinit var picker: NumberPicker

	/**
	 * Listener for when the dismiss early option is clicked.
	 */
	var onDismissEarlyOptionSelectedListener: OnDismissEarlyOptionSelectedListener? = null

	/**
	 * Whether an alarm should be able to be dismissed early or not.
	 */
	private val shouldDismissEarly: Boolean
		get() = checkBox.isChecked

	/**
	 * The alpha that views should have based on the should use text-to-speech
	 * flag.
	 */
	private val alpha: Float
		get() = if (shouldDismissEarly) 1.0f else 0.25f

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

				// Get the time value
				val dismissEarlyTime = NacAlarm.calcDismissEarlyTime(picker.value)

				// Call the listener
				onDismissEarlyOptionSelectedListener?.onDismissEarlyOptionSelected(
					shouldDismissEarly, dismissEarlyTime)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_alarm_dismiss_early)
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
		val container = dialog!!.findViewById<RelativeLayout>(R.id.should_use_dismiss_early)
		val textView = dialog!!.findViewById<TextView>(R.id.should_use_dismiss_early_summary)

		checkBox = dialog!!.findViewById(R.id.should_use_dismiss_early_checkbox)
		pickerTitle = dialog!!.findViewById(R.id.title_how_early_to_dismiss)
		picker = dialog!!.findViewById(R.id.dismiss_early_time_picker)

		// Set the default values
		checkBox.isChecked = defaultShouldDismissEarly

		// Setup the views
		setupCheckBoxColor(checkBox, sharedPreferences!!)
		setupTextView(textView)
		setupTimePickerUsable()

		// Setup the listener
		container.setOnClickListener {

			// Toggle the checkbox
			checkBox.isChecked = !shouldDismissEarly

			// Setup the views
			setupTextView(textView)
			setupTimePickerUsable()

		}

		// Get the scroll picker values
		val values = requireContext().resources.getStringArray(R.array.dismiss_early_times).toList()

		// Setup the scroll picker
		picker.minValue = 0
		picker.maxValue = values.size - 1
		picker.displayedValues = values.toTypedArray()
		picker.value = NacAlarm.calcDismissEarlyIndex(defaultDismissEarlyTime)
	}

	/**
	 * Setup the summary text for whether a user should be able to dismiss an
	 * alarm early or not.
	 */
	private fun setupTextView(textView: TextView)
	{
		// Determine the text ID to use based on whether restrict volume will
		// be used or not
		val textId = if (shouldDismissEarly)
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
	 * Setup whether the dismiss early time container can be used or not.
	 */
	private fun setupTimePickerUsable()
	{
		// Set the alpha
		pickerTitle.alpha = alpha
		picker.alpha = alpha

		// Set whether it can be used or not
		picker.isEnabled = shouldDismissEarly
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacDismissEarlyDialog"

		/**
		 * Show the dialog.
		 */
		fun show(
			manager: FragmentManager,
			shouldDismissEarly: Boolean,
			dismissEarlyTime: Int,
			listener: (Boolean, Int) -> Unit = { _, _ -> })
		{
			// Create the dialog
			val dialog = NacDismissEarlyDialog()

			// Set the default values
			dialog.defaultShouldDismissEarly = shouldDismissEarly
			dialog.defaultDismissEarlyTime = dismissEarlyTime

			// Setup the listener
			dialog.onDismissEarlyOptionSelectedListener = OnDismissEarlyOptionSelectedListener { useDismissEarly, dismissEarlyTime ->
					listener(useDismissEarly, dismissEarlyTime)
			}

			// Show the dialog
			dialog.show(manager, TAG)
		}

	}

}