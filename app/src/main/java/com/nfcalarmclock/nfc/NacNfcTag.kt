package com.nfcalarmclock.nfc

import android.content.Context
import android.content.Intent
import android.nfc.Tag
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.shared.NacSharedConstants
import com.nfcalarmclock.util.NacUtility.quickToast

/**
 */
//@SuppressWarnings("BooleanMethodIsAlwaysInverted")
class NacNfcTag @JvmOverloads constructor(alarm: NacAlarm?, nfcIntent: Intent? = null)
{

	/**
	 * The active alarm to compare with a scanned NFC tag.
	 */
	var activeAlarm: NacAlarm? = null

	/**
	 * NFC tag ID to compare with the active alarm.
	 */
	var nfcId: String? = null

	/**
	 * NFC scan action.
	 *
	 * This is acquired from the Intent.
	 */
	var nfcAction: String? = null

	/**
	 * NFC tag object.
	 */
	var nfcTag: Tag? = null

	/**
	 * Check if the NFC tag is ready and has all attributes set.
	 */
	val isReady: Boolean
		get()
		{
			return (activeAlarm != null) && (nfcId != null) && (nfcAction != null)
		}

	/**
	 * Constructor.
	 */
	init
	{
		// Set alarm
		activeAlarm = alarm

		// Set member variables
		setNfcId(nfcIntent)
		setNfcAction(nfcIntent)
		setNfcTag(nfcIntent)
	}

	/**
	 * Constructor.
	 */
	constructor(nfcIntent: Intent?) : this(null, nfcIntent)

	/**
	 * Check that the NFC tag scanned matches the ID of the one required by the
	 * alarm.
	 *
	 * @param  context  A context.
	 *
	 * @return True if the NFC tag scanned matches the one required by the alarm,
	 *         and False otherwise.
	 */
	fun check(context: Context): Boolean
	{
		// NFC tag is not ready
		if (!isReady)
		{
			return false
		}

		// Compare IDs
		return if (compareIds())
			{
				true
			}
			// IDs do not match
			else
			{
				val cons = NacSharedConstants(context)

				// Show toast
				quickToast(context, cons.errorMessageNfcMismatch)
				false
			}
	}

	/**
	 * Check the saved NFC tag ID in the alarm against the one in the intent.
	 * Return True if the alarm does not have a saved ID, or if the IDs match,
	 * and False otherwise.
	 */
	fun compareIds(): Boolean
	{
		// NFC tag is not ready
		if (!isReady)
		{
			return false
		}

		// Get the alarm's NFC tag ID
		val alarmNfcId = activeAlarm!!.nfcTagId

		return alarmNfcId.isEmpty() || alarmNfcId == nfcId
	}

	/**
	 * @see .setNfcAction
	 */
	fun setNfcAction(nfcIntent: Intent?)
	{
		nfcAction = nfcIntent?.action
	}

	/**
	 * @see .setNfcId
	 */
	fun setNfcId(nfcIntent: Intent?)
	{
		nfcId = NacNfc.parseId(nfcIntent)
	}

	/**
	 * @see .setNfcTag
	 */
	fun setNfcTag(nfcIntent: Intent?)
	{
		nfcTag = NacNfc.getTag(nfcIntent)
	}

}