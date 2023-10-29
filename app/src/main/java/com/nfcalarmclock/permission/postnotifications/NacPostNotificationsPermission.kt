package com.nfcalarmclock.permission.postnotifications

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Helper functions for the POST_NOTIFICATIONS permission.
 */
object NacPostNotificationsPermission
{

	/**
	 * Check if the correct Android version is being used.
	 */
	val isCorrectAndroidVersion: Boolean
		get() {
			// Permission only required for API level >= 33
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
		}

	/**
	 * The name of the POST_NOTIFICATIONS permission.
	 */
	@get:RequiresApi(api = Build.VERSION_CODES.TIRAMISU)
	val permissionName: String
		get() = Manifest.permission.POST_NOTIFICATIONS

	/**
	 * Check if the app has the POST_NOTIFICATIONS permission or not.
	 *
	 * If the phone is not at the required API level, this counts as having the
	 * permission, for simplicity.
	 *
	 * @return True if the app has the POST_NOTIFICATIONS permission, and False
	 *         otherwise.
	 */
	fun hasPermission(context: Context): Boolean
	{
		// Permission not required for API level < 33, so indicate that the app
		// already has the permission, for simplicity
		if (!isCorrectAndroidVersion)
		{
			return true
		}

		// Check if the app has permission to read external storage/media audio
		// (depending on version)
		return (ContextCompat.checkSelfPermission(context, permissionName)
			== PackageManager.PERMISSION_GRANTED)
	}

	/**
	 * Request the POST_NOTIFICATIONS permission.
	 */
	@JvmStatic
	fun requestPermission(activity: Activity?, requestCode: Int)
	{
		// Permission not required for API level < 33
		if (!isCorrectAndroidVersion)
		{
			return
		}

		// Request the permission
		ActivityCompat.requestPermissions(activity!!, arrayOf(permissionName),
			requestCode)
	}

	/**
	 * Check whether the app should request the POST_NOTIFICATIONS permission.
	 *
	 * @return True if the app should request the POST_NOTIFICATIONS permission,
	 *         and False otherwise.
	 */
	@JvmStatic
	fun shouldRequestPermission(context: Context,
		shared: NacSharedPreferences): Boolean
	{
		// Permission not required for API level < 33
		return if (!isCorrectAndroidVersion)
		{
			false
		}
		// The app does not already have the permission.
		// The permission has not been requested yet.
		else
		{
			(!hasPermission(context)
				&& !shared.wasPostNotificationsPermissionRequested)
		}
	}

}