package com.nfcalarmclock.settings.nfc

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.NacAlarmViewModel
import com.nfcalarmclock.alarm.options.nfc.NacDeleteNfcTagDialog
import com.nfcalarmclock.nfc.NacNfcTagViewModel
import com.nfcalarmclock.alarm.options.nfc.NacRenameNfcTagDialog
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.nfc.removeNfcTag
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.scheduler.NacScheduler
import com.nfcalarmclock.timer.NacTimerViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch

/**
 * Manage NFC tag fragment.
 */
@AndroidEntryPoint
class NacNfcTagSettingFragment
	: Fragment(R.layout.frg_manage_nfc_tags)
{

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Alarm view model.
	 */
	private val alarmViewModel: NacAlarmViewModel by viewModels()

	/**
	 * Timer view model.
	 */
	private val timerViewModel: NacTimerViewModel by viewModels()

	/**
	 * RecyclerView containing the NFC tags.
	 */
	private lateinit var recyclerView: RecyclerView

	/**
	 * Alarm card adapter.
	 */
	private lateinit var nfcTagAdapter: NacNfcTagAdapter

	/**
	 * Remove NFC ID from the item.
	 */
	private fun removeNfcId()
	{

	}

	/**
	 * View is created.
	 */
	override fun onViewCreated(root: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(root, savedInstanceState)

		// Context
		val context = requireContext()
		val sharedPreferences = NacSharedPreferences(context)

		// TODO: Can maybe customize this more when going up to API 36, but for now opting out
		// Setup edge to edge for the root view by using the margin that was saved in
		// the main settings fragment. Edge-to-edge is enforced in API >= 35
		//if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.VANILLA_ICE_CREAM)
		//{
		//	root.updateLayoutParams<ViewGroup.MarginLayoutParams> {
		//		topMargin = (activity as NacMainSettingActivity).rvTopMargin
		//	}
		//}

		// Set views
		recyclerView = root.findViewById(R.id.nfc_tag_list_view)
		nfcTagAdapter = NacNfcTagAdapter(sharedPreferences)
		val dividerItemDecoration = DividerItemDecoration(
			context,
			LinearLayoutManager.VERTICAL
		)

		// Setup ListView
		recyclerView.adapter = nfcTagAdapter
		recyclerView.layoutManager = LinearLayoutManager(context)
		recyclerView.addItemDecoration(dividerItemDecoration)
		recyclerView.setHasFixedSize(true)

		// Setup NFC tag adapter
		nfcTagAdapter.onModifyRequestListener = object: NacNfcTagAdapter.OnModifyRequestListener
		{

			/**
			 * Request to delete an NFC tag.
			 */
			override fun onDelete(position: Int, nfcTag: NacNfcTag)
			{
				// Create the dialog
				val deleteNfcTagDialog = NacDeleteNfcTagDialog()

				// Set the listener for renaming an NFC tag
				deleteNfcTagDialog.onDeleteNfcTagListener = NacDeleteNfcTagDialog.OnDeleteNfcTagListener {

					// Delete the NFC tag from the database. The observe() on the
					// LiveData will take care of updating the adapter
					nfcTagViewModel.delete(nfcTag)

					// Prepare to update any alarms that were using this NFC tag
					lifecycleScope.launch {

						// Get all alarms and timers
						val allAlarms = alarmViewModel.getAllAlarms()
						val allTimers = timerViewModel.getAllTimers()

						// Attempt to remove the NFC tag from any matching alarm
						allAlarms.forEach { a ->

							// NFC tag was removed
							if (a.removeNfcTag(nfcTag))
							{
								// Update the alarm in the view model and scheduler
								alarmViewModel.update(a)
								NacScheduler.update(context, a)
							}

						}

						// Attempt to remove the NFC tag from any matching timer
						allTimers.forEach { t ->

							// NFC tag was removed
							if (t.removeNfcTag(nfcTag))
							{
								// Update the timer in the view model
								timerViewModel.update(t)
							}

						}

					}

				}

				// Show the dialog
				deleteNfcTagDialog.show(childFragmentManager, NacDeleteNfcTagDialog.Companion.TAG)
			}

			/**
			 * Request to rename an NFC tag.
			 */
			override fun onRename(position: Int, nfcTag: NacNfcTag)
			{
				// Create the dialog
				val renameNfcTagDialog = NacRenameNfcTagDialog()

				// Set the list of all NFC tags
				renameNfcTagDialog.allNfcTags = nfcTagAdapter.currentList

				// Set the listener for renaming an NFC tag
				renameNfcTagDialog.onRenameNfcTagListener = NacRenameNfcTagDialog.OnRenameNfcTagListener { name ->

					// Set the name of the NFC tag
					nfcTag.name = name

					// Rename the NFC tag in the database. The observe() on the
					// LiveData will take care of updating the adapter
					nfcTagViewModel.update(nfcTag)

					// Update the NFC tag in the adapter
					nfcTagAdapter.notifyItemChanged(position)

				}

				// Show the dialog
				renameNfcTagDialog.show(childFragmentManager, NacRenameNfcTagDialog.Companion.TAG)
			}

		}

		// Setup all the NFC tags
		nfcTagViewModel.allNfcTags.observe(viewLifecycleOwner) {

			// Add the item
			nfcTagAdapter.submitList(it)

		}
	}

}