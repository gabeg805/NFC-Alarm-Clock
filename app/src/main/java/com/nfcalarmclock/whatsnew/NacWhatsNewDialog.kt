package com.nfcalarmclock.whatsnew

import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams
import android.widget.LinearLayout
import android.widget.RelativeLayout
import android.widget.ScrollView
import android.widget.TextView
import com.nfcalarmclock.BuildConfig
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Show what is new with the app after an update.
 */
class NacWhatsNewDialog
	: NacDialogFragment()
{

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacWhatsNewDialog"

	}

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
		val scrollView = dialog!!.findViewById<ScrollView>(R.id.whats_new_scrollview)
		val bulletContainer = dialog!!.findViewById<RelativeLayout>(R.id.whats_new_bullet_container)

		// Setup the views
		setupVersion(textView)
		setupScrollView(scrollView, bulletContainer)
	}

	/**
	 * Setup the scrollview.
	 */
	private fun setupScrollView(scrollView: ScrollView, viewGroup: ViewGroup)
	{
		// Do nothing if there are not that many children. Each bullet counts
		// for two since you have the bullet and then the text next to it
		if (viewGroup.childCount < 10)
		{
			return
		}

		// Set the height of the scrollview
		val height = resources.displayMetrics.heightPixels / 2
		val layoutParams = LinearLayout.LayoutParams(LayoutParams.MATCH_PARENT, height)

		scrollView.layoutParams = layoutParams
	}

	/**
	 * Setup the version.
	 */
	private fun setupVersion(textView: TextView)
	{
		// Prepare the strings
		val versionWord = textView.text.toString()
		val versionName = BuildConfig.VERSION_NAME
		val versionNameAndNum = String.format("%s %s", versionWord, versionName)

		// Set the version
		textView.text = versionNameAndNum
	}

}