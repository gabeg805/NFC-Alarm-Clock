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
import java.util.ArrayList;
import java.util.List;

public class NacSoundDialog
	extends NacDialog
	implements CompoundButton.OnCheckedChangeListener
{

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
	 */
	public NacSoundDialog(NacMediaPlayer mp)
	{
		super();

		this.mPlayer = mp;
		this.mSounds = null;
		this.mIndex = -1;
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = context.getString(R.string.dlg_ringtone_title);

		builder.setTitle(title);
		this.setPositiveButton("OK");
		this.setNegativeButton("Cancel");
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
			rg.addView(rb);
		}
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
		return null;
	}

	/**
	 * Check if the sound list contains the name of the given sound.
	 */
	private boolean containsSongName(List<NacSound> sounds, String name)
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

}
