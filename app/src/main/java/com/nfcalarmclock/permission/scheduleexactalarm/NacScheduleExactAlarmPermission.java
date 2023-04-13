package com.nfcalarmclock.permission.scheduleexactalarm;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;

import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.content.ContextCompat;

import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Helper functions for the SCHEDULE_EXACT_ALARM permission.
 */
public class NacScheduleExactAlarmPermission
{

	/**
	 * Get the name of the SCHEDULE_EXACT_ALARM permission.
	 *
	 * @return The name of the SCHEDULE_EXACT_ALARM permission.
	 */
	@RequiresApi(api = Build.VERSION_CODES.S)
	public static String getPermissionName()
	{
		return Manifest.permission.SCHEDULE_EXACT_ALARM;
	}

	/**
	 * Check if the app has the SCHEDULE_
	 */
	public static boolean hasPermission(@NonNull Context context)
	{
		// Permission not required for API level < 33, so indicate that the app
		// already has the permission, for simplicity
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
			|| (Build.VERSION.SDK_INT > Build.VERSION_CODES.S_V2))
		{
			return true;
		}

		// Get the name of the permission
		String permission = getPermissionName();

		// Check if the app has permission to read external storage/media audio
		// (depending on version)
		return ContextCompat.checkSelfPermission(context, permission)
			== PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * Request the SCHEDULE_EXACT_ALARM permission.
	 */
	public static void requestPermission(@NonNull Activity activity)
	{
		// Permission not required for API level < 31
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
		{
			return;
		}

		// Start the intent to facilitate the user enabling the permission
		Intent intent = new Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM);

		activity.startActivity(intent);
	}

	/**
	 * Check whether the app should request the SCHEDULE_EXACT_ALARM permission.
	 *
	 * @return True if the app should request the SCHEDULE_EXACT_ALARM permission,
	 *         and False otherwise.
	 */
	public static boolean shouldRequestPermission(@NonNull Context context,
		NacSharedPreferences shared)
	{
		// Schedule Exact Alarms permission is only applicable to API 31 and 32.
		if ((Build.VERSION.SDK_INT < Build.VERSION_CODES.S)
			|| (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU))
		{
			return false;
		}

		// Get the name of the permission
		String permission = getPermissionName();

		// The app does not already have the permission.
		// The permission has not been requested yet.
		return !hasPermission(context)
			&& !shared.getWasScheduleExactAlarmPermissionRequested();
	}

}
