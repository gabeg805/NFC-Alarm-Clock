package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;

/**
 * Media fragment for ringtones and music files.
 */
public class NacMediaFragment
	extends Fragment
	implements View.OnClickListener
{

	/**
	 * The alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * The sound.
	 */
	private NacSound mSound;

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
		this.mSound = null;
		this.mPlayer = null;
		this.mInitialSelection = true;
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
	protected NacSound getSound()
	{
		return this.mSound;
	}

	/**
	 * @return The sound path.
	 */
	protected String getSoundPath()
	{
		NacAlarm alarm = this.getAlarm();
		NacSound sound = this.getSound();

		if (alarm != null)
		{
			return alarm.getSoundPath();
		}
		else if (sound != null)
		{
			return sound.getPath();
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

		if (args != null)
		{
			this.mAlarm = NacBundle.getAlarm(args);
			this.mSound = NacBundle.getSound(args);
		}
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		//NacMediaPlayer player = this.getMediaPlayer();
		FragmentActivity activity = getActivity();
		int id = view.getId();

		if (id == R.id.clear)
		{
			this.setMedia("");
			this.safeReset();
			//player.reset();
		}
		else if (id == R.id.cancel)
		{
			activity.finish();
		}
		else if (id == R.id.ok)
		{
			Context context = getContext();
			NacAlarm alarm = this.getAlarm();
			NacSound sound = this.getSound();

			if (alarm != null)
			{
				NacDatabase.BackgroundService.updateAlarm(context, alarm);
			}
			else if (sound != null)
			{
				Intent intent = NacIntent.toIntent(sound);

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
	 */
	@Override
	public void onStop()
	{
		//this.releasePlayer();
		super.onStop();
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
			player.release();

			this.mPlayer = null;
		}
	}

	/**
	 * Reset the player.
	 */
	protected void resetPlayer()
	{
		NacMediaPlayer player = this.getMediaPlayer();

		if (player != null)
		{
			player.reset();
		}
	}

	/**
	 * Play audio from the media player safely.
	 */
	protected int safePlay(String path, boolean repeat)
	{
		this.safeReset();
		this.setMedia(path);

		NacMediaPlayer player = this.getMediaPlayer();

		NacUtility.printf("Player is null? %b %b", player == null, this.mPlayer == null);
		if (player == null)
		{
			this.setupMediaPlayer();

			NacUtility.printf("Player is STILL null? %b %b", player == null, this.mPlayer == null);
			if (player == null)
			{
				NacUtility.printf("Unable to setup media player.");
				return -1;
			}
		}

		NacUtility.printf("Checking path : '%s'", path);
		if ((path != null) && (!path.isEmpty()))
		{
			NacUtility.printf("Playing this shit");
			player.play(path, repeat);
		}

		NacUtility.printf("Returning 0");
		return 0;
	}

	/**
	 * Reset the media player safely.
	 */
	protected int safeReset()
	{
		NacMediaPlayer player = this.getMediaPlayer();

		if (player == null)
		{
			this.setupMediaPlayer();

			return (player != null) ? 0 : -1;
		}

		player.reset();

		return 0;
	}

	/**
	 * Set the alarm sound.
	 */
	protected void setMedia(String media)
	{
		Context context = getContext();
		NacAlarm alarm = this.getAlarm();
		NacSound sound = this.getSound();

		if (alarm != null)
		{
			alarm.setSound(context, media);
		}
		else if (sound != null)
		{
			sound.set(context, media);
		}
	}

	/**
	 * Setup action buttons.
	 */
	protected void setupActionButtons(View root)
	{
		NacSharedPreferences shared = new NacSharedPreferences(getContext());
		Button clear = (Button) root.findViewById(R.id.clear);
		Button cancel = (Button) root.findViewById(R.id.cancel);
		Button ok = (Button) root.findViewById(R.id.ok);

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
	protected void setupMediaPlayer()
	{
		this.mPlayer = new NacMediaPlayer(getContext());
	}

}
