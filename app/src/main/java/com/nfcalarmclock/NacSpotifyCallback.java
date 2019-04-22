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
import kaaes.spotify.webapi.android.models.Image;
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

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.android.appremote.api.error.AuthenticationFailedException;
import com.spotify.android.appremote.api.error.CouldNotFindSpotifyApp;
import com.spotify.android.appremote.api.error.LoggedOutException;
import com.spotify.android.appremote.api.error.NotLoggedInException;
import com.spotify.android.appremote.api.error.OfflineModeException;
import com.spotify.android.appremote.api.error.SpotifyConnectionTerminatedException;
import com.spotify.android.appremote.api.error.SpotifyDisconnectedException;
import com.spotify.android.appremote.api.error.SpotifyRemoteServiceException;
import com.spotify.android.appremote.api.error.UnsupportedFeatureVersionException;
import com.spotify.android.appremote.api.error.UserNotAuthorizedException;

import java.util.concurrent.TimeUnit;

/**
 * Method call order:
 *
 * start() -> requestSuccess() -> onRequestSuccess() -> nextRequest() -> request()
 * request() -> requestSuccess() -> onRequestSuccess()
 */
public class NacSpotifyCallback
	implements Connector.ConnectionListener
{

	/**
	 * Listener for when a request fails.
	 */
	public interface OnRequestFailedListener
	{
		public void onRequestFailed(SpotifyError error);
	}

	/**
	 * Listener for when a request succeeds.
	 */
	public interface OnRequestSuccessListener
	{
		public void onRequestSuccess(Request request, List<String> ids,
			List<String> names, List<String> data);
	}

	/**
	 * The request type when getting data from Spotify.
	 */
	public enum Request
	{
		ALBUM,
		ARTIST,
		MY_PLAYLISTS,
		MY_SAVED_ALBUMS,
		NONE,
		PLAYLIST,
		TOP_ARTISTS,
		TOP_TRACKS
	}

	/**
	 * The application context.
	 */
	private final Context mContext;

	/**
	 * Spotify API.
	 */
	private final SpotifyApi mSpotifyApi;

	/**
	 * Spotify service.
	 */
	private final SpotifyService mSpotifyService;

	/**
	 * Spotify remote.
	 */
	private SpotifyAppRemote mSpotifyRemote;

	/**
	 * User ID.
	 */
	private String mUserId;

	/**
	 * Order in which to run the Spotify requests.
	 */
	private Request[] mRequestOrder;

	/**
	 * Request order position to tell which callback to run.
	 */
	private int mRequestPosition;

	/**
	 */
	private final long DEFAULT_SLEEP = 0;

	/**
	 * Request failed listener.
	 */
	private OnRequestFailedListener mRequestFailedListener;

	/**
	 * Request success listener.
	 */
	private OnRequestSuccessListener mRequestSuccessListener;

	/**
	 */
	public NacSpotifyCallback(Context context, SpotifyApi api,
		SpotifyService service)
	{
		this.mContext = context;
		this.mSpotifyApi = api;
		this.mSpotifyService = service;
		this.mSpotifyRemote = null;
		this.mRequestOrder = new Request[] { Request.TOP_TRACKS,
			Request.TOP_ARTISTS, Request.MY_PLAYLISTS,
			Request.MY_SAVED_ALBUMS };
		this.mRequestPosition = -1;
		this.mRequestFailedListener = null;
		this.mRequestSuccessListener = null;
	}

	/**
	 * @return The spotify API.
	 */
	private SpotifyApi getApi()
	{
		return this.mSpotifyApi;
	}

	/**
	 * @return The request.
	 */
	private Request getRequest()
	{
		Request[] order = this.getRequestOrder();
		int pos = this.getRequestPosition();

		return this.getRequest(order, pos);
	}

	/**
	 * @return The request.
	 */
	private Request getRequest(Request[] order, int pos)
	{
		return (pos == -1) ? Request.NONE : order[pos];
	}

	/**
	 * @return The request order.
	 */
	private Request[] getRequestOrder()
	{
		return this.mRequestOrder;
	}

	/**
	 * @return The request position.
	 */
	private int getRequestPosition()
	{
		return this.mRequestPosition;
	}

	/**
	 * @return The spotify remote.
	 */
	private SpotifyAppRemote getRemote()
	{
		return this.mSpotifyRemote;
	}

	/**
	 * @return The spotify service.
	 */
	private SpotifyService getSpotify()
	{
		return this.mSpotifyService;
	}

	/**
	 * @return The user ID.
	 */
	private String getUserId()
	{
		return this.mUserId;
	}

	/**
	 * Call the next spotify request.
	 */
	private void nextRequest()
	{
		int pos = this.getRequestPosition();

		this.request(pos+1);
	}

	/**
	 * @return The best image.
	 */
	private String parseImages(List<Image> images)
	{
		int size = images.size();

		if (size == 0)
		{
			return null;
		}
		else if (size == 1)
		{
			return images.get(0).url;
		}
		else
		{
			return images.get(1).url;
		}
	}

	/**
	 * partition a[left] to a[right], assumes left < right
	 */
	private int partition(List<String> ids, List<String> names,
		List<String> data, int left, int right)
	{
		int i = left - 1;
		int j = right;

		while (true)
		{
			// find item on left to swap
			while (names.get(++i).compareTo(names.get(right)) < 0)
			{
				// a[right] acts as sentinel
				;
			}

			// find item on right to swap
			while (names.get(right).compareTo(names.get(--j)) < 0)
			{
				// don't go out-of-bounds
				if (j == left)
				{
					break;
				}
			}

			// check if pointers cross
			if (i >= j)
			{
				break;
			}

			// swap two elements into place
			Collections.swap(ids, i, j);
			Collections.swap(names, i, j);

			if (data != null)
			{
				Collections.swap(data, i, j);
			}
		}

		// swap with partition element
		Collections.swap(ids, i, right);
		Collections.swap(names, i, right);

		if (data != null)
		{
			Collections.swap(data, i, right);
		}

		return i;
	}

	/**
	 * Play the uri.
	 */

    private void connected() {
        // Play a playlist
        mSpotifyRemote.getPlayerApi().play("spotify:playlist:37i9dQZF1DX2sUQwD7tbmL");

        // Subscribe to PlayerState
        //mSpotifyAppRemote.getPlayerApi()
        //        .subscribeToPlayerState()
        //        .setEventCallback(playerState -> {
        //            final Track track = playerState.track;
        //            if (track != null) {
        //                Log.d("MainActivity", track.name + " by " + track.artist.name);
        //            }
        //        });
    }

	public void play(String uri)
	{
		this.setSpotifyRemote();

		for (int sec=0; (this.mSpotifyRemote == null) && (sec < 5); sec++)
		{
			try
			{
				TimeUnit.SECONDS.sleep(1);
			}
			catch (InterruptedException e)
			{
				return;
			}
		}

		if (this.mSpotifyRemote != null)
		{
			this.mSpotifyRemote.getPlayerApi().play(uri);
		}
		else
		{
			NacUtility.quickToast(this.mContext, "Unable to play Uri : "+uri);
		}
	}

	public void onConnected(SpotifyAppRemote spotifyAppRemote) {
		NacUtility.printf("MainActivity WORKEDDDDDDDDDDDDDDDDDDDDDDDDDDDDDD");
		mSpotifyRemote = spotifyAppRemote;

		// Now you can start interacting with App Remote
		//connected();

	}

	public void onFailure(Throwable error) {
		NacUtility.printf("This SHIT DID NOT WORK!");
		if (error instanceof SpotifyRemoteServiceException) {
			if (error.getCause() instanceof SecurityException) {
				NacUtility.quickToast(this.mContext, "SecurityException");
			} else if (error.getCause() instanceof IllegalStateException) {
				NacUtility.quickToast(this.mContext, "IllegalStateException");
			}
		} else if (error instanceof NotLoggedInException) {
			NacUtility.quickToast(this.mContext, "NotLoggedInException");
		} else if (error instanceof AuthenticationFailedException) {
			NacUtility.quickToast(this.mContext, "AuthenticationFailedException");
		} else if (error instanceof CouldNotFindSpotifyApp) {
			NacUtility.quickToast(this.mContext, "CouldNotFindSpotifyApp");
		} else if (error instanceof LoggedOutException) {
			NacUtility.quickToast(this.mContext, "LoggedOutException");
		} else if (error instanceof OfflineModeException) {
			NacUtility.quickToast(this.mContext, "OfflineModeException");
		} else if (error instanceof UserNotAuthorizedException) {
			NacUtility.quickToast(this.mContext, "UserNotAuthorizedException");
		} else if (error instanceof UnsupportedFeatureVersionException) {
			NacUtility.quickToast(this.mContext, "UnsupportedFeatureVersionException");
		} else if (error instanceof SpotifyDisconnectedException) {
			NacUtility.quickToast(this.mContext, "SpotifyDisconnectedException");
		} else if (error instanceof SpotifyConnectionTerminatedException) {
			NacUtility.quickToast(this.mContext, "SpotifyConnectionTerminatedException");
		} else {
			NacUtility.quickToast(this.mContext, String.format("Connection failed: %s", error));
		}
	}

	/**
	 * Sort by names.
	 */
	private void quicksort(List<String> ids, List<String> names,
		List<String> data)
	{
		if ((names == null) || (names.size() == 0))
		{
			return;
		}

		this.quicksort(ids, names, data, 0, names.size()-1);
	}

	/**
	 * quicksort a[left] to a[right]
	 */
	private void quicksort(List<String> ids, List<String> names,
		List<String> data, int left, int right)
	{
		if (right <= left)
		{
			return;
		}

		int i = this.partition(ids, names, data, left, right);
		this.quicksort(ids, names, data, left, i-1);
		this.quicksort(ids, names, data, i+1, right);
	}

	/**
	 * Make a request to spotify, corresponding to the one at the given
	 * position in the call order.
	 */
	private void request(int position)
	{
		this.setRequestPosition(position);

		Request request = this.getRequest();

		this.request(request);
	}

	/**
	 * Make a request to spotify.
	 */
	public void request(Request request)
	{
		this.sleep(DEFAULT_SLEEP);

		SpotifyService spotify = this.getSpotify();
		String user = this.getUserId();

		if (request == Request.MY_PLAYLISTS)
		{
			spotify.getMyPlaylists(this.mPlaylistsCallback);
		}
		else if (request == Request.MY_SAVED_ALBUMS)
		{
			spotify.getMySavedAlbums(this.mAlbumsCallback);
		}
		else if (request == Request.TOP_ARTISTS)
		{
			spotify.getTopArtists(this.mArtistsCallback);
		}
		else if (request == Request.TOP_TRACKS)
		{
			spotify.getTopTracks(this.mTracksCallback);
		}
	}

	/**
	 * Make a request that requires an ID associated with the request.
	 */
	public void request(Request request, String id)
	{
		this.sleep(DEFAULT_SLEEP);

		SpotifyService spotify = this.getSpotify();
		String user = this.getUserId();

		if (request == Request.PLAYLIST)
		{
			spotify.getPlaylist(user, id, this.mPlaylistCallback);
		}
	}

	/**
	 * A request failed.
	 */
	private void requestFailed(SpotifyError error)
	{
		String message = error.getErrorDetails().message;

		NacUtility.printf("Failure! %s", message);

		if (this.mRequestFailedListener != null)
		{
			this.mRequestFailedListener.onRequestFailed(error);
		}
	}

	/**
	 * A request succeeded.
	 */
	private void requestSuccess(Request request, List<String> ids,
		List<String> names, List<String> data)
	{
		if (this.mRequestSuccessListener != null)
		{
			this.mRequestSuccessListener.onRequestSuccess(request, ids, names,
				data);
		}
	}

	/**
	 * Set request failed listener.
	 */
	public void setOnRequestFailedListener(OnRequestFailedListener listener)
	{
		this.mRequestFailedListener = listener;
	}

	/**
	 * Set request success listener.
	 */
	public void setOnRequestSuccessListener(OnRequestSuccessListener listener)
	{
		this.mRequestSuccessListener = listener;
	}

	/**
	 * Set the request position.
	 */
	private void setRequestPosition(int position)
	{
		Request[] order = this.getRequestOrder();

		this.mRequestPosition = ((order.length-1) < position) ? -1 : position;
	}

	/**
	 * Set the remote spotify app.
	 */
	private void setSpotifyRemote()
	{
		if (this.mSpotifyRemote != null)
		{
			return;
		}

        ConnectionParams connectionParams =
                new ConnectionParams.Builder(NacSpotify.CLIENT_ID)
						.setAuthMethod(ConnectionParams.AuthMethod.NONE)
                        .setRedirectUri(NacSpotify.REDIRECT_URI)
                        .showAuthView(false)
                        .build();
						//.setAuthMethod(ConnectionParams.AuthMethod.APP_ID)

        SpotifyAppRemote.connect(this.mContext, connectionParams, this);
	}

	/**
	 * Sleep for the set amount of time.
	 */
	private void sleep(long milliseconds)
	{
		if (milliseconds <= 0)
		{
			return;
		}

		try
		{
			TimeUnit.MILLISECONDS.sleep(milliseconds);
		}
		catch (InterruptedException e)
		{
			return;
		}
	}

	/**
	 */
	public void start()
	{
		this.getSpotify().getMe(this.mMeCallback);
	}

	/**
	 * Callback to get the user's information.
	 */
	private SpotifyCallback<UserPrivate> mMeCallback = 
		new SpotifyCallback<UserPrivate>()
	{
		@Override
		public void success(UserPrivate me, Response response)
		{
			mUserId = me.id;

			request(0);
		}

		@Override
		public void failure(SpotifyError error)
		{
			requestFailed(error);
		}
	};

	/**
	 * Callback to get tracks.
	 */
	private SpotifyCallback<Pager<Track>> mTracksCallback =
		new SpotifyCallback<Pager<Track>>()
	{
		@Override
		public void success(Pager<Track> tracks, Response response)
		{
			List<String> ids = new ArrayList<>();
			List<String> names = new ArrayList<>();
			Request request = getRequest();

			for (Track t : tracks.items)
			{
				ids.add(t.id);
				names.add(t.name);
			}

			Collections.sort(names);
			requestSuccess(request, ids, names, null);
			nextRequest();
		}

		@Override
		public void failure(SpotifyError error)
		{
			requestFailed(error);
		}
	};

	/**
	 * Callback to get artists.
	 */
	private SpotifyCallback<Pager<Artist>> mArtistsCallback =
		new SpotifyCallback<Pager<Artist>>()
	{
		@Override
		public void success(Pager<Artist> artists, Response response)
		{
			List<String> ids = new ArrayList<>();
			List<String> names = new ArrayList<>();
			Request request = getRequest();

			for (Artist a : artists.items)
			{
				ids.add(a.id);
				names.add(a.name);
			}

			Collections.sort(names);
			requestSuccess(request, ids, names, null);
			nextRequest();
		}

		@Override
		public void failure(SpotifyError error)
		{
			requestFailed(error);
		}
	};

	/**
	 * Callback to get a playlist.
	 */
	private SpotifyCallback<Playlist> mPlaylistCallback =
		new SpotifyCallback<Playlist>()
	{
		@Override
		public void success(Playlist playlist, Response response)
		{
			List<String> ids = new ArrayList<>();
			List<String> names = new ArrayList<>();
			List<String> artists = new ArrayList<>();
			Request request = Request.PLAYLIST;
			//Request request = getRequest();

			for (PlaylistTrack t : playlist.tracks.items)
			{
				ids.add(t.track.uri);
				names.add(t.track.name);
				artists.add(t.track.artists.get(0).name);
			}

			quicksort(ids, names, artists);
			requestSuccess(request, ids, names, artists);
			//nextRequest();
		}

		@Override
		public void failure(SpotifyError error)
		{
			requestFailed(error);
		}
	};

	/**
	 * Callback to get playlists.
	 */
	private SpotifyCallback<Pager<PlaylistSimple>> mPlaylistsCallback =
		new SpotifyCallback<Pager<PlaylistSimple>>()
	{
		@Override
		public void success(Pager<PlaylistSimple> playlists, Response response)
		{
			List<String> ids = new ArrayList<>();
			List<String> names = new ArrayList<>();
			List<String> images = new ArrayList<>();
			Request request = getRequest();

			for (PlaylistSimple p : playlists.items)
			{
				ids.add(p.id);
				names.add(p.name);
				images.add(parseImages(p.images));
			}

			quicksort(ids, names, images);
			requestSuccess(request, ids, names, images);
			nextRequest();
		}

		@Override
		public void failure(SpotifyError error)
		{
			requestFailed(error);
		}
	};

	/**
	 * Callback to get albums.
	 */
	private SpotifyCallback<Pager<SavedAlbum>> mAlbumsCallback =
		new SpotifyCallback<Pager<SavedAlbum>>()
	{
		@Override
		public void success(Pager<SavedAlbum> albums, Response response)
		{
			List<String> ids = new ArrayList<>();
			List<String> names = new ArrayList<>();
			List<String> images = new ArrayList<>();
			Request request = getRequest();

			for (SavedAlbum a : albums.items)
			{
				ids.add(a.album.id);
				names.add(a.album.name);
				images.add(parseImages(a.album.images));
			}

			quicksort(ids, names, images);
			requestSuccess(request, ids, names, images);
			nextRequest();
		}

		@Override
		public void failure(SpotifyError error)
		{
			requestFailed(error);
		}
	};

}
