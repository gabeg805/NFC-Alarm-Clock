package com.nfcalarmclock;

import android.view.View;
import android.widget.CompoundButton;
import android.widget.CheckBox;

/**
 * Vibrate checkbox for an alarm card.
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
	private CheckBox mVibrate;

	/**
	 */
	public NacCardVibrate(View root)
	{
		this.mVibrate = (CheckBox) root.findViewById(R.id.nac_vibrate);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
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

		this.mVibrate.setChecked(alarm.getVibrate());
	}

	/**
	 * Set the on checked change listener.
	 */
	public void setOnCheckedChangeListener(
		CompoundButton.OnCheckedChangeListener listener)
	{
		this.mVibrate.setOnCheckedChangeListener(
			(CompoundButton.OnCheckedChangeListener)listener);
	}

}
