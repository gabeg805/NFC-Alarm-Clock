package com.nfcalarmclock.settings.startweekon

import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacRadioButtonPromptDialog
import com.nfcalarmclock.system.NacCalendar

/**
 * Dialog for selecting which day the week in an expanded alarm card should start start on.
 */
class NacStartWeekOnDialog
	: NacRadioButtonPromptDialog()
{

	/**
	 * Title string resource ID.
	 */
	override val titleId: Int = R.string.title_start_week_on

	/**
	 * Description string resource ID.
	 */
	override val descriptionId: Int = R.string.description_start_week_on

	/**
	 * String array containing the text of each radio button.
	 */
	override val array: Array<String> by lazy {
		NacCalendar.getFullWeekdays().take(2).toTypedArray()
	}

	/**
	 * Listener for when a start week is selected.
	 */
	fun interface OnStartWeekSelectedListener
	{
		fun onStartWeekSelected(which: Int)
	}

	/**
	 * Listener for when an audio option is clicked.
	 */
	var onStartWeekSelectedListener: OnStartWeekSelectedListener? = null

	/**
	 * Called when the Ok button is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm)
	{
		// Super
		super.onOkClicked(alarm)

		// Call the listener
		onStartWeekSelectedListener?.onStartWeekSelected(currentlySelectedIndex)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacStartWeekOnDialog"

	}

}