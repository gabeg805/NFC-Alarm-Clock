package com.nfcalarmclock.app;

import android.app.Activity;
import android.content.Context;
import android.content.pm.PackageManager;
import android.Manifest;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

/**
 * Permissions class handling checks, listeners, etc.
 */
@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class NacPermissions
{

	/**
	 * @return True if the app has READ_EXTERNAL_STORAGE permissions, and False
	 *         otherwise.
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
	public static void request(Context context, String permission,
		int requestCode)
	{
		ActivityCompat.requestPermissions((Activity) context,
			new String[] { permission }, requestCode);
	}

	/**
	 * Request read permissions.
	 */
	public static void requestRead(Context context, int requestCode)
	{
		NacPermissions.request(context,
			Manifest.permission.READ_EXTERNAL_STORAGE, requestCode);
	}

	///**
	// * Prompt the user to set the READ_EXTERNAL_STORAGE permissions.
	// */
	//public static int setRead(Activity activity)
	//{
	//	// Permission is not granted
	//	// Should we show an explanation?
	//	if (ActivityCompat.shouldShowRequestPermissionRationale(activity,
	//			Manifest.permission.READ_EXTERNAL_STORAGE))
	//	{
	//		NacUtility.print("Should show request permission rationale.");
	//		// Show an explanation to the user *asynchronously* -- don't block
	//		// this thread waiting for the user's response! After the user
	//		// sees the explanation, try again to request the permission.
	//		return -1;
	//	}
	//	else
	//	{
	//		NacUtility.print("NOT Should show request permission rationale.");
	//		// No explanation needed; request the permission
	//		ActivityCompat.requestPermissions(activity,
	//			new String[] { Manifest.permission.READ_EXTERNAL_STORAGE }, 1);

	//		// MY_PERMISSIONS_REQUEST_READ_CONTACTS is an
	//		// app-defined int constant. The callback method gets the
	//		// result of the request.
	//		return 0;
	//	}
	//}

}
