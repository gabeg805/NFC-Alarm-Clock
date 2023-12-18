package com.nfcalarmclock.activealarm

import android.animation.Animator
import android.animation.AnimatorInflater
import android.animation.AnimatorListenerAdapter
import android.animation.ValueAnimator
import android.content.Context
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import com.nfcalarmclock.R

class NacUserSwipeAnimator(context: Context)
{

	/**
	 * Animator that is used to for the user swipe view.
	 */
	private val animator: ValueAnimator =
		AnimatorInflater.loadAnimator(context, R.animator.active_alarm_user_swipe) as ValueAnimator

	private val showFromBottom: Animation = AnimationUtils.loadAnimation(context, R.anim.show_from_bottom_to_top)
	private val hideFromTop: Animation = AnimationUtils.loadAnimation(context, R.anim.hide_from_top_to_bottom)

	/**
	 * Cancel the animator.
	 */
	fun cancel()
	{
		animator.cancel()
	}

	/**
	 * Cancel the animator if it is running.
	 */
	fun cancelIfRunning()
	{
		// Check if the reset animator is running
		if (animator.isRunning)
		{
			// Cancel the reset animator
			cancel()
		}
	}

	/**
	 * Reset the animator to its initial state.
	 */
	fun reset(from: Int, to: Int)
	{
		// Change background to what it originally was
		animator.setIntValues(from, to)
		animator.start()
	}

	/**
	 * Set the listener to call when the animator is updated.
	 */
	fun setOnUpdateListener(listener: (ValueAnimator) -> Unit = {})
	{
		// Set the update listener
		animator.addUpdateListener { listener(it) }
	}

	fun hello(view: View)
	{
		println("HELLO")

		//view.startAnimation(hideFromTop)
		//view.visibility = View.INVISIBLE

		view.apply {
			alpha = 0f
			translationY = 0f
			visibility = View.VISIBLE

			animate()
				.alpha(1f)
				.setDuration(1000)
				.translationY(view.layoutParams.height.toFloat())
				.start()
		}

	}

	fun there(view: View)
	{
		//view.startAnimation(showFromBottom)
		//view.visibility = View.VISIBLE

		println("THERE")
		var sup = true

		view.animate().apply {
			duration = 1000
			alpha(0f)
			translationY(0f)
			setListener(object: AnimatorListenerAdapter() {
				override fun onAnimationEnd(animation: Animator)
				{
					if (!sup)
					{
						return
					}

					println("ON ANIMATION END : $sup")
					view.visibility = View.GONE
					sup = false
				}
			})
		}.start()
	}

}