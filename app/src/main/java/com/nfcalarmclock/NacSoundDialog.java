package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.view.View;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sound dialog.
 */
public abstract class NacSoundDialog
	extends NacDialog
	implements NacDialog.OnDismissListener,NacDialog.OnCancelListener
{

	/**
	 * Build the dialog using a custom layout.
	 */
	public abstract AlertDialog.Builder build(Context context);

	/**
	 * Scale the dialog using custom scaling.
	 */
	public abstract void scale();

	/**
	 * Interface for other classes to implement what to do when an item is
	 * selected.
	 */
	public interface OnItemClickListener
	{
		public void onItemClick(NacSound sound);
	}

	/**
	 * Media player.
	 */
	protected NacMediaPlayer mPlayer;

	/**
	 * List of ringtones.
	 */
	protected NacSound mSound;

	/**
	 * On item click listener.
	 */
	protected OnItemClickListener mItemClickListener;

	/**
	 */
	public NacSoundDialog()
	{
		super();

		this.mPlayer = null;
		this.mSound = null;
		this.mItemClickListener = null;

		this.addOnDismissListener(this);
		this.addOnCancelListener(this);
	}

	/**
	 * Return the sound file filter.
	 */
	protected FilenameFilter getFilter()
	{
		return new FilenameFilter()
		{
			public boolean accept(File dir, String name)
			{
				Locale locale = Locale.getDefault();
				String lower = name.toLowerCase(locale);
				String[] extensions = { ".mp3", ".ogg" };
				//String[] extensions = {".3gp", ".mp4", ".m4a", ".aac",
				//	".ts", ".flac", ".mid", ".xmf", ".mxmf", ".rtttl",
				//	".rtx", ".ota", ".imy", ".mp3", ".mkv", ".wav" };

				if (dir.isDirectory())
				{
					return true;
				}

				for (String e : extensions)
				{
					if (lower.endsWith(e))
					{
						return true;
					}
				}

				return false;
			}
		};
	}

	/**
	 * Item has been selected in the dialog.
	 */
	private void itemClick(NacSound sound)
	{
		if (this.mItemClickListener != null)
		{
			this.mItemClickListener.onItemClick(sound);
		}
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		this.mPlayer = new NacMediaPlayer(context);

		this.setPositiveButton("OK");
		this.setNegativeButton("Cancel");
	}

	/**
	 * Cancel the dialog.
	 */
	@Override
	public boolean onCancelDialog(NacDialog dialog)
	{
		this.mPlayer.release();

		return true;
	}

	/**
	 * Dismiss the dialog.
	 */
	@Override
	public boolean onDismissDialog(NacDialog dialog)
	{
		this.mPlayer.release();

		if (this.mSound != null)
		{
			this.itemClick(this.mSound);
			this.mSound = null;
		}

		return true;
	}

	/**
	 * Play the sound.
	 */
	public void play(NacSound sound)
	{
		this.mSound = sound;

		this.mPlayer.play(sound.path);
	}

	/**
	 * Set the listener for when an item is selected.
	 */
	public void setOnItemClickListener(
		NacSoundDialog.OnItemClickListener listener)
	{
		this.mItemClickListener = listener;
	}

}
