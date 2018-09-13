package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 * @brief Switch button that indicates whether the alarm is enabled or not.
 */
public class NacCardSwitch
	implements CompoundButton.OnCheckedChangeListener
{

	/**
	 * @brief Alarm.
	 */
	 private Alarm mAlarm = null;

	/**
	 * @brief Switch.
	 */
	 private Switch mSwitch = null;

	/**
	 * @brief Constructor.
	 */
	public NacCardSwitch(View r)
	{
		super();

		this.mSwitch = (Switch) r.findViewById(R.id.nacSwitch);
	}

	/**
	 * @brief Initialize the Switch.
	 */
	public void init(Alarm alarm)
	{
		this.mAlarm = alarm;
		boolean state = this.mAlarm.getEnabled();

		this.mSwitch.setChecked(state);
		this.mSwitch.setOnCheckedChangeListener(this);
	}

	/**
	 * @brief Return the height of the view that is visible.
	 */
	public int getHeight()
	{
		return NacUtility.getHeight(this.mSwitch);
	}

	/**
	 * @brief Set the on/off state of the alarm.
	 */
	@Override
	public void onCheckedChanged(CompoundButton v, boolean state)
	{
		NacUtility.printf("Switch : %b", state);
		this.mAlarm.setEnabled(state);
		this.mAlarm.changed();
	}

}
