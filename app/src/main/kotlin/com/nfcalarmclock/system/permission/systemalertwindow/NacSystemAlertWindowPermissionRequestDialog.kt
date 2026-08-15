package com.nfcalarmclock.system.permission.systemalertwindow

import com.nfcalarmclock.R
import com.nfcalarmclock.system.permission.NacPermissionRequestDialog

/**
 * Dialog to request the SYSTEM_ALERT_WINDOW permission.
 */
class NacSystemAlertWindowPermissionRequestDialog
	: NacPermissionRequestDialog()
{

	/**
	 * The name of the permission.
	 */
	override val permission: String = NacSystemAlertWindowPermission.permissionName

	/**
	 * The ID of the icon.
	 */
	override val iconId: Int = R.drawable.desktop_windows_32

	/**
	 * The ID of the title string.
	 */
	override val titleId: Int = R.string.title_permission_system_alert_window

	/**
	 * The ID of the text string.
	 */
	override val descriptionId: Int = R.string.message_permission_system_alert_window_request

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	override fun doPermissionRequestAccepted()
	{
		// Set the flag that the permission was requested
		sharedPreferences.wasSystemAlertWindowPermissionRequested = true

		// Call the accepeted listeners
		super.doPermissionRequestAccepted()
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	override fun doPermissionRequestCanceled()
	{
		// Set the flag that the permission was requested
		sharedPreferences.wasSystemAlertWindowPermissionRequested = true

		// Call the canceled listeners
		super.doPermissionRequestCanceled()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacSystemAlertWindowPermissionDialog"

	}

}