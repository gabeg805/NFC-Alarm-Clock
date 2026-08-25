package com.nfcalarmclock.alarm.options.volume

import android.content.Context
import android.os.Handler
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.system.media.NacAudioAttributes

/**
 * Manage volume based on alarm settings.
 *
 * @param context Context.
 * @param alarm Alarm.
 * @param audioAttributes Audio attributes.
 */
class NacVolumeManager(
	context: Context,
	private val alarm: NacAlarm,
	private val audioAttributes: NacAudioAttributes
)
{

	/**
	 * Listener if a volume key press occurred.
	 */
	fun interface OnVolumeKeyPressListener
	{
		fun onVolumeKeyPress(alarm: NacAlarm)
	}

	/**
	 * Gradually increase the volume.
	 */
	private val graduallyIncreaseVolumeHandler: Handler = Handler(context.mainLooper)

	/**
	 * Restrict the volume.
	 */
	private val restrictVolumeHandler: Handler = Handler(context.mainLooper)

	/**
	 * Volume key press.
	 */
	private val volumeKeyPressHandler: Handler = Handler(context.mainLooper)

	/**
	 * Flag indicating whether to skip the restrict volume check until the
	 * gradually increase volume process has started.
	 */
	private var hasGraduallyIncreaseVolumeStarted: Boolean = false

	/**
	 * Volume level to restrict any volume changes to.
	 *
	 * This is not just the alarm volume, since if the user wants to gradually
	 * increase the volume, the restricted volume in that case should be lower
	 * than the alarm volume.
	 */
	private var volumeToRestrictChangeTo: Int = -1

	/**
	 * Volume level to restrict any volume changes to.
	 *
	 * This is not just the alarm volume, since if the user wants to gradually
	 * increase the volume, the restricted volume in that case should be lower
	 * than the alarm volume.
	 */
	private var initialVolume: Int = 0

	/**
	 * Volume key press listener.
	 */
	var onVolumeKeyPressListener: OnVolumeKeyPressListener? = null

	/**
	 * Cleanup resources.
	 */
	fun cleanup()
	{
		// Check if the the current volume was saved and if so, then it should
		// be reverted
		if (alarm.shouldUseTts || alarm.mediaPath.isNotEmpty())
		{
			// Revert the volume
			audioAttributes.revertVolume()
		}

		// Cleanup the gradually increasing volume handler
		graduallyIncreaseVolumeHandler.removeCallbacksAndMessages(null)

		// Cleanup the restrict volume handler
		restrictVolumeHandler.removeCallbacksAndMessages(null)

		// Cleanup the volume key press handler
		volumeKeyPressHandler.removeCallbacksAndMessages(null)
	}

	/**
	 * Gradually increase the volume.
	 */
	private fun graduallyIncreaseVolume()
	{
		// Get the alarm volume
		val alarmVolume = audioAttributes.alarmToStreamVolume()

		// Get the current volume
		val currentVolume = if (alarm.shouldRestrictVolume)
		{
			// Previous volume that was restricted to
			volumeToRestrictChangeTo
		}
		else
		{
			// Device volume
			audioAttributes.streamVolume
		}

		// Volume has not reached the alarm level yet
		if (currentVolume < alarmVolume)
		{
			// Gradually increase the volume by one step
			val newVolume = currentVolume + 1

			initialVolume = newVolume
			volumeToRestrictChangeTo = newVolume
			audioAttributes.streamVolume = newVolume
		}

		// Wait for a period of time before increasing the volume again.
		// This will get called even if the volume does not need to change, in
		// case the user tries to lower then volume after the alarm volume
		// level has been reached
		graduallyIncreaseVolumeHandler.postDelayed({ graduallyIncreaseVolume() },
			alarm.graduallyIncreaseVolumeWaitTime * 1000L)
	}

	/**
	 * Restrict the volume.
	 */
	private fun restrictVolume()
	{
		// Check if the volume is below the restrict volume.
		// If the volume will be gradually increasing, check that the process
		// has already started
		if ((audioAttributes.streamVolume < volumeToRestrictChangeTo)
			&& (!alarm.shouldGraduallyIncreaseVolume || hasGraduallyIncreaseVolumeStarted))
		{
			// Change the volume
			audioAttributes.streamVolume = volumeToRestrictChangeTo

			// Call the volume key press listener
			onVolumeKeyPressListener?.onVolumeKeyPress(alarm)
		}

		// Run the handler
		restrictVolumeHandler.postDelayed({ restrictVolume() },
			PERIOD_RESTRICT_VOLUME)
	}

	/**
	 * Setup the volume manager.
	 */
	fun setup(alarm: NacAlarm)
	{
		// Using text-to-speech or playing music. The reason being that if these are
		// not being used, then there is no point in changing the volume
		if (alarm.shouldUseTts || alarm.mediaPath.isNotEmpty())
		{
			// Save the current volume level so it can be reverted later
			audioAttributes.saveCurrentVolume()

			// Set the volume to the alarm volume and save the volume level so
			// that it can be correctly reverted back once the wakeup process
			// is complete
			audioAttributes.setStreamVolume()

			// Check if should gradually increase the volume
			if (alarm.shouldGraduallyIncreaseVolume)
			{
				setupGraduallyIncreaseVolume()
			}

			// Check if should restrict the volume
			if (alarm.shouldRestrictVolume)
			{
				setupRestrictVolume()
			}
		}

		// Set the initial volume
		initialVolume = audioAttributes.streamVolume

		// Watch for volume key press
		if (alarm.shouldVolumeDismiss || alarm.shouldVolumeSnooze)
		{
			volumeKeyPressWatchdog()
		}
	}

	/**
	 * Setup gradually increasing the volume.
	 */
	private fun setupGraduallyIncreaseVolume()
	{
		// Set the volume to 0 to start with
		volumeToRestrictChangeTo = 0
		audioAttributes.streamVolume = 0

		// Run handler at a cadence in order to gradually increase the volume
		graduallyIncreaseVolumeHandler.postDelayed({

			// Gradually increase volume
			graduallyIncreaseVolume()

			// Set flag indicating that the gradual increase process has
			// started
			hasGraduallyIncreaseVolumeStarted = true

		}, alarm.graduallyIncreaseVolumeWaitTime * 1000L)
	}

	/**
	 * Setup the restrict volume.
	 */
	private fun setupRestrictVolume()
	{
		// Set the volume to restrict to, if any changes occur
		volumeToRestrictChangeTo = audioAttributes.streamVolume

		// Run handler at a cadence in order to restrict the volume. Volume
		// change events cannot be caught, so need to run this every X
		// milliseconds to enforce it
		restrictVolumeHandler.postDelayed({ restrictVolume() }, PERIOD_RESTRICT_VOLUME)
	}

	/**
	 * Setup whether pressing the volume buttons should snooze the alarm. This runs a
	 * handler every second to see if the volume level has changed.
	 */
	private fun volumeKeyPressWatchdog()
	{
		volumeKeyPressHandler.postDelayed({

			// Get the current volume
			val currentVolume = audioAttributes.streamVolume

			// Volume was changed
			if (initialVolume != currentVolume)
			{
				// Call the volume key press listener
				onVolumeKeyPressListener?.onVolumeKeyPress(alarm)

				// Change the initial volume if alarm does NOT restrict or gradually increase volume
				if (!alarm.shouldRestrictVolume && !alarm.shouldGraduallyIncreaseVolume)
				{
					initialVolume = currentVolume
				}
			}

			// Keep the watchdog running just in case unable to snooze/dismiss
			volumeKeyPressWatchdog()

		}, PERIOD_VOLUME_KEY_PRESS)
	}

	companion object
	{

		/**
		 * Period at which to ensure the volume is restricted.
		 */
		private const val PERIOD_RESTRICT_VOLUME = 1000L

		/**
		 * Period at which to check for volume key press by looking for volume changes.
		 */
		private const val PERIOD_VOLUME_KEY_PRESS = 1000L

	}

}