package com.nfcalarmclock.main

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * Linear layout manager for a recyclerview that is primarily used because it
 * handles the smooth scrolling.
 */
class NacLayoutManager(context: Context)
	: LinearLayoutManager(context)
{

	/**
	 * Smooth scrol lto the specified adapeter position.
	 */
	override fun smoothScrollToPosition(
		recyclerView: RecyclerView,
		state: RecyclerView.State,
		position: Int)
	{
		// Create a smooth scroller
		val context = recyclerView.context
		val smoothScroller = SmoothScroller(context, position)

		// Start the smooth scroll
		startSmoothScroll(smoothScroller)
	}

	/**
	 * Smooth scroller
	 */
	class SmoothScroller(context: Context?, position: Int) : LinearSmoothScroller(context)
	{

		companion object
		{

			/**
			 * Speed to scroll in millimeters per pixel.
			 */
			private const val SPEED = 250f

		}

		/**
		 * Constructor.
		 */
		init
		{
			targetPosition = position
		}

		/**
		 * Calculates the scroll speed.
		 */
		override fun calculateSpeedPerPixel(dm: DisplayMetrics): Float
		{
			return SPEED / dm.densityDpi
		}

	}

}