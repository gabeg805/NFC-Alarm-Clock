package com.nfcalarmclock;

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
import java.io.File;
import java.io.FilenameFilter;
import java.io.FileOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class NacCardSoundMusicDialog
	extends NacCardDialog
	implements DialogInterface.OnClickListener,NacDialogMusicList.ItemClickListener
{

	/**
	 * @brief Context.
	 */
	private Context mContext;

	/**
	 * @brief The sound view in the alarm card.
	 */
	private ImageTextButton mSoundView = null;

	/**
	 * @brief Media player.
	 */
	private NacMediaPlayer mPlayer = null;

	/**
	 * @brief Root view.
	 */
	private View mRoot = null;

	/**
	 * @brief List of ringtones.
	 */
	public List<NacSong> mSounds = null;

	/**
	 * @brief The index in the songs list pointing to the currently selected
	 * item.
	 */
	public int mIndex = -1;

	/**
	 * @param  c  Context.
	 */
	public NacCardSoundMusicDialog(Context c, ImageTextButton b,
		NacMediaPlayer mp)
	{
		super(c);

		this.mContext = c;
		this.mSoundView = b;
		this.mPlayer = mp;
		this.mRoot = super.inflate(R.layout.dlg_alarm_sound_music, (ViewGroup)null);

		if (!NacPermissions.hasRead(mContext))
		{
			NacUtility.print("Do not have permissions.");
			if (NacPermissions.setRead(mContext) < 0)
			{
				return;
			}
		}
		else
		{
			NacUtility.print("Have permissions.");
		}
	}

	/**
	 * @brief Show the music selection dialog.
	 */
	public void show()
	{
		String title = mContext.getString(R.string.dlg_music_title);

		this.init();
		super.build(mRoot, title, this, this);
		super.scale(0.75, 0.75);
	}

	/**
	 * @brief Initialize the music dialog.
	 */
	private void init()
	{
		setSoundList();
		createRecyclerView();
	}

	/**
	 * @brief Setup the dialog's list of music.
	 */
	private void setSoundList()
	{
		this.mSounds = new ArrayList<>();
		String root = Environment.getExternalStorageDirectory().toString();
		String[] search = {"Download", "Music", "Playlists"};

		for (String d : search)
		{
			String dir = root+"/"+d;
			File obj = new File(dir);
			File[] files = obj.listFiles(this.getMusicFilter());

			for (int i=0; (files != null) && (i < files.length); i++)
			{
				String name = files[i].getName();
				this.mSounds.add(new NacSong(name, dir));
				NacUtility.printf("File : %s/%s", dir, name);
			}
		}
	}

	/**
	 * @brief Create the recycler view in the dialog.
	 */
	private void createRecyclerView()
	{
		RecyclerView recyclerView = mRoot.findViewById(R.id.dlg_music_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
		NacDialogMusicList adapter = new NacDialogMusicList(mContext,
			mSounds);
		adapter.setClickListener(this);
		recyclerView.setAdapter(adapter);
		recyclerView.addItemDecoration(new DividerItemDecoration(mContext,
			LinearLayoutManager.VERTICAL));
	}

	/**
	 * @brief Return the music file listing filter.
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
	 * @brief Handle item click events in the music list.
	 */
	@Override
	public void onItemClick(View v, int i)
	{
		String path = this.mSounds.get(i).path;
		this.mIndex = i;

		this.mPlayer.play(path);
	}

	/**
	 * @brief Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		super.onClick(dialog, which);
	}

}
