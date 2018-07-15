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
     private Alarm mAlarm;

    /**
     * @brief Switch.
     */
     private Switch mSwitch;

    /**
     * @brief Constructor.
     */
    public NacCardSwitch(View r)
    {
        this.mSwitch = (Switch) r.findViewById(R.id.nacSwitch);
        this.mSwitch.setOnCheckedChangeListener(this);
    }

    /**
     * @brief Initialize the Switch.
     */
    public void init(Alarm alarm)
    {
		this.mAlarm = alarm;
        this.mSwitch.setChecked(this.mAlarm.getEnabled());
    }

    /**
     * @brief Set the on/off state of the alarm.
     */
    @Override
    public void onCheckedChanged(CompoundButton v, boolean state)
    {
        mAlarm.setEnabled(state);
    }

}
