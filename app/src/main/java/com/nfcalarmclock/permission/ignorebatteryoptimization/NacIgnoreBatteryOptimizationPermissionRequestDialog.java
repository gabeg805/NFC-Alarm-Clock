package com.nfcalarmclock.permission.ignorebatteryoptimization;

import com.nfcalarmclock.R;
import com.nfcalarmclock.permission.NacPermissionRequestDialog;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Dialog to request to ignore battery optimization.
 */
public class NacIgnoreBatteryOptimizationPermissionRequestDialog
	extends NacPermissionRequestDialog
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacIgnoreBatteryOptimizationPermissionDialog";

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	protected void doPermissionRequestAccepted()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		// Set the flag that the permission was requested
		shared.editWasIgnoreBatteryOptimizationPermissionRequested(true);

		// Call the accepeted listeners
		super.doPermissionRequestAccepted();
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	protected void doPermissionRequestCanceled()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		// Set the flag that the permission was requested
		shared.editWasIgnoreBatteryOptimizationPermissionRequested(true);

		// Call the accepeted listeners
		super.doPermissionRequestCanceled();
	}

	/**
	 * Get the ID of the layout.
	 *
	 * @return The ID of the layout.
	 */
	public int getLayoutId()
	{
		return R.layout.dlg_request_ignore_battery_optimization_permission;
	}

	/**
	 * Get the ID of the title string.
	 *
	 * @return The ID of the title string.
	 */
	public int getTitleId()
	{
		return R.string.title_request_permission_ignore_battery_optimization;
	}

}
