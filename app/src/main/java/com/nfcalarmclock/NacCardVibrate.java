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
     * @brief Context.
     */
     private Context mContext;

    /**
     * @brief Alarm card.
     */
     private AlarmCard mCard;

    /**
     * @brief Vibrate checkbox.
     */
     private CheckBox mVibrate;

    /**
     * @brief Constructor.
     */
    public NacCardVibrate(AlarmCard card, Context context)
    {
        this.mContext = context;
        this.mCard = card;
        View root = card.getRoot();
        this.mVibrate = (CheckBox) root.findViewById(R.id.nacVibrate);
        this.mVibrate.setOnCheckedChangeListener(this);
    }

    /**
     * @brief Initialize the vibrate checkbox.
     */
    public void init()
    {
        this.mVibrate.setChecked(mCard.getAlarm().getVibrate());
    }

    /**
     * @brief Save the vibrate state of the alarm.
     */
    @Override
    public void onCheckedChanged(CompoundButton v, boolean state)
    {
        Alarm alarm = mCard.getAlarm();
        alarm.setVibrate(state);
        NacUtility.printf("Vibrate : %b", state);
    }

}
