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
     * @brief Context.
     */
     private Context mContext;

    /**
     * @brief Alarm card.
     */
     private AlarmCard mCard;

    /**
     * @brief Switch.
     */
     private Switch mSwitch;

    /**
     * @brief Constructor.
     */
    public NacCardSwitch(AlarmCard card, Context context)
    {
        this.mContext = context;
        this.mCard = card;
        View root = card.getRoot();
        this.mSwitch = (Switch) root.findViewById(R.id.nacSwitch);
        this.mSwitch.setOnCheckedChangeListener(this);
    }

    /**
     * @brief Initialize the Switch.
     */
    public void init()
    {
        this.mSwitch.setChecked(mCard.getAlarm().getEnabled());
    }

    /**
     * @brief Set the on/off state of the alarm.
     */
    @Override
    public void onCheckedChanged(CompoundButton v, boolean state)
    {
        Alarm alarm = mCard.getAlarm();
        alarm.setEnabled(state);
        NacUtility.printf("Switch : %b", state);
    }

}
