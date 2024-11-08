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
	 * The actions to execute when the permission request is accepted.
	 */
	override fun doPermissionRequestAccepted()
	{
		// Set the flag that the permission was requested
		sharedPreferences!!.wasIgnoreBatteryOptimizationPermissionRequested = true

		// Call the accepeted listeners
		super.doPermissionRequestAccepted()
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	override fun doPermissionRequestCanceled()
	{
		// Set the flag that the permission was requested
		sharedPreferences!!.wasIgnoreBatteryOptimizationPermissionRequested = true

		// Call the accepeted listeners
		super.doPermissionRequestCanceled()
	}

	/**
	 * The ID of the layout.
	 */
	override val layoutId: Int
		get() = R.layout.dlg_request_ignore_battery_optimization_permission

	/**
	 * The ID of the title string.
	 */
	override val titleId: Int
		get() = R.string.title_request_permission_ignore_battery_optimization

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacIgnoreBatteryOptimizationPermissionDialog"

	}

}