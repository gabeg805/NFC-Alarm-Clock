package com.nfcalarmclock.alarm.activealarm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.nfcalarmclock.R
import kotlin.math.hypot

/**
 * Animator to guide how the user should swipe and interact with the view.
 */
class NacSwipeAnimationHandler(activity: AppCompatActivity)
{

	/**
	 * Pulse animation for the snooze button.
	 */
	private val snoozePulseAnimation = AnimationUtils.loadAnimation(activity, R.anim.pulse)

	/**
	 * Pulse animation for the dismiss button.
	 */
	private val dismissPulseAnimation = AnimationUtils.loadAnimation(activity, R.anim.pulse)

	/**
	 * Fade in animation for the slider path.
	 */
	private val sliderPathFadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in)

	/**
	 * Fade out animation for the slider path.
	 */
	private val sliderPathFadeOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out)

	/**
	 * Fade in animation for the slider instructions.
	 */
	private val sliderInstructionsFadeInAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_in)

	/**
	 * Fade out animation for the slider instructions.
	 */
	private val sliderInstructionsFadeOutAnimation = AnimationUtils.loadAnimation(activity, R.anim.fade_out)

	/**
	 * Latest animator for the inactive view.
	 */
	private var latestInactiveViewAnimator: Animator? = null

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
		// Stop the snooze animation
		snoozeAttentionView.clearAnimation()

		// Hide the snooze view
		snoozeAttentionView.visibility = View.INVISIBLE

		// Check if the dismiss view should be modified
		if (dismissAttentionView.visibility != View.GONE)
		{
			// Stop the dismiss animation
			dismissAttentionView.clearAnimation()

			// Hide the dismiss view
			dismissAttentionView.visibility = View.INVISIBLE
		}
	}

	/**
	 * Start the animation to hide the inactive view, which can be the snooze
	 * or dismiss button.
	 */
	fun hideInactiveView(inactiveView: View)
	{
		// Check if the view visibility is gone
		if (inactiveView.visibility == View.GONE)
		{
			// Do nothing
			return
		}

		// Clear the latest animator if it is set
		latestInactiveViewAnimator?.cancel()

		// Get the center for the clipping circle.
		val cx = inactiveView.width / 2
		val cy = inactiveView.height / 2

		// Get the initial radius for the clipping circle.
		val initialRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

		// Create the animation. The final radius is 0.
		val anim = ViewAnimationUtils.createCircularReveal(inactiveView, cx, cy, initialRadius, 0f)

		// Make the view invisible when the animation is done.
		anim.addListener(object : AnimatorListenerAdapter() {

			// Check if the animator was canceled
			var wasCanceled = false

			/**
			 * Called when the animation is canceled.
			 */
			override fun onAnimationCancel(animation: Animator)
			{
				wasCanceled = true
			}

			/**
			 * Called when the animation is ended.
			 */
			override fun onAnimationEnd(animation: Animator)
			{
				// Check if the animator was canceled
				if (wasCanceled)
				{
					return
				}

				// Hide the inactive view
				inactiveView.visibility = View.INVISIBLE
			}

		})

		// Start the animation.
		anim.duration = 150
		anim.start()

		// Set the latest animator for the inactive view
		latestInactiveViewAnimator = anim
	}

	/**
	 * Start the animation to hide the slider path.
	 */
	fun hideSliderPath(sliderPath: View, sliderInstructions: View)
	{
		// Create the listener
		val pathListener = createHideAnimationListener(sliderPath)
		val instructionsListener = createHideAnimationListener(sliderInstructions)

		// Set the listener
		sliderPathFadeOutAnimation.setAnimationListener(pathListener)
		sliderInstructionsFadeOutAnimation.setAnimationListener(instructionsListener)

		// Start the animation
		sliderPath.startAnimation(sliderPathFadeOutAnimation)
		sliderInstructions.startAnimation(sliderInstructionsFadeOutAnimation)
	}

	/**
	 * Start the animation to show the attention views.
	 */
	fun showAttentionViews(snoozeAttentionView: View, dismissAttentionView: View)
	{
		// Show the snooze view
		snoozeAttentionView.visibility = View.VISIBLE

		// Start the snooze animation
		snoozeAttentionView.startAnimation(snoozePulseAnimation)

		// Check if the dismiss view should be modified
		if (dismissAttentionView.visibility != View.GONE)
		{
			// Show the dismiss view
			dismissAttentionView.visibility = View.VISIBLE

			// Start the dismiss animation
			dismissAttentionView.startAnimation(dismissPulseAnimation)
		}
	}

	/**
	 * Start the animation to show the inactive view, which can be the snooze
	 * or dismiss button.
	 */
	fun showInactiveView(inactiveView: View, onEnd: () -> Unit = {})
	{
		// Check if the view visibility is gone
		if (inactiveView.visibility == View.GONE)
		{
			// Call onEnd() right away
			onEnd()
			return
		}

		// Clear the latest animator if it is set
		latestInactiveViewAnimator?.cancel()

		// Get the center for the clipping circle.
		val cx = inactiveView.width / 2
		val cy = inactiveView.height / 2

		// Get the final radius for the clipping circle.
		val finalRadius = hypot(cx.toDouble(), cy.toDouble()).toFloat()

		// Define the animator
		val anim: Animator

		try
		{
			// Create the animator for this view. The start radius is 0.
			anim = ViewAnimationUtils.createCircularReveal(inactiveView, cx, cy, 0f, finalRadius)
		}
		catch (e: IllegalStateException)
		{
			return
		}

		// Make the view visible and start the animation.
		inactiveView.visibility = View.VISIBLE
		anim.duration = 150
		anim.addListener(
			onEnd = {
				onEnd()
			}
		)
		anim.start()

		// Set the latest animator for the inactive view
		latestInactiveViewAnimator = anim
	}

	/**
	 * Start the animation to show the slider path.
	 */
	fun showSliderPath(sliderPath: View, sliderInstructions: View)
	{
		// Create the listener
		val pathListener = createShowAnimationListener(sliderPath)
		val instructionsListener = createShowAnimationListener(sliderInstructions)

		// Set the listener
		sliderPathFadeInAnimation.setAnimationListener(pathListener)
		sliderInstructionsFadeInAnimation.setAnimationListener(instructionsListener)

		// Start the animation
		sliderPath.startAnimation(sliderPathFadeInAnimation)
		sliderInstructions.startAnimation(sliderInstructionsFadeInAnimation)
	}

}