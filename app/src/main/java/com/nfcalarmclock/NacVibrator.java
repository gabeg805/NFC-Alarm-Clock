package com.nfcalarmclock;

import android.app.Activity;
import android.content.Context;
import android.os.AsyncTask;
import android.os.Build;
import android.os.VibrationEffect;
import android.os.Vibrator;
import java.util.concurrent.TimeUnit;

/**
 * Vibrate the phone repeatedly until cancelled.
 */
public class NacVibrator
	extends AsyncTask<Long, Void, Integer>
{

	/**
	 * Vibrate the phone.
	 */
	private Vibrator mVibrator;

	/**
	 */
	public NacVibrator(Activity activity)
	{
		this.mVibrator = (Vibrator) activity.getSystemService(Context.VIBRATOR_SERVICE);
	}


	/**
	 */
	protected Integer doInBackground(Long... times)
	{
		if (!this.mVibrator.hasVibrator() || (times.length != 1))
		{
			return -1;
		}

		this.mVibrator.cancel();

		long duration = times[0];

		while (true)
		{
			this.vibrate(duration);

			try
			{
				TimeUnit.SECONDS.sleep(1);
			}
			catch (InterruptedException ex)
			{
				break;
			}
		}

		return 0;
	}

	/**
	 * Vibrate the phone.
	 */
	public void vibrate(long duration)
	{
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O)
		{
			this.mVibrator.vibrate(VibrationEffect.createOneShot(duration,
				VibrationEffect.DEFAULT_AMPLITUDE));
		}
		else
		{
			this.mVibrator.vibrate(duration);
		}
	}

}
