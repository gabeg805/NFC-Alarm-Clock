package com.nfcalarmclock.system.permission

import android.annotation.SuppressLint
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import com.google.android.material.button.MaterialButton
import com.nfcalarmclock.R
import com.nfcalarmclock.view.dialog.NacBottomSheetDialogFragment
import com.nfcalarmclock.view.toSpannedString
import com.nfcalarmclock.view.toThemedBold

/**
 * Generic dialog for requesting permissions.
 */
abstract class NacPermissionRequestDialog
	: NacBottomSheetDialogFragment()
{

	/**
	 * Listener for the permission request.
	 */
	@Suppress("Unused")
	interface OnPermissionRequestListener
	{
		fun onPermissionRequestAccepted(permission: String)
		fun onPermissionRequestCanceled(permission: String)
	}

	/**
	 * The ID of the icon.
	 */
	abstract val iconId: Int

	/**
	 * The ID of the title string.
	 */
	abstract val titleId: Int

	/**
	 * The ID of the description string.
	 */
	abstract val descriptionId: Int

	/**
	 * The name of the permission.
	 */
	open val permission: String
		get() = ""

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
	 * Called when the creating the view.
	 */
	override fun onCreateView(
		inflater: LayoutInflater,
		container: ViewGroup?,
		savedInstanceState: Bundle?
	): View?
	{
		return inflater.inflate(R.layout.dlg_request_permission, container, false)
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

		// Get the views
		val imageView: ImageView = view.findViewById(R.id.request_icon)
		val titleView: TextView = view.findViewById(R.id.request_title)
		val descriptionView: TextView = view.findViewById(R.id.request_description)
		val okButton: MaterialButton = view.findViewById(R.id.request_ok_button)
		val skipButton: MaterialButton = view.findViewById(R.id.request_skip_button)

		// Setup the views
		imageView.setImageResource(iconId)
		titleView.setText(titleId)

		// Color the summary text with the theme color
		descriptionView.text = resources.getString(descriptionId)
			.toThemedBold(themeColor)
			.toSpannedString()

		// Setup the ok button
		setupPrimaryButton(okButton, listener = {
			doPermissionRequestAccepted()
			dismiss()
		})

		// Setup the skip button
		setupSecondaryButton(skipButton, listener = {
			doPermissionRequestCanceled()
			dismiss()
		})
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
		val dummy: View = dialog!!.findViewById(R.id.request_dummy)
		val separator: View = dialog!!.findViewById(R.id.request_separator)
		val pages: View = dialog!!.findViewById(R.id.request_pages)

		// Show page information
		if (totalNumberOfPages > 1)
		{
			// Make the separate and pages visible
			separator.visibility = View.VISIBLE
			pages.visibility = View.VISIBLE

			// Get the textviews that need to be modified
			val positionTextView: TextView = dialog!!.findViewById(R.id.request_current_page)
			val totalNumTextView: TextView = dialog!!.findViewById(R.id.request_total_num_pages)

			// Set the position and total number of pages
			positionTextView.text = "$position "
			totalNumTextView.text = " $totalNumberOfPages"
		}
		else
		{
			// Make the dummy view show up for spacing and separate and pages disappear
			dummy.visibility = View.INVISIBLE
			separator.visibility = View.GONE
			pages.visibility = View.GONE
		}
	}

}