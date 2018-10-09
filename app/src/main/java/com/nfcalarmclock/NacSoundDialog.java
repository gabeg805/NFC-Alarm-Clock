package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.database.Cursor;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import java.io.File;
import java.io.FilenameFilter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Sound dialog.
 */
public class NacSoundDialog
	extends NacDialog
	implements CompoundButton.OnCheckedChangeListener,NacDialog.OnDismissedListener,NacDialog.OnCanceledListener
{

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
	protected List<NacSound> mSounds;

	/**
	 * The index in the songs list pointing to the currently selected item.
	 */
	protected int mIndex;

	/**
	 * On item click listener.
	 */
	private OnItemClickListener mItemClickListener;

	/**
	 * On long click listener.
	 */
	private View.OnLongClickListener mLongClickListener;

	/**
	 */
	public NacSoundDialog()
	{
		super();

		this.mPlayer = null;
		this.mSounds = null;
		this.mIndex = -1;
		this.mItemClickListener = null;
		this.mLongClickListener = null;

		this.addDismissListener(this);
		this.addCancelListener(this);
	}

	/**
	 * Check if the sound list contains the name of the given sound.
	 */
	protected boolean containsName(List<NacSound> sounds, String name)
	{
		for (NacSound s : sounds)
		{
			if (s.containsName(name))
			{
				return true;
			}
		}

		return false;
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
				String[] extensions = {".3gp", ".mp4", ".m4a", ".aac",
					".ts", ".flac", ".mid", ".xmf", ".mxmf", ".rtttl",
					".rtx", ".ota", ".imy", ".mp3", ".mkv", ".wav",
					".ogg"};
				String lower = name.toLowerCase(locale);

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
	 * @return The sound at the given index, or null if index is not set (=-1).
	 */
	public NacSound getSound()
	{
		return (this.mIndex < 0) ? null : this.mSounds.get(this.mIndex);
	}

	/**
	 * @return The list of sounds for the ringtone manager.
	 */
	protected List<NacSound> getSoundList(Context context)
	{
		NacUtility.printf("Getting sound list in NacSoundDialog");
		return new ArrayList<>();
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
	public void onDialogCanceled(NacDialog dialog)
	{
		this.mPlayer.reset();
	}

	/**
	 * Dismiss the dialog.
	 */
	@Override
	public void onDialogDismissed(NacDialog dialog)
	{
		NacSound sound = this.getSound();

		this.mPlayer.reset();

		if (sound != null)
		{
			this.itemClick(sound);
		}
	}

	/**
	 * Handle selection of radio button.
	 */
	@Override
	public void onCheckedChanged(CompoundButton b, boolean state)
	{
		if (!state)
		{
			return;
		}

		int i = (int) b.getTag();
		String path = this.mSounds.get(i).path;
		this.mIndex = i;

		this.mPlayer.play(path);
	}

	/**
	 * Setup views when the dialog is shown.
	 */
	@Override
	public void onShowDialog(Context context, View root)
	{
		RadioGroup rg = (RadioGroup) root.findViewById(R.id.radio_group);
		this.mSounds = this.getSoundList(context);

		for(int i=0; i < this.mSounds.size(); i++)
		{
			RadioButton rb = new RadioButton(context);
			String name = this.mSounds.get(i).name;

			rb.setText(name);
			rb.setTag(i);
			rb.setOnCheckedChangeListener(this);

			if (this.mLongClickListener != null)
			{
				rb.setOnLongClickListener(this.mLongClickListener);
			}

			rg.addView(rb);
		}
	}

	/**
	 * Set the listener for when an item is selected.
	 */
	public void setOnItemClickListener(
		NacSoundDialog.OnItemClickListener listener)
	{
		this.mItemClickListener = listener;
	}

	/**
	 * Set the long click listener.
	 */
	public void setOnLongClickListener(View.OnLongClickListener listener)
	{
		this.mLongClickListener = listener;
	}

}
