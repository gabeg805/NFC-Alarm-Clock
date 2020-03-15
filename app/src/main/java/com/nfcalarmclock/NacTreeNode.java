package com.nfcalarmclock;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 */
public class NacTreeNode<T>
{

	/**
	 * Root node of this node.
	 */
	private NacTreeNode<T> mRoot;

	/**
	 * Data.
	 */
	private T mData;

	/**
	 * Children of this node.
	 */
	private List<NacTreeNode<T>> mChildren;

	/**
	 */
	public NacTreeNode()
	{
		this(null, null);
	}

	/**
	 */
	public NacTreeNode(NacTreeNode<T> root, T data)
	{
		this.mRoot = root;
		this.mData = data;
		this.mChildren = new ArrayList<>();
	}

	/**
	 * Add a child.
	 */
	public void addChild(NacTreeNode<T> child)
	{
		// Should also try exists(child) when I get a chance.
		//if (this.exists(child.getData()))
		if (this.exists(child))
		{
			return;
		}

		this.getChildren().add(child);
	}

	/**
	 * @see addChild
	 */
	public void addChild(T childData)
	{
		NacTreeNode<T> child = new NacTreeNode<T>(this, childData);

		this.addChild(child);
	}

	/**
	 * @return The number of children.
	 */
	public int count()
	{
		return this.getChildren().size();
	}

	/**
	 * @return True if the child exists, and False otherwise.
	 */
	public boolean exists(T data)
	{
		return (this.getChild(data) != null);
	}

	/**
	 * @see exists
	 */
	public boolean exists(NacTreeNode<T> child)
	{
		return ((child != null) && this.exists(child.getData()));
	}

	/**
	 * @see getChild
	 */
	public NacTreeNode<T> getChild(int index)
	{
		List<NacTreeNode<T>> children = this.getChildren();
		int size = this.count();

		return ((index >= 0) && (index < size)) ? children.get(index)
			: null;
	}

	/**
	 * @return The requested child with the given data.
	 */
	public NacTreeNode<T> getChild(T data)
	{
		List<NacTreeNode<T>> children = this.getChildren();

		for (NacTreeNode<T> c : children)
		{
			if (c.getData().equals(data))
			{
				return c;
			}
		}

		return null;
	}

	/**
	 * @see getChild
	 */
	public NacTreeNode<T> getChild(NacTreeNode<T> child)
	{
		return (child != null) ? this.getChild(child.getData()) : null;
	}

	/**
	 * @return The children.
	 */
	public List<NacTreeNode<T>> getChildren()
	{
		return this.mChildren;
	}

	/**
	 * @return The data.
	 */
	public T getData()
	{
		return this.mData;
	}

	/**
	 * @return The root node.
	 */
	public NacTreeNode<T> getRoot()
	{
		return this.mRoot;
	}

	/**
	 * @see count
	 */
	public int size()
	{
		return this.count();
	}

	///**
	// * Sort the children.
	// */
	//public void sort()
	//{
	//	Collections.sort(this.mChildren);
	//}

}
