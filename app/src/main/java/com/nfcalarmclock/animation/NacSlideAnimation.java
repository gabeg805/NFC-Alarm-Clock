package com.nfcalarmclock;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.View;

/**
 * Slide animation.
 */
public class NacSlideAnimation
	extends Animation
	implements Animation.AnimationListener
{

	/**
	 * Hide the view during the specified state.
	 */
	protected enum HideOnStatus
	{
		NEVER,
		END,
		START
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
	 * View visibility.
	 */
	protected int mVisibility;

	/**
	 * Hide on status.
	 */
	protected HideOnStatus mHideOnStatus;

	/**
	 * Skip the listener.
	 */
	protected boolean mSkipListener;

	/**
	 */
	public NacSlideAnimation(View view)
	{
		this(view, NacUtility.getHeight(view), 0);
	}

	/**
	 */
	public NacSlideAnimation(View view, int fromHeight, int toHeight)
	{
		this.mView = view;
		this.mFromHeight = fromHeight;
		this.mToHeight = toHeight;
		this.mVisibility = view.getVisibility();
		this.mHideOnStatus = HideOnStatus.NEVER;
		this.mSkipListener = false;
	}

	/**
	 */
	@Override
	protected void applyTransformation(float interpolatedTime,
		Transformation transformation)
	{
		if (this.mView.getHeight() != this.mToHeight)
		{
			int newHeight = (int) (this.mFromHeight
				+ ((this.mToHeight - this.mFromHeight) * interpolatedTime));
			this.mView.getLayoutParams().height = newHeight;
			this.mView.requestLayout();
		}
	}

	/**
	 * @return The hide on status.
	 */
	private HideOnStatus getHideOnStatus()
	{
		return this.mHideOnStatus;
	}

	/**
	 * @return The height.
	 */
	public int getHeight()
	{
		int fromHeight = this.mFromHeight;
		int toHeight = this.mToHeight;

		return (fromHeight > toHeight) ? fromHeight : toHeight;
	}

	/**
	 * @return The view.
	 */
	private View getView()
	{
		return this.mView;
	}

	/**
	 * @return The view visibility.
	 */
	private int getVisibility()
	{
		return this.mVisibility;
	}

	/**
	 * Hide the view.
	 */
	public void hide()
	{
		if (this.getVisibility() == View.VISIBLE)
		{
			this.getView().setVisibility(View.GONE);
		}
	}

	/**
	 */
	@Override
	public void initialize(int width, int height, int parentWidth,
		int parentHeight)
	{
		super.initialize(width, height, parentWidth, parentHeight);
	}

	/**
	 */
	@Override
	public void onAnimationEnd(Animation animation)
	{
		HideOnStatus status = this.getHideOnStatus();

		switch (status)
		{
			case END:
				this.hide();
				break;

			case START:
				this.show();
				break;

			case NEVER:
			default:
				return;
		}

		this.mVisibility = this.getView().getVisibility();
	}

	/**
	 */
	@Override
	public void onAnimationRepeat(Animation animation)
	{
	}

	/**
	 */
	@Override
	public void onAnimationStart(Animation animation)
	{
		HideOnStatus status = this.getHideOnStatus();

		switch (status)
		{
			case END:
				this.show();
				break;

			case START:
				this.hide();
				break;

			case NEVER:
			default:
				break;
		}
	}

	/**
	 * Set the heights.
	 */
	public void setHeights(int fromHeight, int toHeight)
	{
		this.mFromHeight = fromHeight;
		this.mToHeight = toHeight;
	}

	/**
	 * Set the flags to hide the view on animation end.
	 */
	public void setHideOnEnd()
	{
		this.mHideOnStatus = HideOnStatus.END;

		setAnimationListener(this);
	}

	/**
	 * Hide the view never.
	 */
	public void setHideOnNever()
	{
		this.mHideOnStatus = HideOnStatus.NEVER;

		setAnimationListener(null);
	}

	/**
	 * Hide the view on animation start.
	 */
	public void setHideOnStart()
	{
		this.mHideOnStatus = HideOnStatus.START;

		setAnimationListener(this);
	}

	/**
	 * Setup heights for closing the view.
	 */
	public void setupForClose()
	{
		View view = this.getView();
		int viewHeight = view.getMeasuredHeight();
		int fromHeight = this.mFromHeight;
		int toHeight = this.mToHeight;
		this.mSkipListener = false;
		this.mVisibility = view.getVisibility();

		if (fromHeight < toHeight)
		{
			this.mFromHeight = toHeight;
			this.mToHeight = fromHeight;
		}
	}

	/**
	 * Setup heights for opening the view.
	 */
	public void setupForOpen()
	{
		View view = this.getView();
		int viewHeight = view.getMeasuredHeight();
		int fromHeight = this.mFromHeight;
		int toHeight = this.mToHeight;
		this.mSkipListener = false;
		this.mVisibility = view.getVisibility();

		if (fromHeight > toHeight)
		{
			this.mFromHeight = toHeight;
			this.mToHeight = fromHeight;
		}
	}

	/**
	 * Skip the listener.
	 */
	public void setupForSkipListener()
	{
		this.mSkipListener = true;
	}

	/**
	 * Set visibility.
	 */
	public void setVisibility(int visibility)
	{
		this.mVisibility = visibility;
	}

	/**
	 * @return True if the listener should be skipped and False otherwise.
	 */
	public boolean skipListener()
	{
		return this.mSkipListener;
	}

	/**
	 * Show the view.
	 */
	public void show()
	{
		if (this.getVisibility() == View.GONE)
		{
			this.getView().setVisibility(View.VISIBLE);
		}
	}

	/**
	 */
	@Override
	public boolean willChangeBounds()
	{
		return true;
	}

}
