package com.nfcalarmclock.settings

import android.annotation.SuppressLint
import android.content.Intent
import android.content.SharedPreferences
import android.os.Bundle
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import com.nfcalarmclock.R
import com.nfcalarmclock.log.NacLog
import com.nfcalarmclock.system.getDeviceProtectedStorageContext
import com.nfcalarmclock.system.sendEmail
import com.nfcalarmclock.view.quickToast

/**
 * Help settings.
 */
class NacHelpSettingFragment
	: NacBaseSettingFragment()
{

	/**
	 * Listener for when the shared preference changes (for debug mode/write to log).
	 */
	private lateinit var onSharedPreferenceChangeListener: SharedPreferences.OnSharedPreferenceChangeListener

	/**
	 * Address.
	 */
	private val address: String
		get()
		{
			val appName = resources.getString(R.string.app_name)
			val dot = "."
			val ext = "com"
			val alphabet = "abcdefghijklmnopqrstuvwxyz"
			val name = "${alphabet[2]}${alphabet[14]}${alphabet[13]}${alphabet[19]}${alphabet[0]}${alphabet[2]}${alphabet[19]}@"

			return "$name${appName.lowercase().replace(" ", "")}$dot$ext"
		}

	/**
	 * Called when creating the preferences.
	 */
	override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?)
	{
		// Get the device protected storage context, if available
		val deviceContext = getDeviceProtectedStorageContext(requireContext())

		// Set device protected storage as the storage location to use
		preferenceManager.setStorageDeviceProtected()

		// Inflate the XML file and add the hierarchy to the current preference
		addPreferencesFromResource(R.xml.help_preferences)

		// Set the default values on this preference that are in the "android:defaultValue"
		// attribute
		PreferenceManager.setDefaultValues(deviceContext, R.xml.help_preferences,  false)

		// Setup the preferences
		setupDebugMode()
		setupShareLogs()
		setupSendEmail()
	}

	/**
	 * Fragment started.
	 */
	override fun onStart()
	{
		// Super
		super.onStart()

		// Register change listener
		sharedPreferences!!.instance.registerOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
	}

	/**
	 * Fragment stopped.
	 */
	override fun onStop()
	{
		// Super
		super.onStop()

		// Unregister change listener
		sharedPreferences!!.instance.unregisterOnSharedPreferenceChangeListener(onSharedPreferenceChangeListener)
	}

	/**
	 * Setup debug mode to write to logs.
	 */
	private fun setupDebugMode()
	{
		// Get the preference key
		val writeToLogkey = getString(R.string.key_app_should_write_to_log)

		// Define the shared preference change listener. It needs to be registered to work though
		onSharedPreferenceChangeListener = SharedPreferences.OnSharedPreferenceChangeListener { _, key ->

			// Wrong key, so do not care about this change
			if (key != writeToLogkey)
			{
				return@OnSharedPreferenceChangeListener
			}

			// Get the context
			val context = requireContext()

			// Re-initialize the logger
			NacLog.close()
			NacLog.init(context, sharedPreferences!!)

		}
	}

	/**
	 * Setup send email.
	 */
	@SuppressLint("UseKtx")
	private fun setupSendEmail()
	{
		// Get the preference
		val key = getString(R.string.key_settings_help_email_suggestion)
		val pref = findPreference<Preference>(key)

		// Set the click listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->

			// Prepare for the email
			val context = requireContext()

			// Send email
			context.sendEmail(
				Intent.ACTION_SEND,
				type = "message/rfc822",
				subject = "NFC Alarm Clock (Email chat)",
				to = address,
				onError = {
					quickToast(context, R.string.error_message_unable_to_email)
				}
			)

			// Return
			true
		}
	}

	/**
	 * Setup share logs.
	 */
	private fun setupShareLogs()
	{
		// Get the preference
		val key = getString(R.string.key_settings_help_email_logs)
		val pref = findPreference<Preference>(key)

		// Set the click listener
		pref!!.onPreferenceClickListener = Preference.OnPreferenceClickListener { _ ->

			// Close log so no lock files are present and all messages are flushed
			NacLog.close()

			// Prepare for the email
			val context = requireContext()
			val logFiles = NacLog.getDirectory(context).listFiles()

			// Send the email
			context.sendEmail(
				Intent.ACTION_SEND_MULTIPLE,
				type = "message/rfc822",
				subject = "NFC Alarm Clock (Debug Logs)",
				to = address,
				attachmentList = logFiles,
				onError = {
					quickToast(context, R.string.error_message_unable_to_email)
				}
			)

			// Re-initialize log
			NacLog.init(context, sharedPreferences!!)

			// Return
			true
		}
	}

}