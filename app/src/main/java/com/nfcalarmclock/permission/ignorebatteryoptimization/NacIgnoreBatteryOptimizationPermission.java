package com.nfcalarmclock.permission.ignorebatteryoptimization;

import static android.content.Context.POWER_SERVICE;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.PowerManager;
import android.provider.Settings;
import androidx.annotation.NonNull;
import com.nfcalarmclock.shared.NacSharedPreferences;

/**
 * Helper functions for ignoring battery optimization.
 */
public class NacIgnoreBatteryOptimizationPermission
{

	/**
	 * Check if the app has the permission to ignore battery optimizations.
	 */
	public static boolean hasPermission(@NonNull Context context)
	{
		// Permission not required for API level < 28, so indicate that the app
		// already has the permission, for simplicity
		if (!isCorrectAndroidVersion())
		{
			return true;
		}

		// Get system level attributes
		String packageName = context.getPackageName();
		PowerManager powerManager = (PowerManager) context.getSystemService(POWER_SERVICE);

		// Check if the app has the permission to ignore battery optimizations
		return powerManager.isIgnoringBatteryOptimizations(packageName);
	}

	/**
	 * Check if the correct Android version is being used.
	 *
	 * @return True if the correct Android version is being used, and False
	 *         otherwise.
	 */
	public static boolean isCorrectAndroidVersion()
	{
		// Permission only required for API level >= 28
		return Build.VERSION.SDK_INT >= Build.VERSION_CODES.P;
	}

	/**
	 * Request permission from the user to ignore battery optimizations.
	 */
	public static void requestPermission(@NonNull Activity activity)
	{
		// Permission not required for API level < 28
		if (!isCorrectAndroidVersion())
		{
			return;
		}

		// Start the intent to ignore battery optimization
		Intent intent = new Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS);

		activity.startActivity(intent);
	}

	/**
	 * Check whether the app has permission to ignore battery optimizations.
	 *
	 * @return True if the app has permission to ignore battery optimizations
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
			&& !shared.getWasIgnoreBatteryOptimizationPermissionRequested();
	}

}
