package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * Switch button that indicates whether the alarm is enabled or not.
 */
public class NacCardSwitch
	implements CompoundButton.OnCheckedChangeListener
{

	/**
	 * Alarm.
	 */
	 private NacAlarm mAlarm;

	/**
	 * On/off switch.
	 */
	 private Switch mSwitch;

	/**
	 */
	public NacCardSwitch(View r)
	{
		super();

		this.mAlarm = null;
		this.mSwitch = (Switch) r.findViewById(R.id.nacSwitch);
	}

	/**
	 * Initialize the Switch.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;
		boolean state = this.mAlarm.getEnabled();

		this.mSwitch.setChecked(state);
		this.mSwitch.setOnCheckedChangeListener(this);
	}

	/**
	 * Return the height of the view that is visible.
	 */
	public int getHeight()
	{
		return NacUtility.getHeight(this.mSwitch);
	}

	/**
	 * Set the on/off state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		NacUtility.printf("Switch : %b", state);
		this.mAlarm.setEnabled(state);
		this.mAlarm.changed();
	}

}
