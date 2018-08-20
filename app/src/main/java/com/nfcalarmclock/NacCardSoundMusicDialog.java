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
	implements DialogInterface.OnClickListener
{

	/**
	 * @brief Context.
	 */
	private Context mContext;

	/**
	 * @brief Root view.
	 */
	private View mRoot = null;

	/**
	 * @brief List of ringtones.
	 */
	private List<NacSong> mSounds = null;

	/**
	 * @brief Media player.
	 */
	private NacCardMediaPlayer mPlayer = null;

	/**
	 * @param  c  Context.
	 */
	public NacCardSoundMusicDialog(Context c, NacCardMediaPlayer mp)
	{
		super(c);

		this.mContext = c;
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
			String path = root+"/"+d;
			NacUtility.printf("Directory : %s", path);
			File dir = new File(path);
			File[] files = dir.listFiles(this.getMusicFilter());

			for (int i=0; (files != null) && (i < files.length); i++)
			{
				String name = files[i].getName();
				this.mSounds.add(new NacSong(name, path));
				NacUtility.printf("File : %s", name);
			}
		}
	}

	private void createRecyclerView()
	{
		RecyclerView recyclerView = mRoot.findViewById(R.id.dlg_music_list);
		recyclerView.setLayoutManager(new LinearLayoutManager(mContext));
		MyRecyclerViewAdapter adapter = new MyRecyclerViewAdapter(mContext,
			mSounds);
		//adapter.setClickListener(this);
		recyclerView.setAdapter(adapter);
		recyclerView.addItemDecoration(new DividerItemDecoration(mContext,
			LinearLayoutManager.VERTICAL));

		//for (int i=0; i < this.mSounds.size(); i++)
		//{
		//	//MusicSelection ms = new MusicSelection(mContext, null);
		//	String name = this.mSounds.get(i).name;
		//	String dir = this.mSounds.get(i).dir;

		//	//NacUtility.printf("InitMusicSelection ::: %s ::: %s.", name, dir);
		//	//ms.setSongName(name);
		//	//ms.setDirName(dir);
		//	//layout.addView(ms);
		//}
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
					".ogg", ".txt"};
				String lower = name.toLowerCase(locale);

				for (String e : extensions)
				{
					if (lower.endsWith(e))
					{
						//NacUtility.printf("Name : %s | Ends with : %s", name, e);
						return true;
					}
				}
				return false;
			}
		};

		return filter;
	}

	/**
	 * @brief Handles click events on the Ok/Cancel buttons in the dialog.
	 */
	@Override
	public void onClick(DialogInterface dialog, int which)
	{
		super.onClick(dialog, which);
		mPlayer.reset();
	}

	public class MyRecyclerViewAdapter
		extends RecyclerView.Adapter<MyRecyclerViewAdapter.ViewHolder>
	{

		private LayoutInflater mInflater;
		//private ItemClickListener mClickListener;
		private List<NacSong> mSongs;

		// data is passed into the constructor
		MyRecyclerViewAdapter(Context context, List<NacSong> songs)
		{
			this.mInflater = LayoutInflater.from(context);
			this.mSongs = songs;
		}

		// inflates the row layout from xml when needed
		@Override
		public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType)
		{
			View view = mInflater.inflate(R.layout.list_item_music, parent, false);
			return new ViewHolder(view);
		}

		// binds the data to the TextView in each row
		@Override
		public void onBindViewHolder(ViewHolder holder, int position)
		{
			NacSong song = mSongs.get(position);
			holder.setName(song.name);
			holder.setDirectory(song.dir);
		}

		// total number of rows
		@Override
		public int getItemCount()
		{
			return mSongs.size();
		}

		// stores and recycles views as they are scrolled off screen
		public class ViewHolder
			extends RecyclerView.ViewHolder
		{

		//implements View.OnClickListener {
			private TextView mNameView;
			private TextView mDirectoryView;

			ViewHolder(View itemView)
			{
				super(itemView);
				mNameView = itemView.findViewById(R.id.ms_song);
				mDirectoryView = itemView.findViewById(R.id.ms_directory);
				//itemView.setOnClickListener(this);
			}

			//@Override
			//public void onClick(View view) {
			//	if (mClickListener != null) mClickListener.onItemClick(view, getAdapterPosition());
			//}

			public void setName(String name)
			{
				mNameView.setText(name);
			}

			public void setDirectory(String dir)
			{
				mDirectoryView.setText(dir);
			}
		}

		// convenience method for getting data at click position
		NacSong getItem(int id)
		{
			return mSongs.get(id);
		}

		// allows clicks events to be caught
		//void setClickListener(ItemClickListener itemClickListener) {
		//	this.mClickListener = itemClickListener;
		//}

		//// parent activity will implement this method to respond to click events
		//public interface ItemClickListener {
		//	void onItemClick(View view, int position);
		//}
	}
}
