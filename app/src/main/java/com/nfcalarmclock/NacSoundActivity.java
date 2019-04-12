package com.nfcalarmclock;

import android.app.Activity;
import android.net.Uri;
import android.content.Intent;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

/**
 * Display options for selecting an alarm sound.
 */
public class NacSoundActivity
	extends Activity
{

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

	/**
	 */
	@Override
	protected void onNewIntent(Intent intent)
	{
		super.onNewIntent(intent);

		Uri uri = intent.getData();

		if (uri == null)
		{
			return;
		}

		AuthenticationResponse response = AuthenticationResponse.fromUri(uri);

		switch (response.getType())
		{
			// Response was successful and contains auth token
			case TOKEN:
				// Handle successful response
				NacUtility.quickToast(this, "Successful response!");
				break;

			// Auth flow returned an error
			case ERROR:
				// Handle error response
				NacUtility.quickToast(this, "Error response!");
				break;

			// Most likely auth flow was cancelled
			default:
				// Handle other cases
				NacUtility.quickToast(this, "Other response!");
				break;
		}
	}


}
