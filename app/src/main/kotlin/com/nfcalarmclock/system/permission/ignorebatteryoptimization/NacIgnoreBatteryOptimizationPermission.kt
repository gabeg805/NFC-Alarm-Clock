package com.nfcalarmclock.system.permission.ignorebatteryoptimization

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.PowerManager
import android.provider.Settings
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Helper functions for ignoring battery optimization.
 */
object NacIgnoreBatteryOptimizationPermission
{

	/**
	 * Check if the correct Android version is being used.
	 */
	private val isCorrectAndroidVersion: Boolean
		get() {
			// Permission only required for API level >= 28
			return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P
		}

	/**
	 * Check if the app has the permission to ignore battery optimizations.
	 */
	fun hasPermission(context: Context): Boolean
	{
		// Permission not required for API level < 28, so indicate that the app
		// already has the permission, for simplicity
		if (!isCorrectAndroidVersion)
		{
			return true
		}

		// Get system level attributes
		val powerManager = context.getSystemService(Context.POWER_SERVICE) as PowerManager

		// Check if the app has the permission to ignore battery optimizations
		return powerManager.isIgnoringBatteryOptimizations(context.packageName)
	}

	/**
	 * Request permission from the user to ignore battery optimizations.
	 */
	@JvmStatic
	fun requestPermission(activity: Activity)
	{
		// Permission not required for API level < 28
		if (!isCorrectAndroidVersion)
		{
			return
		}

		// Start the intent to ignore battery optimization
		val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)

		activity.startActivity(intent)
	}

	/**
	 * Check whether the app has permission to ignore battery optimizations.
	 *
	 * @return True if the app has permission to ignore battery optimizations
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
				&& !shared.wasIgnoreBatteryOptimizationPermissionRequested)
		}
	}

}