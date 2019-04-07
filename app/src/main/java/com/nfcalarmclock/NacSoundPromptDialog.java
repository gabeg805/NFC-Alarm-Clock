package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.view.View;
import android.widget.Button;
import java.util.List;

import com.spotify.android.appremote.api.ConnectionParams;
import com.spotify.android.appremote.api.Connector;
import com.spotify.android.appremote.api.SpotifyAppRemote;

import com.spotify.protocol.client.Subscription;
import com.spotify.protocol.types.PlayerState;
import com.spotify.protocol.types.Track;

import com.spotify.sdk.android.authentication.AuthenticationClient;
import com.spotify.sdk.android.authentication.AuthenticationHandler;
import com.spotify.sdk.android.authentication.AuthenticationRequest;
import com.spotify.sdk.android.authentication.AuthenticationResponse;

import android.support.v7.app.AppCompatActivity;

/**
 * Save the name of the selected ringtone or song.
 */
public class NacSoundPromptDialog
	extends NacDialog
	implements View.OnClickListener,
		NacDialog.OnDismissListener,
		NacDialog.OnShowListener
{

	/**
	 * Listener for when an item in a dialog is selected.
	 */
	private NacMediaDialog.OnItemClickListener mItemClickListener;

	/**
	 */
	public NacSoundPromptDialog()
	{
		super();

		this.addOnShowListener(this);

		this.mItemClickListener = null;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = context.getString(R.string.dlg_prompt_title);

		builder.setTitle(title);
		this.setNegativeButton("Cancel");
	}

	/**
	 * Handle button click events.
	 */
	@Override
	public void onClick(View view)
	{
		Context context = view.getContext();
		int id = view.getId();
		NacMediaDialog dialog = null;

		if (id == R.id.dlg_ringtone)
		{
			dialog = new NacRingtoneDialog();
		}
		else if (id == R.id.dlg_music)
		{
			try
			{
				dialog = new NacMusicDialog();
			}
			catch (UnsupportedOperationException e)
			{
				NacUtility.printf("Caught the exception. Not going to show the dialog.");
				return;
			}
		}
		else if (id == R.id.dlg_spotify)
		{
			AuthenticationRequest.Builder builder =
				new AuthenticationRequest.Builder(NacSpotify.CLIENT_ID,
					AuthenticationResponse.Type.TOKEN, NacSpotify.REDIRECT_URI);

			builder.setScopes(new String[] { "streaming" });
			AuthenticationRequest request = builder.build();

			AuthenticationClient.openLoginInBrowser((AppCompatActivity)context,
				request);
			return;
		}
		else
		{
			return;
		}

		dialog.saveData(this.getData());
		dialog.build(context);
		dialog.setOnItemClickListener(this.mItemClickListener);
		dialog.addOnDismissListener(this);
		dialog.show();
		dialog.scale();
	}

	/**
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.dismiss();

		return true;
	}

	/**
	 * Setup the views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(NacDialog dialog, View root)
	{
		Button music = (Button) root.findViewById(R.id.dlg_music);
		Button ringtone = (Button) root.findViewById(R.id.dlg_ringtone);
		Button spotify = (Button) root.findViewById(R.id.dlg_spotify);

		music.setOnClickListener(this);
		ringtone.setOnClickListener(this);
		spotify.setOnClickListener(this);
		this.scale(0.9, 0.5, false, true);
	}

	/**
	 * Set the listener for when an item is selected.
	 */
	public void setOnItemClickListener(
		NacMediaDialog.OnItemClickListener listener)
	{
		this.mItemClickListener = listener;
	}

}
