package com.nfcalarmclock;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import java.util.ArrayList;
import java.util.List;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import android.net.Uri;
import android.content.Intent;

import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyService;

/**
 */
public class NacSpotifyFragment
	extends NacMediaFragment
	implements View.OnClickListener,
		Connector.ConnectionListener
{

	/**
	 * Spotify remote.
	 */
	private SpotifyAppRemote mSpotifyRemote;

	/**
	 * Spotify authentication token.
	 */
	private String mToken;

	/**
	 */
	public NacSpotifyFragment()
	{
		super();

		this.mSpotifyRemote = null;
		this.mToken = null;
	}

	/**
	 */
	private void connected()
	{
		// Play a playlist
		mSpotifyRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");
	}

	/**
	 * Create a new instance of this fragment.
	 */
	public static Fragment newInstance(NacAlarm alarm)
	{
		Fragment fragment = new NacSpotifyFragment();
		Bundle bundle = NacAlarmParcel.toBundle(alarm);

		fragment.setArguments(bundle);

		return fragment;
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		super.onClick(view);

		int id = view.getId();
	}

	/**
	 */
	@Override
	public void onConnected(SpotifyAppRemote spotifyAppRemote)
	{
		this.mSpotifyRemote = spotifyAppRemote;

		connected();
	}

	/**
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.frg_spotify, container, false);
	}

	/**
	 */
	@Override
	public void onFailure(Throwable throwable)
	{
		NacUtility.quickToast(getContext(), "Failed to get connection params"+throwable.getMessage());
		NacUtility.printf("Failed to get connection params %s", throwable.getMessage());

		// Something went wrong when attempting to connect! Handle errors here
	}

	/**
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		setupActionButtons(view);
	}

	/**
	 * Setup spotify.
	 */
	public void setupSpotify()
	{
		FragmentActivity activity = getActivity();
		Context context = getContext();

		if (!SpotifyAppRemote.isSpotifyInstalled(context))
		{
			AuthenticationClient.openDownloadSpotifyActivity(activity);
			activity.finish();
			return;
		}

		AuthenticationRequest request = new AuthenticationRequest.Builder(
			NacSpotify.CLIENT_ID, AuthenticationResponse.Type.TOKEN,
			NacSpotify.REDIRECT_URI)
			.setScopes(new String[] { "app-remote-control", "playlist-read",
				"playlist-read-private", "user-read-recently-played",
				"streaming", "user-library-read", "user-modify-playback-state",
				"user-read-private", "user-top-read" })
			.setShowDialog(true)
			.build();

		Intent intent = AuthenticationClient.createLoginActivityIntent(
			getActivity(), request);
		startActivityForResult(intent, NacSpotify.REQUEST_CODE);
	}

	//public void connectSpotify()
	//{
	//	ConnectionParams connectionParams =
	//		new ConnectionParams.Builder(NacSpotify.CLIENT_ID)
	//			.setRedirectUri(NacSpotify.REDIRECT_URI)
	//			.showAuthView(true)
	//			.build();

	//	SpotifyAppRemote.connect(getContext(), connectionParams, this);
	//}

	/**
	 */
	@Override
	public void onActivityResult(int requestCode, int resultCode,
		Intent intent)
	{
		super.onActivityResult(requestCode, resultCode, intent);

		// Check if result comes from the correct activity
		if (requestCode == NacSpotify.REQUEST_CODE)
		{
			AuthenticationResponse response = AuthenticationClient.getResponse(
				resultCode, intent);

			switch (response.getType())
			{
				// Response was successful and contains auth token
				case TOKEN:
					NacUtility.quickToast(getContext(), "Successful actvity result!");
					NacUtility.printf("Successful actvity result! Expires : %d", response.getExpiresIn());
					this.mToken = response.getAccessToken();

					// Continue with stuff
					SpotifyApi api = new SpotifyApi();

					api.setAccessToken(this.mToken);

					SpotifyService spotify = api.getService();

					break;

				// Auth flow returned an error
				case ERROR:
					NacUtility.quickToast(getContext(), "ERROR actvity result! %s"+response.getError());
					NacUtility.printf("ERROR actvity result! %s", response.getError());
					break;

				// Most likely auth flow was cancelled
				default:
					NacUtility.quickToast(getContext(), "Default actvity result!");
					NacUtility.printf("Default actvity result!");
					break;
			}
		}
	}

}
