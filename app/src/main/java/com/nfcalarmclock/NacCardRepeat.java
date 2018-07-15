package com.nfcalarmclock;

import android.content.Context;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.TextView;

/**
 * @brief Container for all repeat views. Users are able to repeat the alarm on
 *        the requested days.
 * 
 * @details The repeat views are:
 *              * The text displayed by default beneath the time.
 *              * The checkbox indicating whether or not the user wants to
 *                repeat the alarm.
 *              * The alarm day buttons.
 */
public class NacCardRepeat
    implements CompoundButton.OnCheckedChangeListener,View.OnClickListener
{

    /**
     * @brief Alarm.
     */
    private Alarm mAlarm;

    /**
     * @brief Text of days to repeat.
     */
    private TextView mText;

    /**
     * @brief Repeat checkbox.
     */
    private CheckBox mCheckbox;

    /**
     * @brief Buttons to select which days to repeat the alarm on.
     */
    private DayOfWeekButtons mDays;

    /**
     * @brief Constructor.
     */
    public NacCardRepeat(View r)
    {
        this.mText = (TextView) r.findViewById(R.id.nacRepeatText);
        this.mCheckbox = (CheckBox) r.findViewById(R.id.nacRepeatCheckbox);
        this.mDays = (DayOfWeekButtons) r.findViewById(R.id.nacRepeatDays);
        this.mDays.setOnClickListener(this);
        this.mCheckbox.setOnCheckedChangeListener(this);
    }

    /**
     * @brief Initialize the repeat text, checkbox, and day buttons.
     */
    public void init(Alarm alarm)
    {
		this.mAlarm = alarm;
        this.mText.setText(alarm.getDaysString());
        this.mCheckbox.setChecked(alarm.getRepeat());

        if (!alarm.getRepeat())
        {
            return;
        }
        else
        {
            int days = alarm.getDays();
            for (int i=0; i < 7; i++)
            {
                if (((days >> i) & 1) != 0)
                {
                    this.mDays.enableButton(i);
                }
            }
        }
    }

    /**
     * @brief Save the repeat state of the alarm.
     */
    @Override
    public void onCheckedChanged(CompoundButton v, boolean state)
    {
        mAlarm.setRepeat(state);
    }

    /**
     * @brief Save which day was selected to be repeated, or deselected so that
     *        it is not repeated.
     */
    @Override
    public void onClick(View v)
    {
        if (!mCheckbox.isChecked())
        {
            return;
        }

        byte day = mAlarm.indexToDay((int)v.getTag());
        mDays.toggleButton((Button)v);
        mAlarm.toggleDay(day);
        mText.setText(mAlarm.getDaysString());
    }

}
