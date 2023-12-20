package com.nfcalarmclock.activealarm

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.animation.addListener
import androidx.core.animation.doOnEnd
import com.nfcalarmclock.R
import kotlin.math.hypot

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

	private val snoozeScaleAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse)
	private val dismissScaleAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse)

	private val sliderPathScaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up)
	private val sliderPathScaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down)
	private val inactiveButtonScaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up)
	private val inactiveButtonScaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down)

	private fun createHideAnimationListener(
		view: View,
		onStart: () -> Unit = {},
		onEnd: () -> Unit = {},
		onRepeat: () -> Unit = {}
	): Animation.AnimationListener
	{
		return object: Animation.AnimationListener {

			/**
			 * Called when the animation is started.
			 */
			override fun onAnimationStart(p0: Animation?)
			{
				// Call the listener
				onStart()
			}

			/**
			 * Called when the animation is ended.
			 */
			override fun onAnimationEnd(p0: Animation?)
			{
				// Hide the view
				view.visibility = View.INVISIBLE

				// Call the listener
				onEnd()
			}

			/**
			 * Called when the animation is repeated.
			 */
			override fun onAnimationRepeat(p0: Animation?)
			{
				// Call the listener
				onRepeat()
			}

		}
	}

	private fun createShowAnimationListener(
		view: View,
		onStart: () -> Unit = {},
		onEnd: () -> Unit = {},
		onRepeat: () -> Unit = {}
	): Animation.AnimationListener
	{
		return object: Animation.AnimationListener {

			/**
			 * Called when the animation is started.
			 */
			override fun onAnimationStart(p0: Animation?)
			{
				// Show the view
				view.visibility = View.VISIBLE

				// Call the listener
				onStart()
			}

			/**
			 * Called when the animation is ended.
			 */
			override fun onAnimationEnd(p0: Animation?)
			{
				// Call the listener
				onEnd()
			}

			/**
			 * Called when the animation is repeated.
			 */
			override fun onAnimationRepeat(p0: Animation?)
			{
				// Call the listener
				onRepeat()
			}

		}
	}

	fun hideSnoozeOrDismissButton(inactiveView: View)
	{
		//// Start the animation to hide the inactive view
		//inactiveButtonScaleDown.setAnimationListener(createHideAnimationListener(inactiveView))
		//inactiveView.startAnimation(inactiveButtonScaleDown)

		// Get the center for the clipping circle.
		val cx = inactiveView.width / 2
		val cy = inactiveView.height / 2

		// Get the initial radius for the clipping circle.
		val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

		// Create the animation. The final radius is 0.
		val anim = ViewAnimationUtils.createCircularReveal(inactiveView, cx, cy, initialRadius, 0f)

		// Make the view invisible when the animation is done.
		anim.addListener(object : AnimatorListenerAdapter() {

			override fun onAnimationEnd(animation: Animator) {
				inactiveView.visibility = View.INVISIBLE
			}
		})

		// Start the animation.
		anim.duration = 150
		anim.start()
	}

	fun showSnoozeOrDismissButton(inactiveView: View, onEnd: () -> Unit = {})
	{
		//// Start the animation to show the inactive view
		//inactiveButtonScaleUp.setAnimationListener(createShowAnimationListener(inactiveView, onEnd = onEnd))
		//inactiveView.startAnimation(inactiveButtonScaleUp)

		// Get the center for the clipping circle.
		val cx = inactiveView.width / 2
		val cy = inactiveView.height / 2

		// Get the final radius for the clipping circle.
		val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

		// Create the animator for this view. The start radius is 0.
		val anim = ViewAnimationUtils.createCircularReveal(inactiveView, cx, cy, 0f, finalRadius)

		// Make the view visible and start the animation.
		inactiveView.visibility = View.VISIBLE
		anim.duration = 150
		anim.addListener(
			onEnd = {
				onEnd()
			}
		)
		anim.start()
	}

	fun startAttentionAnimations(snoozeAttentionView: View, dismissAttentionView: View)
	{
		// Show the views
		snoozeAttentionView.visibility = View.VISIBLE
		dismissAttentionView.visibility = View.VISIBLE

		// Start the animations
		snoozeAttentionView.startAnimation(snoozeScaleAnimation)
		dismissAttentionView.startAnimation(dismissScaleAnimation)
	}

	fun stopAttentionAnimations(snoozeAttentionView: View, dismissAttentionView: View)
	{
		// Stop the animations
		snoozeAttentionView.clearAnimation()
		dismissAttentionView.clearAnimation()

		// Hide the views
		snoozeAttentionView.visibility = View.INVISIBLE
		dismissAttentionView.visibility = View.INVISIBLE
	}

	fun startSliderPathAnimation(sliderPath: View)
	{
		// Set the animation listener
		sliderPathScaleUp.setAnimationListener(object: Animation.AnimationListener {

			/**
			 * Called when the animation is started.
			 */
			override fun onAnimationStart(p0: Animation?)
			{
				// Show the slider path
				sliderPath.visibility = View.VISIBLE
			}

			// Do nothing
			override fun onAnimationEnd(p0: Animation?) {}

			// Do nothing
			override fun onAnimationRepeat(p0: Animation?) {}

		})

		// Start the animation
		sliderPath.startAnimation(sliderPathScaleUp)
	}

	fun stopSliderPathAnimation(sliderPath: View)
	{
		// Set the animation listener
		sliderPathScaleDown.setAnimationListener(object: Animation.AnimationListener {

			// Do nothing
			override fun onAnimationStart(p0: Animation?) {}

			/**
			 * Called when the animation is ended.
			 */
			override fun onAnimationEnd(p0: Animation?)
			{
				// Hide the slider path
				sliderPath.visibility = View.INVISIBLE
			}

			// Do nothing
			override fun onAnimationRepeat(p0: Animation?) {}

		})

		// Start the animation
		sliderPath.startAnimation(sliderPathScaleDown)
	}

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