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
	 * Check if the app has the SCHEDULE_EXACT_ALARM permission.
	 */
	public static boolean hasPermission(@NonNull Context context)
	{
		// Android version not correct so indicate it already has the
		// permission, for simplicity
		if (!isCorrectAndroidVersion())
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
	 * Check if the correct Android version is being used.
	 *
	 * @return True if the correct Android version is being used, and False
	 *         otherwise.
	 */
	public static boolean isCorrectAndroidVersion()
	{
		// Permission only required for API level 31 <= x < 33
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
			&& (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU);
	}

	/**
	 * Request the SCHEDULE_EXACT_ALARM permission.
	 */
	public static void requestPermission(@NonNull Activity activity)
	{
		// Android version not correct so indicate it should not request
		// the permission, for simplicity
		if (!isCorrectAndroidVersion())
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
		// Android version not correct so indicate it should not request
		// the permission, for simplicity
		if (!isCorrectAndroidVersion())
		{
			return false;
		}

		// The app does not already have the permission.
		// The permission has not been requested yet.
		return !hasPermission(context)
			&& !shared.getWasScheduleExactAlarmPermissionRequested();
	}

}
