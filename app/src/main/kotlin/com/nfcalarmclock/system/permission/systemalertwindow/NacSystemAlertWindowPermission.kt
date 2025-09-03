package com.nfcalarmclock.system.permission.systemalertwindow

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.provider.Settings
import androidx.core.net.toUri
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Helper functions for the SCHEDULE_EXACT_ALARM permission.
 */
@Suppress("SameReturnValue")
object NacSystemAlertWindowPermission
{

	/**
	 * Check if the correct Android version is being used.
	 */
	val isCorrectAndroidVersion: Boolean
		get() {
			// Permission only required for API level >= 33
			return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
					&& (Build.VERSION.SDK_INT <= Build.VERSION_CODES.VANILLA_ICE_CREAM)
		}

	/**
	 * The name of the SYSTEM_ALERT_WINDOW permission.
	 */
	val permissionName: String
		get() = Manifest.permission.SYSTEM_ALERT_WINDOW

	/**
	 * Check if the app has the SYSTEM_ALERT_WINDOW permission.
	 */
	fun hasPermission(context: Context): Boolean
	{
		// Android version not correct so indicate it already has the
		// permission, for simplicity
		if (!isCorrectAndroidVersion)
		{
			return true
		}

		// Check if the app has permission
		return Settings.canDrawOverlays(context)
	}

	/**
	 * Request the SYSTEM_ALERT_WINDOW permission.
	 */
	fun requestPermission(activity: Activity?)
	{
		// Permission not required for API level < 33
		if (!isCorrectAndroidVersion || (activity == null))
		{
			return
		}

		// If not, form up an Intent to launch the permission request
		val uri = ("package:"+activity.packageName).toUri()
		val intent = Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, uri)

		// Launch Intent, with the supplied request code
		activity.startActivity(intent)
	}

	/**
	 * Check whether the app should request the SYSTEM_ALERT_WINDOW permission.
	 *
	 * @return True if the app should request the SYSTEM_ALERT_WINDOW permission,
	 *         and False otherwise.
	 */
	fun shouldRequestPermission(
		context: Context,
		shared: NacSharedPreferences
	): Boolean
	{
		// Android version not correct so indicate it should not request
		// the permission, for simplicity
		return if (!isCorrectAndroidVersion)
		{
			false
		}
		// The app does not already have the permission.
		// The permission has not been requested yet.
		else
		{
			(!hasPermission(context) && !shared.wasSystemAlertWindowPermissionRequested)
		}
	}

}