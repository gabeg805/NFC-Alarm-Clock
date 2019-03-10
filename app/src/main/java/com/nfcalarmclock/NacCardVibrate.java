package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CheckBox;

/**
 * Checkbox to indicate whether the phone should vibrate when the alarm is
 * activated.
 */
public class NacCardVibrate
	implements CompoundButton.OnCheckedChangeListener
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
	 * @param  r  The root view.
	 */
	public NacCardVibrate(View r)
	{
		super();

		this.mAlarm = null;
		this.mVibrate = (CheckBox) r.findViewById(R.id.nacVibrate);
	}

	/**
	 * Initialize the vibrate checkbox.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;
		boolean state = this.mAlarm.getVibrate();

		this.mVibrate.setChecked(state);
		this.mVibrate.setOnCheckedChangeListener(this);
	}

	/**
	 * Save the vibrate state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		this.mAlarm.setVibrate(state);
		this.mAlarm.changed();
	}

}
