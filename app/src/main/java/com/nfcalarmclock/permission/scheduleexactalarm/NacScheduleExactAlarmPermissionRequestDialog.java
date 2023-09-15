package com.nfcalarmclock.permission.scheduleexactalarm;

import android.Manifest;
import android.os.Build;
import androidx.annotation.RequiresApi;
import com.nfcalarmclock.R;
import com.nfcalarmclock.permission.NacPermissionRequestDialog;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Dialog to request to allow scheduling an exact alarm.
 */
@RequiresApi(api=Build.VERSION_CODES.S)
public class NacScheduleExactAlarmPermissionRequestDialog
	extends NacPermissionRequestDialog
{

	/**
	 * Tag for the class.
	 */
	public static final String TAG = "NacScheduleExactAlarmPermissionDialog";

	/**
	 * The actions to execute when the permission request is accepted.
	 */
	protected void doPermissionRequestAccepted()
	{
		NacSharedPreferences shared = this.getSharedPreferences();

		// Set the flag that the permission was requested
		shared.editWasScheduleExactAlarmPermissionRequested(true);

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
		shared.editWasScheduleExactAlarmPermissionRequested(true);

		// Call the accepeted listeners
		super.doPermissionRequestCanceled();
	}

	/**
	 * Get the name of the permission.
	 *
	 * @return The name of the permission.
	 */
	public String getPermission()
	{
		return Manifest.permission.SCHEDULE_EXACT_ALARM;
	}

	/**
	 * Get the ID of the layout.
	 *
	 * @return The ID of the layout.
	 */
	public int getLayoutId()
	{
		return R.layout.dlg_request_schedule_exact_alarm_permission;
	}

	/**
	 * Get the ID of the title string.
	 *
	 * @return The ID of the title string.
	 */
	public int getTitleId()
	{
		return R.string.title_request_permission_schedule_exact_alarm;
	}

}
