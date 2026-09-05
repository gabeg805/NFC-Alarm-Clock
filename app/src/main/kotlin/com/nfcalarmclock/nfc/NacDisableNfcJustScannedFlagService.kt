package com.nfcalarmclock.nfc

import android.content.Context
import android.content.Intent
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.system.NacLifecycleService
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

/**
 * Disable the NacSharedPreferences.wasNfcJustScannedToDismiss flag so that NFC cannot be
 * continuously triggered when a tag is being scanned.
 */
class NacDisableNfcJustScannedFlagService
	: NacLifecycleService()
{

	/**
	 * Service is started.
	 */
	@UnstableApi
	override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int
	{
		// Super
		super.onStartCommand(intent, flags, startId)

		lifecycleScope.launch {

			NacLog.i("Preparing to disable NFC just scanned flag")
			delay(1500)
			NacLog.i("Disabling NFC just scanned flag")

			// Get the shared preferences
			val sharedPreferences = NacSharedPreferences(this@NacDisableNfcJustScannedFlagService)

			// Disable the flag
			sharedPreferences.wasNfcJustScannedToDismiss = false

			// Stop the service
			super.stopSelf()

		}

		return START_NOT_STICKY
	}

	companion object
	{

		/**
		 * Start the disable NFC just scanned flag service.
		 */
		fun startService(context: Context)
		{
			// Create the intent
			val intent = Intent(Intent.ACTION_DEFAULT, null, context, NacDisableNfcJustScannedFlagService::class.java)

			// Start the service
			context.startService(intent)
		}

	}

}