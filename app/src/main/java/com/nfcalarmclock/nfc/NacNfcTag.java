package com.nfcalarmclock.nfc;

import android.content.Context;
import android.content.Intent;
import android.nfc.Tag;
import com.nfcalarmclock.alarm.db.NacAlarm;
import com.nfcalarmclock.shared.NacSharedConstants;
import com.nfcalarmclock.util.NacUtility;

/**
 */
//@SuppressWarnings("BooleanMethodIsAlwaysInverted")
public class NacNfcTag
{

	/**
	 * The active alarm to compare with a scanned NFC tag.
	 */
	private NacAlarm mActiveAlarm;

	/**
	 * NFC tag ID to compare with the active alarm.
	 */
	private String mNfcId;

	/**
	 * NFC scan action.
	 * <p>
	 * This is acquired from the Intent.
	 */
	private String mNfcAction;

	/**
	 * NFC tag object.
	 */
	private Tag mNfcTag;

	/**
	 */
	public NacNfcTag(NacAlarm activeAlarm, Intent nfcIntent)
	{
		this.setActiveAlarm(activeAlarm);
		this.setNfcId(nfcIntent);
		this.setNfcAction(nfcIntent);
		this.setNfcTag(nfcIntent);
	}

	/**
	 */
	public NacNfcTag(NacAlarm activeAlarm)
	{
		this(activeAlarm, null);
	}

	/**
	 */
	public NacNfcTag(Intent nfcIntent)
	{
		this(null, nfcIntent);
	}

	/**
	 * Check that the NFC tag scanned matches the ID of the one required by the
	 * alarm.
	 *
	 * @param  context  A context.
	 *
	 * @return True if the NFC tag scanned matches the one required by the alarm,
	 *     and False otherwise.
	 */
	public boolean check(Context context)
	{
		if (!this.isReady())
		{
			NacAlarm alarm = this.getActiveAlarm();
			String id = this.getNfcId();
			String action = this.getNfcAction();

			return false;
		}

		if (this.compareIds())
		{
			return true;
		}
		else
		{
			NacSharedConstants cons = new NacSharedConstants(context);
			NacUtility.quickToast(context, cons.getErrorMessageNfcMismatch());
			return false;
		}
	}

	/**
	 * Check the saved NFC tag ID in the alarm against the one in the intent.
	 * Return True if the alarm does not have a saved ID, or if the IDs match,
	 * and False otherwise.
	 */
	public boolean compareIds()
	{
		if (!this.isReady())
		{
			return false;
		}

		NacAlarm alarm = this.getActiveAlarm();
		String alarmId = alarm.getNfcTagId();
		String nfcId = this.getNfcId();

		return alarmId.isEmpty() || alarmId.equals(nfcId);
	}

	/**
	 * Get the active alarm to compare with a scanned NFC tag.
	 *
	 * @return The active alarm.
	 */
	public NacAlarm getActiveAlarm()
	{
		return this.mActiveAlarm;
	}

	/**
	 * Get the NFC scan action.
	 * <p>
	 * This is acquired from the Intent.
	 *
	 * @return The NFC scan action.
	 */
	public String getNfcAction()
	{
		return this.mNfcAction;
	}

	/**
	 * Get the NFC tag ID to compare with the active alarm.
	 *
	 * @return The NFC tag ID.
	 */
	public String getNfcId()
	{
		return this.mNfcId;
	}

	/**
	 * Get the NFC tag object.
	 *
	 * @return The NFC tag object.
	 */
	public Tag getNfcTag()
	{
		return this.mNfcTag;
	}

	/**
	 * Check if the NFC tag is ready and has all attributes set.
	 *
	 * @return True if the NFC tag is ready and all attributes have been set, and
	 *     False otherwise.
	 */
	public boolean isReady()
	{
		NacAlarm alarm = this.getActiveAlarm();
		String id = this.getNfcId();
		String action = this.getNfcAction();

		return (alarm != null) && (id != null) && (action != null);
	}

	/**
	 * Set the active alarm to compare with a scanned NFC tag.
	 *
	 * @param  activeAlarm  The active alarm.
	 */
	public void setActiveAlarm(NacAlarm activeAlarm)
	{
		this.mActiveAlarm = activeAlarm;
	}

	/**
	 * Set the NFC scan action.
	 *
	 * @param  action  The NFC scan action.
	 */
	public void setNfcAction(String action)
	{
		this.mNfcAction = action;
	}

	/**
	 * @see #setNfcAction(String)
	 */
	public void setNfcAction(Intent nfcIntent)
	{
		String action = (nfcIntent != null) ? nfcIntent.getAction() : null;

		this.setNfcAction(action);
	}

	/**
	 * Set the NFC tag ID to compare with the active alarm.
	 *
	 * @param  id  The NFC tag ID.
	 */
	public void setNfcId(String id)
	{
		this.mNfcId = id;
	}

	/**
	 * @see #setNfcId(String)
	 */
	public void setNfcId(Intent nfcIntent)
	{
		String id = NacNfc.parseId(nfcIntent);

		this.setNfcId(id);
	}

	/**
	 * Set the NFC tag object.
	 *
	 * @param  tag  The NFC tag object.
	 */
	public void setNfcTag(Tag tag)
	{
		this.mNfcTag = tag;
	}

	/**
	 * @see #setNfcTag(Tag)
	 */
	public void setNfcTag(Intent nfcIntent)
	{
		Tag tag = NacNfc.getTag(nfcIntent);

		this.setNfcTag(tag);
	}

}
