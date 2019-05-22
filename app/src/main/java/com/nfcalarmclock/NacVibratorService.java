package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import java.util.concurrent.TimeUnit;

import android.app.IntentService;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

/**
 * Vibrate the phone repeatedly until cancelled.
 */
public class NacVibratorService
	extends IntentService
{

	/**
	 */
	public NacVibratorService()
	{
		super("NacService");
		NacUtility.printf("Vibrator constructor.");
	}

	/**
	 */
	@SuppressWarnings("deprecation")
	@Override
	protected void onHandleIntent(Intent intent)
	{
		NacUtility.printf("Calling Vibrating!");
		Activity activity = (Activity) getApplicationContext();
		Vibrator vibrator = (Vibrator) activity.getSystemService(
			Context.VIBRATOR_SERVICE);
		long duration = intent.getLongExtra("duration", 0);

		if (!vibrator.hasVibrator() || (duration == 0))
		{
			NacUtility.printf("Can't Vibrating!");
			return;
		}

		vibrator.cancel();

		while (true)
		{
			NacUtility.printf("Vibrating!");
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
			{
				vibrator.vibrate(VibrationEffect.createOneShot(duration,
					VibrationEffect.DEFAULT_AMPLITUDE));
			}
			else
			{
				vibrator.vibrate(duration);
			}

			try
			{
				TimeUnit.SECONDS.sleep(1);
			}
			catch (InterruptedException ex)
			{
				break;
			}
		}
	}

}
