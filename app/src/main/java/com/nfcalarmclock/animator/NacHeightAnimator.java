package com.nfcalarmclock.animator;

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
		addUpdateListener(this);
	}

	/**
	 * Call the listener for when the view is collapsing.
	 */
	public void callOnAnimateCollapseListener()
	{
		OnAnimateHeightListener listener = this.getOnAnimateHeightListener();
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
		int fromHeight = this.getFromHeight();
		int toHeight = this.getToHeight();
		return (fromHeight > toHeight);
	}

	/**
	 * @return True if the view is expanding, and False otherwise.
	 */
	public boolean isExpanding()
	{
		int fromHeight = this.getFromHeight();
		int toHeight = this.getToHeight();
		return (fromHeight < toHeight);
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
		return (toHeight == height);
	}

	/**
	 * Update the height of the view.
	 */
	@Override
	public void onAnimationUpdate(ValueAnimator animation)
	{
		View view = this.getView();
		view.getLayoutParams().height = this.getAnimatedHeight();

		view.requestLayout();

		if (this.isCollapsing())
		{
			this.callOnAnimateCollapseListener();
		}
		else if (this.isExpanding())
		{
			this.callOnAnimateExpandListener();
		}

		this.mUpdateCounter += 1;
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
		this.mUpdateCounter = 0;
		super.start();
	}

}
