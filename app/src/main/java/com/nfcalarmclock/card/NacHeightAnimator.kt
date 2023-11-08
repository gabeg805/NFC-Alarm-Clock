package com.nfcalarmclock.card

import android.animation.ValueAnimator
import android.animation.ValueAnimator.AnimatorUpdateListener
import android.view.View

/**
 * Animator to expand or collapse a view.
 */
class NacHeightAnimator @JvmOverloads constructor(

	/**
	 * View who's height will be animated.
	 */
	private val view: View,

	/**
	 * Height before starting the animation.
	 */
	fromHeight: Int = 0,

	/**
	 * Height after ending the animation.
	 */
	toHeight: Int = 0

	// Constructor
) : ValueAnimator(),

	// Interface
	AnimatorUpdateListener
{

	/**
	 * Listener for when the view's height is changing.
	 */
	interface OnAnimateHeightListener
	{
		fun onAnimateCollapse(animator: NacHeightAnimator)
		fun onAnimateExpand(animator: NacHeightAnimator)
	}

	/**
	 * Type of animation.
	 */
	enum class AnimationType
	{
		COLLAPSE,
		EXPAND
	}

	/**
	 * The height to start with.
	 */
	var fromHeight = 0

	/**
	 * The height to end with.
	 */
	var toHeight = 0

	/**
	 * Type of animation.
	 */
	var animationType: AnimationType = AnimationType.COLLAPSE

	/**
	 * Listener for when the view's height is changing.
	 */
	var onAnimateHeightListener: OnAnimateHeightListener? = null

	/**
	 * The animated height.
	 */
	private val animatedHeight: Int
		get() = animatedValue as Int

	/**
	 * Check if the view is collapsing or not.
	 */
	private val isCollapsing: Boolean
		get() = animationType == AnimationType.COLLAPSE

	/**
	 * Check if the view is expanding or not.
	 */
	private val isExpanding: Boolean
		get() = animationType == AnimationType.EXPAND

	/**
	 * Check if this is the first update or not.
	 */
	val isFirstUpdate: Boolean
		get() = (fromHeight == animatedHeight) && (updateCounter == 0)

	/**
	 * Check if this is the last update or not.
	 */
	val isLastUpdate: Boolean
		get() = animatedHeight == toHeight

	/**
	 * Count the number of times the animation has updated.
	 */
	private var updateCounter = 0

	/**
	 * Constructor.
	 */
	init
	{
		setHeights(fromHeight, toHeight)
		addUpdateListener(this)
	}

	/**
	 * Update the height of the view.
	 */
	override fun onAnimationUpdate(animation: ValueAnimator)
	{
		// Set the height to the animated height
		view.layoutParams.height = animatedHeight

		// Change the view's bounds
		view.requestLayout()

		// Collapse
		if (isCollapsing)
		{
			onAnimateHeightListener?.onAnimateCollapse(this)
		}
		//Expand
		else if (isExpanding)
		{
			onAnimateHeightListener?.onAnimateExpand(this)
		}

		// Count number of updates
		updateCounter += 1
	}

	/**
	 * Set the heights.
	 */
	fun setHeights(fromHeight: Int, toHeight: Int)
	{
		// Set the member variable heights
		this.fromHeight = fromHeight
		this.toHeight = toHeight

		// Set the values
		setIntValues(fromHeight, toHeight)
	}

	/**
	 * Start the animation.
	 */
	override fun start()
	{
		// Reset the update counter
		updateCounter = 0

		// Start the animation
		super.start()
	}

}