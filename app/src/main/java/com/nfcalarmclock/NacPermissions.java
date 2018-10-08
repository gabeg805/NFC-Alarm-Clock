package com.nfcalarmclock;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.support.v4.content.ContextCompat;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * @brief NFC Alarm Clock Utility class.
 * 
 * @details Composed of static methods that can be used for various things.
 */
public class NacPermissions
{

	public interface OnResultListener
	{
		public void onResult(int request, String[] permissions, int[] grant);
	}

	/**
	 * @brief Check if the app has READ_EXTERNAL_STORAGE permissions.
	 */
	public static boolean hasRead(Context c)
	{
		return ContextCompat.checkSelfPermission(c,
			Manifest.permission.READ_EXTERNAL_STORAGE)
			== PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * @brief Prompt the user to set the READ_EXTERNAL_STORAGE permissions.
	 */
	public static int setRead(Context c)
	{
		return NacPermissions.setRead((AppCompatActivity)c);
	}

	/**
	 * @brief Prompt the user to set the READ_EXTERNAL_STORAGE permissions.
	 */
	public static int setRead(AppCompatActivity a)
	{
		// Permission is not granted
		// Should we show an explanation?
		if (ActivityCompat.shouldShowRequestPermissionRationale(a,
				Manifest.permission.READ_EXTERNAL_STORAGE))
		{
			NacUtility.print("Should show request permission rationale.");
			// Show an explanation to the user *asynchronously* -- don't block
			// this thread waiting for the user's response! After the user
			// sees the explanation, try again to request the permission.
			return -1;
		}
		else
		{
			NacUtility.print("NOT Should show request permission rationale.");
			// No explanation needed; request the permission
			ActivityCompat.requestPermissions(a, new String[]
				{
					Manifest.permission.READ_EXTERNAL_STORAGE
				}, 1);

			// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
			// app-defined int constant. The callback method gets the
			// result of the request.
			return 0;
		}
	}

}
