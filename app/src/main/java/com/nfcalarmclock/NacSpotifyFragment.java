package com.nfcalarmclock;

import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.List;
import com.spotify.android.appremote.api.SpotifyAppRemote;
import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;
import kaaes.spotify.webapi.android.SpotifyApi;
import kaaes.spotify.webapi.android.SpotifyCallback;
import kaaes.spotify.webapi.android.SpotifyError;
import kaaes.spotify.webapi.android.SpotifyService;
import kaaes.spotify.webapi.android.models.Album;
import kaaes.spotify.webapi.android.models.Artist;
import kaaes.spotify.webapi.android.models.ArtistSimple;
import kaaes.spotify.webapi.android.models.Pager;
import kaaes.spotify.webapi.android.models.Playlist;
import kaaes.spotify.webapi.android.models.PlaylistSimple;
import kaaes.spotify.webapi.android.models.PlaylistTrack;
import kaaes.spotify.webapi.android.models.Track;
import kaaes.spotify.webapi.android.models.TrackSimple;
import kaaes.spotify.webapi.android.models.SavedAlbum;
import kaaes.spotify.webapi.android.models.SavedTrack;
import kaaes.spotify.webapi.android.models.UserPrivate;
import retrofit.client.Response;

import android.app.SearchManager;
import android.support.v7.widget.SearchView;

import java.net.URL;
import java.net.HttpURLConnection;
import java.io.InputStream;
import java.io.IOException;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.AsyncTask;
import android.widget.ImageView;
import android.content.res.Resources;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

/**
 */
public class NacSpotifyFragment
	extends NacMediaFragment
	implements View.OnClickListener,
		NacSpotifyCallback.OnRequestSuccessListener,
		Connector.ConnectionListener
{

	/**
	 * Spotify data requester.
	 */
	private NacSpotifyCallback mSpotifyRequester;

	/**
	 * Selected view.
	 */
	private LinearLayout mSelectedView;

	/**
	 * Selected title view.
	 */
	private TextView mSelectedTitleView;

	/**
	 * Playlist view.
	 */
	private LinearLayout mMyPlaylistsView;

	/**
	 * Album view.
	 */
	private LinearLayout mMySavedAlbumsView;

	/**
	 * Track view.
	 */
	private LinearLayout mTopTracksView;

	/**
	 * Artist view.
	 */
	private LinearLayout mTopArtistsView;

	/**
	 */
	public NacSpotifyFragment()
	{
		super();

		this.mSpotifyRequester = null;
		this.mSelectedView = null;
		this.mSelectedTitleView = null;
		this.mMyPlaylistsView = null;
		this.mMySavedAlbumsView = null;
		this.mTopTracksView = null;
		this.mTopArtistsView = null;
	}

	/**
	 * Authenticate spotify.
	 */
	public void authenticateSpotify()
	{
		FragmentActivity activity = getActivity();
		Context context = getContext();

		if (!SpotifyAppRemote.isSpotifyInstalled(context))
		{
			NacUtility.quickToast(context, "Spotify is not installed!");
			//AuthenticationClient.openDownloadSpotifyActivity(activity);
			//activity.finish();
			//return;
		}

		AuthenticationRequest request = new AuthenticationRequest.Builder(
			NacSpotify.CLIENT_ID, AuthenticationResponse.Type.TOKEN,
			NacSpotify.REDIRECT_URI)
			.setScopes(new String[] { 
				"playlist-read-private", 
				"streaming", "user-library-read", 
				"user-read-private", "user-top-read" })
			.build();
		//"app-remote-control"
		//"playlist-read"
		//"user-modify-playback-state"
		//"user-read-recently-played" (BETA)

		Intent intent = AuthenticationClient.createLoginActivityIntent(
			activity, request);
		startActivityForResult(intent, NacSpotify.REQUEST_CODE);
	}

	/**
	 * Download cover images.
	 */
	private NacCoverImage downloadCoverImages(Context context, String id, String name, String image)
	{
		NacCoverImage cover = new NacCoverImage(context);

		cover.setText(name);
		cover.setTag(id);
		cover.setOnClickListener(this);
		new AsyncImageDownload(cover).execute(image);

		return cover;
	}

	/**
	 * @return The playlist view
	 */
	private LinearLayout getMyPlaylistsView()
	{
		return this.mMyPlaylistsView;
	}

	/**
	 * @return The album view.
	 */
	private LinearLayout getMySavedAlbumsView()
	{
		return this.mMySavedAlbumsView;
	}

	/**
	 * @return The view that uses the given request.
	 */
	private LinearLayout getRequestView(NacSpotifyCallback.Request request)
	{
		if (request == NacSpotifyCallback.Request.MY_PLAYLISTS)
		{
			return getMyPlaylistsView();
		}
		else if (request == NacSpotifyCallback.Request.MY_SAVED_ALBUMS)
		{
			return getMySavedAlbumsView();
		}
		else if (request == NacSpotifyCallback.Request.TOP_ARTISTS)
		{
			return getTopArtistsView();
		}
		else if (request == NacSpotifyCallback.Request.TOP_TRACKS)
		{
			return getTopTracksView();
		}
		else if (this.isSelectRequest(request))
		{
			return getSelectedView();
		}
		else
		{
			return null;
		}
	}

	private boolean isSelectRequest(NacSpotifyCallback.Request request)
	{
		return (request == NacSpotifyCallback.Request.PLAYLIST);
	}

	/**
	 * @return The selected view.
	 */
	private LinearLayout getSelectedView()
	{
		return this.mSelectedView;
	}

	/**
	 * @return The selected title view.
	 */
	private TextView getSelectedTitleView()
	{
		return this.mSelectedTitleView;
	}

	/**
	 * @return The artist view.
	 */
	private LinearLayout getTopArtistsView()
	{
		return this.mTopArtistsView;
	}

	/**
	 * @return The track view.
	 */
	private LinearLayout getTopTracksView()
	{
		return this.mTopTracksView;
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
					this.startSpotify(response);
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

	/**
	 */
	@Override
	public void onClick(View view)
	{
		super.onClick(view);

		if (this.isActionButton(view.getId()))
		{
			return;
		}
		else if (view instanceof NacImageSubTextButton)
		{
			NacUtility.printf("Id : %s | Spotify Remote : %b", (String) view.getTag(), mSpotifyRemote != null);
			return;
		}

		NacCoverImage cover = (NacCoverImage) view;
		String title = cover.getText();
		String id = (String) view.getTag();

		NacUtility.printf("Id : %s | Title : %s", id, title);

		getSelectedView().setVisibility(View.VISIBLE);
		getSelectedTitleView().setVisibility(View.VISIBLE);
		getMySavedAlbumsView().setVisibility(View.GONE);
		getMyPlaylistsView().setVisibility(View.GONE);
		getTopArtistsView().setVisibility(View.GONE);
		getTopTracksView().setVisibility(View.GONE);

		getSelectedTitleView().setText(title);

		this.mSpotifyRequester.request(NacSpotifyCallback.Request.PLAYLIST, id);
	}

	/**
	 */
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
		Bundle savedInstanceState)
	{
		return inflater.inflate(R.layout.frg_spotify, container, false);
	}

	@Override
	protected void onInitialSelection()
	{
		super.onInitialSelection();
		this.authenticateSpotify();
	}

	/**
	 */
	@Override
	public void onViewCreated(View view, Bundle savedInstanceState)
	{
		Context context = view.getContext();
		this.mSelectedView = view.findViewById(R.id.selected);
		this.mSelectedTitleView = view.findViewById(R.id.selected_title);
		this.mMyPlaylistsView = view.findViewById(R.id.playlists);
		this.mTopTracksView = view.findViewById(R.id.tracks);
		this.mTopArtistsView = view.findViewById(R.id.artists);
		this.mMySavedAlbumsView = view.findViewById(R.id.albums);

		SearchManager searchManager = (SearchManager) context.getSystemService(
			Context.SEARCH_SERVICE);
		SearchView searchView = (SearchView) view.findViewById(R.id.search);

		searchView.setSearchableInfo(searchManager.getSearchableInfo(
			getActivity().getComponentName()));
		//searchView.setIconified(false);

		setupActionButtons(view);
	}

	/**
	 * Start spotify.
	 */
	private SpotifyAppRemote mSpotifyRemote;

	private void startSpotify(AuthenticationResponse response)
	{
		String token = response.getAccessToken();
		SpotifyApi api = new SpotifyApi().setAccessToken(token);
		SpotifyService spotify = api.getService();
		this.mSpotifyRequester = new NacSpotifyCallback(api, spotify);

		ConnectionParams connectionParams =
			new ConnectionParams.Builder(NacSpotify.CLIENT_ID)
				.setRedirectUri(NacSpotify.REDIRECT_URI)
				.showAuthView(true)
				.build();

		SpotifyAppRemote.connect(getContext(), connectionParams, this);
		this.mSpotifyRequester.setOnRequestSuccessListener(this);
		this.mSpotifyRequester.start();
	}

	@Override
	public void onConnected(SpotifyAppRemote spotifyAppRemote)
	{
		NacUtility.printf("MainActivity WORKEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
		mSpotifyRemote = spotifyAppRemote;
	}

	@Override
	public void onFailure(Throwable throwable)
	{
		NacUtility.printf("MainActivity: %s", throwable.getMessage());
	}

	/**
	 */
	public void onRequestSuccess(NacSpotifyCallback.Request request,
		List<String> ids, List<String> names, List<String> data)
	{
		Context context = getContext();
		LinearLayout root = this.getRequestView(request);

		for (int i=0; i < names.size(); i++)
		{
			String id = ids.get(i);
			String name = names.get(i);
			String info = (data != null) ? data.get(i) : "";
			View view;
			
			if (this.isSelectRequest(request))
			{
				NacUtility.printf("Selected playlist track : %s", name);
				view = new NacImageSubTextButton(context);

				((NacImageSubTextButton)view).setTextTitle(name);
				((NacImageSubTextButton)view).setTextSubtitle(info);
				((NacImageSubTextButton)view).setImageBackground(R.mipmap.play);
				view.setTag(id);
				view.setOnClickListener(this);
			}
			else
			{
				//view = downloadCoverImages(context, id, name, info);
				NacCoverImage cover = new NacCoverImage(context);

				cover.setText(name);
				cover.setTag(id);
				cover.setOnClickListener(this);
				new AsyncImageDownload(cover).execute(info);

				view = cover;
			}

			root.addView(view);
		}

		if (names.size() == 0)
		{
			root.setVisibility(View.GONE);
		}
	}

	/**
	 */
	private class AsyncImageDownload
		extends AsyncTask<String, String, Bitmap>
	{

		/**
		 */
		private NacCoverImage mCover;

		/**
		 */
		public AsyncImageDownload(NacCoverImage cover)
		{
			this.mCover = cover;
		}

		/**
		 */
		@Override
		protected Bitmap doInBackground(String... params)
		{
			Bitmap bitmap = null;

			try
			{
				URL url = new URL(params[0]);
				bitmap = BitmapFactory.decodeStream((InputStream) url.getContent());
			}
			catch (IOException e)
			{
				NacUtility.printf("Unable to download image : %s", params[0]);
			}

			return bitmap;
		}

		/**
		 * @return The cover image view.
		 */
		private NacCoverImage getCoverImage()
		{
			return this.mCover;
		}

		/**
		 */
		@Override
		protected void onPostExecute(Bitmap bitmap)
		{
			Resources res = getResources();
			int width = (int) res.getDimension(R.dimen.isz_frg_spotify_image);
			int height = (int) res.getDimension(R.dimen.isz_frg_spotify_image);
			int spacing = (int) res.getDimension(R.dimen.sp_frg_spotify_cover_text);
			int padding = (int) res.getDimension(R.dimen.p_frg_spotify_cover);
			NacCoverImage cover = this.getCoverImage();

			cover.setSpacing(spacing);
			cover.setPadding(padding, padding, padding, padding);
			cover.setImage(bitmap);
			cover.setImageSize(width, height);
		}

	}

}
