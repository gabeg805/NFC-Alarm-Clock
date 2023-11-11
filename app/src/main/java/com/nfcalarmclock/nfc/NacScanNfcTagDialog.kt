package com.nfcalarmclock.nfc

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Prompt user to scan an NFC tag that will be used to dismiss the given alarm
 * when it goes off.
 */
class NacScanNfcTagDialog
	: NacDialogFragment()
{

	/**
	 * Listener for using any NFC tag.
	 */
	interface OnScanNfcTagListener
	{
		fun onUseAnyNfcTag(alarm: NacAlarm)
		fun onCancelNfcTagScan(alarm: NacAlarm)
	}

	/**
	 * Alarm.
	 */
	var alarm: NacAlarm? = null

	/**
	 * Listener for when the name is entered.
	 */
	var onScanNfcTagListener: OnScanNfcTagListener? = null

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_scan_nfc_tag)
			.setPositiveButton(R.string.action_use_any) { _, _ ->

				// Call the listener
				onScanNfcTagListener?.onUseAnyNfcTag(alarm!!)

			}
			.setNegativeButton(R.string.action_cancel) { _, _ ->

				// Call the listener
				onScanNfcTagListener?.onCancelNfcTagScan(alarm!!)

			}
			.setView(R.layout.dlg_scan_nfc_tag)
			.create()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacScanNfcTagDialog"

	}

}