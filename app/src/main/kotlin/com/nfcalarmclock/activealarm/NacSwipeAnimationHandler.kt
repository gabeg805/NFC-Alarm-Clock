package com.nfcalarmclock.activealarm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.core.animation.addListener
import com.nfcalarmclock.R
import kotlin.math.hypot

/**
 * Animator to guide how the user should swipe and interact with the view.
 */
class NacSwipeAnimationHandler(context: Context)
{

	/**
	 * Pulse animation for the snooze button.
	 */
	private val snoozePulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse)

	/**
	 * Pulse animation for the dismiss button.
	 */
	private val dismissPulseAnimation = AnimationUtils.loadAnimation(context, R.anim.pulse)

	/**
	 * Fade in animation for the slider path.
	 */
	private val sliderPathFadeInAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_in)

	/**
	 * Fade out animation for the slider path.
	 */
	private val sliderPathFadeOutAnimation = AnimationUtils.loadAnimation(context, R.anim.fade_out)

	//private val inactiveButtonScaleUp = AnimationUtils.loadAnimation(context, R.anim.scale_up)
	//private val inactiveButtonScaleDown = AnimationUtils.loadAnimation(context, R.anim.scale_down)

	/**
	 * Create an animation listener to hide a view.
	 */
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

	/**
	 * Create an animation listener to show a view.
	 */
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

	/**
	 * Start the animation to hide the attention views.
	 */
	fun hideAttentionViews(snoozeAttentionView: View, dismissAttentionView: View)
	{
		// Stop the animations
		snoozeAttentionView.clearAnimation()
		dismissAttentionView.clearAnimation()

		// Hide the views
		snoozeAttentionView.visibility = View.INVISIBLE
		dismissAttentionView.visibility = View.INVISIBLE
	}

	/**
	 * Start the animation to hide the inactive view, which can be the snooze
	 * or dismiss button.
	 */
	fun hideInactiveView(inactiveView: View)
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

			/**
			 * Called when the animation is ended.
			 */
			override fun onAnimationEnd(animation: Animator) {
				inactiveView.visibility = View.INVISIBLE
			}

		})

		// Start the animation.
		anim.duration = 150
		anim.start()
	}

	/**
	 * Start the animation to hide the slider path.
	 */
	fun hideSliderPath(sliderPath: View)
	{
		// Create the listener
		val listener = createHideAnimationListener(sliderPath)

		// Set the listener
		sliderPathFadeOutAnimation.setAnimationListener(listener)

		// Start the animation
		sliderPath.startAnimation(sliderPathFadeOutAnimation)
	}

	/**
	 * Start the animation to show the attention views.
	 */
	fun showAttentionViews(snoozeAttentionView: View, dismissAttentionView: View)
	{
		// Show the views
		snoozeAttentionView.visibility = View.VISIBLE
		dismissAttentionView.visibility = View.VISIBLE

		// Start the animations
		snoozeAttentionView.startAnimation(snoozePulseAnimation)
		dismissAttentionView.startAnimation(dismissPulseAnimation)
	}

	/**
	 * Start the animation to show the inactive view, which can be the snooze
	 * or dismiss button.
	 */
	fun showInactiveView(inactiveView: View, onEnd: () -> Unit = {})
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

	/**
	 * Start the animation to show the slider path.
	 */
	fun showSliderPath(sliderPath: View)
	{
		// Create the listener
		val listener = createShowAnimationListener(sliderPath)

		// Set the listener
		sliderPathFadeInAnimation.setAnimationListener(listener)

		// Start the animation
		sliderPath.startAnimation(sliderPathFadeInAnimation)
	}

}