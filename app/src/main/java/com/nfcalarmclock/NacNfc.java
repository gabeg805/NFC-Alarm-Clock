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
	 * @see disable
	 */
	public void disable()
	{
		Context context = this.getContext();

		NacNfc.disable(context);
	}

	/**
	 * Disable NFC dispatch, so the app does not waste battery when it does not
	 * need to discover NFC tags.
	 */
	public static void disable(Context context)
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);

		if (nfcAdapter == null)
		{
			return;
		}

		nfcAdapter.disableForegroundDispatch((Activity)context);
	}

	/**
	 * @see enable
	 */
	public void enable()
	{
		Context context = this.getContext();

		NacNfc.enable(context);
	}

	/**
	 * Enable NFC dispatch, so that the app can discover NFC tags.
	 */
	public static void enable(Context context)
	{
		NfcAdapter nfcAdapter = NfcAdapter.getDefaultAdapter(context);
		NacSharedPreferences shared = new NacSharedPreferences(context);

		if (!shared.getRequireNfc())
		{
			return;
		}
		else if (nfcAdapter == null)
		{
			NacUtility.quickToast(context, "Your device doesn't support NFC");
			return;
		}
		else
		{
			if (!nfcAdapter.isEnabled())
			{
				NacUtility.toast(context, "Please enable NFC to dismiss the alarm");
				((Activity)context).startActivity(
					new Intent(Settings.ACTION_NFC_SETTINGS));
			}

			Intent intent = new Intent(context, NacAlarmActivity.class)
				.addFlags(Intent.FLAG_RECEIVER_REPLACE_PENDING);
			PendingIntent pending = PendingIntent.getActivity(context, 0,
				intent, 0);
			IntentFilter[] filter = new IntentFilter[]{};

			nfcAdapter.enableForegroundDispatch((Activity)context, pending,
				filter, null);
		}
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

}
