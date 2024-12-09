package com.nfcalarmclock.system.permission.scheduleexactalarm

import android.os.Build
import androidx.annotation.RequiresApi
import com.nfcalarmclock.R
import com.nfcalarmclock.system.permission.NacPermissionRequestDialog

/**
 * Dialog to request to allow scheduling an exact alarm.
 */
@RequiresApi(api = Build.VERSION_CODES.S)
class NacScheduleExactAlarmPermissionRequestDialog
	: NacPermissionRequestDialog()
{

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

	/**
	 * The ID of the text string.
	 */
	override val textId: Int = R.string.message_permission_schedule_exact_alarm_request

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	override fun doPermissionRequestAccepted()
	{
		// Set the flag that the permission was requested
		sharedPreferences!!.wasScheduleExactAlarmPermissionRequested = true

		// Call the accepeted listeners
		super.doPermissionRequestAccepted()
	}

	/**
	 * The actions to execute when the permission request is canceled.
	 */
	override fun doPermissionRequestCanceled()
	{
		// Set the flag that the permission was requested
		sharedPreferences!!.wasScheduleExactAlarmPermissionRequested = true

		// Call the accepeted listeners
		super.doPermissionRequestCanceled()
	}

	companion object
	{

		/**
		 * Tag for the class.
		 */
		const val TAG = "NacScheduleExactAlarmPermissionDialog"

	}

}