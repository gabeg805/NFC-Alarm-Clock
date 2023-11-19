package com.nfcalarmclock.ratemyapp

import androidx.appcompat.app.AppCompatActivity
import com.nfcalarmclock.shared.NacSharedPreferences

/**
 * Handle when to prompt the user to rate my app.
 *
 * The FOSS version should never actually request to rate the app. This is just
 * here to satisfy the condititon of being able to check if should request, as
 * well as request, but the methods themselves do nothing.
 */
object NacRateMyApp
{

	/**
	 * The FOSS version should never request to rate the app.
	 *
	 * @return False because the FOSS version should never request to rate the
	 *         app.
	 */
	fun shouldRequest(shared: NacSharedPreferences): Boolean
	{
		return false
	}

	/**
	 * Request to rate my app.
	 *
	 * This does nothing on the FOSS version.
	 */
	fun request(activity:AppCompatActivity, shared: NacSharedPreferences)
	{
	}

}