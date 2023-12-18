package com.nfcalarmclock.activealarm

import android.animation.AnimatorInflater
import android.animation.ValueAnimator
import android.content.Context
import androidx.core.animation.doOnEnd
import com.nfcalarmclock.R

/**
 * Animator to guide how the user should swipe and interact with the view.
 */
class NacSwipeGuideAnimator(context: Context)
{

	/**
	 * Neutral animator when the user is not interacting with it.
	 */
	private var neutralAnimator: ValueAnimator =
		AnimatorInflater.loadAnimator(context, R.animator.active_alarm_neutral_swipe_guide) as ValueAnimator

	/**
	 * Action animator when the user is interacting with it.
	 */
	private var actionAnimator: ValueAnimator =
		AnimatorInflater.loadAnimator(context, R.animator.active_alarm_action_swipe_guide) as ValueAnimator

	/**
	 * Check if the swipe guide is collapsed.
	 */
	private var isCollapsed: Boolean = false

	/**
	 * Constructor.
 	 */
	init
	{
		// Define what to do when the action animation ends
		actionAnimator.doOnEnd {

			// Check if the swipe guide is collapsed
			if (isCollapsed)
			{
				// Resume the neutral animator
				neutralAnimator.resume()

				// Disable the flag
				isCollapsed = false
			}
		}
	}

	/**
	 * Cancel the animators.
	 */
	fun cancel()
	{
		neutralAnimator.cancel()
		actionAnimator.cancel()
	}

	/**
	 * Collapse the guide.
	 */
	fun collapse()
	{
		// Pause the neutral animator
		neutralAnimator.pause()

		// Define the start and end values of the animator.
		val endValue = 0
		val startValue = if (isCollapsed)
		{
			// The collapse process was interrupted so start where the action
			// animator left off
			actionAnimator.animatedValue as Int
		}
		else
		{
			// Start where the neutral animator left off
			neutralAnimator.animatedValue as Int
		}

		// Setup the animator
		actionAnimator.setIntValues(startValue, endValue)

		// Start the animator
		actionAnimator.start()

		// Disable the flag
		isCollapsed = false
	}

	/**
	 * Expand the guide.
	 */
	fun expand()
	{
		// Define the start and end values of the animator. If the action
		// animator was interrupted, then the start value could be much larger
		// than the neutral animator's start current value, so enforce this by
		// simply using the neutral animator's current value
		val startValue = neutralAnimator.animatedValue as Int
		val endValue = actionAnimator.animatedValue as Int

		// Setup the animator
		actionAnimator.setIntValues(startValue, endValue)

		// Reverse the action animator
		actionAnimator.reverse()

		// Enable the flag
		isCollapsed = true
	}

	/**
	 * Start the animator. This will only start the neutral animator.
	 */
	fun start(listener: (ValueAnimator) -> Unit = {})
	{
		// Setup the animators. They will have the same update listener
		neutralAnimator.addUpdateListener { listener(it) }
		actionAnimator.addUpdateListener { listener(it) }

		// Start the neutral animator
		neutralAnimator.start()
	}

}