package com.nfcalarmclock;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.nfc.NfcAdapter;
import android.provider.Settings;

/**
 */
public class NacNfc
{

	/**
	 * The context.
	 */
	private Context mContext;

	/**
	 */
	public NacNfc(Context context)
	{
		this.mContext = context;
	}

	/**
	 * @see exists
	 */
	public boolean exists()
	{
		Context context = this.getContext();
		return NacNfc.exists(context);
	}

	/**
	 * Check if NFC exists on this device.
	 */
	public static boolean exists(Context context)
	{
		return (NfcAdapter.getDefaultAdapter(context) != null);
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return True if the NFC adapter is enabled, and False otherwise.
	 */
	public static boolean isEnabled(Context context)
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
		return (nfcAdapter != null) && nfcAdapter.isEnabled();
	}

	/**
	 * Prompt the user to enable NFC.
	 */
	public static void prompt(Context context)
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);

		if (nfcAdapter == null)
		{
			return;
		}

		NacSharedConstants cons = new NacSharedConstants(context);
		Intent settings = new Intent(Settings.ACTION_NFC_SETTINGS);

		NacUtility.toast(context, cons.getMessageNfcRequest());
		context.startActivity(settings);
	}

	/**
	 * @see start
	 */
	public void start()
	{
		Context context = this.getContext();
		NacNfc.start(context);
	}

	/**
	 * Enable NFC dispatch, so that the app can discover NFC tags.
	 */
	public static void start(Context context)
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);

		if ((nfcAdapter == null) || !NacNfc.isEnabled(context))
		{
			return;
		}

		Intent intent = new Intent(context, NacAlarmActivity.class)
			.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
		PendingIntent pending = PendingIntent.getActivity(context, 0,
			intent, 0);

		nfcAdapter.enableForegroundDispatch((Activity)context, pending, null, null);
	}

	/**
	 * @see stop
	 */
	public void stop()
	{
		Context context = this.getContext();
		NacNfc.stop(context);
	}

	/**
	 * Stop NFC dispatch, so the app does not waste battery when it does not
	 * need to discover NFC tags.
	 */
	public static void stop(Context context)
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);

		if ((nfcAdapter == null) || !NacNfc.isEnabled(context))
		{
			return;
		}

		nfcAdapter.disableForegroundDispatch((Activity)context);
	}

	/**
	 * @return True if an NFC tag was scanned/discovered and False otherwise.
	 */
	public static boolean wasScanned(Context context, Intent intent)
	{
		if (intent == null)
		{
			return false;
		}

		String action = intent.getAction();

		if ((action == null) || action.isEmpty())
		{
			return false;
		}

		return action.equals(NfcAdapter.ACTION_NDEF_DISCOVERED)
			|| action.equals(NfcAdapter.ACTION_TECH_DISCOVERED)
			|| action.equals(NfcAdapter.ACTION_TAG_DISCOVERED);
	}

}
