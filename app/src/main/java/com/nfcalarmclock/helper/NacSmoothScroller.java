package com.nfcalarmclock;

import android.content.Context;
//import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;

import androidx.recyclerview.widget.LinearSmoothScroller;

/**
 * Smooth scroller
 */
public class NacSmoothScroller
	extends LinearSmoothScroller
{

	/**
	 * Speed to scroll in millimeters per pixel.
	 */
	private static final float SPEED = 250f;

	/**
	 */
	public NacSmoothScroller(Context context, int position)
	{
		super(context);

		setTargetPosition(position);
	}

	/**
	 */
	@Override
	protected float calculateSpeedPerPixel(DisplayMetrics dm)
	{
		return SPEED / dm.densityDpi;
	}

	/**
	 */
	//@Override
	//protected int getVerticalSnapPreference()
	//{
	//	return mSnap;
	//}

}
