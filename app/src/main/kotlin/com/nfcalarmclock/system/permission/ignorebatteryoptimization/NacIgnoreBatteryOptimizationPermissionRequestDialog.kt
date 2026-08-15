package com.nfcalarmclock.system.permission.ignorebatteryoptimization

import com.nfcalarmclock.R
import com.nfcalarmclock.system.permission.NacPermissionRequestDialog

/**
 * Dialog to request to ignore battery optimization.
 */
class NacIgnoreBatteryOptimizationPermissionRequestDialog
	: NacPermissionRequestDialog()
{

	/**
	 * The ID of the layout.
	 */
	override val iconId: Int = R.drawable.battery_alert

	/**
	 * The ID of the title string.
	 */
	override val titleId: Int = R.string.title_permission_disable_battery_optimization

	/**
	 * The ID of the text string.
	 */
	override val descriptionId = R.string.message_permission_ignore_battery_optimization_request

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	override fun doPermissionRequestAccepted()
	{
		// Set the flag that the permission was requested
		sharedPreferences.wasIgnoreBatteryOptimizationPermissionRequested = true

		// Call the accepeted listeners
		super.doPermissionRequestAccepted()
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	override fun doPermissionRequestCanceled()
	{
		// Set the flag that the permission was requested
		sharedPreferences.wasIgnoreBatteryOptimizationPermissionRequested = true

		// Call the accepeted listeners
		super.doPermissionRequestCanceled()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacIgnoreBatteryOptimizationPermissionDialog"

	}

}