package com.nfcalarmclock;

import android.net.Uri;
import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
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
		 * Flag indicating whether the file is a directory.
		 */
		private boolean mIsDirectory;

		/**
		 * Flag indicating whether the file is a file.
		 */
		private boolean mIsFile;

		/**
		 */
		public Metadata(String directory, String name, long id)
		{
			this.mDirectory = directory;
			this.mName = name;
			this.mId = id;
			this.mIsDirectory = false;
			this.mIsFile = false;

			if (!directory.isEmpty())
			{
				String path = this.getPath();
				File file = new File(path);

				this.mIsDirectory = file.isDirectory();
				this.mIsFile = file.isFile();
			}
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
			return String.format("%s/%s", this.getDirectory(), this.getName());
		}

		/**
		 * @return True if it is a directory, and False otherwise.
		 */
		public boolean isDirectory()
		{
			return this.mIsDirectory;
		}

		/**
		 * @return True if it is a file, and False otherwise.
		 */
		public boolean isFile()
		{
			return this.mIsFile;
		}

		/**
		 * Print information.
		 */
		public void print()
		{
			NacUtility.printf("Directory : %s", this.getDirectory());
			NacUtility.printf("Filename  : %s", this.getName());
			NacUtility.printf("ID        : %d", this.getId());
			NacUtility.printf("Is Dir?   : %b", this.isDirectory());
			NacUtility.printf("Is File?  : %b", this.isFile());
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
		 * Add a file/folder to the given directory.
		 *
		 * @param  name  The name of the file or directory.
		 * @param  id    The content ID, used to create the content Uri.
		 */
		public void add(String name, long id)
		{
			NacTreeNode<String> dir = this.getDirectory();

			if (dir == null)
			{
				return;
			}

			dir.addChild(name, id);
			//dir.addChild(name, (id < 0) ? null : id);
		}

		/**
		 * @see add
		 */
		public void add(String name)
		{
			this.add(name, -1);
		}

		/**
		 * Add item to the given directory.
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
		 * @see addToDirectory
		 */
		public void addToDirectory(String toDirectory, String name)
		{
			this.addToDirectory(toDirectory, name, -1);
		}

		/**
		 * Change directory.
		 */
		public void cd(String path)
		{
			NacTreeNode<String> dir = this.getDirectory();
			NacTreeNode<String> newDir = (path.equals("..")) ? dir.getRoot()
				: dir.getChild(path);

			this.cd(newDir);
		}

		/**
		 * @see cd
		 */
		public void cd(NacTreeNode<String> dir)
		{
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

			while (ref != null)
			{
				path = path.isEmpty() ? node.getKey()
					: String.format("%s/%s", ref.getKey(), path);
				ref = ref.getRoot();
			}

			return path;
		}

		/**
		 * List contents of a directory.
		 */
		public List<Metadata> ls()
		{
			NacTreeNode<String> node = this.getDirectory();
			String dirPath = this.getPath(node);
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
		 * @see ls
		 */
		public List<Metadata> ls(String path)
		{
			NacTreeNode<String> dir = this.getDirectory();

			if (dir.getKey().equals(path))
			{
				return this.ls();
			}
			else
			{
				String home = this.getHome();
				String newPath = path.replace(home, "");
				String[] items = newPath.split("/");
				NacTreeNode<String> newDir = dir;

				for (int i=0; i < items.length; i++)
				{
					if (items[i].isEmpty())
					{
						continue;
					}

					newDir = newDir.getChild(items[i]);
					this.setDirectory(newDir);
				}


				List<Metadata> listing = this.ls();
				this.setDirectory(dir);
				return listing;
			}
		}

		/**
		 * Set the current directory.
		 */
		public void setDirectory(NacTreeNode<String> dir)
		{
			this.mDirectory = dir;
		}

	}

}
