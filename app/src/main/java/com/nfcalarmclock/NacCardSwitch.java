package com.nfcalarmclock;

import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.View;
import android.widget.CompoundButton;
import androidx.appcompat.widget.SwitchCompat;

import androidx.core.graphics.ColorUtils;

/**
 * On/off switch on an alarm card.
 */
public class NacCardSwitch
{

	/**
	 * Alarm.
	 */
	private NacAlarm mAlarm;

	/**
	 * On/off switch for an alarm.
	 */
	 private SwitchCompat mSwitch;

	/**
	 */
	public NacCardSwitch(View root)
	{
		this.mAlarm = null;
		this.mSwitch = (SwitchCompat) root.findViewById(R.id.nac_switch);
	}

	/**
	 * @return The alarm.
	 */
	private NacAlarm getAlarm()
	{
		return this.mAlarm;
	}

	/**
	 * Initialize the switch.
	 */
	public void init(NacAlarm alarm)
	{
		this.mAlarm = alarm;
		this.set();
	}

	/**
	 * Set the switch.
	 */
	public void set()
	{
		NacAlarm alarm = this.getAlarm();
		boolean enabled = alarm.getEnabled();

		this.mSwitch.setChecked(enabled);
	}

	/**
	 * Set the switch color.
	 */
	public void setColor(NacSharedPreferences shared)
	{

		int theme = shared.getThemeColor();
		int themeDark = ColorUtils.blendARGB(theme, Color.BLACK, 0.6f);
		int[] thumbColors = new int[] {theme, Color.GRAY};
		int[] trackColors = new int[] {themeDark, Color.DKGRAY};

		int[][] states = new int[][] {
			new int[] { android.R.attr.state_checked},
			new int[] {-android.R.attr.state_checked}};
		ColorStateList thumbStateList = new ColorStateList(states, thumbColors);
		ColorStateList trackStateList = new ColorStateList(states, trackColors);

		this.mSwitch.setThumbTintList(thumbStateList);
		this.mSwitch.setTrackTintList(trackStateList);
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
