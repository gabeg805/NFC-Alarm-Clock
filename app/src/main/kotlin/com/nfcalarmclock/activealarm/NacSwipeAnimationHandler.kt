package com.nfcalarmclock.activealarm

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Context
import android.view.View
import android.view.ViewAnimationUtils
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import androidx.core.animation.addListener
import com.nfcalarmclock.R
import java.lang.IllegalStateException
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

	//private val helloBroth: NacCustomArcView = activity.findViewById(R.id.hellobroth)

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
		//// Start the animation to hide the inactive view
		//inactiveButtonScaleDown.setAnimationListener(createHideAnimationListener(inactiveView))
		//inactiveView.startAnimation(inactiveButtonScaleDown)

		// Check if the view visibility is gone
		if (inactiveView.visibility == View.GONE)
		{
			// Do nothing
			return
		}

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

	fun scanNfc(context: Context, scanNfcView1: View, scanNfcView2: View, scanNfcView3: View)
	{
		//val handler = Handler(Looper.getMainLooper())
		//var index = 0

		//handler.post(object: Runnable {

		//	override fun run()
		//	{

		//		val highlight = context.resources.getColor(R.color.white)
		//		val gray = context.resources.getColor(R.color.gray)

		//		if (index == 0)
		//		{
		//			scanNfcView1.backgroundTintList = ColorStateList.valueOf(highlight)
		//		}
		//		else
		//		{
		//			scanNfcView1.backgroundTintList = ColorStateList.valueOf(gray)
		//		}

		//		if (index == 1)
		//		{
		//			scanNfcView2.backgroundTintList = ColorStateList.valueOf(highlight)
		//		}
		//		else
		//		{
		//			scanNfcView2.backgroundTintList = ColorStateList.valueOf(gray)
		//		}

		//		if (index == 2)
		//		{
		//			scanNfcView3.backgroundTintList = ColorStateList.valueOf(highlight)
		//		}
		//		else
		//		{
		//			scanNfcView3.backgroundTintList = ColorStateList.valueOf(gray)
		//		}

		//		index = (index+1) % 3


		//		println("Changing color")
		//		handler.postDelayed(this, 400)
		//	}
		//})
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
	 * Show bros animation.
	 */
	fun showBroth()
	{
		//helloBroth.startAnimation()
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

		// Check if the view visibility is gone
		if (inactiveView.visibility == View.GONE)
		{
			// Call onEnd() right away
			onEnd()
			return
		}

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