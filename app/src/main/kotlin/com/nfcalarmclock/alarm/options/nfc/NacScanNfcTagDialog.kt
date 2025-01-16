package com.nfcalarmclock.alarm.options.nfc

import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.FLAG_READER_NFC_A
import android.nfc.NfcAdapter.FLAG_READER_NFC_B
import android.nfc.NfcAdapter.FLAG_READER_NFC_BARCODE
import android.nfc.NfcAdapter.FLAG_READER_NFC_F
import android.nfc.NfcAdapter.FLAG_READER_NFC_V
import android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
import android.nfc.Tag
import android.view.View
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.observeBackStackEntry
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Scan an NFC tag that will be used to dismiss the given alarm when it goes
 * off.
 */
@AndroidEntryPoint
class NacScanNfcTagDialog

	// Constructor
	: NacGenericAlarmOptionsDialog(),

	// Interface
	NfcAdapter.ReaderCallback
{

	/**
	 * Layout resource ID.
	 */
	override val layoutId = R.layout.dlg_scan_nfc_tag

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Alarm.
	 */
	var alarm: NacAlarm? = null

	/**
	 * Called when the OK buton is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
	}

	/**
	 * Setup OK button.
	 */
	override fun setupOkButton(alarm: NacAlarm?)
	{
		setupSelectNfcTagButton()
	}

	/**
	 * Setup the select NFC tag button.
	 */
	private fun setupSelectNfcTagButton()
	{
		// Get the ok (select NFC tag) button
		val selectNfcButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)

		// Rename the button
		selectNfcButton.setText(R.string.action_select_nfc_tag)

		// Set the visibility of the button
		lifecycleScope.launch {
			selectNfcButton.visibility = if (nfcTagViewModel.count() > 0) View.VISIBLE else View.GONE
		}

		// Setup the button
		setupPrimaryButton(selectNfcButton, listener = {

			// Get the nav controller
			val navController = findNavController()

			try
			{
				// Navigate to the select NFC tag dialog
				navController.navigate(R.id.nacSelectNfcTagDialog, arguments)
			}
			catch (_: IllegalStateException)
			{
			}

			// Observe the back stack entry
			observeBackStackEntry(navController, this,
				onBackStackPopulated = {

					// Get the alarm from the select NFC tag dialog
					val a = navController.currentBackStackEntry?.savedStateHandle?.get<NacAlarm>("YOYOYO")
					println("A : ${a?.nfcTagId}")

					// Save the alarm and dismiss
					onSaveAlarm(a)
					dismiss()
				})

		})
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
		val flags = FLAG_READER_NFC_A or
			FLAG_READER_NFC_B or
			FLAG_READER_NFC_BARCODE or
			FLAG_READER_NFC_F or
			FLAG_READER_NFC_V or
			FLAG_READER_SKIP_NDEF_CHECK

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

		// Disable NFC reader mode
		nfcAdapter.disableReaderMode(activity)
	}

	/**
	 * Called when an NFC tag is discovered.
	 */
	override fun onTagDiscovered(tag: Tag?)
	{
		// Parse and save the ID from the NFC tag
		alarm?.nfcTagId = NacNfc.parseId(tag).toString()
		println("NFC : ${alarm?.nfcTagId}")

		// TODO: Add saving use any NFC tag logic in main activity?
		// TODO: Add the save alarm dialog here

		// Save the alarm to the backstack
		onSaveAlarm(alarm)

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