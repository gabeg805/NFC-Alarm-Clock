package com.nfcalarmclock;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Switch;

/**
 */
public class NacCardSwitch
{

	/**
	 * On/off switch for an alarm.
	 */
	 private Switch mSwitch;

	/**
	 */
	public NacCardSwitch(View root)
	{
		this.mSwitch = (Switch) root.findViewById(R.id.nac_switch);
	}

	/**
	 * Initialize the switch.
	 */
	public void init(boolean value)
	{
		this.set(value);
	}

	/**
	 * Set the switch.
	 */
	public void set(boolean value)
	{
		this.mSwitch.setChecked(value);
	}

	/**
	 * Set the switch color.
	 */
	public void setColor(NacSharedPreferences shared)
	{
		int[][] states = new int[][] {
			new int[] { android.R.attr.state_checked},
			new int[] {-android.R.attr.state_checked}};
		int[] thumbColors = new int[] {shared.getThemeColor(), Color.LTGRAY};
		int[] trackColors = new int[] {shared.getThemeColor(), Color.GRAY};
		ColorStateList thumbStateList = new ColorStateList(states, thumbColors);
		ColorStateList trackStateList = new ColorStateList(states, trackColors);

		this.mSwitch.getThumbDrawable().setTintList(thumbStateList);
		this.mSwitch.getTrackDrawable().setTintList(trackStateList);
	}

	/**
	 * Set the on checked change listener.
	 */
	public void setOnCheckedChangeListener(
		CompoundButton.OnCheckedChangeListener listener)
	{
		this.mSwitch.setOnCheckedChangeListener(listener);
	}

}
