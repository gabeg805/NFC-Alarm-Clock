package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
//import android.support.v4.content.ContextCompat;
//import android.support.v4.app.ActivityCompat;
//import android.support.v7.app.AppCompatActivity;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Permissions class handling checks, listeners, etc.
 */
public class NacPermissions
{

	/**
	 * Result listener.
	 */
	public interface OnResultListener
	{
		public void onResult(int request, String[] permissions, int[] grant);
	}

	/**
	 * Check if the app has READ_EXTERNAL_STORAGE permissions.
	 */
	public static boolean hasRead(Context context)
	{
		return ContextCompat.checkSelfPermission(context,
			Manifest.permission.READ_EXTERNAL_STORAGE)
			== PackageManager.PERMISSION_GRANTED;
	}

	/**
	 * Request permission.
	 */
	public static void request(Context context, String permission, int result)
	{
		ActivityCompat.requestPermissions((Activity) context,
			new String[] { permission }, result);
	}

	/**
	 * Prompt the user to set the READ_EXTERNAL_STORAGE permissions.
	 */
	public static int setRead(Context context)
	{
		return NacPermissions.setRead((Activity)context);
	}

	/**
	 * Prompt the user to set the READ_EXTERNAL_STORAGE permissions.
	 */
	public static int setRead(Activity activity)
	{
		// Permission is not granted
		// Should we show an explanation?
		if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
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
			ActivityCompat.requestPermissions(activity,
				new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1);

			// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
			// app-defined int constant. The callback method gets the
			// result of the request.
			return 0;
		}
	}

	/**
	 * Set the permissions result listener.
	 */
	public static void setResultListener(Context context,
		OnResultListener listener)
	{
		((NacActivity)context).setPermissionsResultListener(listener);
	}

}
