package com.nfcalarmclock.file;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.MediaStore;

import com.nfcalarmclock.util.NacUtility;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 */
@SuppressWarnings("RedundantSuppression")
public class NacFile
{
	/**
	 */
	public static class Metadata
	{

		/**
		 * Directory the file resides in.
		 */
		private String mDirectory;

		/**
		 * File name.
		 */
		private String mName;

		/**
		 * File ID.
		 */
		private long mId;

		/**
		 */
		public Metadata(String directory, String name, long id)
		{
			this.setDirectory(directory);
			this.setName(name);
			this.setId(id);
		}

		/**
		 */
		public Metadata(String name, long id)
		{
			this("", name, id);
		}

		/**
		 * @return The directory.
		 */
		public String getDirectory()
		{
			return this.mDirectory;
		}

		/**
		 * @return The file ID.
		 */
		public long getId()
		{
			return this.mId;
		}

		/**
		 * @return The file name.
		 */
		public String getName()
		{
			return this.mName;
		}

		/**
		 * @return The file path.
		 */
		public String getPath()
		{
			String dir = this.getDirectory();
			String name = this.getName();
			Locale locale = Locale.getDefault();

			return dir.isEmpty()
				? name
				: String.format(locale, "%1$s/%2$s", dir, name);
		}

		/**
		 * @return True if it is a directory, and False otherwise.
		 */
		public boolean isDirectory()
		{
			return (this.getId() == -1);
		}

		/**
		 * @return True if it is a file, and False otherwise.
		 */
		public boolean isFile()
		{
			return (this.getId() != -1);
		}

		/**
		 * Set the directory.
		 */
		public void setDirectory(String directory)
		{
			this.mDirectory = directory;
		}

		/**
		 * Set the file ID.
		 */
		public void setId(long id)
		{
			this.mId = id;
		}

		/**
		 * Set the file name.
		 */
		public void setName(String name)
		{
			this.mName = name;
		}

		/**
		 * Convert the input to an external Uri.
		 */
		public Uri toExternalUri()
		{
			return ContentUris.withAppendedId(
				MediaStore.Audio.Media.EXTERNAL_CONTENT_URI, this.getId());
		}

		/**
		 * Convert the input to an internal Uri.
		 */
		@SuppressWarnings("unused")
		public Uri toInternalUri()
		{
			return ContentUris.withAppendedId(
				MediaStore.Audio.Media.INTERNAL_CONTENT_URI, this.getId());
		}

	}

	/**
	 * Organize files in a tree structure.
	 */
	public static class Tree
		extends NacTreeNode<String>
	{


		/**
		 * The current directory.
		 */
		private NacTreeNode<String> mDirectory;

		/**
		 */
		public Tree(String path)
		{
			super(null, path, null);
			this.setDirectory(this);
		}

		/**
		 * @see #add(String, long)
		 */
		public void add(String name)
		{
			this.add(name, -1);
		}

		/**
		 * Add a file/folder to the given directory in the NacTree structure.
		 *
		 * @param  name  The name of the file or directory to add.
		 * @param  id    The content ID, used to create the content Uri.
		 */
		public void add(String name, long id)
		{
			NacTreeNode<String> dir = this.getDirectory();

			if ((dir == null) || (name == null) || name.isEmpty())
			{
				return;
			}

			dir.addChild(name, id);
		}

		/**
		 * @see #addToDirectory(String, String, long)
		 */
		@SuppressWarnings("unused")
		public void addToDirectory(String toDirectory, String name)
		{
			this.addToDirectory(toDirectory, name, -1);
		}

		/**
		 * Add a file/directory to the given directory in the NacTree structure.
		 *
		 * @param  toDirectory  The name of the directory to add to.
		 * @param  name         The name of the file or directory to add.
		 * @param  id           The content ID, used to create the content Uri.
		 */
		public void addToDirectory(String toDirectory, String name, long id)
		{
			NacTreeNode<String> currentDir = this.getDirectory();
			NacTreeNode<String> dir = toDirectory.equals(".") ? currentDir
				: currentDir.getChild(toDirectory);

			if (dir == null)
			{
				return;
			}

			dir.addChild(name, id);
		}

		/**
		 * @see #cd(NacTreeNode)
		 */
		public void cd(String path)
		{
			NacTreeNode<String> fromDir = this.getDirectory();
			String name = NacFile.basename(path);

			if (name.equals(".."))
			{
				this.cd(fromDir.getRoot());
			}
			else
			{
				String currentDir = this.getDirectoryPath();
				String newDir = path.replace(currentDir, "");
				String[] splitPath = this.strip(newDir).split("/");
				NacTreeNode<String> toDir = fromDir;

				for (String sp : splitPath)
				{
					if (sp.isEmpty())
					{
						continue;
					}

					toDir = toDir.getChild(sp);
					if (toDir == null)
					{
						break;
					}

					this.cd(toDir);
				}
			}
		}

		/**
		 * Change directory to the given NacTree node.
		 */
		public void cd(NacTreeNode<String> dir)
		{
			if (dir == null)
			{
				return;
			}

			this.setDirectory(dir);
		}

		/**
		 * @return The current directory.
		 */
		public NacTreeNode<String> getDirectory()
		{
			return this.mDirectory;
		}

		/**
		 * @return The path of the current directory.
		 */
		public String getDirectoryPath()
		{
			NacTreeNode<String> currentDir = this.getDirectory();
			return this.getPath(currentDir);
		}

		/**
		 * @return The home directory.
		 */
		public String getHome()
		{
			return this.getKey();
		}

		/**
		 * @return The path that leads to the desired node.
		 */
		public String getPath(NacTreeNode<String> node)
		{
			if (node == null)
			{
				return "";
			}

			NacTreeNode<String> ref = node;
			String path = "";
			Locale locale = Locale.getDefault();

			while (ref != null)
			{
				String key = ref.getKey();

				if (path.isEmpty())
				{
					path = key;
				}
				else if (!key.isEmpty())
				{
					path = String.format(locale, "%1$s/%2$s", key, path);
				}

				ref = ref.getRoot();
			}

			return path;
		}

		/**
		 * @see #ls(String)
		 */
		public List<Metadata> ls()
		{
			NacTreeNode<String> node = this.getDirectory();
			String dirPath = this.getDirectoryPath();
			List<Metadata> listing = new ArrayList<>();

			if (node == null)
			{
				return listing;
			}

			for (NacTreeNode<String> child : node.getChildren())
			{
				String name = child.getKey();
				long id = (long) child.getValue();
				Metadata metadata = new Metadata(dirPath, name, id);

				listing.add(metadata);
			}

			return listing;
		}

		/**
		 * List the contents of the given path.
		 *
		 * @return The list of files/directories at the given path.
		 *
		 * @param  path  The path to list the contents of.
		 */
		public List<Metadata> ls(String path)
		{
			NacTreeNode<String> dir = this.getDirectory();
			String home = this.getHome();
			String pathKey = NacFile.basename(path);
			String dirKey = dir.getKey();

			if (dirKey.equals(pathKey) || path.equals(home))
			{
				return this.ls();
			}
			else
			{
				String[] splitPath = this.strip(path).split("/");
				NacTreeNode<String> newDir = dir;

				for (String sp : splitPath)
				{
					if (sp.isEmpty())
					{
						continue;
					}

					newDir = newDir.getChild(sp);
					if (newDir == null)
					{
						break;
					}

					this.setDirectory(newDir);
				}


				List<Metadata> listing = this.ls();
				this.setDirectory(dir);
				return listing;
			}
		}

		/**
		 * @see #lsSort(String)
		 */
		public List<Metadata> lsSort()
		{
			List<Metadata> directories = new ArrayList<>();
			List<Metadata> files = new ArrayList<>();
			List<Metadata> list;

			for (Metadata metadata : this.ls())
			{
				if (metadata.isDirectory())
				{
					list = directories;
				}
				else if (metadata.isFile())
				{
					list = files;
				}
				else
				{
					continue;
				}

				String name = metadata.getName();
				int i;

				for (i=0; i < list.size(); i++)
				{
					Metadata md = list.get(i);

					if (name.compareTo(md.getName()) <= 0)
					{
						break;
					}
				}

				list.add(i, metadata);
			}

			directories.addAll(files);
			return directories;
		}

		/**
		 * List the contents of the given path and sort the output.
		 *
		 * @return The sorted list of files/directories at the given path.
		 *
		 * @param  path  The path to list the contents of.
		 */
		public List<Metadata> lsSort(String path)
		{
			List<Metadata> directories = new ArrayList<>();
			List<Metadata> files = new ArrayList<>();
			List<Metadata> list;

			for (Metadata metadata : this.ls(path))
			{
				if (metadata.isDirectory())
				{
					list = directories;
				}
				else if (metadata.isFile())
				{
					list = files;
				}
				else
				{
					continue;
				}

				String name = metadata.getName();
				int i;

				for (i=0; i < list.size(); i++)
				{
					Metadata md = list.get(i);

					if (name.compareTo(md.getName()) <= 0)
					{
						break;
					}
				}

				list.add(i, metadata);
			}

			directories.addAll(files);
			return directories;
		}

		/**
		 * Print the contents of the current directory.
		 */
		@SuppressWarnings("unused")
		public void print()
		{
			for (Metadata metadata : this.ls())
			{
				NacUtility.printf("NacFile.Tree : print : %s", metadata.getPath());
			}
		}

		/**
		 * Strip the home directory away from a path.
		 */
		public String strip(String path)
		{
			String home = this.getHome();
			String reducedPath = path.replace(home, "");
			String strippedPath = NacFile.strip(reducedPath);

			if (strippedPath.isEmpty())
			{
				return "";
			}

			if (strippedPath.charAt(0) == '/')
			{
				strippedPath = strippedPath.substring(1);
			}

			return strippedPath;
		}

		/**
		 * Set the current directory.
		 */
		public void setDirectory(NacTreeNode<String> dir)
		{
			this.mDirectory = dir;
		}

	}

	/**
	 * @see #basename(String)
	 */
	public static String basename(Uri uri)
	{
		String path = uri.toString();
		return NacFile.basename(path);
	}

	/**
	 * @return The basename of a file path.
	 */
	public static String basename(String path)
	{
		if (NacFile.isEmpty(path))
		{
			return "";
		}

		String[] items = path.split("/");

		return (items.length > 0) ? items[items.length-1] : "";
	}

	/**
	 * @return The dirname of a file path.
	 */
	public static String dirname(String path)
	{
		if (NacFile.isEmpty(path))
		{
			return "";
		}

		String basename = NacFile.basename(path);
		return path.substring(0, path.length()-basename.length());
	}

	/**
	 * @return True if the string is empty or null, and False otherwise.
	 */
	public static boolean isEmpty(String path)
	{
		return (path == null) || path.isEmpty();
	}

	/**
	 * Remove extension from file name.
	 */
	@SuppressWarnings("unused")
	public static String removeExtension(String name)
	{
		if (NacFile.isEmpty(name))
		{
			return "";
		}

		return (name.contains(".")) ?
			name.substring(0, name.lastIndexOf('.')) : name;
	}

	/**
	 * Strip away any trailing '/' characters.
	 */
	public static String strip(String path)
	{
		if (NacFile.isEmpty(path))
		{
			return "";
		}

		int length = path.length();
		if (length == 0)
		{
			return "";
		}

		String normalPath = path;
		if (normalPath.charAt(length-1) == '/')
		{
			normalPath= normalPath.substring(0, length-1);
		}

		return normalPath;
	}

	/**
	 * Convert a path to a relative path's directory name
	 *
	 * @param  path  Path to a file or directory.
	 */
	public static String toRelativeDirname(String path)
	{
		String relativePath = NacFile.toRelativePath(path);
		String relativeDirname = NacFile.dirname(relativePath);

		return NacFile.strip(relativeDirname);
	}

	/**
	 * @see #toRelativeDirname(String)
	 */
	public static String toRelativeDirname(Uri uri)
	{
		String path = uri.toString();
		return NacFile.toRelativeDirname(path);
	}

	/**
	 * Convert a path to a relative path.
	 *
	 * @param  path  Path to a file or directory.
	 */
	@SuppressWarnings("SdCardPath")
	public static String toRelativePath(String path)
	{
		if ((path == null) || path.isEmpty())
		{
			return "";
		}

		String emulated = "/storage/emulated/0";
		String sdcard = "/sdcard";
		String relativePath = path;

		if (path.startsWith(emulated))
		{
			relativePath = path.substring(emulated.length());
		}
		else if (path.startsWith(sdcard))
		{
			relativePath = path.substring(sdcard.length());
		}

		if (!relativePath.isEmpty() && (relativePath.charAt(0) == '/'))
		{
			relativePath = relativePath.substring(1);
		}

		return relativePath;
	}

}
