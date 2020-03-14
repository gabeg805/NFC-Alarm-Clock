package com.nfcalarmclock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class NacFileListingTree
	extends NacTreeNode<String>
{

	/**
	 * The current directory.
	 */
	private NacTreeNode<String> mDirectory;

	/**
	 */
	public NacFileListingTree(String path)
	{
		super(null, path);
		this.setDirectory(this);
	}

	/**
	 * Add item to the given directory.
	 */
	public void add(String childData, String subChildData)
	{
		NacTreeNode<String> dir = this.getDirectory();

		if (dir == null)
		{
			return;
		}

		NacTreeNode<String> childDir = (childData.isEmpty() || childData.equals("."))
			? dir : dir.getChild(childData);

		if (childDir == null)
		{
			return;
		}

		childDir.addChild(subChildData);
	}

	/**
	 * @see add
	 */
	public void add(String childData)
	{
		this.add(".", childData);
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
		NacUtility.printf("Listing files after cd to: %s! %b", dir.getData(), dir != null);
		for (String p : this.ls())
		{
			NacUtility.printf("cd : ls : %s", p);
		}
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
		return this.getData();
	}

	/**
	 * List contents.
	 */
	public List<String> ls()
	{
		NacTreeNode<String> dir = this.getDirectory();
		List<String> listing = new ArrayList<>();

		if (dir == null)
		{
			return listing;
		}

		List<NacTreeNode<String>> children = dir.getChildren();

		for (NacTreeNode<String> child : children)
		{
			listing.add(child.getData());
		}

		return listing;
	}

	/**
	 * List contents.
	 */
	public List<String> ls(String path)
	{
		NacTreeNode<String> dir = this.getDirectory();

		if (dir.getData().equals(path))
		{
			return this.ls();
		}
		else
		{
			String home = this.getData();
			String newPath = path.replace(home, "");
			String[] items = newPath.split("/");
			NacTreeNode<String> newDir = dir;

			for (int i=0; i < items.length; i++)
			{
				if (items[i].isEmpty())
				{
					continue;
				}

				NacUtility.printf("Item : %s", items[i]);
				newDir = newDir.getChild(items[i]);
				this.setDirectory(newDir);
			}


			List<String> listing = this.ls();
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
