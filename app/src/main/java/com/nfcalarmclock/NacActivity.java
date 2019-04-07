package com.nfcalarmclock;

import android.content.Intent;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

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
	public void setPermissionsResultListener(
		NacPermissions.OnResultListener listener)
	{
		this.mListener = listener;
	}

	/**
	 */
	@Override
	protected void onActivityResult(int requestCode, int resultCode,
		Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		// Check if result comes from the correct activity
		if (requestCode == NacSpotify.REQUEST_CODE)
		{
			AuthenticationResponse response = AuthenticationClient
				.getResponse(resultCode, intent);

			switch (response.getType())
			{
				// Response was successful and contains auth token
				case TOKEN:
					// Handle successful response
					NacUtility.quickToast(this, "Handle successful response!");
					break;

				// Auth flow returned an error
				case ERROR:
					// Handle error response
					NacUtility.quickToast(this, "Handle error response!");
					break;

				// Most likely auth flow was cancelled
				default:
					// Handle other cases
					NacUtility.quickToast(this, "Handle other response!");
					break;
			}
		}
	}

}
