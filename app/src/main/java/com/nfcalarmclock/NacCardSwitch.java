package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
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
	public NacCardSwitch(View root)
	{
		super();

		Context context = root.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mAlarm = null;
		this.mSwitch = (Switch) root.findViewById(R.id.nacSwitch);
	}

	/**
	 * Initialize the Switch.
	 */
	public void init(NacAlarm alarm)
	{
		Context context = this.mSwitch.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		int[][] states = new int[][] {
			new int[] { android.R.attr.state_checked},
			new int[] {-android.R.attr.state_checked}};
		int[] thumbColors = new int[] {shared.themeColor, Color.LTGRAY};
		int[] trackColors = new int[] {shared.themeColor, Color.GRAY};
		ColorStateList thumbStateList = new ColorStateList(states, thumbColors);
		ColorStateList trackStateList = new ColorStateList(states, trackColors);
		this.mAlarm = alarm;

		this.mSwitch.setChecked(alarm.getEnabled());
		this.mSwitch.setOnCheckedChangeListener(this);
		this.mSwitch.getThumbDrawable().setTintList(thumbStateList);
		this.mSwitch.getTrackDrawable().setTintList(trackStateList);
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
		this.mAlarm.setEnabled(state);
		this.mAlarm.changed();
	}

}
