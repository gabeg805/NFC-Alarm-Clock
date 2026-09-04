package com.nfcalarmclock.alarm.options.nextalarmformat

import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacRadioButtonPromptDialog

/**
 * Dialog for selecting which format they want to use when showing when the next alarm
 * will run.
 */
class NacNextAlarmFormatDialog
	: NacRadioButtonPromptDialog()
{

	/**
	 * Title string resource ID.
	 */
	override val titleId: Int = R.string.title_select_next_alarm_format

	/**
	 * Description string resource ID.
	 */
	override val descriptionId: Int = R.string.description_next_alarm_format

	/**
	 * String array containing the text of each radio button.
	 */
	override val array: Array<String> by lazy {
		arrayOf(
			getString(R.string.description_next_alarm_format_time_in),
			getString(R.string.description_next_alarm_format_time_on)
		)
	}

	/**
	 * Listener for when a next alarm format is selected.
	 */
	fun interface OnNextAlarmFormatSelectedListener
	{
		fun onNextAlarmFormatSelected(which: Int)
	}

	/**
	 * Listener for when an audio option is clicked.
	 */
	var onNextAlarmFormatListener: OnNextAlarmFormatSelectedListener? = null

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm)
	{
		// Super
		super.onOkClicked(alarm)

		// Call the listener
		onNextAlarmFormatListener?.onNextAlarmFormatSelected(currentlySelectedIndex)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacNextAlarmFormatDialog"

	}

}