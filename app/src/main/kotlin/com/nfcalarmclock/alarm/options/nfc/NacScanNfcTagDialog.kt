package com.nfcalarmclock.alarm.options.nfc

import android.content.DialogInterface
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.FLAG_READER_NFC_A
import android.nfc.NfcAdapter.FLAG_READER_NFC_B
import android.nfc.NfcAdapter.FLAG_READER_NFC_BARCODE
import android.nfc.NfcAdapter.FLAG_READER_NFC_F
import android.nfc.NfcAdapter.FLAG_READER_NFC_V
import android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
import android.nfc.Tag
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment

/**
 * Scan an NFC tag that will be used to dismiss the given alarm when it goes
 * off.
 */
class NacScanNfcTagDialog

	// Constructor
	: NacBottomSheetDialogFragment(),

	// Interface
	NfcAdapter.ReaderCallback
{

	/**
	 * Listener for using any NFC tag.
	 */
	interface OnScanNfcTagListener
	{
		fun onCancel(alarm: NacAlarm)
		fun onDone(alarm: NacAlarm)
		fun onScanned(alarm: NacAlarm, tagId: String)
		fun onSelected(alarm: NacAlarm, nfcTag: NacNfcTag)
		fun onUseAny(alarm: NacAlarm)
	}

	/**
	 * Alarm.
	 */
	var alarm: NacAlarm? = null

	/**
	 * List of NFC tags.
	 */
	var allNfcTags: List<NacNfcTag> = ArrayList()

	/**
	 * Last saved/selected NFC tag.
	 */
	var lastNfcTag: NacNfcTag = NacNfcTag()

	/**
	 * Listener for when the NFC tag is scanned.
	 */
	var onScanNfcTagListener: OnScanNfcTagListener? = null

	/**
	 * Called when the dialog view is created.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?)
	: View?
	{
		return inflater.inflate(R.layout.dlg_scan_nfc_tag, container, false)
	}

	/**
	 * Called when the dialog is canceled.
	 */
	override fun onCancel(dialog: DialogInterface)
	{
		// Super
		super.onCancel(dialog)

		// Call the listener
		onScanNfcTagListener?.onCancel(alarm!!)
	}

	/**
	 * Called when the dialog view is created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the views
		val useAnyNfcButton = view.findViewById(R.id.use_any_nfc_tag) as MaterialButton
		val selectNfcButton = view.findViewById(R.id.select_nfc_tag) as MaterialButton

		// Setup the use any NFC button
		setupPrimaryButton(useAnyNfcButton, listener = {

			// Call the listener
			onScanNfcTagListener?.onUseAny(alarm!!)

			// Dismiss the dialog
			dismiss()

		})

		// Set the visibility of the select button
		selectNfcButton.visibility = if (allNfcTags.isNotEmpty()) View.VISIBLE else View.GONE

		// Setup the select NFC button
		setupSecondaryButton(selectNfcButton, listener = {

			// Create the select NFC tag dialog
			val dialog = NacSelectNfcTagDialog()

			// Setup the dialog
			dialog.allNfcTags = allNfcTags
			dialog.selectedNfcTag = lastNfcTag
			dialog.onSelectNfcTagListener = object: NacSelectNfcTagDialog.OnSelectNfcTagListener
			{

				/**
				 * Called when the Select NFC Tag is canceled.
				 */
				override fun onCancel()
				{
					// Show the current dialog
					this@NacScanNfcTagDialog.dialog?.show()
				}

				/**
				 * Called when the NFC Tag is selected.
				 */
				override fun onSelected(nfcTag: NacNfcTag)
				{
					// Call the listener
					onScanNfcTagListener?.onSelected(alarm!!, nfcTag)

					// Dismiss the dialog
					dismiss()
				}

			}

			// Hide the current dialog
			this.dialog?.hide()

			// Show the dialog
			dialog.show(childFragmentManager, NacSelectNfcTagDialog.TAG)

		})
	}

	/**
	 * Called when the fragment is no longer in use.
	 */
	override fun onDestroy()
	{
		// Super
		super.onDestroy()

		// Call the listener
		onScanNfcTagListener?.onDone(alarm!!)
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
		// Parse the tag ID from the NFC tag
		val tagId = NacNfc.parseId(tag).toString()

		// Call the listener
		onScanNfcTagListener?.onScanned(alarm!!, tagId)

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