package com.nfcalarmclock;

import android.view.animation.AccelerateInterpolator;
import android.view.animation.Animation;
import android.view.View;

/**
 * Card slide animation.
 */
public class NacCardSlideAnimation
	extends NacSlideAnimation
	implements Animation.AnimationListener
{

	/**
	 * Callback when the animation starts or ends.
	 */
	public interface OnAnimationListener
	{
		public void onAnimationEnd(Animation animation);
		public void onAnimationStart(Animation animation);
	}

	/**
	 * Collapse view.
	 */
	private View mCollapseView;

	/**
	 * Expand view.
	 */
	private View mExpandView;

	/**
	 * Animation start/end listener.
	 */
	private OnAnimationListener mAnimationListener;

	/**
	 */
	public NacCardSlideAnimation(View mainView, View collapseView,
		View expandView)
	{
		super(mainView);

		this.mCollapseView = collapseView;
		this.mExpandView = expandView;
		this.mAnimationListener = null;

		setAnimationListener(this);
		setInterpolator(new AccelerateInterpolator());
	}

	/**
	 */
	@Override
	public void onAnimationEnd(Animation animation)
	{
		if (skipListener())
		{
			return;
		}

		int fromHeight = this.mFromHeight;
		int toHeight = this.mToHeight;

		if (fromHeight > toHeight)
		{
			this.mCollapseView.setVisibility(View.VISIBLE);
			this.mExpandView.setVisibility(View.GONE);
			this.mCollapseView.setEnabled(true);
			this.mExpandView.setEnabled(false);

			if (this.mAnimationListener != null)
			{
				this.mAnimationListener.onAnimationEnd(animation);
			}
		}
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
		if (skipListener())
		{
			return;
		}

		int fromHeight = this.mFromHeight;
		int toHeight = this.mToHeight;

		if (fromHeight < toHeight)
		{
			this.mCollapseView.setVisibility(View.GONE);
			this.mExpandView.setVisibility(View.VISIBLE);
			this.mCollapseView.setEnabled(false);
			this.mExpandView.setEnabled(true);

			if (this.mAnimationListener != null)
			{
				this.mAnimationListener.onAnimationStart(animation);
			}
		}
	}

	/**
	 * Set the animation start/end listener.
	 */
	public void setOnAnimationListener(OnAnimationListener listener)
	{
		this.mAnimationListener = listener;
	}

}
