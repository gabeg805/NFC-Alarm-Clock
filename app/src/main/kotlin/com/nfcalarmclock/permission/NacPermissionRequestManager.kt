package com.nfcalarmclock.permission

import android.annotation.SuppressLint
import android.content.Context
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.permission.NacPermissionRequestDialog.OnPermissionRequestListener
import com.nfcalarmclock.permission.ignorebatteryoptimization.NacIgnoreBatteryOptimizationPermission
import com.nfcalarmclock.permission.ignorebatteryoptimization.NacIgnoreBatteryOptimizationPermissionRequestDialog
import com.nfcalarmclock.permission.postnotifications.NacPostNotificationsPermission
import com.nfcalarmclock.permission.postnotifications.NacPostNotificationsPermission.requestPermission
import com.nfcalarmclock.permission.postnotifications.NacPostNotificationsPermissionRequestDialog
import com.nfcalarmclock.permission.scheduleexactalarm.NacScheduleExactAlarmPermission
import com.nfcalarmclock.permission.scheduleexactalarm.NacScheduleExactAlarmPermissionRequestDialog
import com.nfcalarmclock.shared.NacSharedPreferences
import java.util.EnumSet

/**
 * Manage permission requests.
 */
class NacPermissionRequestManager(activity: AppCompatActivity)
{

	/**
	 * Enum of all permissions that could be requested.
	 */
	enum class Permission
	{
		IGNORE_BATTERY_OPTIMIZATION,
		POST_NOTIFICATIONS,
		SCHEDULE_EXACT_ALARM
	}

	/**
	 * A set of all the permissions that need to be requested.
	 */
	private var permissionRequestSet: EnumSet<Permission> = EnumSet.noneOf(Permission::class.java)

	/**
	 * Current position number of the permission being requested.
	 */
	private var currentPosition = 0

	/**
	 * Total number of permissions that need to be requested.
	 */
	private var totalNumberOfPermissions = 0

	/**
	 * Check if analysis has been completed or not.
	 *
	 * This way it does not need to keep getting repeated if the app goes through
	 * its onStop/onResume lifecycle.
	 */
	private var isAnalyzed = false

	/**
	 * Constructor.
	 */
	init
	{
		analyze(activity)
	}

	/**
	 * Analyze the permissions that need to be requested.
	 */
	private fun analyze(context: Context)
	{
		val shared = NacSharedPreferences(context)
		val set = EnumSet.noneOf(Permission::class.java)

		// Check if analysis has already been completed or not
		if (isAnalyzed)
		{
			return
		}

		// Post notifications
		if (NacPostNotificationsPermission.shouldRequestPermission(context, shared))
		{
			set.add(Permission.POST_NOTIFICATIONS)
		}

		// Schedule exact alarms
		if (NacScheduleExactAlarmPermission.shouldRequestPermission(context, shared))
		{
			set.add(Permission.SCHEDULE_EXACT_ALARM)
		}

		// Ignore battery optimization
		if (NacIgnoreBatteryOptimizationPermission.shouldRequestPermission(context, shared))
		{
			set.add(Permission.IGNORE_BATTERY_OPTIMIZATION)
		}

		// Set the permissions that need to be requested
		permissionRequestSet = set

		// Set the current position
		currentPosition = 0

		// Set the total number of permissions that need to be requested
		totalNumberOfPermissions = set.size

		// Set the flag indicating that analysis has been completed
		isAnalyzed = true
	}

	/**
	 * Get the number of permissions that need to be requested.
	 *
	 * @return The number of permissions that need to be requested.
	 */
	fun count(): Int
	{
		return totalNumberOfPermissions
	}

	/**
	 * Increment the current position.
	 */
	private fun incrementCurrentPosition()
	{
		currentPosition += 1
	}

	/**
	 * Request all permissions that need to be requested.
	 */
	fun requestPermissions(activity: AppCompatActivity)
	{
		// Analyze which permissions need to be requested
		analyze(activity)

		// Show the first permission dialog that should be requested
		showNextPermissionRequestDialog(activity)
	}

	/**
	 * Reset all attributes.
	 */
	fun reset()
	{
		permissionRequestSet = EnumSet.noneOf(Permission::class.java)
		currentPosition = 0
		totalNumberOfPermissions = 0
		isAnalyzed = false
	}

	/**
	 * Setup dialog page information.
	 */
	private fun setupDialogPageInfo(dialog: NacPermissionRequestDialog)
	{
		// Set position
		dialog.position = currentPosition

		// Set total number of pages
		dialog.totalNumberOfPages = totalNumberOfPermissions
	}

	/**
	 * Show the next permission request dialog.
	 */
	@SuppressLint("NewApi")
	fun showNextPermissionRequestDialog(activity: AppCompatActivity)
	{
		// Get the permission request set
		val permissionRequestSet = permissionRequestSet

		// Increment the current position
		incrementCurrentPosition()

		// Show the dialog to request the permission to...
		// Post notifications
		if (permissionRequestSet.contains(Permission.POST_NOTIFICATIONS))
		{
			showPostNotificationPermissionDialog(activity)
		}
		// Schedule exact alarm
		else if (permissionRequestSet.contains(Permission.SCHEDULE_EXACT_ALARM))
		{
			showScheduleExactAlarmPermissionDialog(activity)
		}
		// Ignore battery optimization
		else if (permissionRequestSet.contains(Permission.IGNORE_BATTERY_OPTIMIZATION))
		{
			showIgnoreBatteryOptimizationPermissionDialog(activity)
		}
		// Reset
		else
		{
			reset()
		}
	}

	/**
	 * Show the dialog to ignore battery optimizations.
	 */
	private fun showIgnoreBatteryOptimizationPermissionDialog(
		activity: AppCompatActivity)
	{
		// Create the dialog
		val dialog = NacIgnoreBatteryOptimizationPermissionRequestDialog()

		// Setup the current position and total number of pages in the dialog
		setupDialogPageInfo(dialog)

		// Handle the cases where the permission request is accepted/canceled
		dialog.onPermissionRequestListener = object : OnPermissionRequestListener
		{
			/**
			 * Called when the permission request is accepted.
			 */
			override fun onPermissionRequestAccepted(permission: String)
			{
				permissionRequestSet.remove(Permission.IGNORE_BATTERY_OPTIMIZATION)
				NacIgnoreBatteryOptimizationPermission.requestPermission(activity)
			}

			/**
			 * Called when the permission request was canceled.
			 */
			override fun onPermissionRequestCanceled(permission: String)
			{
				permissionRequestSet.remove(Permission.IGNORE_BATTERY_OPTIMIZATION)
				showNextPermissionRequestDialog(activity)
			}
		}

		// Show the dialog
		dialog.show(activity.supportFragmentManager,
			NacIgnoreBatteryOptimizationPermissionRequestDialog.TAG)
	}

	/**
	 * Show the POST_NOTIFICATIONS permission dialog.
	 */
	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	fun showPostNotificationPermissionDialog(activity: AppCompatActivity)
	{
		// Do nothing if an older version of Android is being used
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
		{
			return
		}

		// Create the dialog
		val dialog = NacPostNotificationsPermissionRequestDialog()

		// Setup the current position and total number of pages in the dialog
		setupDialogPageInfo(dialog)

		// Handle the cases where the permission request is accepted/canceled
		dialog.onPermissionRequestListener = object : OnPermissionRequestListener
		{
			/**
			 * Called when the permission request is accepted.
			 */
			override fun onPermissionRequestAccepted(permission: String)
			{
				permissionRequestSet.remove(Permission.POST_NOTIFICATIONS)
				requestPermission(activity, 69)
			}

			/**
			 * Called when the permission request is canceled.
			 */
			override fun onPermissionRequestCanceled(permission: String)
			{
				permissionRequestSet.remove(Permission.POST_NOTIFICATIONS)
				showNextPermissionRequestDialog(activity)
			}
		}

		// Show the dialog
		dialog.show(activity.supportFragmentManager,
			NacPostNotificationsPermissionRequestDialog.TAG)
	}

	/**
	 * Show the dialog to request the schedule exact alarm permission.
	 */
	@RequiresApi(api = Build.VERSION_CODES.S)
	fun showScheduleExactAlarmPermissionDialog(activity: AppCompatActivity)
	{
		// Do nothing if an older version of Android is being used
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
		{
			return
		}

		// Create the dialog
		val dialog = NacScheduleExactAlarmPermissionRequestDialog()

		// Setup the current position and total number of pages in the dialog
		setupDialogPageInfo(dialog)

		// Handle the cases where the permission request is accepted/canceled
		dialog.onPermissionRequestListener = object : OnPermissionRequestListener
		{
			/**
			 * Called when the permission request is accepted.
			 */
			override fun onPermissionRequestAccepted(permission: String)
			{
				permissionRequestSet.remove(Permission.SCHEDULE_EXACT_ALARM)
				NacScheduleExactAlarmPermission.requestPermission(activity)
			}

			/**
			 * Called when the permission request was canceled.
			 */
			override fun onPermissionRequestCanceled(permission: String)
			{
				permissionRequestSet.remove(Permission.SCHEDULE_EXACT_ALARM)
				showNextPermissionRequestDialog(activity)
			}
		}

		// Show the dialog
		dialog.show(activity.supportFragmentManager,
			NacScheduleExactAlarmPermissionRequestDialog.TAG)
	}

}