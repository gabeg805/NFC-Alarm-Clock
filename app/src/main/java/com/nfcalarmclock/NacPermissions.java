package com.nfcalarmclock;

import android.annotation.TargetApi;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.Manifest;
import android.net.Uri;
import android.os.Build;
import android.provider.Settings;
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
	 * @return True if the app has SYSTEM_ALERT_WINDOW permissions, and False
	 *         otherwise.
	 */
	@TargetApi(Build.VERSION_CODES.M)
	public static boolean hasDrawOverlay(Context context)
	{
		return (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) ?
			Settings.canDrawOverlays(context) : true;
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
	 */
	public static void requestDrawOverlay(Activity activity, int requestCode)
	{
		Intent intent = new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
			Uri.parse("package:" + activity.getPackageName()));

		activity.startActivityForResult(intent, requestCode);
	}

	/**
	 * Prompt the user to set the READ_EXTERNAL_STORAGE permissions.
	 */
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
