package com.nfcalarmclock.support

import androidx.fragment.app.FragmentActivity
import android.net.Uri

import android.content.Intent




/**
 * Support setting that intentionally has the Google billing flow removed.
 */
class NacSupportSetting(

	/**
	 * Fragment activity.
	 */
	private val fragmentActivity: FragmentActivity

)
{

	/**
	 * Listener for when a support event occurs.
	 */
	fun interface OnSupportEventListener
	{
		fun onSupported()
	}

	/**
	 * Listener for when a support events occurs.
	 */
	var onSupportEventListener: OnSupportEventListener? = null

	/**
	 * Start the support flow.
	 */
	fun start()
	{
		// Call the support listener
		onSupportEventListener?.onSupported()

		// Open the browser
		val uri = Uri.parse("https://www.nfcalarmclock.com")
		val intent = Intent(Intent.ACTION_VIEW, uri)

		fragmentActivity.startActivity(intent)
	}

}