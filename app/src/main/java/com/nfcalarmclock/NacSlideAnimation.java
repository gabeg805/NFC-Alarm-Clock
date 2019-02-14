package com.nfcalarmclock;

import android.view.animation.Animation;
import android.view.animation.Transformation;
import android.view.View;
import android.view.ViewGroup.LayoutParams;

/**
 * NFC Alarm Clock Slide Animation.
 */
public class NacSlideAnimation
	extends Animation
{

	/**
	 * The view to animate.
	 */
	private View mView;

	/**
	 * The original height of the view.
	 */
	private int mFromHeight;

	/**
	 * The height to set the view to, once the animation is complete.
	 */
	private int mToHeight;

	/**
	 */
	public NacSlideAnimation(View v, int from, int to)
	{
		this(v, from, to, -1);
	}

	/**
	 */
	public NacSlideAnimation(View v, int from, int to, int duration)
	{
		this.mView = v;
		this.mFromHeight = from;
		this.mToHeight = to;

		if (duration < 0)
		{
			float density = v.getContext().getResources().getDisplayMetrics().density;
			duration = (int)(((to > from) ? to : from)/density);
		}

		this.mView.getLayoutParams().height = this.mFromHeight;
		this.setDuration(duration);
	}

	/**
	 * Apply the transformation to the view to animate it.
	 */
	@Override
	protected void applyTransformation(float time, Transformation trans)
	{
		if (this.mView.getHeight() != this.mToHeight)
		{
			int newHeight = (int) (this.mFromHeight + ((this.mToHeight
				- this.mFromHeight) * time));
			this.mView.getLayoutParams().height = newHeight;
			this.mView.requestLayout();
		}
	}

	/**
	 * Initialize the animation.
	 */
	@Override
	public void initialize(int width, int height, int parentWidth,
		int parentHeight)
	{
		super.initialize(width, height, parentWidth, parentHeight);
	}

	/**
	 * Change the bounds of the animation.
	 */
	@Override
	public boolean willChangeBounds()
	{
		return true;
	}

}
