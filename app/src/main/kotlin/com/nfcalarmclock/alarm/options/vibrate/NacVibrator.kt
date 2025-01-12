package com.nfcalarmclock.alarm.options.vibrate

import android.annotation.TargetApi
import android.content.Context
import android.os.Build
import android.os.Handler
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import com.nfcalarmclock.alarm.db.NacAlarm

/**
 * Vibrate the device.
 */
class NacVibrator(context: Context)
{

	/**
	 * Vibrator object.
	 */
	@Suppress("deprecation")
	private val vibrator: Vibrator =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			// Get the manager
			val manager = context.getSystemService(
				Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

			// Return the vibrator
			manager.defaultVibrator
		}
		// Use the old API
		else
		{
			context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
		}

	/**
	 * Vibrate handler to vibrate the device at periodic intervals.
	 */
	private val handler: Handler = Handler(context.mainLooper)

	/**
	 * Count the number of times the vibration has been repeated.
	 *
	 * This is only used for custom patterns.
	 */
	private var currentRepeatCount: Int = 0

	/**
	 * Flag if the vibrator is running.
	 */
	var isRunning: Boolean = false

	/**
	 * Cleanup any resources.
	 */
	fun cleanup()
	{
		// Stop any current vibrations
		vibrator.cancel()

		// Stop any future vibrations from occuring
		handler.removeCallbacksAndMessages(null)

		// Clear the flag
		isRunning = false
	}

	/**
	 * Do the vibration.
	 */
	@Suppress("deprecation")
	@TargetApi(Build.VERSION_CODES.O)
	private fun doVibrate(duration: Long, wait: Long)
	{
		// Cancel the previous vibration, if any
		cleanup()

		// Check if the new API needs to be used
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			// Create the vibration effect
			val effect = VibrationEffect.createOneShot(duration,
				VibrationEffect.DEFAULT_AMPLITUDE)

			// Vibrate
			vibrator.vibrate(effect)
		}
		// The old API can be used
		else
		{
			// Vibrate
			vibrator.vibrate(duration)
		}

		// Set the flag
		isRunning = true
	}

	/**
	 * Vibrate the device for an alarm.
	 */
	fun vibrate(alarm: NacAlarm)
	{
		// Vibrate with a pattern
		if (alarm.shouldVibratePattern)
		{
			vibrate(
				alarm.vibrateDuration,
				alarm.vibrateWaitTime,
				alarm.vibrateRepeatPattern,
				alarm.vibrateWaitTimeAfterPattern)
		}
		// Vibrate normally
		else
		{
			vibrate(alarm.vibrateDuration, alarm.vibrateWaitTime)
		}
	}

	/**
	 * Vibrate the device.
	 */
	fun vibrate(duration: Long, wait: Long)
	{
		// Vibrate
		doVibrate(duration, wait)

		// Wait for a period of time before vibrating the device again
		handler.postDelayed({ doVibrate(duration, wait) }, wait)
	}

	/**
	 * Vibrate the device with a pattern.
	 *
	 * @param duration Amount of time (ms) to vibrate for.
	 * @param wait Amount of time (ms) to wait after vibrating.
	 * @param repeatPattern Number of times to repeat a pattern.
	 * @param waitAfterPattern Amount of time (ms) to wait after a pattern is complete.
	 */
	fun vibrate(
		duration: Long,
		wait: Long,
		repeatPattern: Int,
		waitAfterPattern: Long)
	{
		// Vibrate
		println("FIRST VIBRATE")
		doVibrate(duration, wait)

		// Increase the count
		currentRepeatCount += 1

		// Wait for a period of time before vibrating the device again
		handler.postDelayed({

			// Check the current repeat count
			if (currentRepeatCount+1 == repeatPattern)
			{
				println("VIBRATE wait after pattern : $currentRepeatCount")
				// This is the last repeat before reaching the limit, so the wait time
				// should be the pattern wait time
				vibrate(duration, waitAfterPattern)

				// Reset the repeat count
				currentRepeatCount = 0
			}
			else
			{
				println("VIBRATE normally : $currentRepeatCount")
				// Vibrate normally
				vibrate(duration, wait)

				// Increase the count
				currentRepeatCount += 1
			}

		}, wait)
	}

}