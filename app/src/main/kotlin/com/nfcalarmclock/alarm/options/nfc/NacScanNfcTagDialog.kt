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
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.options.NacGenericAlarmOptionsDialog
import com.nfcalarmclock.alarm.options.navigate
import com.nfcalarmclock.util.addAlarm
import com.nfcalarmclock.util.getAlarm
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
	 * Called when the OK buton is clicked.
	 */
	override fun onOkClicked(alarm: NacAlarm?)
	{
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
		val id = NacNfc.parseId(tag).toString()
		val alarm = arguments?.getAlarm()
		alarm?.nfcTagId = id

		println("NFC : $id")

		// Get the nav controller
		val navController = findNavController()
		val newArgs = arguments?.addAlarm(alarm)

		// Navigate to the select NFC tag dialog
		navController.navigate(R.id.nacSaveNfcTagDialog, newArgs, this,
			onBackStackPopulated = {

				// Get the alarm from the select NFC tag dialog
				val a = navController.currentBackStackEntry?.savedStateHandle?.get<NacAlarm>("YOYOYO")
				println("S : ${a?.nfcTagId}")

				// Save the alarm and dismiss
				onSaveAlarm(a)
				dismiss()
			})

		// Save the alarm to the backstack
		onSaveAlarm(alarm)

		// Dismiss the dialog
		dismiss()
	}

	/**
	 * Setup all alarm options.
	 */
	override fun setupAlarmOptions(alarm: NacAlarm?)
	{
		// Get the alarm, or build a new one, to get default values
		val a = alarm ?: NacAlarm.build()

		// Setup the views
		setupCurrentlySelectedInfo(a.nfcTagId)
	}

	/**
	 * Setup the currently selected information.
	 */
	private fun setupCurrentlySelectedInfo(id: String)
	{
		// Get the views and NFC tag name
		val title: TextView = dialog!!.findViewById(R.id.title_currently_selected)
		val description: TextView = dialog!!.findViewById(R.id.description_nfc_tag_name)

		lifecycleScope.launch {

			// Find the NFC tag
			nfcTagViewModel.findNfcTag(id)
				?.let {

					// Setup the views
					title.visibility = View.VISIBLE
					description.visibility = View.VISIBLE
					description.text = it.name

				}

		}
	}

	/**
	 * Setup any extra buttons.
	 */
	override fun setupExtraButtons(alarm: NacAlarm?)
	{
		// Get the views
		val useAnyButton: MaterialButton = dialog!!.findViewById(R.id.use_any_nfc_tag_button)
		val cancelButton: MaterialButton = dialog!!.findViewById(R.id.cancel_button)
		val parentView: LinearLayout = useAnyButton.parent as LinearLayout

		// Swap views
		parentView.removeView(cancelButton)
		parentView.removeView(useAnyButton)
		parentView.addView(useAnyButton)
		parentView.addView(cancelButton)

		// Setup the button
		setupSecondaryButton(useAnyButton, listener = {

			// Clear the NFC tag ID
			alarm?.nfcTagId = ""

			// Save the alarm and dismiss
			onSaveAlarm(alarm)
			dismiss()

		})
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

			// Navigate to the select NFC tag dialog
			navController.navigate(R.id.nacSelectNfcTagDialog, arguments, this,
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

}