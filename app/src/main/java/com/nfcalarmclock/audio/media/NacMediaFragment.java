package com.nfcalarmclock.audio.media;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.lifecycle.ViewModelProvider;

import com.nfcalarmclock.NacUtility;
import com.nfcalarmclock.R;
import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.alarm.NacAlarmViewModel;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.shared.NacSharedPreferences;
import com.nfcalarmclock.system.NacBundle;
import com.nfcalarmclock.system.NacIntent;

/**
 * Media fragment for ringtones and music files.
 */
public class NacMediaFragment
	extends Fragment
	implements View.OnClickListener
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Media path.
	 */
	private String mMediaPath;

	/**
	 * Media player.
	 */
	private NacMediaPlayer mPlayer;

	/**
	 * The initial selection flag.
	 */
	private boolean mInitialSelection;

	/**
	 */
	public NacMediaFragment()
	{
		super();

		this.mAlarm = null;
		this.mMediaPath = null;
		this.mPlayer = null;
		this.mInitialSelection = true;
	}

	/**
	 * @return True if the media path matches the given path, and False otherwise.
	 */
	protected boolean isSelectedPath(String path)
	{
		String selectedPath = getMediaPath();
		return !selectedPath.isEmpty() && selectedPath.equals(path);
	}

	/**
	 * @return The alarm.
	 */
	protected NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The alarm.
	 */
	protected String getMedia()
	{
		return this.mMediaPath;
	}

	/**
	 * @return The media path.
	 */
	protected String getMediaPath()
	{
		NacAlarm alarm = this.getAlarm();
		String media = this.getMedia();

		if (alarm != null)
		{
			return alarm.getMediaPath();
		}
		else if (media != null)
		{
			return media;
		}
		else
		{
			return "";
		}
	}

	/**
	 * @return The media player.
	 */
	protected NacMediaPlayer getMediaPlayer()
	{
		return this.mPlayer;
	}

	/**
	 * @return True if the ID corresponds to an action button, and False
	 * otherwise.
	 */
	public boolean isActionButton(int id)
	{
		return ((id == R.id.clear) || (id == R.id.cancel) || (id == R.id.ok));
	}

	/**
	 * @return True if this is the first time the fragment is being selected,
	 *         and False otherwise.
	 */
	public boolean isInitialSelection()
	{
		return this.mInitialSelection;
	}

	/**
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		this.mAlarm = NacBundle.getAlarm(args);
		this.mMediaPath = NacBundle.getMedia(args);
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		FragmentActivity activity = requireActivity();
		int id = view.getId();

		if (id == R.id.clear)
		{
			this.setMedia("");
			this.safeReset();
		}
		else if (id == R.id.cancel)
		{
			activity.finish();
		}
		else if (id == R.id.ok)
		{
			NacAlarm alarm = this.getAlarm();
			String media = this.getMedia();

			if (alarm != null)
			{
				NacAlarmViewModel viewModel = new ViewModelProvider(activity)
					.get(NacAlarmViewModel.class);
				viewModel.update(activity, alarm);
			}
			else if (media != null)
			{
				//Intent intent = NacIntent.toIntent(activity, null, alarm);
				Intent intent = NacIntent.toIntent(media);
				activity.setResult(Activity.RESULT_OK, intent);
			}

			activity.finish();
		}
	}

	/**
	 * Called when the fragment is first selected by the user.
	 */
	protected void onInitialSelection()
	{
		this.mInitialSelection = false;
	}

	/**
	 */
	@Override
	public void onPause()
	{
		this.releasePlayer();
		super.onPause();
	}

	/**
	 */
	@Override
	public void onStart()
	{
		super.onStart();
		this.setupMediaPlayer();
	}

	/**
	 * Called when the fragment is selected by the user.
	 */
	protected void onSelected()
	{
		if (this.isInitialSelection())
		{
			this.onInitialSelection();
		}
	}

	/**
	 * Release the player.
	 */
	protected void releasePlayer()
	{
		NacMediaPlayer player = this.getMediaPlayer();

		if (player != null)
		{
			player.releaseWrapper();

			this.mPlayer = null;
		}
	}

	/**
	 * @see #safePlay(Uri, boolean)
	 */
	protected int safePlay(Uri contentUri)
	{
		return this.safePlay(contentUri, true);
	}

	/**
	 * Play audio from the media player safely.
	 *
	 * @param  contentUri  The Uri of the content to play.
	 * @param  repeat      Whether the media player should repeat the media or
	 *     not.
	 */
	@SuppressWarnings("SameParameterValue")
    protected int safePlay(Uri contentUri, boolean repeat)
	{
		String path = contentUri.toString();

		if (path.isEmpty() || !path.startsWith("content://"))
		{
			return -1;
		}

		this.safeReset();
		this.setMedia(path);
		NacMediaPlayer player = this.getMediaPlayer();

		if (player == null)
		{
			player = this.setupMediaPlayer();

			if (player == null)
			{
				return -1;
			}
		}

		player.play(contentUri, repeat);
		return 0;
	}

	/**
	 * Reset the media player safely.
	 */
	protected void safeReset()
	{
		NacMediaPlayer player = this.getMediaPlayer();

		if (player == null)
		{
			player = this.setupMediaPlayer();
		}

		player.reset();
	}

	/**
	 * Set the alarm sound.
	 *
	 * Use Alarm when editing an alarm card, and use media path when editing a
	 * preference.
	 */
	protected void setMedia(String media)
	{
		Context context = getContext();
		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			alarm.setMedia(context, media);
		}
		else
		{
			this.mMediaPath = media;
		}
	}

	/**
	 * Setup action buttons.
	 */
	protected void setupActionButtons(View root)
	{
		NacSharedPreferences shared = new NacSharedPreferences(getContext());
		Button clear = root.findViewById(R.id.clear);
		Button cancel = root.findViewById(R.id.cancel);
		Button ok = root.findViewById(R.id.ok);

		clear.setTextColor(shared.getThemeColor());
		cancel.setTextColor(shared.getThemeColor());
		ok.setTextColor(shared.getThemeColor());
		clear.setOnClickListener(this);
		cancel.setOnClickListener(this);
		ok.setOnClickListener(this);
	}

	/**
	 * Setup the media player.
	 */
	protected NacMediaPlayer setupMediaPlayer()
	{
		Context context = getContext();
		int focus = AudioManager.AUDIOFOCUS_GAIN_TRANSIENT;
		this.mPlayer = new NacMediaPlayer(context, focus);

		return this.mPlayer;
	}

	/**
	 * Show an error indicating that audio was unable to be played.
	 */
	public void showErrorPlayingAudio()
	{
		Context context = getContext();
		NacSharedConstants cons = new NacSharedConstants(context);

		// Commented this because would have needed to add path as arg
		//NacUtility.printf("Unable to play ringtone : %s", path);
		//NacUtility.printf("Unable to play music : %d | %s",
		//	metadata.getId(), metadata.getPath());
		NacUtility.toast(context, cons.getErrorMessagePlayAudio());
	}

}
