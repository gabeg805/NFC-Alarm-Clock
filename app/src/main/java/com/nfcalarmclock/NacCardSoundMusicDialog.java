package com.nfcalarmclock;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Environment;
import android.support.v7.widget.DividerItemDecoration;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import android.app.Activity;
import android.content.Intent;
import android.Manifest;
import android.support.v4.app.ActivityCompat;
import android.content.pm.PackageManager;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;

/**
 * Music dialog when selecting an alarm sound.
 */
public class NacCardSoundMusicDialog
	extends NacDialog
	implements CompoundButton.OnCheckedChangeListener,View.OnLongClickListener,NacPermissions.OnResultListener
{

	/**
	 * Media player.
	 */
	private NacMediaPlayer mPlayer;

	/**
	 * List of ringtones.
	 */
	private List<NacSound> mSounds;

	/**
	 * The index in the songs list pointing to the currently selected item.
	 */
	private int mIndex;

	private static final int NAC_SOUND_READ_REQUEST = 1;
	/**
	 */
	public NacCardSoundMusicDialog(NacMediaPlayer mp)
	{
		super();

		Context context = mp.getContext();
		this.mPlayer = mp;
		this.mSounds = null;
		this.mIndex = -1;
	}

	/**
	 */
	@Override
	public void onResult(int request, String[] permissions, int[] grant)
	{
		NacUtility.printf("OnResult listener worked!");
		NacUtility.printf("Request : %d", request);
		
		for (String p : permissions)
		{
			NacUtility.printf("Permission : %s", p);
		}
		
		for (int g : grant)
		{
			NacUtility.printf("Grant : %d", g);
		}

		switch (request)
		{
			case NAC_SOUND_READ_REQUEST:
				if ((grant.length > 0)
					&& (grant[0] == PackageManager.PERMISSION_GRANTED))
				{
					this.show();
				}
				else
				{
					this.dismiss();
				}

				break;
			default:
				break;
		}
	}

	/**
	 * Build the dialog.
	 */
	@Override
	public void onBuildDialog(Context context, AlertDialog.Builder builder)
	{
		String title = context.getString(R.string.dlg_music_title);

		builder.setTitle(title);
		this.setPositiveButton("OK");
		this.setNegativeButton("Cancel");
	}

	/**
	 * Show path of sound when long clicked.
	 */
	@Override
	public boolean onLongClick(View view)
	{
		Context context = view.getContext();
		int index = (int) view.getTag();
		String path = this.mSounds.get(index).path;

		Toast.makeText(context, path, Toast.LENGTH_LONG).show();

		return true;
	}

	/**
	 * Setup views for when the dialog is shown.
	 */
	@Override
	public void onShowDialog(Context context, View root)
	{
		if (!NacPermissions.hasRead(context))
		{
			ActivityCompat.requestPermissions((Activity)context,
				new String[] { Manifest.permission.READ_EXTERNAL_STORAGE },
				NAC_SOUND_READ_REQUEST);

			((MainActivity)context).setPermissionResultListener(this);
			this.hide();
			return;
		}

		this.mSounds = this.getSoundList();
		RadioGroup rg = (RadioGroup) root.findViewById(R.id.radio_group);

		for(int i=0; i < this.mSounds.size(); i++)
		{
			RadioButton rb = new RadioButton(context);
			String name = this.mSounds.get(i).name;

			rb.setText(name);
			rb.setTag(i);
			rb.setOnCheckedChangeListener(this);
			rb.setOnLongClickListener(this);
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
	 * Setup the dialog's list of music.
	 */
	private List<NacSound> getSoundList()
	{
		List<NacSound> list = new ArrayList<>();
		String root = Environment.getExternalStorageDirectory().toString();
		String[] search = {"Download", "Music", "Playlists"};
		FilenameFilter filter = this.getMusicFilter();

		for (String d : search)
		{
			String dir = root+"/"+d;
			File obj = new File(dir);
			File[] files = obj.listFiles(filter);

			for (int i=0; (files != null) && (i < files.length); i++)
			{
				String name = files[i].getName();

				list.add(new NacSound(name, dir));
			}
		}

		return list;
	}

	/**
	 * Return the music file listing filter.
	 */
	private FilenameFilter getMusicFilter()
	{
		FilenameFilter filter = new FilenameFilter()
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

		return filter;
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
