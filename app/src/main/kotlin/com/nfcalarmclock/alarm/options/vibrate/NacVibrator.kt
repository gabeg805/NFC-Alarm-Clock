package com.nfcalarmclock.alarm.options.vibrate

import android.content.Context
import android.os.Build
import android.os.VibrationAttributes
import android.os.VibrationEffect
import android.os.Vibrator
import android.os.VibratorManager
import android.media.AudioAttributes
import android.os.Handler
import androidx.lifecycle.LifecycleCoroutineScope
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.log.NacLog
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Vibrate the device.
 *
 * @param context Context.
 * @param scope Coroutine scope.
 */
class NacVibrator(
	context: Context,
	private val scope: LifecycleCoroutineScope
)
{

	/**
	 * Vibrator object.
	 */
	@Suppress("deprecation")
	private val vibrator: Vibrator =
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			// Get the manager
			val manager = context.getSystemService(Context.VIBRATOR_MANAGER_SERVICE) as VibratorManager

			// Return the vibrator
			manager.defaultVibrator
		}
		// Use the old API
		else
		{
			context.getSystemService(Context.VIBRATOR_SERVICE) as Vibrator
		}

	/**
	 * Handler to repeatedly vibrate the device.
	 */
	private val handler : Handler = Handler(context.mainLooper)
	private var job: Job? = null

	/**
	 * Flag if the vibrator is running.
	 */
	var isRunning: Boolean = false

	/**
	 * Cleanup any resources.
	 */
	fun cleanup()
	{
		// Cancel job
		job?.cancel()

		// Stop any current vibrations
		stop()
	}

	/**
	 * Stop any vibration.
	 */
	private fun stop()
	{
		// Stop any current vibrations
		vibrator.cancel()

		// Clear the flag
		isRunning = false
	}

	/**
	 * Vibrate the device using on/off timings.
	 */
	@Suppress("deprecation")
	private fun vibrate(timings: List<Long>, wait: Long)
	{
		// API 26+
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			// Match amplitudes to the timings list (0 for pause, DEFAULT_AMPLITUDE for vibrate)
			val amplitudes: MutableList<Int> = ArrayList()
			val repeatPattern = timings.size / 3

			// Timings list is built around a pause, vibrate, pause sequence, so build the
			// amplitude list the same way
			repeat(repeatPattern) {
				amplitudes.addAll(listOf(0, VibrationEffect.DEFAULT_AMPLITUDE, 0))
			}

			//// This vibration sequence uses a pattern that ends with a different wait than
			//// wait in between vibrations. Add another wait at the end to account for this pattern
			//if (amplitudes.size != timings.size)
			//{
			//	amplitudes.add(0)
			//}

			//// Create a vibration that will repeat indefinitely (that is what the 0 is for)
			// Create a vibration
			val effect = try
			{
				VibrationEffect.createWaveform(timings.toLongArray(), amplitudes.toIntArray(), -1)
			}
			catch (e: IllegalArgumentException)
			{
				NacLog.e("Unable to create vibration effect. timings=$timings | amplitudes=$amplitudes", throwable = e)
				throw e
			}

			// Vibrate (API 33+)
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			{
				val attr = VibrationAttributes.createForUsage(VibrationAttributes.USAGE_ALARM)
				vibrator.vibrate(effect, attr)
			}
			// Vibrate (API 26-32)
			else
			{
				val attr = AudioAttributes.Builder()
					.setUsage(AudioAttributes.USAGE_ALARM)
					.build()
				vibrator.vibrate(effect, attr)
			}
		}
		// API 25-
		else
		{
			// Vibrate
			@Suppress("DEPRECATION")
			vibrator.vibrate(timings.toLongArray(), -1)
		}

		// Set the flag
		isRunning = true

		job = scope.launch {

			// Calculate the total duration of the vibration
			val duration = timings.sum()
			delay(duration)

			// Stop any previous vibration that may be running
			stop()

			// Avoid race condition between canceling and starting the vibration back to back
			delay(wait)

			vibrate(timings, wait)
			//handler.postDelayed({

			//}, duration)

		}

	}

	/**
	 * Vibrate the device for an alarm.
	 */
	fun vibrateAlarm(alarm: NacAlarm)
	{
		// Vibrate with a pattern
		if (alarm.shouldVibratePattern)
		{
			NacLog.i("Vibrating for ${alarm.vibrateDuration} ms, then waiting for ${alarm.vibrateWaitTime} ms. Repeat ${alarm.vibrateRepeatPattern} times, then wait for ${alarm.vibrateWaitTimeAfterPattern} ms (indefinitely)")

			vibrateWithPattern(
				alarm.vibrateDuration,
				alarm.vibrateWaitTime,
				alarm.vibrateRepeatPattern,
				alarm.vibrateWaitTimeAfterPattern)
		}
		// Vibrate normally
		else
		{
			NacLog.i("Vibrating for ${alarm.vibrateDuration} ms, then waiting for ${alarm.vibrateWaitTime} ms (indefinitely)")

			vibrateNormally(alarm.vibrateDuration, alarm.vibrateWaitTime)
		}
	}

	/**
	 * Vibrate the device normally.
	 */
	fun vibrateNormally(duration: Long, wait: Long)
	{
		// Vibrate pattern will be: pause 0ms, vibrate <duration> ms, pause <wait> ms
		//val timings = listOf(0, duration, wait)
		val timings = listOf(0, duration, 0)

		// Vibrate
		vibrate(timings, wait)
	}

	/**
	 * Vibrate the device with a pattern.
	 *
	 * @param duration Amount of time (ms) to vibrate for.
	 * @param wait Amount of time (ms) to wait after vibrating.
	 * @param repeatPattern Number of times to repeat a pattern.
	 * @param waitAfterPattern Amount of time (ms) to wait after a pattern is complete.
	 */
	fun vibrateWithPattern(
		duration: Long,
		wait: Long,
		repeatPattern: Int,
		waitAfterPattern: Long)
	{
		val timings: MutableList<Long> = ArrayList()

		// Vibrate pattern will be: pause 0ms, vibrate <duration> ms, pause <wait> ms
		// Repeat this for <repeatPattern> times
		repeat(repeatPattern) {
			timings.addAll(listOf(0, duration, wait))
		}

		//// Lastly, add a wait of <waitAfterPattern> ms
		//timings.add(waitAfterPattern)

		// Vibrate
		vibrate(timings, waitAfterPattern)
	}

}