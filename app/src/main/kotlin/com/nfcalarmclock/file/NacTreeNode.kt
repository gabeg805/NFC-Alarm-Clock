package com.nfcalarmclock.file

/**
 * Node in a tree.
 */
open class NacTreeNode<T>(

	/**
	 * Key.
	 */
	var key: T,

	/**
	 * Value.
	 */
	var value: Any? = null,

	/**
	 * Root node of this node.
	 */
	var root: NacTreeNode<T>? = null

)
{

	/**
	 * Children of this node.
	 */
	val children: MutableList<NacTreeNode<T>> = ArrayList()

	/**
	 * Add a child.
	 */
	private fun addChild(child: NacTreeNode<T>)
	{
		// Child already exists. Do not add it
		if (this.exists(child))
		{
			return
		}

		// Add child
		children.add(child)
	}

	/**
	 * @see .addChild
	 */
	fun addChild(key: T, value: Any?)
	{
		// Create child
		val child = NacTreeNode(key, value, this)

		// Add child
		this.addChild(child)
	}

	/**
	 * Get the number of children.
	 *
	 * @return The number of children.
	 */
	fun count(): Int
	{
		return children.size
	}

	/**
	 * Check if the child exists.
	 *
	 * @return True if the child exists, and False otherwise.
	 */
	fun exists(key: T): Boolean
	{
		return this.getChild(key) != null
	}

	/**
	 * Check if the child exists as a child of the node.
	 *
	 * @return True if the child exists as a child of the node, and False
	 *         otherwise.
	 */
	fun exists(child: NacTreeNode<T>?): Boolean
	{
		return this.getChild(child) != null
	}

	/**
	 * Get the child with the key.
	 *
	 * @return The child with the key.
	 */
	fun getChild(key: T): NacTreeNode<T>?
	{
		// Iterate over each child
		for (c in children)
		{
			// Key matches
			if (c.key == key)
			{
				// Return the child
				return c
			}
		}

		// Unable to find the child
		return null
	}

	/**
	 * @see .getChild
	 */
	private fun getChild(child: NacTreeNode<T>?): NacTreeNode<T>?
	{
		return if (child != null)
			{
				this.getChild(child.key)
			}
			else
			{
				null
			}
	}

	/**
	 * @see .count
	 */
	fun size(): Int
	{
		return this.count()
	}

}