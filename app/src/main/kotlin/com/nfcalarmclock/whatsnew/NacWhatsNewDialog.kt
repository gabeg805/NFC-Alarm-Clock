package com.nfcalarmclock.whatsnew

import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.toThemedBold
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog.OnReadWhatsNewListener

/**
 * Show what is new with the app after an update.
 */
class NacWhatsNewDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Listener for when what's new dialog has been read.
	 */
	fun interface OnReadWhatsNewListener
	{
		fun onReadWhatsNew()
	}

	/**
	 * Listener for when a text-to-speech option and frequency is selected.
	 */
	var onReadWhatsNewListener: OnReadWhatsNewListener? = null

	/**
	 * Called when the dialog is canceled.
	 */
	override fun onCancel(dialog: DialogInterface)
	{
		// Call the listener
		onReadWhatsNewListener?.onReadWhatsNew()
	}

	/**
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_whats_new, container, false)
	}

	/**
	 * Called when the view has been created.
	 */
	override fun onViewCreated(view: View, savedInstanceState: Bundle?)
	{
		// Super
		super.onViewCreated(view, savedInstanceState)

		// Get the theme color
		val themeColor = sharedPreferences.themeColor

		// Get all the whats new items and add theme color to bold sections
		val allMessages = resources.getTextArray(R.array.whats_new_items)
			.map { it.toString().toThemedBold(themeColor) }
			.toList()

		// Setup the views
		setupOkButton()
		setupRecyclerView(allMessages)
		setBehaviorState(BottomSheetBehavior.STATE_EXPANDED)
	}

	/**
	 * Setup the OK button.
	 */
	private fun setupOkButton()
	{
		// Get the ok button
		val okButton: MaterialButton = dialog!!.findViewById(R.id.ok_button)

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {

			// Call the listener
			onReadWhatsNewListener?.onReadWhatsNew()

			// Dismiss
			dismiss()

		})
	}

	/**
	 * Setup the recycler view.
	 */
	private fun setupRecyclerView(allMessages: List<String>)
	{
		// Get the views
		val recyclerView = dialog!!.findViewById<RecyclerView>(R.id.whats_new_recyclerview)
		val listAdapter = NacWhatsNewListAdapter()

		// Setup the list adapter
		listAdapter.submitList(allMessages)

		// Setup the recyclerview
		recyclerView.adapter = listAdapter
		recyclerView.layoutManager = LinearLayoutManager(context)

		// Set the height of the recyclerview
		setupScrollableViewHeight(recyclerView, 60)
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacWhatsNewDialog"

		/**
		 * Show the dialog.
		 */
		fun show(
			manager: FragmentManager,
			listener: () -> Unit = {})
		{
			// Create the dialog
			val dialog = NacWhatsNewDialog()

			// Setup the listener
			dialog.onReadWhatsNewListener = OnReadWhatsNewListener {
				listener()
			}

			// Show the dialog
			dialog.show(manager, TAG)
		}

	}

}