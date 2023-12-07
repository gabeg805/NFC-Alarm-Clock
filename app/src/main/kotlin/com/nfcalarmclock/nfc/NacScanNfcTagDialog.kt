package com.nfcalarmclock.nfc

import android.app.AlertDialog
import android.app.Dialog
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.FLAG_READER_NFC_A
import android.nfc.NfcAdapter.FLAG_READER_NFC_B
import android.nfc.NfcAdapter.FLAG_READER_NFC_BARCODE
import android.nfc.NfcAdapter.FLAG_READER_NFC_F
import android.nfc.NfcAdapter.FLAG_READER_NFC_V
import android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
import android.nfc.Tag
import android.os.Bundle
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Prompt user to scan an NFC tag that will be used to dismiss the given alarm
 * when it goes off.
 */
class NacScanNfcTagDialog

	// Constructor
	: NacDialogFragment(),

	// Interface
	NfcAdapter.ReaderCallback
{

	/**
	 * Listener for using any NFC tag.
	 */
	interface OnScanNfcTagListener
	{
		fun onCancelNfcTagScan(alarm: NacAlarm)
		fun onDoneScanningNfcTag(alarm: NacAlarm)
		fun onNfcTagScanned(alarm: NacAlarm, tagId: String)
		fun onUseAnyNfcTag(alarm: NacAlarm)
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

	/**
	 * Called when the fragment is no longer in use.
	 */
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		// Call the listener
		onScanNfcTagListener?.onDoneScanningNfcTag(alarm!!)
	}

	/**
	 * Called when the fragment is visible to the user.
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Check if NFC is not on the device or not enabled
		if (!NacNfc.isEnabled(activity))
		{
			return
		}

		// Get the NFC adapter
		val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

		// Get all the NFC tags that can be read
		val flags = FLAG_READER_NFC_A or FLAG_READER_NFC_B or FLAG_READER_NFC_BARCODE or FLAG_READER_NFC_F or FLAG_READER_NFC_V or FLAG_READER_SKIP_NDEF_CHECK

		// Enable NFC reader mode
		nfcAdapter.enableReaderMode(activity, this, flags, null)
	}

	/**
	 * Called when the fragment is no longer started.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Check if NFC is not on the device or not enabled
		if (!NacNfc.isEnabled(activity))
		{
			return
		}

		// Get the NFC adapter
		val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

		// Disable NFC reader modeo
		nfcAdapter.disableReaderMode(activity)
	}

	/**
	 * Called when an NFC tag is discovered.
	 */
	override fun onTagDiscovered(tag: Tag?)
	{
		// Parse the tag ID from the NFC tag
		val tagId = NacNfc.parseId(tag).toString()

		// Call the listener
		onScanNfcTagListener?.onNfcTagScanned(alarm!!, tagId)

		// Dismiss the dialog
		dismiss()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacScanNfcTagDialog"

	}

}