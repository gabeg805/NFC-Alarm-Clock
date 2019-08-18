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
	 * Collapse view.
	 */
	private View mCollapseView;

	/**
	 * Expand view.
	 */
	private View mExpandView;

	/**
	 */
	public NacCardSlideAnimation(View mainView, View collapseView, View expandView)
	{
		super(mainView);

		this.mCollapseView = collapseView;
		this.mExpandView = expandView;

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
		}
	}

}
