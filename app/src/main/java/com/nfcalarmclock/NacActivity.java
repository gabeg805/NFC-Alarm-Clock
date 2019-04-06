package com.nfcalarmclock;

import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

/**
 * Activity class handling setting the permissions result listener and calling
 * the listener.
 */
public class NacActivity
	extends AppCompatActivity
	implements ActivityCompat.OnRequestPermissionsResultCallback
{

	/**
	 * Permissions result listener.
	 */
	protected NacPermissions.OnResultListener mListener;

	/**
	 * @return The result listener.
	 */
	private NacPermissions.OnResultListener getPermissionsResultListener()
	{
		return this.mListener;
	}

	/**
	 * Call the listener if set. This is run when the user has selected
	 * Deny/Grant in the permissions prompt.
	 */
	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions,
		int[] grantResults)
	{
		if (this.getPermissionsResultListener() != null)
		{
			this.getPermissionsResultListener().onResult(requestCode,
				permissions, grantResults);
		}
	}

	/**
	 * Set custom permission result listener.
	 */
	public void setPermissionsResultListener(NacPermissions.OnResultListener listener)
	{
		this.mListener = listener;
	}

}
