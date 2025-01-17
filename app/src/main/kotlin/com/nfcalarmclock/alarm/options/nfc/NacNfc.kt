package com.nfcalarmclock.alarm.options.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.NfcAdapter.FLAG_READER_NFC_A
import android.nfc.NfcAdapter.FLAG_READER_NFC_B
import android.nfc.NfcAdapter.FLAG_READER_NFC_BARCODE
import android.nfc.NfcAdapter.FLAG_READER_NFC_F
import android.nfc.NfcAdapter.FLAG_READER_NFC_V
import android.nfc.NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK
import android.nfc.Tag
import android.os.Build
import android.os.Parcelable
import android.provider.Settings
import com.nfcalarmclock.R
import com.nfcalarmclock.util.NacUtility.quickToast
import com.nfcalarmclock.util.NacUtility.toast

/**
 * NFC helper object.
 */
object NacNfc
{

	/**
	 * Disable NFC reader mode.
	 */
	fun disableReaderMode(activity: Activity)
	{
		// Get the NFC adapter
		val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

		// Disable NFC reader mode
		nfcAdapter.disableReaderMode(activity)
	}

	/**
	 * Enable NFC reader mode.
	 */
	fun enableReaderMode(activity: Activity, callback:NfcAdapter.ReaderCallback)
	{
		// Get the NFC adapter
		val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

		// Get all the NFC tags that can be read
		val flags = FLAG_READER_NFC_A or
			FLAG_READER_NFC_B or
			FLAG_READER_NFC_BARCODE or
			FLAG_READER_NFC_F or
			FLAG_READER_NFC_V or
			FLAG_READER_SKIP_NDEF_CHECK

		// Enable NFC reader mode
		nfcAdapter.enableReaderMode(activity, callback, flags, null)
	}

	/**
	 * Check if NFC exists on this device.
	 */
	fun exists(context: Context): Boolean
	{
		return NfcAdapter.getDefaultAdapter(context) != null
	}

	/**
	 * Get an NFC tag from the given Intent.
	 *
	 * @return An NFC tag from the given Intent.
	 */
	@Suppress("deprecation")
	private fun getTag(intent: Intent?): Tag?
	{
		// Use the updated form of Intent.getParecelableExtra() if API >= 33
		return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU)
			{
				// Return the tag from the intent or null if the intent is null
				intent?.getParcelableExtra(NfcAdapter.EXTRA_TAG,
					Tag::class.java)
			}
			else
			{
				if (intent != null)
				{
					intent.getParcelableExtra<Parcelable>(NfcAdapter.EXTRA_TAG) as Tag?
				}
				else
				{
					null
				}
			}
	}

	/**
	 * Check if the NFC adapter is enabled.
	 *
	 * @return True if the NFC adapter is enabled, and False otherwise.
	 */
	fun isEnabled(context: Context?): Boolean
	{
		val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

		return (nfcAdapter != null) && nfcAdapter.isEnabled
	}

	/**
	 * Parse NFC tag ID to a readable format.
	 */
	fun parseId(nfcTag: Tag?): String?
	{
		// NFC tag is not defined
		if (nfcTag == null)
		{
			return null
		}

		// Check the ID of the NFC tag
		val srcId = nfcTag.id ?: return ""

		// Unable to find an ID on the NFC tag
		val id = StringBuilder()
		val buffer = CharArray(2)

		// Compile the NFC tag ID
		for (b in srcId)
		{
			buffer[0] = Character.forDigit(b.toInt() ushr 4 and 0x0F, 16)
			buffer[1] = Character.forDigit(b.toInt() and 0x0F, 16)
			id.append(buffer)
		}

		return id.toString()
	}

	/**
	 * @see .parseId
	 */
	fun parseId(intent: Intent?): String?
	{
		// Get the NFC tag from the intent
		val nfcTag = getTag(intent)

		return if (nfcTag != null)
			{
				// Get the ID from the NFC tag
				parseId(nfcTag)
			}
			else
			{
				null
			}
	}

	/**
	 * Prompt the user to enable NFC.
	 */
	fun prompt(context: Context)
	{
		// NFC adapter does not exist
		if (!exists(context))
		{
			return
		}

		val settings = Intent(Settings.ACTION_NFC_SETTINGS)
		val message = context.getString(R.string.message_nfc_request)

		// Prompt the user to enable NFC
		toast(context, message)
		context.startActivity(settings)
	}

	/**
	 * @see .start
	 */
	fun start(activity: Activity)
	{
		// Create an intent for an activity
		val intent = Intent(activity, activity.javaClass)
			.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)

		// Start the activity with the intent
		start(activity, intent)
	}

	/**
	 * Enable NFC dispatch, so that the app can discover NFC tags.
	 */
	fun start(activity: Activity, intent: Intent?)
	{
		val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

		// NFC adapter is not present or not enabled
		if ((nfcAdapter == null) || !isEnabled(activity))
		{
			return
		}

		// Determine the pending intent flags
		var flags = 0

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S)
		{
			flags = flags or PendingIntent.FLAG_MUTABLE
		}

		// Create the pending intent
		val pending = PendingIntent.getActivity(activity, 0, intent, flags)

		// Enable NFC foreground dispatch
		try
		{
			nfcAdapter.enableForegroundDispatch(activity, pending, null, null)
		}
		catch (e: SecurityException)
		{
			quickToast(activity, R.string.error_message_unable_to_scan_nfc)
		}
		catch (_: IllegalStateException)
		{
		}
	}

	/**
	 * Stop NFC dispatch, so the app does not waste battery when it does not
	 * need to discover NFC tags.
	 */
	fun stop(context: Context)
	{
		val nfcAdapter = NfcAdapter.getDefaultAdapter(context)

		// NFC adapter is not present or not enabled
		if ((nfcAdapter == null) || !isEnabled(context))
		{
			return
		}

		// Disable NFC foreground dispatch
		try
		{
			nfcAdapter.disableForegroundDispatch(context as Activity)
		}
		catch (_: IllegalStateException)
		{
		}
	}

	/**
	 * Check if an NFC tag was scanned/discovered and False otherwise.
	 *
	 * @return True if an NFC tag was scanned/discovered and False otherwise.
	 */
	fun wasScanned(intent: Intent?): Boolean
	{
		// Check if intent is null
		if (intent == null)
		{
			return false
		}

		// Check the intent's action
		return if (intent.action.isNullOrEmpty())
			{
				false
			}
			else
			{
				(intent.action == NfcAdapter.ACTION_NDEF_DISCOVERED)
					|| (intent.action == NfcAdapter.ACTION_TECH_DISCOVERED)
					|| (intent.action == NfcAdapter.ACTION_TAG_DISCOVERED)
			}
	}

}