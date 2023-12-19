package com.nfcalarmclock.activealarm

import android.annotation.SuppressLint
import android.view.MotionEvent
import android.view.View
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.RelativeLayout
import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm

class NacSwipeLayoutHandler(

	/**
	 * Activity.
	 */
	activity: AppCompatActivity,

	/**
	 * Alarm.
	 */
	alarm: NacAlarm?,

	/**
	 * Listener for an alarm action.
	 */
	onAlarmActionListener: OnAlarmActionListener

	// Constructor
) : NacActiveAlarmLayoutHandler(activity, alarm, onAlarmActionListener)
{

	/**
	 * View that represents the parent click and slide. This is the max size
	 * that the click and slide view can be. The parent needs to be there so
	 * that resizing of the children does not affect any of the other views in
	 * the activity
	 */
	private val parentClickAndSlideView: RelativeLayout = activity.findViewById(R.id.parent_click_and_slide)

	/**
	 * View that represents the example click and slide.
	 */
	private val exampleClickAndSlideView: RelativeLayout = activity.findViewById(R.id.example_click_and_slide)

	/**
	 * Animator for the guide on how the user should swipe.
	 */
	private val swipeGuideAnimator: NacSwipeGuideAnimator = NacSwipeGuideAnimator(activity)

	/**
	 * Animator for how the user is swiping.
	 */
	private val userSwipeAnimator: NacUserSwipeAnimator = NacUserSwipeAnimator(activity)



	private val sliderPath: RelativeLayout = activity.findViewById(R.id.alarm_action_slider_path)
	private val sliderPathScaleUp = AnimationUtils.loadAnimation(activity, R.anim.scale_up)
	private val sliderPathScaleDown = AnimationUtils.loadAnimation(activity, R.anim.scale_down)

	private val snoozeButton: RelativeLayout = activity.findViewById(R.id.round_snooze_button)
	private val dismissButton: RelativeLayout = activity.findViewById(R.id.round_dismiss_button)
	private var startAlarmActionX: Float = -1f
	private var endAlarmActionX: Float = -1f

	private val snoozeAttentionView: RelativeLayout = activity.findViewById(R.id.snooze_attention_view)
	private val dismissAttentionView: RelativeLayout = activity.findViewById(R.id.dismiss_attention_view)
	private val snoozeScaleAnimation = AnimationUtils.loadAnimation(activity, R.anim.pulse)
	private val dismissScaleAnimation = AnimationUtils.loadAnimation(activity, R.anim.pulse)


	/**
	 * Setup an alarm action button.
	 */
	@SuppressLint("ClickableViewAccessibility")
	private fun setupAlarmActionButton(button: View)
	{
		// X position information
		val origX = button.x
		var dx = 0f

		// Set the listener
		button.setOnTouchListener { view, motionEvent ->

			// Check action of motion event
			when (motionEvent.action)
			{

				// Finger DOWN on button
				MotionEvent.ACTION_DOWN ->
				{
					// Compute the offset X position
					dx = view.x - motionEvent.rawX

					// Cancel the animations
					snoozeAttentionView.clearAnimation()
					dismissAttentionView.clearAnimation()

					// Show the slider path
					// TODO: Hide the attention view and other snooze/dismiss button
					//sliderPath.visibility = View.VISIBLE
					sliderPathScaleUp.setAnimationListener(object: Animation.AnimationListener {
						override fun onAnimationStart(p0: Animation?)
						{
							println("Scale up vis start")
							sliderPath.visibility = View.VISIBLE
						}

						override fun onAnimationEnd(p0: Animation?)
						{
							println("Scale up vis end")
						}

						override fun onAnimationRepeat(p0: Animation?)
						{
						}

					})
					sliderPath.startAnimation(sliderPathScaleUp)
				}

				// Finger UP on button
				MotionEvent.ACTION_UP ->
				{
					// Animate back to the original X position
					view.animate()
						.x(origX)
						.setDuration(500)
						.start()

					// Start the animations
					snoozeAttentionView.startAnimation(snoozeScaleAnimation)
					dismissAttentionView.startAnimation(dismissScaleAnimation)

					// Hide the slider path
					//sliderPath.visibility = View.INVISIBLE
					sliderPathScaleDown.setAnimationListener(object: Animation.AnimationListener {
						override fun onAnimationStart(p0: Animation?)
						{
							println("Scale down vis start")
							sliderPath.visibility = View.VISIBLE
						}

						override fun onAnimationEnd(p0: Animation?)
						{
							println("Scale down vis end")
							sliderPath.visibility = View.INVISIBLE
						}

						override fun onAnimationRepeat(p0: Animation?)
						{
						}

					})
					sliderPath.startAnimation(sliderPathScaleDown)
				}

				// Moving finger
				MotionEvent.ACTION_MOVE ->
				{
					// Calculate the new X position
					var newX = motionEvent.rawX + dx

					// Check if the new X position is less than the start position
					if (newX < startAlarmActionX)
					{
						// Check if the view's position is equal to the start position
						if (view.x == startAlarmActionX)
						{
							// Do not move the view
							return@setOnTouchListener true
						}

						// Set the new X position to the start position
						newX = startAlarmActionX
					}
					// Check if the new X position is greater than the end position
					else if (newX > endAlarmActionX)
					{
						// Check if the view's position is equal to the end position
						if (view.x == endAlarmActionX)
						{
							// Do not move the view
							return@setOnTouchListener true
						}

						// Set the new X position to the end position
						newX = endAlarmActionX
					}

					// Set the new X position
					view.x = newX
				}

			}

			// Return
			true
		}
	}

	/**
	 * Setup the dismiss button.
	 */
	private fun setupDismissButton()
	{
		// Determine the right bound of where the snooze/dismiss button can go
		endAlarmActionX = dismissButton.x

		// Setup the dismiss button
		setupAlarmActionButton(dismissButton)
	}

	/**
	 * Setup the snooze button.
	 */
	private fun setupSnoozeButton()
	{
		// Determine the left bound of where the snooze/dismiss button can go
		startAlarmActionX = snoozeButton.x

		// Setup the snooze button
		setupAlarmActionButton(snoozeButton)
	}

	/**
	 * Start the layout and run any setup that needs to run.
	 */
	fun start()
	{

		// Start the swipe guide animator
		// TODO: Make the swipe guide on the action buttons
		swipeGuideAnimator.start {
			//exampleClickAndSlideView.setViewSizeFromAnimator(it)
			//snoozeAttentionView.setViewSizeFromAnimator(it)
		}

		snoozeAttentionView.startAnimation(snoozeScaleAnimation)
		dismissAttentionView.startAnimation(dismissScaleAnimation)

		// Setup the snooze and dismiss buttons
		setupSnoozeButton()
		setupDismissButton()
	}

	/**
	 * Stop the layout handler.
	 */
	fun stop()
	{
		// Cleanup the animators
		swipeGuideAnimator.cancel()
		userSwipeAnimator.cancel()
	}

}