package com.nfcalarmclock;

import android.view.View;
//import android.widget.CompoundButton;
import android.widget.RelativeLayout;
//import android.widget.CheckBox;

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
	private RelativeLayout mVibrate;
	//private CheckBox mVibrate;

	/**
	 */
	public NacCardVibrate(View root)
	{
		this.mVibrate = (RelativeLayout) root.findViewById(R.id.nac_vibrate);
		//this.mVibrate = (CheckBox) root.findViewById(R.id.nac_vibrate);
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
	private RelativeLayout getVibrate()
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
		RelativeLayout view = this.getVibrate();

		view.setAlpha(vibrate ? 1.0f : 0.3f);
		//view.setChecked(alarm.getVibrate());
	}

	/**
	 * Set the on checked change listener.
	 */
	//public void setOnCheckedChangeListener(
	//	CompoundButton.OnCheckedChangeListener listener)
	public void setOnClickListener(View.OnClickListener listener)
	{
		this.mVibrate.setOnClickListener((View.OnClickListener)listener);
		//this.mVibrate.setOnCheckedChangeListener(
		//	(CompoundButton.OnCheckedChangeListener)listener);
	}

}
