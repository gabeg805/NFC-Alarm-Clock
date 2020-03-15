package com.nfcalarmclock;

import android.content.Context;
import android.os.Environment;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.database.Cursor;
import android.provider.MediaStore;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import android.view.LayoutInflater;
import android.net.Uri;
import android.content.ContentUris;

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

	private Context mContext;

	/**
	 * The path view.
	 */
	private TextView mPathView;

	/**
	 * The container view for the directory/file buttons.
	 */
	//private NacButtonGroup mContainer;
	private LinearLayout mContainer;

	/**
	 * File browser click listener.
	 */
	private OnClickListener mListener;

	/**
	 * Currently selected view.
	 */
	private NacImageSubTextButton mSelected;

	/**
	 * Current directory.
	 */
	private String mCurrentDirectory;

	private HashMap<String, List<String>> mDirectories;
	private NacFileListingTree mFileListing;

	/**
	 */
	public NacFileBrowser(View root, int pathId, int groupId)
	{
		this.mContext = root.getContext();
		this.mPathView = (TextView) root.findViewById(pathId);
		this.mContainer = (LinearLayout) root.findViewById(groupId);
		//this.mContainer = (NacButtonGroup) root.findViewById(groupId);
		this.mSelected = null;
		//this.mCurrentDirectory = NacFileBrowser.getHome();
		//this.mDirectories = new HashMap<String, List<String>>();
		String home = NacFileBrowser.getHome();
		this.mFileListing = new NacFileListingTree(home);

		this.scanDirectories();
		//this.mContainer.removeAllViews();
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

		//Context context = container.getContext();
		Context context = this.getContext();
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
			NacUtility.printf("Adding directory!");
			this.addDirectory(file);
		}
		else if (file.isFile())
		{
			NacUtility.printf("Adding file!");
			this.addFile(file);
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

		Context context = this.getContext();
		NacImageSubTextButton entry = new NacImageSubTextButton(context);
		String title = NacSound.getTitle(context, file);
		//String artist = "Unknown";
		String artist = NacSound.getArtist(context, file);

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
	 * Add all files under the given path.
	 */
	public void addListing(String path)
	{
		for (File file : this.listing(path))
		{
			try
			{
				NacUtility.printf("File : %s", file.getCanonicalPath());
			}
			catch (IOException e)
			{
				NacUtility.printf("File getCanonicalPath exception!");
			}

			this.addEntry(file);
		}
	}

	/**
	 * Clear the views out of the browser.
	 */
	private void clearEntries()
	{
		NacButtonGroup container = this.getContainer();

		container.removeAllViews();
	}

	/**
	 * Deselect the currently selected item from the file browser.
	 */
	public void deselect()
	{
		this.select((View)null);
	}

	/**
	 * @return The container view.
	 */
	private NacButtonGroup getContainer()
	{
		Context context = this.getContext();
		LinearLayout container = this.mContainer;
		int count = container.getChildCount();

		if (count == 0)
		{
			NacButtonGroup group = (NacButtonGroup) LayoutInflater.from(context).inflate(R.layout.nac_file_browser, null);
			container.addView(group);
			return group;
		}
		else
		{
			for (int i=0; i < count; i++)
			{
				View view = container.getChildAt(i);

				if (view.getVisibility() == View.VISIBLE)
				{
					return (NacButtonGroup) view;
				}
			}
		}

		//return this.mContainer;
		return null;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The current directory path.
	 */
	public String getCurrentDirectory()
	{
		return this.mCurrentDirectory;
	}

	/**
	 * @return The directories.
	 */
	public HashMap<String, List<String>> getDirectories()
	{
		return this.mDirectories;
	}

	/**
	 * @return The home directory.
	 */
	public static String getHome()
	{
		return Environment.getExternalStorageDirectory().toString();
		//return "/sdcard";
	}

	/**
	 * @return The OnClickListener.
	 */
	private OnClickListener getListener()
	{
		return this.mListener;
	}

	/**
	 * @return The name of the file represented by the view.
	 */
	public String getName(View view)
	{
		if (view == null)
		{
			return "";
		}

		File file = (File) view.getTag();

		return file.getName();
	}

	/**
	 * @return The file path repesented by the view.
	 */
	public String getPath(View view)
	{
		if (view == null)
		{
			return "";
		}

		File file = (File) view.getTag();
		String name = file.getName();

		try
		{
			return file.getCanonicalPath();
		}
		catch (IOException e)
		{
			NacUtility.printf("NacFileBrowser : getSelectedPath : IOException occurred when trying to getCanonicalPath().");
			return "";
		}
	}

	/**
	 * @return The currently selected view.
	 */
	public NacImageSubTextButton getSelected()
	{
		return this.mSelected;
	}

	/**
	 * @return The currently selected name.
	 */
	public String getSelectedName()
	{
		return this.getName(this.getSelected());
	}

	/**
	 * @return The path of the currently selected view.
	 */
	public String getSelectedPath()
	{
		return this.getPath(this.getSelected());
	}

	/**
	 * @return True if something is selected and False otherwise.
	 */
	public boolean isSelected()
	{
		return (this.getSelected() != null);
	}

	/**
	 * @return True if the given path matches the currently selected path, and
	 *         False otherwise.
	 */
	public boolean isSelected(String path)
	{
		String selectedPath = this.getSelectedPath();

		return (!path.isEmpty() && (path.equals(selectedPath)));
	}

	public List<File> listing(String listPath)
	{
		NacFileListingTree tree = this.mFileListing;
		String home = tree.getHome();
		List<File> directories = new ArrayList<>();
		List<File> files = new ArrayList<>();
		//List<String> contents = listPath.equals(home) ? tree.ls()
		//	: tree.ls(dir);

		NacUtility.printf("Compiling list of files!!");
		NacUtility.printf("Path : %s", listPath);


		for (String name : tree.ls(listPath))
		{
			NacUtility.printf("Name : %s", name);
			String path = String.format("%s/%s", listPath, name);
			File file = new File(path);

			if (file.isDirectory())
			{
				directories.add(file);
			}
			else if (file.isFile())
			{
				files.add(file);
			}
			else
			{
				NacUtility.printf("Didn't add it! Oh no!");
			}
		}

		Collections.sort(directories);
		Collections.sort(files);
		directories.addAll(files);

		return directories;
	}

	/**
	 * @return A listing of music files and directories under a given path.
	 *         Directories will be listed first, before files.
	 */
	//public List<File> listing(String path)
	//public static List<File> listing(String path)
	public static List<File> listing(Context context, String listPath)
	{
		//File[] listing = new File(path).listFiles(NacSound.getFilter());
		//File[] listing = new File(path).listFiles();
		HashMap<String, List<String>> dirstruct = new HashMap<String, List<String>>();
		List<String> alarm = new ArrayList<>();
		List<String> audiobook = new ArrayList<>();
		List<String> music = new ArrayList<>();
		List<String> notification = new ArrayList<>();
		List<String> podcast = new ArrayList<>();
		List<String> ringtone = new ArrayList<>();
		String[] columns = new String[] {
			//MediaStore.Audio.Media.VOLUME_NAME,
			MediaStore.Audio.Media.DATA,
			MediaStore.Audio.Media.RELATIVE_PATH,
			MediaStore.Audio.Media.DISPLAY_NAME,
			MediaStore.Audio.Media.IS_ALARM,
			MediaStore.Audio.Media.IS_AUDIOBOOK,
			MediaStore.Audio.Media.IS_MUSIC,
			MediaStore.Audio.Media.IS_NOTIFICATION,
			MediaStore.Audio.Media.IS_PODCAST,
			MediaStore.Audio.Media.IS_RINGTONE,
			};
		String home = NacFileBrowser.getHome();
		Locale locale = Locale.getDefault();
		Cursor c = context.getContentResolver().query(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, null, null,
			"_display_name");

		while (c.moveToNext())
		{
			int dataIndex = c.getColumnIndex(
				MediaStore.Audio.Media.DATA);
			int pathIndex = c.getColumnIndex(
				MediaStore.Audio.Media.RELATIVE_PATH);
			int nameIndex = c.getColumnIndex(
				MediaStore.Audio.Media.DISPLAY_NAME);
			int isAlarmIndex = c.getColumnIndex(
				MediaStore.Audio.Media.IS_ALARM);
			int isAudiobookIndex = c.getColumnIndex(
				MediaStore.Audio.Media.IS_AUDIOBOOK);
			int isMusicIndex = c.getColumnIndex(
				MediaStore.Audio.Media.IS_MUSIC);
			int isNotificationIndex = c.getColumnIndex(
				MediaStore.Audio.Media.IS_NOTIFICATION);
			int isPodcastIndex = c.getColumnIndex(
				MediaStore.Audio.Media.IS_PODCAST);
			int isRingtoneIndex = c.getColumnIndex(
				MediaStore.Audio.Media.IS_RINGTONE);
			String data = c.getString(dataIndex);
			String path = c.getString(pathIndex);
			String name = c.getString(nameIndex);
			int isAlarm = c.getInt(isAlarmIndex);
			int isAudiobook = c.getInt(isAudiobookIndex);
			int isMusic = c.getInt(isMusicIndex);
			int isNotification = c.getInt(isNotificationIndex);
			int isPodcast = c.getInt(isPodcastIndex);
			int isRingtone = c.getInt(isRingtoneIndex);
			String fullpath = String.format("%s/%s%s", home, path, name);

			NacUtility.printf("DATA : %s", data);
			NacUtility.printf("Browser show : %s", fullpath);

			if (isAlarm != 0)
			{
				alarm.add(fullpath);
			}
			else if (isAudiobook != 0)
			{
				audiobook.add(fullpath);
			}
			else if (isMusic != 0)
			{
				music.add(fullpath);
			}
			else if (isNotification != 0)
			{
				notification.add(fullpath);
			}
			else if (isPodcast != 0)
			{
				podcast.add(fullpath);
			}
			else if (isRingtone != 0)
			{
				ringtone.add(fullpath);
			}
		}

		//directories.put("Current", home);
		dirstruct.put("Alarm", alarm);
		dirstruct.put("Audiobook", audiobook);
		dirstruct.put("Music", music);
		dirstruct.put("Notification", notification);
		dirstruct.put("Podcast", podcast);
		dirstruct.put("Ringtone", ringtone);

		//String[] keys = (String[]) dirstruct.keySet().toArray();
		Object[] keys = dirstruct.keySet().toArray();

		Arrays.sort(keys);

		List<File> directories = new ArrayList<>();
		List<File> files = new ArrayList<>();
		//String home = NacFileBrowser.getHome();

		NacUtility.printf("Listing : %s", listPath);
		NacUtility.printf("Home : %s", home);
		//NacUtility.printf("Listing length : %d", (listing != null) ? listing.length : -1);

		if (!listPath.equals(home))
		{
			directories.add(new File(listPath + "/.."));
		}

		//for (int i=0; (listing != null) && (i < listing.length); i++)
		for (int i=0; i < keys.length; i++)
		{
			NacUtility.printf("Listing file : %d", i);
			if (((String)keys[i]).equals("Current"))
			{
				continue;
			}

			File file = new File(home+"/"+keys[i]);

			//if (listing[i].isDirectory())
			if (file.isDirectory())
			{
				//directories.add(listing[i]);
				directories.add(file);
			}
			//else if (listing[i].isFile())
			else if (file.isFile())
			{
				//files.add(listing[i]);
				files.add(file);
			}
		}

		Collections.sort(directories);
		Collections.sort(files);
		directories.addAll(files);

		return directories;
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
		String name = this.getName(view);
		String path = this.getPath(view);

		if (path.isEmpty())
		{
			return;
		}

		if (file.isFile())
		{
			if (this.isSelected(path))
			{
				this.deselect();
			}
			else
			{
				this.select(view);
			}
		}
		else
		{
			this.mCurrentDirectory = path;
		}

		listener.onClick(this, file, path, name);
	}

	/**
	 * Populate views into the browser.
	 */
	private void populateEntries(String path)
	{
		String showPath = path.isEmpty() ? NacFileBrowser.getHome() : path;
		NacUtility.printf("Populating entries at : %s", showPath);

		this.mPathView.setText(showPath);
		this.addListing(showPath);
	}

	/**
	 * Change directory to previous ("../") directory.
	 */
	public boolean previousDirectory()
	{
		String home = NacFileBrowser.getHome();
		String currentDirectory = this.getCurrentDirectory();

		if (home.equals(currentDirectory))
		{
			return false;
		}

		try
		{
			File parentFile = new File(currentDirectory + "/..");
			String parentPath = parentFile.getCanonicalPath();

			this.show(parentPath);
			return true;
		}
		catch (IOException e)
		{
			NacUtility.printf("NacFileBrowser : previousDirectory : IOException occurred when trying to getCanonicalPath().");
			return false;
		}
	}

	/**
	 * Set the file browser on click listener.
	 */
	public void setOnClickListener(OnClickListener listener)
	{
		this.mListener = listener;
	}

	/**
	 * @see select
	 */
	public void select(String path)
	{
		File file = new File(path);

		if (path.isEmpty() || !file.isFile())
		{
			return;
		}

		NacButtonGroup container = this.getContainer();
		int count = container.getChildCount();

		for (int i=0; i < count; i++)
		{
			View view = container.getChildAt(i);
			String viewPath = this.getPath(view);

			if (path.equals(viewPath))
			{
				this.select(view);
				return;
			}
		}
	}

	/**
	 * Set the currently selected file.
	 */
	public void select(View view)
	{
		if (this.getSelected() != null)
		{
			this.getSelected().unselect();
		}

		this.mSelected = (NacImageSubTextButton) view;

		if (this.getSelected() != null)
		{
			this.getSelected().select();
		}
	}

	/**
	 * Scan directories and save the file listings in each.
	 */
	private void scanDirectories()
	{
		Context context = this.getContext();
		NacFileListingTree tree = this.mFileListing;
		String home = NacFileBrowser.getHome();
		String[] columns = new String[] {
			MediaStore.Audio.Media._ID,
			MediaStore.Audio.Media.RELATIVE_PATH,
			MediaStore.Audio.Media.DISPLAY_NAME,
			};
		Cursor c = context.getContentResolver().query(
			MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, columns, null, null,
			"_display_name");

		while (c.moveToNext())
		{
			int idIndex = c.getColumnIndex(MediaStore.Audio.Media._ID);
			int pathIndex = c.getColumnIndex(
				MediaStore.Audio.Media.RELATIVE_PATH);
			int nameIndex = c.getColumnIndex(
				MediaStore.Audio.Media.DISPLAY_NAME);
			long id = c.getLong(idIndex);
			String path = c.getString(pathIndex);
			String name = c.getString(nameIndex);
			String fullpath = String.format("%s/%s%s", home, path, name);
			String[] parts = path.split("/");
			NacTreeNode<String> currentDirectory = tree.getDirectory();
			//Uri contentUri = ContentUris.withAppendedId(
			//	MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, id);
			//NacFile file = new NacFile(contentUri, fullpath, name, NacFile.Type.FILE);

			NacUtility.printf("\nBrowser show : %s", fullpath);
			//NacUtility.printf("Content uri : %s || %s", contentUri.toString(), contentUri.getPath());

			for (int i=0; i < parts.length; i++)
			{
				String dir = parts[i];

				tree.add(dir);
				tree.cd(dir);
			}

			tree.add(name, id);
			tree.cd(currentDirectory);
		}
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
		//this.mCurrentDirectory = showPath;

		this.clearEntries();
		this.populateEntries(path);
	}

}
