package com.nfcalarmclock;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.ColorFilter;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.StateListDrawable;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffColorFilter;
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
	  * List of color filters for the thumb and track on/off states (the array
	  * will be of length 4).
	  */
	 private ColorFilter[] mColorFilters;

	/**
	 */
	public NacCardSwitch(View root)
	{
		super();

		Context context = root.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		this.mAlarm = null;
		this.mSwitch = (Switch) root.findViewById(R.id.nacSwitch);
		this.mColorFilters = new ColorFilter[4];

		this.mColorFilters[0] = this.mSwitch.getThumbDrawable().getColorFilter();
		this.mColorFilters[1] = this.mSwitch.getTrackDrawable().getColorFilter();
		this.mColorFilters[2] = new PorterDuffColorFilter(shared.themeColor, PorterDuff.Mode.SRC_IN);
		this.mColorFilters[3] = new PorterDuffColorFilter(0xFF000000 + shared.themeColor, PorterDuff.Mode.SRC_IN);
	}

	/**
	 * Initialize the Switch.
	 */
	public void init(NacAlarm alarm)
	{
		boolean state = alarm.getEnabled();
		this.mAlarm = alarm;

		this.mSwitch.setChecked(state);
		this.mSwitch.setOnCheckedChangeListener(this);
		this.setSwitchColor(state);
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
		this.setSwitchColor(state);
	}

	/**
	 * Set the switch color based on its state.
	 *
	 * @param  state  True is an enabled switch and False means the switch is
	 *                disabled.
	 */
	public void setSwitchColor(boolean state)
	{
		ColorFilter thumb = this.mColorFilters[0];
		ColorFilter track = this.mColorFilters[1];

		if (state)
		{
			thumb = this.mColorFilters[2];
			track = this.mColorFilters[3];
		}

		this.mSwitch.getThumbDrawable().setColorFilter(thumb);
		this.mSwitch.getTrackDrawable().setColorFilter(track);
	}

}
