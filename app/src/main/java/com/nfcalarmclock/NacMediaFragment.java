package com.nfcalarmclock;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;

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
	 * Media player.
	 */
	private NacMediaPlayer mPlayer;

	/**
	 */
	public NacMediaFragment()
	{
		super();

		this.mAlarm = null;
		this.mPlayer = null;
	}

	/**
	 * @return The alarm.
	 */
	protected NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The media path.
	 */
	protected String getMedia()
	{
		NacAlarm alarm = this.getAlarm();

		return (alarm == null) ? "" : alarm.getSound();
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
	 */
	@Override
	public void onCreate(Bundle savedInstanceState)
	{
		super.onCreate(savedInstanceState);

		Bundle args = getArguments();

		if (args != null)
		{
			this.mAlarm = NacAlarmParcel.getAlarm(args);
		}
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		NacMediaPlayer player = this.getMediaPlayer();
		FragmentActivity activity = getActivity();
		int id = view.getId();

		if (id == R.id.clear)
		{
			this.setMedia("");
			player.reset();
			//group.clearCheck();
		}
		else if (id == R.id.cancel)
		{
			player.release();
			activity.finish();
		}
		else if (id == R.id.ok)
		{
			NacDatabase db = new NacDatabase(getContext());
			NacAlarm alarm = this.getAlarm();

			db.update(alarm);
			db.close();
			player.release();
			activity.finish();
		}
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
		super.onStop();
		this.releasePlayer();
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
	 * Set the alarm sound.
	 */
	protected void setMedia(String media)
	{
		NacAlarm alarm = this.getAlarm();

		if (alarm != null)
		{
			alarm.setSound(media);
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
