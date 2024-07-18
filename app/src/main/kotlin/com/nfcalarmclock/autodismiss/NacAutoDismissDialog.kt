package com.nfcalarmclock.autodismiss

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.FragmentManager
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment
import com.nfcalarmclock.view.dialog.NacScrollablePickerDialogFragment.OnScrollablePickerOptionSelectedListener

/**
 * Select an amount of time to auto dismiss an alarm.
 */
class NacAutoDismissDialog
	: NacScrollablePickerDialogFragment()
{

	/**
	 * The list of values for the scrollable picker.
	 */
	override val scrollablePickerValues: List<String>
		get()
		{
			val array = resources.getStringArray(R.array.auto_dismiss_summaries)

			return array.asList()
		}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.auto_dismiss)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Get the current index of thee scrollable picker
				val autoDismissTime = NacAlarm.calcAutoDismissTime(scrollablePicker!!.value)

				// Call the listener
				onScrollablePickerOptionSelectedListener?.onScrollablePickerOptionSelected(
					autoDismissTime)

			}
			.setNegativeButton(R.string.action_cancel, null)
			.setView(R.layout.dlg_scrollable_picker)
			.create()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacAutoDismissDialog"

		/**
		 * Show the dialog.
		 */
		fun show(
			manager: FragmentManager,
			autoDismissTime: Int,
			listener: (Int) -> Unit = { _ -> })
		{
			// Create the dialog
			val dialog = NacAutoDismissDialog()

			// Setup the index
			dialog.defaultScrollablePickerIndex = NacAlarm.calcAutoDismissIndex(autoDismissTime)

			// Setup the listener
			dialog.onScrollablePickerOptionSelectedListener = OnScrollablePickerOptionSelectedListener { index ->

				// Get the value
				val autoDismissTime = NacAlarm.calcAutoDismissTime(index)

				// Call the listener
				listener(autoDismissTime)
			}

			// Show the dialog
			dialog.show(manager, TAG)
		}

	}

}