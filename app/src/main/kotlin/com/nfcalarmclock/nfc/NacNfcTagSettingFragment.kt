package com.nfcalarmclock.nfc

import android.os.Bundle
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.R
import com.nfcalarmclock.nfc.db.NacNfcTag
import dagger.hilt.android.AndroidEntryPoint

/**
 * Manage NFC tag fragment.
 */
@AndroidEntryPoint
class NacNfcTagSettingFragment
	: Fragment(R.layout.frg_manage_nfc_tags)
{

	/**
	 * RecyclerView containing the NFC tags.
	 */
	private lateinit var recyclerView: RecyclerView

	/**
	 * Alarm card adapter.
	 */
	private lateinit var nfcTagAdapter: NacNfcTagAdapter

	/**
	 * NFC tag view model.
	 */
	private val nfcTagViewModel: NacNfcTagViewModel by viewModels()

	/**
	 * Called when the view is created.
	 */
	override fun onViewCreated(root: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(root, savedInstanceState)

		// Context
		val context = requireContext()

		// Set views
		recyclerView = root.findViewById(R.id.nfc_tag_list_view)
		nfcTagAdapter = NacNfcTagAdapter()
		val dividerItemDecoration = DividerItemDecoration(context,
			LinearLayoutManager.VERTICAL)

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

				}

				// Show the dialog
				deleteNfcTagDialog.show(childFragmentManager, NacDeleteNfcTagDialog.TAG)
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
				renameNfcTagDialog.show(childFragmentManager, NacRenameNfcTagDialog.TAG)
			}

		}

		// Setup all the NFC tags
		nfcTagViewModel.allNfcTags.observe(viewLifecycleOwner) {

			// Add the item
			nfcTagAdapter.submitList(it)

		}
	}

}