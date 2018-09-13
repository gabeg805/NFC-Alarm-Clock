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

/**
 * @brief The recycler view adapter which will contain the view holders.
 */
public class NacDialogMusicList
	extends RecyclerView.Adapter<NacDialogMusicList.NacDialogMusicItem>
{

	/**
	 * @brief Definition for the item listener.
	 */
	public interface ItemClickListener
	{
		void onItemClick(View v, int p);
	}

	/**
	 * @brief The app context.
	 */
	private Context mContext = null;

	/**
	 * @brief The item click listener.
	 */
	private ItemClickListener mClickListener;

	/**
	 * @brief List of songs.
	 */
	private List<NacSong> mSongs;

	/**
	 * @param  c  The app context.
	 * @param  s  List of songs.
	 */
	public NacDialogMusicList(Context c, List<NacSong> s)
	{
		this.mContext = c;
		this.mSongs = s;
	}

	/**
	 * @brief Set the item click listener.
	 */
	public void setClickListener(ItemClickListener listener)
	{
		this.mClickListener = listener;
	}

	// convenience method for getting data at click position
	public NacSong getItem(int id)
	{
		return mSongs.get(id);
	}

	/**
	 * @brief Inflate the row.
	 */
	@Override
	public NacDialogMusicItem onCreateViewHolder(ViewGroup p, int t)
	{
		View view = LayoutInflater.from(mContext).inflate(
			R.layout.list_item_music, p, false);

		return new NacDialogMusicItem(view);
	}

	/**
	 * @brief Bind data to the viewholder.
	 */
	@Override
	public void onBindViewHolder(NacDialogMusicItem item, int position)
	{
		NacSong song = mSongs.get(position);

		item.setName(song.name);
		item.setDirectory(song.dir);
	}

	// total number of rows
	@Override
	public int getItemCount()
	{
		return mSongs.size();
	}

	// stores and recycles views as they are scrolled off screen
	public class NacDialogMusicItem
		extends RecyclerView.ViewHolder
		implements View.OnClickListener
	{

		/**
		 * @brief The name of the song.
		 */
		private TextView mNameView = null;

		/**
		 * @brief The directory where the song is located.
		 */
		private TextView mDirectoryView = null;

		/**
		 * @param  v  The parent view.
		 */
		public NacDialogMusicItem(View v)
		{
			super(v);

			this.mNameView = v.findViewById(R.id.ms_song);
			this.mDirectoryView = v.findViewById(R.id.ms_directory);

			v.setOnClickListener(this);
		}

		/**
		 * @brief Set the name of the song.
		 */
		public void setName(String name)
		{
			mNameView.setText(name);
		}

		/**
		 * @brief Set the directory where the song is located.
		 */
		public void setDirectory(String dir)
		{
			mDirectoryView.setText(dir);
		}

		/**
		 */
		@Override
		public void onClick(View v)
		{
			if (mClickListener != null)
			{
				mClickListener.onItemClick(v, getAdapterPosition());
			}
		}

	}

}
