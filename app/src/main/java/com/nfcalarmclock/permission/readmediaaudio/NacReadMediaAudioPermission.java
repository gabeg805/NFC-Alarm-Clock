package com.nfcalarmclock.permission.readmediaaudio;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Helper functions for the READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE permission.
 */
public class NacReadMediaAudioPermission
{

	/**
	 * Get the name of the READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE permission.
	 *
	 * @return The name of the READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE
	 *         permission.
	 */
	public static String getPermissionName()
	{
		// Define the permission string based on which version of Android is
		// running. Later versions need the more granular READ_MEDIA_AUDIO
		// permission whereas older versions can request READ_EXTERNAL_STORAGE
		// which allows an app to see a lot more files that are stored on the
		// user's phone
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			return Manifest.permission.READ_MEDIA_AUDIO;
		}
		else
		{
			return Manifest.permission.READ_EXTERNAL_STORAGE;
		}
	}

	/**
	 * Check if the app has the READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE
	 * permission or not.
	 *
	 * @return True if the app has the READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE
	 *         permission, and False otherwise.
	 */
	public static boolean hasPermission(Context context)
	{
		// Get the name of the permission
		String permission = getPermissionName();

		// Check if the app has permission to read external storage/media audio
		// (depending on version)
		return ContextCompat.checkSelfPermission(context, permission)
				== PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * Request the READ_MEDIA_AUDIO/READ_EXTERNAL_STORAGE permission.
	 */
	public static void requestPermission(Activity activity, int requestCode)
	{
		// Get the name of the permission
		String permission = getPermissionName();

		// Request the permission
		ActivityCompat.requestPermissions(activity,
			new String[] { permission }, requestCode);
	}

}
