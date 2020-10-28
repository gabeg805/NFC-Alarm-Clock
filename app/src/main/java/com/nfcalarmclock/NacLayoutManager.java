package com.nfcalarmclock;

import android.content.Context;
import android.util.DisplayMetrics;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.LinearSmoothScroller;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Linear layout manager for a recyclerview that is primarily used because it
 * handles the smooth scrolling.
 */
public class NacLayoutManager
	extends LinearLayoutManager
{

	/**
	 */
	public NacLayoutManager(Context context)
	{
		super(context);
	}

	/**
	 */
    @Override
    public void smoothScrollToPosition(RecyclerView recyclerView,
		RecyclerView.State state, int position)
	{
		Context context = recyclerView.getContext();
		SmoothScroller smoothScroller = new SmoothScroller(context, position);

        startSmoothScroll(smoothScroller);
    }

	/**
	 * Smooth scroller
	 */
	public static class SmoothScroller
		extends LinearSmoothScroller
	{

		/**
		 * Speed to scroll in millimeters per pixel.
		 */
		private static final float SPEED = 250f;

		/**
		 */
		public SmoothScroller(Context context, int position)
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

	}

}
