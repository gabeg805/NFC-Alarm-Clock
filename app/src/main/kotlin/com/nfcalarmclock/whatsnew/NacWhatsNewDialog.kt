package com.nfcalarmclock.whatsnew

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup.LayoutParams
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.fragment.app.FragmentManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment
import com.nfcalarmclock.view.toThemedBold
import com.nfcalarmclock.whatsnew.NacWhatsNewDialog.OnReadWhatsNewListener

/**
 * Show what is new with the app after an update.
 */
class NacWhatsNewDialog
	: NacDialogFragment()
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
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Create the dialog
		return AlertDialog.Builder(requireContext())
			.setTitle(R.string.title_whats_new)
			.setPositiveButton(R.string.action_ok) { _, _ ->

				// Call the listener
				onReadWhatsNewListener?.onReadWhatsNew()

			}
			.setView(R.layout.dlg_whats_new)
			.create()
	}

	/**
	 * Called when the fragment is resumed
	 */
	override fun onResume()
	{
		// Super
		super.onResume()

		// Get the views
		val textView = dialog!!.findViewById<TextView>(R.id.whats_new_version)
		val recyclerView = dialog!!.findViewById<RecyclerView>(R.id.whats_new_bullet_container)

		// Get the theme color
		val themeColor = sharedPreferences!!.themeColor

		// Get all the whats new items and add theme color to bold sections
		val allMessages = resources.getTextArray(R.array.whats_new_items)
			.map { it.toString().toThemedBold(themeColor) }
			.toList()

		// Setup the views
		setupVersion(textView)
		setupRecyclerView(recyclerView, allMessages)
		setupContainer(allMessages)
	}

	/**
	 * Setup container.
	 */
	private fun setupContainer(allMessages: List<String>)
	{
		// Do nothing if there are not that many children
		if (allMessages.size < 6)
		{
			return
		}

		// Get the container
		val container = dialog!!.findViewById<LinearLayout>(R.id.whats_new_layout)

		// Get the new height of the container
		val height = resources.displayMetrics.heightPixels / 2
		val layoutParams = FrameLayout.LayoutParams(LayoutParams.MATCH_PARENT, height)

		// Set the height of the container
		container.layoutParams = layoutParams
	}

	/**
	 * Setup the recycler view.
	 */
	private fun setupRecyclerView(recyclerView: RecyclerView, allMessages: List<String>)
	{
		// Setup the list adapter
		val listAdapter = NacWhatsNewListAdapter()

		listAdapter.submitList(allMessages)

		// Setup the recycler view
		recyclerView.adapter = listAdapter
		recyclerView.layoutManager = LinearLayoutManager(context)
	}

	/**
	 * Setup the version.
	 */
	@SuppressLint("SetTextI18n")
	private fun setupVersion(textView: TextView)
	{
		// Prepare the version name and number
		val versionName = getString(R.string.version)
		val versionNum = BuildConfig.VERSION_NAME

		// Set the text
		textView.text = "$versionName $versionNum"
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