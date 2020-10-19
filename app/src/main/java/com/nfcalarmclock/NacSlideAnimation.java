package com.nfcalarmclock;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.View;

/**
 * Slide animation.
 */
public class NacSlideAnimation
	extends Animation
{

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
	}

	/**
	 */
	@Override
	protected void applyTransformation(float interpolatedTime,
		Transformation transformation)
	{
		View view = this.getView();
		int fromHeight = this.getFromHeight();
		int toHeight = this.getToHeight();
		int newHeight = -1;

		//if (view.getHeight() != toHeight)
		//if (interpolatedTime == 1)
		if ((view.getHeight() == toHeight) && (interpolatedTime == 1))
		{
			NacUtility.printf("Apply INtransformation : %d -> %d || New : %d || Time : %f", fromHeight, toHeight, newHeight, interpolatedTime);
		}
		else
		{
			//int newHeight = (int) (fromHeight + ((toHeight - fromHeight) * interpolatedTime));
			newHeight = (int) (fromHeight + ((toHeight - fromHeight) * interpolatedTime));
			NacUtility.printf("Apply transformation : %d -> %d || New : %d || Time : %f", fromHeight, toHeight, newHeight, interpolatedTime);
			view.getLayoutParams().height = newHeight;
			view.requestLayout();
		}
	}

	/**
	 * @return The from height.
	 */
	public int getFromHeight()
	{
		return this.mFromHeight;
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

	//@Override
	//public void initialize(int width, int height, int parentWidth,
	//	int parentHeight)
	//{
	//	super.initialize(width, height, parentWidth, parentHeight);
	//}

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
	 * Set the heights.
	 */
	public void setHeights(int fromHeight, int toHeight)
	{
		this.mFromHeight = fromHeight;
		this.mToHeight = toHeight;
	}

	/**
	 */
	@Override
	public boolean willChangeBounds()
	{
		return true;
	}

}
