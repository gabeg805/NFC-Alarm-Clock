package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.CheckBox;

/**
 * @brief Checkbox to indicate whether the phone should vibrate when the alarm
 *        is activated.
 */
public class NacCardVibrate
    implements CompoundButton.OnCheckedChangeListener
{

	/**
	 * @brief Alarm.
	 */
	private Alarm mAlarm;
    /**
     * @brief Vibrate checkbox.
     */
     private CheckBox mVibrate;

    /**
     * @brief Constructor.
     */
    public NacCardVibrate(View r)
    {
        this.mVibrate = (CheckBox) r.findViewById(R.id.nacVibrate);
        this.mVibrate.setOnCheckedChangeListener(this);
    }

    /**
     * @brief Initialize the vibrate checkbox.
     */
    public void init(Alarm alarm)
    {
		this.mAlarm = alarm;
        this.mVibrate.setChecked(this.mAlarm.getVibrate());
    }

    /**
     * @brief Save the vibrate state of the alarm.
     */
    @Override
    public void onCheckedChanged(CompoundButton v, boolean state)
    {
        mAlarm.setVibrate(state);
        NacUtility.printf("Vibrate : %b", state);
    }

}
