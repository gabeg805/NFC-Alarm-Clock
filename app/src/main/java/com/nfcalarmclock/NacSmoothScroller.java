package com.nfcalarmclock;

import android.content.Context;
import android.support.v7.widget.LinearSmoothScroller;
import android.util.DisplayMetrics;

/**
 * @brief NFC Alarm Clock smooth scroller.
 */
public class NacSmoothScroller
	extends LinearSmoothScroller
{

	/**
	 * @brief The position to scroll to.
	 */
	private int mPosition;

	/**
	 * @brief Scrolling snap preference.
	 */
	private int mPreference;

	/**
	 * @brief Speed to scroll in millimeters per pixel.
	 */
	private final float mSpeed = 100;

	/**
	 * @brief Constructor for the smooth scroller.
	 */
	public NacSmoothScroller(Context c, int pos)
	{
		this(c, pos, LinearSmoothScroller.SNAP_TO_START);
	}

	/**
	 * @brief Constructor for the smooth scroller.
	 */
	public NacSmoothScroller(Context c, int pos, int pref)
	{
		super(c);
		this.mPosition = pos;
		this.mPreference = pref;
		this.setTargetPosition(this.mPosition);
	}

	@Override
	protected int getVerticalSnapPreference()
	{
		return mPreference;
	}

	@Override
	protected float calculateSpeedPerPixel(DisplayMetrics dm)
	{
		return mSpeed / dm.densityDpi;
	}

}
