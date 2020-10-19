package com.nfcalarmclock;

import android.animation.Animator;
import android.animation.AnimatorInflater;
import android.view.View;
import android.animation.ValueAnimator;

/**
 * Animator to expand or collapse a view.
 */
public class NacHeightAnimator
	extends ValueAnimator
	implements ValueAnimator.AnimatorUpdateListener
{

	/**
	 * Listener for when the view is collapsing.
	 */
	public interface OnViewCollapseListener
	{
		public void onViewCollapse(NacHeightAnimator animator);
	}

	/**
	 * Listener for when the view is expanding.
	 */
	public interface OnViewExpandListener
	{
		public void onViewExpand(NacHeightAnimator animator);
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
	 * Listener for when the view is collapsing.
	 */
	protected OnViewCollapseListener mOnViewCollapseListener;

	/**
	 * Listener for when the view is expanding.
	 */
	protected OnViewExpandListener mOnViewExpandListener;

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
		this.mView = view;
		this.mFromHeight = fromHeight;
		this.mToHeight = toHeight;

		addUpdateListener(this);
	}

	/**
	 * Call the listener for when the view is collapsing.
	 */
	public void callOnViewCollapseListener()
	{
		OnViewCollapseListener listener = this.getOnViewCollapseListener();
		if (listener != null)
		{
			listener.onViewCollapse(this);
		}
	}

	/**
	 * Call the listener for when the view is expanding.
	 */
	public void callOnViewExpandListener()
	{
		OnViewExpandListener listener = this.getOnViewExpandListener();
		if (listener != null)
		{
			listener.onViewExpand(this);
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
	protected OnViewCollapseListener getOnViewCollapseListener()
	{
		return this.mOnViewCollapseListener;
	}

	/**
	 * @return The listener for when the view is expanding.
	 */
	protected OnViewExpandListener getOnViewExpandListener()
	{
		return this.mOnViewExpandListener;
	}

	/**
	 * @return The to height.
	 */
	public int getToHeight()
	{
		return this.mToHeight;
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
		return fromHeight == height;
	}

	/**
	 * @return True if this is the last update, and False otherwise.
	 */
	public boolean isLastUpdate()
	{
		int toHeight = this.getToHeight();
		int height = this.getAnimatedHeight();
		return toHeight == height;
	}

	/**
	 * Update the height of the view.
	 * 
	 * TODO The first iteration gets called twice for some reason?
	 */
	@Override
	public void onAnimationUpdate(ValueAnimator animation)
	{
		View view = this.getView();
		view.getLayoutParams().height = this.getAnimatedHeight();
		view.requestLayout();

		if (this.isCollapsing())
		{
			this.callOnViewCollapseListener();
		}
		else if (this.isExpanding())
		{
			this.callOnViewExpandListener();
		}
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
	 * Set the listener for when the view is collapsing.
	 */
	public void setOnViewCollapseListener(OnViewCollapseListener listener)
	{
		this.mOnViewCollapseListener = listener;
	}

	/**
	 * Set the listener for when the view is expanding.
	 */
	public void setOnViewExpandListener(OnViewExpandListener listener)
	{
		this.mOnViewExpandListener = listener;
	}

}
