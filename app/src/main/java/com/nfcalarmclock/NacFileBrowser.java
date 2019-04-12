package com.nfcalarmclock;

import android.content.Context;
import android.os.Environment;
import android.os.Handler;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.View;
import android.view.ViewGroup;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * A music file browser.
 */
public class NacFileBrowser
	implements View.OnClickListener
{

	/**
	 * Click listener for the file browser.
	 */
	public interface OnClickListener
	{
		public void onClick(NacFileBrowser browser, File file, String path,
			String name);
	}

	/**
	 * Direction.
	 */
	private enum Direction
	{
		LEFT,
		RIGHT
	}

	/**
	 * The root view.
	 */
	private NacButtonGroup mContainer;

	/**
	 * File browser click listener.
	 */
	private OnClickListener mListener;

	/**
	 * Direction the slide in/out animations should go.
	 */
	private Direction mDirection;

	/**
	 * Duration of the slide out animation.
	 */
	private long mDuration;

	/**
	 */
	public NacFileBrowser(View root, int id)
	{
		this.mContainer = (NacButtonGroup) root.findViewById(id);
		this.mDirection = Direction.RIGHT;
		this.mDuration = 0;

		this.mContainer.removeAllViews();
	}

	/**
	 * Add a directory entry to the file browser.
	 */
	public void addDirectory(File file)
	{
		NacButtonGroup container = this.getContainer();

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addDirectory : Container is null.");
			return;
		}

		Context context = container.getContext();
		NacImageTextButton entry = new NacImageTextButton(context);
		String name = file.getName();

		container.add(entry);
		entry.setText(name.equals("..") ? "(Previous folder)" : name);
		entry.setImageBackground(R.mipmap.folder);
		entry.setTag(file);
		entry.setOnClickListener(this);
	}

	/**
	 * Add an entry.
	 */
	public void addEntry(File file)
	{
		if (file.isDirectory())
		{
			this.addDirectory(file);
		}
		else if (file.isFile())
		{
			this.addFile(file);
		}
	}

	/**
	 * Add all files under the given path.
	 */
	public void addFileListing(String path)
	{
		for (File file : this.fileListing(path))
		{
			this.addEntry(file);
		}
	}

	/**
	 * Add a music file entry to the file browser.
	 */
	public void addFile(File file)
	{
		NacButtonGroup container = this.getContainer();

		if (file.length() == 0)
		{
			return;
		}

		if (container == null)
		{
			NacUtility.printf("NacFileBrowser : addFile : Container is null.");
			return;
		}

		Context context = container.getContext();
		NacImageSubTextButton entry = new NacImageSubTextButton(context);
		String title = NacMedia.getTitle(file);
		String artist = NacMedia.getArtist(file);

		if (title.isEmpty())
		{
			return;
		}

		container.add(entry);
		entry.setTextTitle(title);
		entry.setTextSubtitle(artist);
		entry.setImageBackground(R.mipmap.play);
		entry.setTag(file);
		entry.setOnClickListener(this);
	}

	/**
	 * Run the animation to clear the views out of the browser.
	 */
	private void clearAnimation()
	{
		final NacButtonGroup container = this.getContainer();
		Animation slideOut = this.getOutAnimation();
		long duration = slideOut.getDuration();
		this.mDuration = duration;

		container.startAnimation(slideOut);

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				container.setVisibility(View.GONE);
				container.removeAllViews();
			}
		}, duration);
	}

	/**
	 * @return A listing of music files and directories under a given path.
	 *         Directories will be listed first, before files.
	 */
	public List<File> fileListing(String path)
	{
		File[] listing = new File(path).listFiles(NacMedia.getFilter());
		List<File> directories = new ArrayList<>();
		List<File> files = new ArrayList<>();
		String home = this.getHome();

		if (!path.equals(home))
		{
			directories.add(new File(path + "/.."));
		}

		for (int i=0; (listing != null) && (i < listing.length); i++)
		{
			if (listing[i].isDirectory())
			{
				directories.add(listing[i]);
			}
			else if (listing[i].isFile())
			{
				files.add(listing[i]);
			}
		}

		Collections.sort(directories);
		Collections.sort(files);
		directories.addAll(files);

		return directories;
	}

	/**
	 * @return The container view.
	 */
	private NacButtonGroup getContainer()
	{
		return this.mContainer;
	}

	/**
	 * @return The direction the slide in/out animations should go.
	 */
	private Direction getDirection()
	{
		return this.mDirection;
	}

	/**
	 * @return The duration of the slide out animation.
	 */
	private long getDuration()
	{
		return this.mDuration;
	}

	/**
	 * @return The home directory.
	 */
	private String getHome()
	{
		return Environment.getExternalStorageDirectory().toString();
	}

	/**
	 * @return The animation to use when sliding the browser in.
	 */
	private Animation getInAnimation()
	{
		Context context = this.getContainer().getContext();
		int id = (this.getDirection() == Direction.LEFT)
			? R.anim.slide_left_in : R.anim.slide_right_in;

		return AnimationUtils.loadAnimation(context, id);
	}

	/**
	 * @return The OnClickListener.
	 */
	private OnClickListener getListener()
	{
		return this.mListener;
	}

	/**
	 * @return The animation to use when sliding the browser out.
	 */
	private Animation getOutAnimation()
	{
		Context context = this.getContainer().getContext();
		int id = (this.getDirection() == Direction.LEFT)
			? R.anim.slide_left_out : R.anim.slide_right_out;

		return AnimationUtils.loadAnimation(context, id);
	}

	/**
	 */
	@Override
	public void onClick(View view)
	{
		OnClickListener listener = this.getListener();

		if (listener == null)
		{
			return;
		}

		File file = (File) view.getTag();
		String name = file.getName();
		String path;

		this.mDirection = (name.equals("..")) ? Direction.LEFT
			: Direction.RIGHT;

		try
		{
			path = file.getCanonicalPath();
		}
		catch (IOException e)
		{
			NacUtility.printf("NacFileBrowser : onClick : IOException occurred when trying to getCanonicalPath().");
			return;
		}

		listener.onClick(this, file, path, name);
	}

	/**
	 * Run the animation to populate views into the browser.
	 */
	private void populateEntries(final String path)
	{
		final NacButtonGroup container = this.getContainer();

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				addFileListing(path.isEmpty() ? getHome() : path);
			}
		}, this.getDuration());
	}

	/**
	 * Set the file browser on click listener.
	 */
	public void setOnClickListener(OnClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * Show the home directory.
	 */
	public void show()
	{
		this.show("");
	}

	/**
	 * Show the directory contents at the given path.
	 */
	public void show(String path)
	{
		this.clearAnimation();
		this.populateEntries(path);
		this.showAnimation();
	}

	/**
	 * Run the animation to show the views in the browser.
	 */
	private void showAnimation()
	{
		final NacButtonGroup container = this.getContainer();
		final Animation slideIn = this.getInAnimation();
		long duration = slideIn.getDuration();

		new Handler().postDelayed(new Runnable()
		{
			@Override
			public void run()
			{
				container.setVisibility(View.GONE);
				container.startAnimation(slideIn);

				new Handler().postDelayed(new Runnable()
				{
					@Override
					public void run()
					{
						container.setVisibility(View.VISIBLE);
					}
				}, 100);
			}
		}, duration);
	}

}
