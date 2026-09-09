package com.nfcalarmclock

import android.app.Application
import android.os.Build
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.shared.NacSharedPreferences
import dagger.hilt.android.HiltAndroidApp

/**
 * NFC Alarm Clock application.
 */
@HiltAndroidApp
class NacNfcAlarmClockApplication : Application()
{

	/**
	 * Application is created.
	 */
	override fun onCreate()
	{
		// Super
		super.onCreate()

		// Move the shared preference to device protected storage
		NacSharedPreferences.moveToDeviceProtectedStorage(this)

		// Create shared preferences
		val sharedPreferences = NacSharedPreferences(this)

		// Initialize logger
		NacLog.init(this, sharedPreferences)
		NacLog.i("Starting app v${BuildConfig.VERSION_NAME} (Android API ${Build.VERSION.SDK_INT})")
	}

}