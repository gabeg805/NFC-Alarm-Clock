package com.nfcalarmclock.permission.postnotifications;

import android.app.Activity;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Helper functions for the POST_NOTIFICATIONS permission.
 */
public class NacPostNotificationsPermission
{

	/**
	 * Get the name of the POST_NOTIFICATIONS permission.
	 *
	 * @return The name of the POST_NOTIFICATIONS permission.
	 */
	@RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	public static String getPermissionName()
	{
		return Manifest.permission.POST_NOTIFICATIONS;
	}

	/**
	 * Check if the app has the POST_NOTIFICATIONS permission or not.
	 * <p>
	 * If the phone is not at the required API level, this counts as having the
	 * permission, for simplicity.
	 *
	 * @return True if the app has the POST_NOTIFICATIONS permission, and False
	 *         otherwise.
	 */
	public static boolean hasPermission(@NonNull Context context)
	{
		// Permission not required for API level < 33, so indicate that the app
		// already has the permission, for simplicity
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
		// Permission only required for API level >= 33
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU;
	}

	/**
	 * Request the POST_NOTIFICATIONS permission.
	 */
	public static void requestPermission(Activity activity, int requestCode)
	{
		// Permission not required for API level < 33
		if (!isCorrectAndroidVersion())
		{
			return;
		}

		// Get the name of the permission
		String permission = getPermissionName();

		// Request the permission
		ActivityCompat.requestPermissions(activity, new String[] { permission },
			requestCode);
	}

	/**
	 * Check whether the app should request the POST_NOTIFICATIONS permission.
	 *
	 * @return True if the app should request the POST_NOTIFICATIONS permission,
	 *         and False otherwise.
	 */
	public static boolean shouldRequestPermission(@NonNull Context context,
		NacSharedPreferences shared)
	{
		// Permission not required for API level < 33
		if (!isCorrectAndroidVersion())
		{
			return false;
		}

		// The app does not already have the permission.
		// The permission has not been requested yet.
		return !hasPermission(context)
			&& !shared.getWasPostNotificationsPermissionRequested();
	}

}

