package com.nfcalarmclock;

import android.view.View;
import android.widget.ImageView;
//import android.widget.RelativeLayout;

/**
 * Vibrate view for an alarm card.
 */
public class NacCardVibrate
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * Vibrate checkbox.
	 */
	private ImageView mVibrate;
	//private RelativeLayout mVibrate;

	/**
	 */
	public NacCardVibrate(View root)
	{
		this.mVibrate = (ImageView) root.findViewById(R.id.nac_vibrate);
		//this.mVibrate = (RelativeLayout) root.findViewById(R.id.nac_vibrate);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * @return The vibrate view.
	 */
	//private RelativeLayout getVibrateView()
	private ImageView getVibrateView()
	{
		return this.mVibrate;
	}

	/**
	 * Initialize the vibrate checkbox.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;
		this.set();
	}

	/**
	 * Set the sound.
	 */
	public void set()
	{
		NacAlarm alarm = this.getAlarm();
		boolean vibrate = alarm.getVibrate();
		View view = this.getVibrateView();
		//RelativeLayout view = this.getVibrateView();

		if (view != null)
		{
			view.setAlpha(vibrate ? 1.0f : 0.3f);
		}
	}

	/**
	 * Set the on checked change listener.
	 */
	public void setOnClickListener(View.OnClickListener listener)
	{
		View view = this.getVibrateView();
		if (view != null)
		{
			view.setOnClickListener((View.OnClickListener)listener);
		}
	}

}
