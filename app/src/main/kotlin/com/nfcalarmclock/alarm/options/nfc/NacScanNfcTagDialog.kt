package com.nfcalarmclock.alarm.options.nfc

import android.nfc.NfcAdapter
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
import com.nfcalarmclock.view.setupThemeColor
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

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

		// Enable NFC reader mode
		NacNfc.enableReaderMode(requireActivity(), this)
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

		// Disable NFC reader mode
		NacNfc.disableReaderMode(requireActivity())
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

		// Disable NFC reader mode
		NacNfc.disableReaderMode(requireActivity())

		lifecycleScope.launch {

			// NFC tag already exists so no need to save it
			if (nfcTagViewModel.findNfcTag(id) != null)
			{
				withContext(Dispatchers.Main)
				{
					// Save the alarm to the backstack
					onSaveAlarm(alarm)

					// Dismiss the dialog
					dismiss()
				}
			}
			// Save the NFC tag
			else
			{
				// Get the nav controller
				val navController = findNavController()
				val newArgs = arguments?.addAlarm(alarm)

				// Navigate to the select NFC tag dialog
				navController.navigate(R.id.nacSaveNfcTagDialog, newArgs, this@NacScanNfcTagDialog,
					onBackStackPopulated = {

						// Get the alarm from the select NFC tag dialog
						val a = navController.currentBackStackEntry?.savedStateHandle?.get<NacAlarm>("YOYOYO")

						// Save the alarm and dismiss
						onSaveAlarm(a)
						dismiss()
					})
			}

		}

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
		// No NFC ID is set
		if (id.isEmpty())
		{
			return
		}

		// Get the views and NFC tag name
		val title: TextView = dialog!!.findViewById(R.id.title_currently_selected)
		val description: TextView = dialog!!.findViewById(R.id.description_nfc_tag_name)

		lifecycleScope.launch {

			// Find the NFC tag
			val tag = nfcTagViewModel.findNfcTag(id)

			// Change the visibility of the views
			title.visibility = View.VISIBLE
			description.visibility = View.VISIBLE

			// Set the description
			description.text = tag?.name ?: "(${resources.getString(R.string.message_show_nfc_tag_id)}) $id"

		}
	}

	/**
	 * Setup any extra buttons.
	 */
	override fun setupExtraButtons(alarm: NacAlarm?)
	{
		// Get the button
		val useAnyButton: MaterialButton = dialog!!.findViewById(R.id.use_any_nfc_tag_button)

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
		val addNfcButton: MaterialButton = dialog!!.findViewById(R.id.add_nfc_tag_button)

		// Rename the button
		selectNfcButton.setText(R.string.action_select_nfc_tag)

		// Set the visibility of the button based on if there are any NFC tags saved
		lifecycleScope.launch {
			val vis = if (nfcTagViewModel.count() > 0) View.VISIBLE else View.GONE

			selectNfcButton.visibility = vis
			addNfcButton.visibility = vis
		}

		// Setup the select NFC tag button
		setupPrimaryButton(selectNfcButton, listener = {

			// Get the nav controller
			val navController = findNavController()

			// Navigate to the select NFC tag dialog
			navController.navigate(R.id.nacSelectNfcTagDialog, arguments, this,
				onBackStackPopulated = {

					// Get the alarm from the select NFC tag dialog
					val a = navController.currentBackStackEntry?.savedStateHandle?.get<NacAlarm>("YOYOYO")

					// Save the alarm and dismiss
					onSaveAlarm(a)
					dismiss()
				})

		})

		// Setup the add button
		addNfcButton.setupThemeColor(sharedPreferences)

		// Set the click listener
		addNfcButton.setOnClickListener {
			println("HELLO")
			// TODO: Make sure the backup does not show any currently selected NFC tags
			// TODO: Make a thing to get a list of NFC tags, even if the list has one element
			// TODO: Only show the plus if an NFC tag has already been selected
			// TODO: Do I want to forego the plus and just add it to select NFC tag? That way a person can select all of them in one go
		}
	}

}