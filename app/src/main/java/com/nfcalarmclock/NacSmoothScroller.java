package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;

/**
 * Smooth scroller
 */
public class NacSmoothScroller
	extends LinearSmoothScroller
{

	/**
	 * Scrolling snap preference.
	 */
	private final int mSnap;

	/**
	 * Speed to scroll in millimeters per pixel.
	 */
	private final float mSpeed;

	/**
	 */
	public NacSmoothScroller(Context context, int position, int snap, int speed)
	{
		super(context);

		this.mSnap = snap;
		this.mSpeed = speed;

		setTargetPosition(position);
	}

	/**
	 */
	@Override
	protected float calculateSpeedPerPixel(DisplayMetrics dm)
	{
		return mSpeed / dm.densityDpi;
	}

	/**
	 */
	@Override
	protected int getVerticalSnapPreference()
	{
		return mSnap;
	}

}
