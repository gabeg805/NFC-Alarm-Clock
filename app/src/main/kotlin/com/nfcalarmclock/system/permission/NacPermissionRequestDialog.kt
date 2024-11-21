package com.nfcalarmclock.system.permission

import android.annotation.SuppressLint
import android.app.AlertDialog
import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.View
import android.widget.TextView
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacDialogFragment

/**
 * Generic dialog for requesting permissions.
 */
abstract class NacPermissionRequestDialog
	: NacDialogFragment()
{

	/**
	 * Listener for when what's new dialog has been read.
	 */
	@Suppress("Unused")
	interface OnPermissionRequestListener
	{
		fun onPermissionRequestAccepted(permission: String)
		fun onPermissionRequestCanceled(permission: String)
	}

	/**
	 * The ID of the layout.
	 */
	abstract val layoutId: Int

	/**
	 * The name of the permission.
	 */
	open val permission: String
		get() = ""

	/**
	 * The ID of the title string.
	 */
	abstract val titleId: Int

	/**
	 * Position of this dialog in the permission request manager.
	 */
	var position = 0

	/**
	 * Total number of pages in the permission request manager.
	 */
	var totalNumberOfPages = 0

	/**
	 * Listener for when the permission request is done.
	 */
	var onPermissionRequestListener: OnPermissionRequestListener? = null

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	protected open fun doPermissionRequestAccepted()
	{
		onPermissionRequestListener?.onPermissionRequestAccepted(permission)
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	protected open fun doPermissionRequestCanceled()
	{
		onPermissionRequestListener?.onPermissionRequestCanceled(permission)
	}

	/**
	 * Called when the dialog is canceled.
	 */
	override fun onCancel(dialog: DialogInterface)
	{
		doPermissionRequestCanceled()
	}

	/**
	 * Called when the dialog is created.
	 */
	override fun onCreateDialog(savedInstanceState: Bundle?): Dialog
	{
		// Setup the shared preferences
		setupSharedPreferences()

		// Build the dialog
		return AlertDialog.Builder(requireContext())
			.setPositiveButton(R.string.action_ok) { _, _ ->
				doPermissionRequestAccepted()
			}
			.setNegativeButton(R.string.action_cancel) { _, _ ->
				doPermissionRequestCanceled()
			}
			.setTitle(titleId)
			.setView(layoutId)
			.create()
	}

	/**
	 * Called when the view is created.
	 *
	 * This is called right after onCreateDialog().
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Setup the page information
		setupPageInfo()
	}

	/**
	 * Set the page information in the dialog.
	 */
	@SuppressLint("SetTextI18n")
	private fun setupPageInfo()
	{
		// Get the separate and number of pages views
		val separator = dialog!!.findViewById<View>(R.id.request_separator)
		val pages = dialog!!.findViewById<View>(R.id.request_pages)

		// Show page information
		if (totalNumberOfPages > 1)
		{
			// Make the separate and pages visible
			separator.visibility = View.VISIBLE
			pages.visibility = View.VISIBLE

			// Get the textviews that need to be modified
			val positionTextView = dialog!!.findViewById<TextView>(R.id.request_current_page)
			val totalNumTextView = dialog!!.findViewById<TextView>(R.id.request_total_num_pages)

			// Set the position and total number of pages
			positionTextView.text = "$position "
			totalNumTextView.text = " $totalNumberOfPages"
		}
		else
		{
			// Make the separate and pages disappear
			separator.visibility = View.GONE
			pages.visibility = View.GONE
		}
	}

}