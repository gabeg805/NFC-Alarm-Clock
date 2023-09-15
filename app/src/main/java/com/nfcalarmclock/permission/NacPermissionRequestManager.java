package com.nfcalarmclock.permission;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Build;
import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.FragmentManager;
import com.nfcalarmclock.permission.ignorebatteryoptimization.NacIgnoreBatteryOptimizationPermission;
import com.nfcalarmclock.permission.ignorebatteryoptimization.NacIgnoreBatteryOptimizationPermissionRequestDialog;
import com.nfcalarmclock.permission.postnotifications.NacPostNotificationsPermission;
import com.nfcalarmclock.permission.postnotifications.NacPostNotificationsPermissionRequestDialog;
import com.nfcalarmclock.permission.scheduleexactalarm.NacScheduleExactAlarmPermission;
import com.nfcalarmclock.permission.scheduleexactalarm.NacScheduleExactAlarmPermissionRequestDialog;
import com.nfcalarmclock.shared.NacSharedPreferences;
import java.util.EnumSet;

/**
 * TODO: Should I have request code?
 */
public class NacPermissionRequestManager
{

	/**
	 * Enum of all permissions that could be requested.
	 */
	public enum Permission
	{
		IGNORE_BATTERY_OPTIMIZATION,
		POST_NOTIFICATIONS,
		SCHEDULE_EXACT_ALARM
	}

	/**
	 * A set of all the permissions that need to be requested.
	 */
	private EnumSet<Permission> mPermissionRequestSet = EnumSet.noneOf(Permission.class);

	/**
	 * Current position number of the permission being requested.
	 */
	private int mCurrentPosition = 0;

	/**
	 * Total number of permissions that need to be requested.
	 */
	private int mTotalNumberOfPermissions = 0;

	/**
	 * Check if analysis has been completed or not.
	 *
	 * This way it does not need to keep getting repeated if the app goes through
	 * its onStop/onResume lifecycle.
	 */
	private boolean mIsAnalyzed = false;

	/**
	 * Constructor.
	 */
	public NacPermissionRequestManager(AppCompatActivity activity)
	{
		this.analyze(activity);
	}

	/**
	 * Analyze the permissions that need to be requested.
	 */
	public void analyze(Context context)
	{
		NacSharedPreferences shared = new NacSharedPreferences(context);
		EnumSet<Permission>	set = EnumSet.noneOf(Permission.class);

		// Check if analysis has already been completed or not
		if (this.isAnalyzed())
		{
			return;
		}

		// Post notifications
		if (NacPostNotificationsPermission.shouldRequestPermission(context, shared))
		{
			set.add(Permission.POST_NOTIFICATIONS);
		}

		// Schedule exact alarms
		if (NacScheduleExactAlarmPermission.shouldRequestPermission(context, shared))
		{
			set.add(Permission.SCHEDULE_EXACT_ALARM);
		}

		// Ignore battery optimization
		if (NacIgnoreBatteryOptimizationPermission.shouldRequestPermission(context, shared))
		{
			set.add(Permission.IGNORE_BATTERY_OPTIMIZATION);
		}

		// Set the permissions that need to be requested
		this.mPermissionRequestSet = set;

		// Set the current position
		this.mCurrentPosition = 0;

		// Set the total number of permissions that need to be requested
		this.mTotalNumberOfPermissions = set.size();

		// Set the flag indicating that analysis has been completed
		this.mIsAnalyzed = true;
	}

	/**
	 * Get the number of permissions that need to be requested.
	 *
	 * @return The number of permissions that need to be requested.
	 */
	public int count()
	{
		return this.mTotalNumberOfPermissions;
		//return this.getPermissionRequestSet().size();
	}

	/**
	 * Get the current position number of the permission being requested.
	 *
	 * @return The current position number of the permission being requested.
	 */
	public int currentPosition()
	{
		return this.mCurrentPosition;
	}

	/**
	 * Get the set of permissions that need to be requested.
	 *
	 * @return The set of permissions that need to be requested.
	 */
	public EnumSet<Permission> getPermissionRequestSet()
	{
		return this.mPermissionRequestSet;
	}

	/**
	 * Increment the current position.
	 */
	public void incrementCurrentPosition()
	{
		this.mCurrentPosition += 1;
	}

	/**
	 * Check if analysis has been completed or not.
	 *
	 * @return True if analysis has been completed, and False otherwise.
	 */
	public boolean isAnalyzed()
	{
		return this.mIsAnalyzed;
	}

	/**
	 * Request all permissions that need to be requested.
	 */
	public void requestPermissions(AppCompatActivity activity)
	{
		// Analyze which permissions need to be requested
		this.analyze(activity);

		// Show the first permission dialog that should be requested
		this.showNextPermissionRequestDialog(activity);
	}

	/**
	 * Reset all attributes.
	 */
	public void reset()
	{
		this.mPermissionRequestSet = EnumSet.noneOf(Permission.class);
		this.mCurrentPosition = 0;
		this.mTotalNumberOfPermissions = 0;
		this.mIsAnalyzed = false;
	}

	/**
	 * Setup dialog page information.
	 */
	private void setupDialogPageInfo(NacPermissionRequestDialog dialog)
	{
		int position = this.currentPosition();
		int totalNumOfPages = this.count();

		// Set position
		dialog.setPosition(position);

		// Set total number of pages
		dialog.setTotalNumberOfPages(totalNumOfPages);
	}

	/**
	 * Show the next permission request dialog.
	 */
	@SuppressLint("NewApi")
	public void showNextPermissionRequestDialog(AppCompatActivity activity)
	{
		// Get the permission request set
		EnumSet<Permission> permissionRequestSet = this.getPermissionRequestSet();

		// Increment the current position
		this.incrementCurrentPosition();

		// Show the dialog to request the permission to post notifications
		if (permissionRequestSet.contains(Permission.POST_NOTIFICATIONS))
		{
			this.showPostNotificationPermissionDialog(activity);
		}
		// Show the dialog to request the permission schedule an exact alarm
		else if (permissionRequestSet.contains(Permission.SCHEDULE_EXACT_ALARM))
		{
			this.showScheduleExactAlarmPermissionDialog(activity);
		}
		// Check if should ask to ignore battery optimizations
		else if (permissionRequestSet.contains(Permission.IGNORE_BATTERY_OPTIMIZATION))
		{
			this.showIgnoreBatteryOptimizationPermissionDialog(activity);
		}
		// Reset everything
		else
		{
			this.reset();
		}
	}

	/**
	 * Show the dialog to ignore battery optimizations.
	 */
	public void showIgnoreBatteryOptimizationPermissionDialog(
		AppCompatActivity activity)
	{
		// Create the dialog
		NacIgnoreBatteryOptimizationPermissionRequestDialog dialog =
			new NacIgnoreBatteryOptimizationPermissionRequestDialog();

		// Setup the current position and total number of pages in the dialog
		this.setupDialogPageInfo(dialog);

		// Handle the cases where the permission request is accepted/canceled
		dialog.setOnPermissionRequestListener(
			new NacIgnoreBatteryOptimizationPermissionRequestDialog.OnPermissionRequestListener()
			{

				/**
				 * Called when the permission request is accepted.
				 */
				public void onPermissionRequestAccepted(String permission)
				{
					getPermissionRequestSet().remove(Permission.IGNORE_BATTERY_OPTIMIZATION);
					NacIgnoreBatteryOptimizationPermission.requestPermission(activity);
				}

				/**
				 * Called when the permission request was canceled.
				 */
				public void onPermissionRequestCanceled(String permission)
				{
					getPermissionRequestSet().remove(Permission.IGNORE_BATTERY_OPTIMIZATION);
					showNextPermissionRequestDialog(activity);
				}

			});

		// Show the dialog
		FragmentManager fragmentManager = activity.getSupportFragmentManager();

		dialog.show(fragmentManager,
			NacIgnoreBatteryOptimizationPermissionRequestDialog.TAG);
	}

	/**
	 * Show the POST_NOTIFICATIONS permission dialog.
	 */
	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	private void showPostNotificationPermissionDialog(AppCompatActivity activity)
	{
		// Do nothing if an older version of Android is being used
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
		{
			return;
		}

		// Create the dialog
		NacPostNotificationsPermissionRequestDialog dialog =
			new NacPostNotificationsPermissionRequestDialog();

		// Setup the current position and total number of pages in the dialog
		this.setupDialogPageInfo(dialog);

		// Handle the cases where the permission request is accepted/canceled
		dialog.setOnPermissionRequestListener(
			new NacPostNotificationsPermissionRequestDialog.OnPermissionRequestListener()
			{

				/**
				 * Called when the permission request is accepted.
				 */
				@Override
				public void onPermissionRequestAccepted(String permission)
				{
					getPermissionRequestSet().remove(Permission.POST_NOTIFICATIONS);
					NacPostNotificationsPermission.requestPermission(activity, 69);
				}

				/**
				 * Called when the permission request is canceled.
				 */
				@Override
				public void onPermissionRequestCanceled(String permission)
				{
					getPermissionRequestSet().remove(Permission.POST_NOTIFICATIONS);
					showNextPermissionRequestDialog(activity);
				}

			});

		// Show the dialog
		FragmentManager fragmentManager = activity.getSupportFragmentManager();

		dialog.show(fragmentManager,
			NacPostNotificationsPermissionRequestDialog.TAG);
	}

	/**
	 * Show the dialog to request the schedule exact alarm permission.
	 */
	@RequiresApi(api = Build.VERSION_CODES.S)
	public void showScheduleExactAlarmPermissionDialog(AppCompatActivity activity)
	{
		// Do nothing if an older version of Android is being used
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
		{
			return;
		}

		// Create the dialog
		NacScheduleExactAlarmPermissionRequestDialog dialog =
			new NacScheduleExactAlarmPermissionRequestDialog();

		// Setup the current position and total number of pages in the dialog
		this.setupDialogPageInfo(dialog);

		// Handle the cases where the permission request is accepted/canceled
		dialog.setOnPermissionRequestListener(
			new NacScheduleExactAlarmPermissionRequestDialog.OnPermissionRequestListener()
			{

				/**
				 * Called when the permission request is accepted.
				 */
				public void onPermissionRequestAccepted(String permission)
				{
					getPermissionRequestSet().remove(Permission.SCHEDULE_EXACT_ALARM);
					NacScheduleExactAlarmPermission.requestPermission(activity);
				}

				/**
				 * Called when the permission request was canceled.
				 */
				public void onPermissionRequestCanceled(String permission)
				{
					getPermissionRequestSet().remove(Permission.SCHEDULE_EXACT_ALARM);
					showNextPermissionRequestDialog(activity);
				}

			});

		// Show the dialog
		FragmentManager fragmentManager = activity.getSupportFragmentManager();

		dialog.show(fragmentManager,
			NacScheduleExactAlarmPermissionRequestDialog.TAG);
	}

}
