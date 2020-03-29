package com.nfcalarmclock;

import java.util.ArrayList;
import java.util.List;

/**
 * Node in a tree.
 */
public class NacTreeNode<T>
{

	/**
	 * Root node of this node.
	 */
	private NacTreeNode<T> mRoot;

	/**
	 * Children of this node.
	 */
	private List<NacTreeNode<T>> mChildren;

	/**
	 * Key.
	 */
	private T mKey;

	/**
	 * Value.
	 */
	private Object mValue;

	/**
	 */
	public NacTreeNode()
	{
		this(null, null, null);
	}

	/**
	 */
	public NacTreeNode(NacTreeNode<T> root, T key, Object value)
	{
		this.mRoot = root;
		this.mKey = key;
		this.mValue = value;
		this.mChildren = new ArrayList<>();
	}

	/**
	 * Add a child.
	 */
	public void addChild(NacTreeNode<T> child)
	{
		if (this.exists(child))
		{
			return;
		}

		this.getChildren().add(child);
	}

	/**
	 * @see addChild
	 */
	public void addChild(T key, Object value)
	{
		NacTreeNode<T> child = new NacTreeNode<T>(this, key, value);

		this.addChild(child);
	}

	/**
	 * @see addChild
	 */
	public void addChild(T key)
	{
		this.addChild(key, null);
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
	public boolean exists(T key)
	{
		return (this.getChild(key) != null);
	}

	/**
	 * @see exists
	 */
	public boolean exists(NacTreeNode<T> child)
	{
		return (this.getChild(child) != null);
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
	public NacTreeNode<T> getChild(T key)
	{
		List<NacTreeNode<T>> children = this.getChildren();

		for (NacTreeNode<T> c : children)
		{
			if (c.getKey().equals(key))
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
		return (child != null) ? this.getChild(child.getKey()) : null;
	}

	/**
	 * @return The children.
	 */
	public List<NacTreeNode<T>> getChildren()
	{
		return this.mChildren;
	}

	/**
	 * @return The key of the node.
	 */
	public T getKey()
	{
		return this.mKey;
	}

	/**
	 * @return The root node.
	 */
	public NacTreeNode<T> getRoot()
	{
		return this.mRoot;
	}

	/**
	 * @return The value of the node.
	 */
	public Object getValue()
	{
		return this.mValue;
	}

	/**
	 * @see count
	 */
	public int size()
	{
		return this.count();
	}

}
