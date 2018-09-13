package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CheckBox;

/**
 * @brief Checkbox to indicate whether the phone should vibrate when the alarm
 *		  is activated.
 */
public class NacCardVibrate
	implements CompoundButton.OnCheckedChangeListener
{

	/**
	 * @brief Alarm.
	 */
	private Alarm mAlarm = null;

	/**
	 * @brief Vibrate checkbox.
	 */
	private CheckBox mVibrate = null;

	/**
	 * @param  r  The root view.
	 */
	public NacCardVibrate(View r)
	{
		super();

		this.mVibrate = (CheckBox) r.findViewById(R.id.nacVibrate);
	}

	/**
	 * @brief Initialize the vibrate checkbox.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;
		boolean state = this.mAlarm.getVibrate();

		this.mVibrate.setChecked(state);
		this.mVibrate.setOnCheckedChangeListener(this);
	}

	/**
	 * @brief Save the vibrate state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		NacUtility.printf("Vibrate : %b", state);
		this.mAlarm.setVibrate(state);
		this.mAlarm.changed();
	}

}
