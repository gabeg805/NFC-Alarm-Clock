package com.nfcalarmclock.permission.scheduleexactalarm

import android.Manifest
import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.provider.Settings
import androidx.annotation.RequiresApi
import androidx.core.content.ContextCompat
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Helper functions for the SCHEDULE_EXACT_ALARM permission.
 */
object NacScheduleExactAlarmPermission
{

	/**
	 * Check if the correct Android version is being used.
	 */
	val isCorrectAndroidVersion: Boolean
		get() {
			// Permission only required for API level 31 <= x < 33
			return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
				&& (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU)
		}

	/**
	 * The name of the SCHEDULE_EXACT_ALARM permission.
	 */
	@get:RequiresApi(api = Build.VERSION_CODES.S)
	val permissionName: String
		get() = Manifest.permission.SCHEDULE_EXACT_ALARM

	/**
	 * Check if the app has the SCHEDULE_EXACT_ALARM permission.
	 */
	fun hasPermission(context: Context): Boolean
	{
		// Android version not correct so indicate it already has the
		// permission, for simplicity
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
	 * Request the SCHEDULE_EXACT_ALARM permission.
	 */
	@JvmStatic
	fun requestPermission(activity: Activity)
	{
		// Android version not correct so indicate it should not request
		// the permission, for simplicity
		if (!isCorrectAndroidVersion)
		{
			return
		}

		// Start the intent to facilitate the user enabling the permission
		val intent = Intent(Settings.ACTION_REQUEST_SCHEDULE_EXACT_ALARM)

		activity.startActivity(intent)
	}

	/**
	 * Check whether the app should request the SCHEDULE_EXACT_ALARM permission.
	 *
	 * @return True if the app should request the SCHEDULE_EXACT_ALARM permission,
	 *         and False otherwise.
	 */
	@JvmStatic
	fun shouldRequestPermission(context: Context,
		shared: NacSharedPreferences): Boolean
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
			(!hasPermission(context)
				&& !shared.wasScheduleExactAlarmPermissionRequested)
		}
	}

}