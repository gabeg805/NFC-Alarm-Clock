package com.nfcalarmclock.permission.scheduleexactalarm

import android.Manifest
import android.os.Build
import androidx.annotation.RequiresApi
import com.nfcalarmclock.R
import com.nfcalarmclock.permission.NacPermissionRequestDialog

/**
 * Dialog to request to allow scheduling an exact alarm.
 */
@RequiresApi(api = Build.VERSION_CODES.S)
class NacScheduleExactAlarmPermissionRequestDialog
	: NacPermissionRequestDialog()
{

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	override fun doPermissionRequestAccepted()
	{
		// Set the flag that the permission was requested
		sharedPreferences.editWasScheduleExactAlarmPermissionRequested(true)

		// Call the accepeted listeners
		super.doPermissionRequestAccepted()
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	override fun doPermissionRequestCanceled()
	{
		// Set the flag that the permission was requested
		sharedPreferences.editWasScheduleExactAlarmPermissionRequested(true)

		// Call the accepeted listeners
		super.doPermissionRequestCanceled()
	}

	/**
	 * The name of the permission.
	 */
	override val permission: String
		get() = NacScheduleExactAlarmPermission.permissionName

	/**
	 * The ID of the layout.
	 */
	override val layoutId: Int
		get() = R.layout.dlg_request_schedule_exact_alarm_permission

	/**
	 * The ID of the title string.
	 */
	override val titleId: Int
		get() = R.string.title_request_permission_schedule_exact_alarm

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacScheduleExactAlarmPermissionDialog"

	}

}