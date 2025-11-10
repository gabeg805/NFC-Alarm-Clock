package com.nfcalarmclock.nfc

import android.app.Activity
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.nfc.NfcAdapter
import android.nfc.Tag
import android.os.Build
import android.os.Parcelable
import android.provider.Settings
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.nfc.db.NacNfcTag
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.timer.db.NacTimer
import com.nfcalarmclock.view.quickToast
import com.nfcalarmclock.view.toast

/**
 * Bundle name for ID of an NFC tag that was scanned.
 */
const val SCANNED_NFC_TAG_ID_BUNDLE_NAME = "NacNfcScannedNfcTagIdBundle"

/**
 * Bundle name an NFC tag that was scanned that already exists.
 */
const val SCANNED_NFC_TAG_ALREADY_EXISTS_BUNDLE_NAME = "NacNfcScannedNfcTagAlreadyExistsBundle"

/**
 * Check if the alarm can be dismissed with the scanned NFC tag.
 *
 * @return True if the alarm can be dismissed with the scanned NFC tag, and False
 *         otherwise.
 */
fun NacAlarm.canDismissWithScannedNfc(nfcId: String, validNfcTagsForDismissal: MutableList<NacNfcTag>): Boolean?
{
	// Get all NFC IDs from the NFC tags. This is used when the alarm has multiple
	// NFC tags set and need to check that list, instead of doing a string to string
	// comparison
	validNfcTagsForDismissal.forEach { println("Using jank nfc tag? ${it.name} | ${it.nfcId}") }
	val validNfcIdsForDismissal = validNfcTagsForDismissal.map { it.nfcId }

	// Compare the two NFC IDs
	//   if the alarm NFC ID is empty, this is good, or
	//   if the two NFC IDs are equal, this is also good
	//   if the NFC IDs match a particular dismiss order
	return if (this.nfcTagId.isEmpty()
		|| (this.nfcTagId == nfcId)
		|| (!this.shouldUseNfcTagDismissOrder && validNfcIdsForDismissal.contains(nfcId))
		|| (this.shouldUseNfcTagDismissOrder && (this.nfcTagIdList.size > 1)
				&& ((this.nfcTagDismissOrder == NacNfcTagDismissOrder.SEQUENTIAL)
					|| (this.nfcTagDismissOrder == NacNfcTagDismissOrder.RANDOM))
				&& validNfcIdsForDismissal.first() == nfcId))
	{
		// NFC tags need to be dismissed in a particular order
		if (this.shouldUseNfcTagDismissOrder
			&& ((this.nfcTagDismissOrder == NacNfcTagDismissOrder.SEQUENTIAL)
				|| (this.nfcTagDismissOrder == NacNfcTagDismissOrder.RANDOM)))
		{
			// Remove the first NFC tag since it matched the one that was scanned
			validNfcTagsForDismissal.removeAt(0)

			// Can dismiss the alarm when all the NFC tags have been scanned. Otherwise,
			// return null to indicate that
			validNfcTagsForDismissal.isEmpty()
				.takeIf { it }
		}
		// Can dismiss the alarm
		else
		{
			true
		}
	}
	// Something went wrong when comparing the NFC IDs
	else
	{
		false
	}
}

/**
 * Get the list of NFC tags that can be used to dismiss this alarm.
 *
 * @return The list of NFC tags that can be used to dismiss this alarm.
 */
suspend fun NacAlarm.getNfcTagsForDismissing(
	nfcTagViewModel: NacNfcTagViewModel
): MutableList<NacNfcTag>
{
	// Get the NFC tags
	val nfcTags = this.nfcTagIdList.takeIf { this.nfcTagId.isNotEmpty() }
		?.map { nfcTagViewModel.findNfcTag(it) ?: NacNfcTag("", it) }
		?.toMutableList()
		?: mutableListOf()
	nfcTags.forEach { println("Got nfc tag? ${it.name} | ${it.nfcId}") }

	// Random dismiss order so shuffle the list
	if (this.nfcTagDismissOrder == NacNfcTagDismissOrder.RANDOM)
	{
		nfcTags.shuffle()
	}

	return nfcTags
}

/**
 * Get the list of NFC tags that can be used to dismiss this alarm.
 *
 * @return The list of NFC tags that can be used to dismiss this alarm.
 */
fun NacAlarm.getNfcTagNamesForDismissing(nfcTags: MutableList<NacNfcTag>, prefix: String = ""): String?
{
	return if (this.shouldUseNfcTagDismissOrder)
	{
		// Order the NFC tags based on how the user wants them ordered
		return when (this.nfcTagDismissOrder)
		{
			// Sequential. Show the first NFC tag
			NacNfcTagDismissOrder.SEQUENTIAL -> nfcTags.getOrNull(0)?.getTextWithPrefix(prefix)

			// Random. The list should already be randomized. Show the first NFC tag in the
			// randomized list
			NacNfcTagDismissOrder.RANDOM -> nfcTags.getOrNull(0)?.getTextWithPrefix(prefix)

			// Unknown
			else -> null
		}
	}
	// Show all NFC tags, if any are present
	else
	{
		nfcTags.takeIf { nfcTags.isNotEmpty() }
			?.joinToString(" \u2027 ") { it.getTextWithPrefix(prefix) }
	}
}

/**
 * Remove an NFC tag if it is being used.
 *
 * @return True if the NFC tag was removed, and False otherwise.
 */
fun NacAlarm.removeNfcTag(nfcTag: NacNfcTag): Boolean
{
	// NFC ID matches
	if (this.nfcTagId == nfcTag.nfcId)
	{
		// Clear the NFC tag ID
		this.nfcTagId = ""

	}
	// NFC ID is in the list of NFC tags
	else if (this.nfcTagIdList.contains(nfcTag.nfcId))
	{
		// Remove the NFC ID, but keep the rest of the list
		this.nfcTagId = this.nfcTagIdList
			.filter { it != nfcTag.nfcId }
			.joinToString(" || ")
	}
	// No matching NFC tag
	else
	{
		return false
	}

	return true
}

/**
 * Remove an NFC tag if it is being used.
 *
 * @return True if the NFC tag was removed, and False otherwise.
 */
fun NacTimer.removeNfcTag(nfcTag: NacNfcTag): Boolean
{
	// Remove the NFC tag
	val status = (this as NacAlarm).removeNfcTag(nfcTag)

	// Clear the start timer on scan flag, if necessary
	if (nfcTagId.isEmpty())
	{
		this.shouldScanningNfcTagStartTimer = false
	}

	return status
}

/**
 * Set the NFC tag IDs from a list.
 */
fun NacAlarm.setNfcTagIds(nfcTags: List<NacNfcTag>)
{
	this.nfcTagId = nfcTags.joinToString(" || ") { it.nfcId }
}

/**
 * Whether NFC should be used with the alarm.
 *
 * @return True if NFC should be used, and False otherwise.
 */
fun NacAlarm.shouldUseNfc(
	context: Context,
	sharedPreferences: NacSharedPreferences = NacSharedPreferences(context)
): Boolean
{
	return NacNfc.exists(context)
			&& this.shouldUseNfc
			&& sharedPreferences.shouldShowNfcButton
}

/**
 * NFC helper object.
 */
object NacNfc
{

	/**
	 * Check if the alarm can be dismissed with the scanned NFC tag.
	 *
	 * @return True if the alarm can be dismissed with the scanned NFC tag, and False
	 *         otherwise.
	 */
	fun canDismissWithScannedNfc(
		context: Context,
		alarm: NacAlarm?,
		nfcId: String?,
		nfcTags: MutableList<NacNfcTag>?
	): Boolean
	{
		// Get the NFC ID from the intent
		val sharedPreferences = NacSharedPreferences(context)

		// Alarm and NFC ID are present
		if ((alarm != null) && (nfcId != null))
		{
			// NFC can be shown which means it should be checked
			if (sharedPreferences.shouldShowNfcButton)
			{
				nfcTags?.forEach { println("Can dismiss nfc tag? ${it.name} | ${it.nfcId}") }
				// Check if the scanned NFC tag can be used to dismiss the alarm
				val status = alarm.canDismissWithScannedNfc(nfcId, nfcTags!!)

				when (status)
				{
					// Dismiss alarm
					true -> return true

					// Do not dismiss the alarm. Continue down below
					false -> {}

					// NFC tags need to be dismissed in a certain order and there are
					// still more NFC tags that need to be scanned
					else -> return false
				}
			}
			// Do not show NFC button, so do not need to bother checking NFC
			else
			{
				return true
			}
		}

		// Something went wrong when comparing the NFC IDs
		quickToast(context, R.string.error_message_nfc_mismatch)
		return false
	}

	/**
	 * Disable NFC reader mode.
	 */
	fun disableReaderMode(activity: Activity)
	{
		// Get the NFC adapter
		val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

		// Disable NFC reader mode
		nfcAdapter?.disableReaderMode(activity)
	}

	/**
	 * Enable NFC reader mode.
	 */
	fun enableReaderMode(activity: Activity, callback: NfcAdapter.ReaderCallback)
	{
		// Get the NFC adapter
		val nfcAdapter = NfcAdapter.getDefaultAdapter(activity)

		// Get all the NFC tags that can be read
		val flags = NfcAdapter.FLAG_READER_NFC_A or
				NfcAdapter.FLAG_READER_NFC_B or
				NfcAdapter.FLAG_READER_NFC_BARCODE or
				NfcAdapter.FLAG_READER_NFC_F or
				NfcAdapter.FLAG_READER_NFC_V or
				NfcAdapter.FLAG_READER_SKIP_NDEF_CHECK

		// Enable NFC reader mode
		nfcAdapter?.enableReaderMode(activity, callback, flags, null)
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
				intent?.getParcelableExtra(
					NfcAdapter.EXTRA_TAG,
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

		// Prompt the user to enable NFC
		toast(context, R.string.message_nfc_request)
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
		catch (_: SecurityException)
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

/**
 * Status of NFC reader mode. This is really only used as a means to communicate between
 * the Scan NFC Tag dialog and the main activity so that reader mode is always enabled
 * when the main activity is visible.
 */
object NacNfcReaderMode
{

	/**
	 * Status of NFC reader mode, whether it is enabled or not.
	 */
	private val mutableLiveData: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

	/**
	 * The publically available status of NFC reader mode.
	 */
	val liveData: LiveData<Boolean> = mutableLiveData

	/**
	 * Update the status of NFC reader mode.
	 */
	fun update(status: Boolean)
	{
		mutableLiveData.value = status
	}

}