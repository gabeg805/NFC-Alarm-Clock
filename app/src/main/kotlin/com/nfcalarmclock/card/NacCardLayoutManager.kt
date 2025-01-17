package com.nfcalarmclock.card

import android.content.Context
import android.util.DisplayMetrics
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSmoothScroller
import androidx.recyclerview.widget.RecyclerView

/**
 * Linear layout manager for a recyclerview that is primarily used because it
 * handles the smooth scrolling.
 */
class NacCardLayoutManager(context: Context)
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
		val smoothScroller = SmoothScroller(recyclerView.context, position)

		// Start the smooth scroll
		startSmoothScroll(smoothScroller)
	}

	/**
	 * Smooth scroller
	 */
	class SmoothScroller(context: Context?, position: Int)
		: LinearSmoothScroller(context)
	{

		companion object
		{

			/**
			 * Speed to scroll in millimeters per pixel.
			 */
			private const val SPEED = 50f

		}

		/**
		 * Constructor.
		 */
		init
		{
			targetPosition = position
		}

		/**
		 * Calculate the amount to scroll to consider the view visible.
		 */
		override fun calculateDtToFit(viewStart: Int,
			viewEnd: Int,
			boxStart: Int,
			boxEnd: Int,
			snapPreference: Int): Int
		{
			return (boxStart + (boxEnd - boxStart) / 2) - (viewStart + (viewEnd - viewStart) / 2)
		}

		/**
		 * Calculate the scroll speed.
		 */
		override fun calculateSpeedPerPixel(dm: DisplayMetrics): Float
		{
			return SPEED / dm.densityDpi
		}

	}

}