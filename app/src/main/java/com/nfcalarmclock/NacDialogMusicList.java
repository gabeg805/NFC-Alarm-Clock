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
	 * Item click listener interface to implement.
	 */
	public interface ItemClickListener
	{
		void onItemClick(View view, int position);
	}

	/**
	 * Item click listener.
	 */
	private ItemClickListener mListener;

	/**
	 * List of songs.
	 */
	private List<NacSound> mSounds;

	/**
	 */
	public NacDialogMusicList(List<NacSound> songs)
	{
		this.mSounds = songs;
	}

	/**
	 * Set the item click listener.
	 */
	public void setClickListener(ItemClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * Inflate the row.
	 */
	@Override
	public NacDialogMusicItem onCreateViewHolder(ViewGroup parent, int type)
	{
		Context context = parent.getContext();
		View view = LayoutInflater.from(context).inflate(
			R.layout.list_item_music, parent, false);

		return new NacDialogMusicItem(view);
	}

	/**
	 * Bind data to the viewholder.
	 */
	@Override
	public void onBindViewHolder(NacDialogMusicItem item, int position)
	{
		NacSound sound = this.mSounds.get(position);
		File file = new File(sound.path);
		String name = sound.name;
		String dir = file.getParent();

		item.setName(name);
		item.setDirectory(dir);
	}

	// total number of rows
	@Override
	public int getItemCount()
	{
		return mSounds.size();
	}

	/**
	 * Stores and recycles views as they are scrolled off screen.
	 */
	public class NacDialogMusicItem
		extends RecyclerView.ViewHolder
		implements View.OnClickListener
	{

		/**
		 * The name of the song.
		 */
		private TextView mNameView;

		/**
		 * The directory where the song is located.
		 */
		private TextView mDirectoryView;

		/**
		 * @param  root  The parent view.
		 */
		public NacDialogMusicItem(View root)
		{
			super(root);

			this.mNameView = root.findViewById(R.id.ms_song);
			this.mDirectoryView = root.findViewById(R.id.ms_directory);

			root.setOnClickListener(this);
		}

		/**
		 * Set the name of the song.
		 */
		public void setName(String name)
		{
			this.mNameView.setText(name);
		}

		/**
		 * Set the directory where the song is located.
		 */
		public void setDirectory(String dir)
		{
			this.mDirectoryView.setText(dir);
		}

		/**
		 */
		@Override
		public void onClick(View view)
		{
			if (mListener != null)
			{
				mListener.onItemClick(view, getAdapterPosition());
			}
		}

	}

}
