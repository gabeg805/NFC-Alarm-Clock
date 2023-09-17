package com.nfcalarmclock.card;

import android.view.View;
import android.animation.ValueAnimator;

/**
 * Animator to expand or collapse a view.
 */
@SuppressWarnings("UnnecessaryInterfaceModifier")
public class NacHeightAnimator
	extends ValueAnimator
	implements ValueAnimator.AnimatorUpdateListener
{

	/**
	 * Listener for when the view's height is changing.
	 */
	public interface OnAnimateHeightListener
	{
		public void onAnimateCollapse(NacHeightAnimator animator);
		public void onAnimateExpand(NacHeightAnimator animator);
	}

	/**
	 * Type of animation.
	 */
	public enum AnimationType
	{
		COLLAPSE,
		EXPAND
	}

	/**
	 * The view to slide.
	 */
	protected View mView;

	/**
	 * The height to start with.
	 */
	protected int mFromHeight;

	/**
	 * The height to end with.
	 */
	protected int mToHeight;

	/**
	 * Type of animation.
	 */
	protected AnimationType mAnimationType;

	/**
	 * Count the number of times the animation has updated.
	 */
	protected int mUpdateCounter;

	/**
	 * Listener for when the view's height is changing.
	 */
	protected OnAnimateHeightListener mOnAnimateHeightListener;

	/**
	 */
	public NacHeightAnimator(View view)
	{
		this(view, 0, 0);
	}

	/**
	 */
	public NacHeightAnimator(View view, int fromHeight, int toHeight)
	{
		this.mUpdateCounter = 0;

		this.setView(view);
		this.setHeights(fromHeight, toHeight);
		this.setAnimationType(AnimationType.COLLAPSE);
		addUpdateListener(this);
	}

	/**
	 * Call the listener for when the view is collapsing.
	 */
	public void callOnAnimateCollapseListener()
	{
		OnAnimateHeightListener listener = this.getOnAnimateHeightListener();

		// Listener is not null
		if (listener != null)
		{
			listener.onAnimateCollapse(this);
		}
	}

	/**
	 * Call the listener for when the view is expanding.
	 */
	public void callOnAnimateExpandListener()
	{
		OnAnimateHeightListener listener = this.getOnAnimateHeightListener();

		// Listener is not null
		if (listener != null)
		{
			listener.onAnimateExpand(this);
		}
	}

	/**
	 * @return The animated height.
	 */
	public int getAnimatedHeight()
	{
		return (int) getAnimatedValue();
	}

	/**
	 * @return The animation type.
	 */
	public AnimationType getAnimationType()
	{
		return this.mAnimationType;
	}

	/**
	 * @return The from height.
	 */
	public int getFromHeight()
	{
		return this.mFromHeight;
	}

	/**
	 * @return The listener for when the view is collapsing.
	 */
	protected OnAnimateHeightListener getOnAnimateHeightListener()
	{
		return this.mOnAnimateHeightListener;
	}

	/**
	 * @return The to height.
	 */
	public int getToHeight()
	{
		return this.mToHeight;
	}

	/**
	 * @return The update counter.
	 */
	public int getUpdateCounter()
	{
		return this.mUpdateCounter;
	}

	/**
	 * @return The view.
	 */
	public View getView()
	{
		return this.mView;
	}

	/**
	 * @return True if the view is collapsing, and False otherwise.
	 */
	public boolean isCollapsing()
	{
		return this.getAnimationType() == AnimationType.COLLAPSE;
	}

	/**
	 * @return True if the view is expanding, and False otherwise.
	 */
	public boolean isExpanding()
	{
		return this.getAnimationType() == AnimationType.EXPAND;
	}

	/**
	 * @return True if this is the first update, and False otherwise.
	 */
	public boolean isFirstUpdate()
	{
		int fromHeight = this.getFromHeight();
		int height = this.getAnimatedHeight();

		return (fromHeight == height) && (this.getUpdateCounter() == 0);
	}

	/**
	 * @return True if this is the last update, and False otherwise.
	 */
	public boolean isLastUpdate()
	{
		int toHeight = this.getToHeight();
		int height = this.getAnimatedHeight();

		return (height == toHeight);
	}

	/**
	 * Update the height of the view.
	 */
	@Override
	public void onAnimationUpdate(ValueAnimator animation)
	{
		View view = this.getView();
		view.getLayoutParams().height = this.getAnimatedHeight();

		// Change the view's bounds
		view.requestLayout();

		// Collapse
		if (this.isCollapsing())
		{
			this.callOnAnimateCollapseListener();
		}
		// Expand
		else if (this.isExpanding())
		{
			this.callOnAnimateExpandListener();
		}

		// Count number of updates
		this.mUpdateCounter += 1;
	}

	/**
	 * Set the animation type.
	 *
	 * @param  type  Animation type.
	 */
	public void setAnimationType(AnimationType type)
	{
		this.mAnimationType = type;
	}

	/**
	 * Set the heights.
	 */
	public void setHeights(int fromHeight, int toHeight)
	{
		this.mFromHeight = fromHeight;
		this.mToHeight = toHeight;

		setIntValues(fromHeight, toHeight);
	}

	/**
	 * Set the listener for when the view's height is changing.
	 */
	public void setOnAnimateHeightListener(OnAnimateHeightListener listener)
	{
		this.mOnAnimateHeightListener = listener;
	}

	/**
	 * Set the view.
	 */
	public void setView(View view)
	{
		this.mView = view;
	}

	/**
	 * Start the animation.
	 */
	@Override
	public void start()
	{
		// Reset the update counter
		this.mUpdateCounter = 0;

		// Start the animation
		super.start();
	}

}
