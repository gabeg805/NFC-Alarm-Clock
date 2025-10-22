package com.nfcalarmclock.system

import android.app.ForegroundServiceStartNotAllowedException
import android.os.Build
import android.os.PowerManager
import androidx.lifecycle.LifecycleService
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.activealarm.NacActiveAlarmService.Companion.WAKELOCK_TAG
import com.nfcalarmclock.view.toast

@UnstableApi
abstract class NacLifecycleService
	: LifecycleService()
{

	/**
	 * Acquire a wakelock
	 *
	 * @param timeoutSec The timeout of the wakelock in seconds.
	 *
	 * @return The acquired wakelock.
	 */
	protected fun acquireWakeLock(timeoutSec: Int): PowerManager.WakeLock
	{
		// Get the power manager and timeout for the wakelock
		val powerManager = getSystemService(POWER_SERVICE) as PowerManager
		val timeout = timeoutSec * 1000L

		// Acquire the wakelock
		val wakeLock = powerManager.newWakeLock(PowerManager.PARTIAL_WAKE_LOCK,
			WAKELOCK_TAG)
		wakeLock!!.acquire(timeout)

		return wakeLock
	}

	/**
	 * Show the foreground notification.
	 *
	 * Handle the try/except logic so that inheritors of the class do not need to
	 * copy/paste that logic.
	 */
	protected fun showForegroundNotification(unit: () -> Unit = {})
	{
		try
		{
			// Start the service in the foreground
			unit()
		}
		catch (e: Exception)
		{
			// Not allowed to start foreground service
			if ((Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) && (e is ForegroundServiceStartNotAllowedException))
			{
				toast(this, R.string.error_message_unable_to_start_foreground_service)
			}
		}
	}

	/**
	 * Stop the service.
	 */
	@Suppress("deprecation")
	protected fun stopThisService()
	{
		// Stop the foreground service using the updated form of
		// stopForeground() for API >= 33
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
		{
			super.stopForeground(STOP_FOREGROUND_REMOVE)
		}
		else
		{
			super.stopForeground(true)
		}

		// Stop the service
		super.stopSelf()
	}

}