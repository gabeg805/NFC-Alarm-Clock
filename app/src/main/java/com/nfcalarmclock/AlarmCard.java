package com.nfcalarmclock;

import android.content.Context;
import android.content.res.Resources;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.RecyclerView;
import android.text.format.DateFormat;
import android.util.Log;
import android.view.ContextMenu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.View.OnCreateContextMenuListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Switch;
import android.widget.TextView;
import android.widget.Toast;
import java.util.Locale;

/**
 * @brief Holder of all important views.
 */
public class AlarmCard
    extends RecyclerView.ViewHolder
{
    public AppCompatActivity activity;
    public Context context;
    public CardView card;
    public ImageView expandbutton;
    public ImageView collapsebutton;
    public RelativeLayout summary;
    public RelativeLayout expandable;
    public RelativeLayout time;
    public TextView hourminute;
    public TextView meridian;
    public Switch switched;
    public TextView repeattext;
    public DayOfWeekButtons repeatdays;
    public CheckBox repeat;
    public ImageTextButton sound;
    public CheckBox vibrate;
    public ImageTextButton name;
    public ImageTextButton delete;

    public Alarm mAlarm;

    /**
     * @brief Define all views in the holder.
     */
    public AlarmCard(Context c, View v)
    {
        super(v);
        this.activity = (AppCompatActivity) c;
        this.context = c;
        this.card = (CardView) v.findViewById(R.id.view_card_alarm);
        this.expandbutton = (ImageView) v.findViewById(R.id.alarmExpandButton);
        this.collapsebutton = (ImageView) v.findViewById(R.id.alarmCollapseButton);
        this.summary = (RelativeLayout) v.findViewById(R.id.alarmMinorSummary);
        this.expandable = (RelativeLayout) v.findViewById(R.id.alarmMinorExpand);
        this.time = (RelativeLayout) v.findViewById(R.id.alarmTimeSet);
        this.hourminute = (TextView) v.findViewById(R.id.alarmTime);
        this.meridian = (TextView) v.findViewById(R.id.alarmMeridian);
        this.switched = (Switch) v.findViewById(R.id.alarmSwitch);
        this.repeattext = (TextView) v.findViewById(R.id.alarmRepeatText);
        this.repeatdays = (DayOfWeekButtons) v.findViewById(R.id.alarmRepeatDays);
        this.repeat = (CheckBox) v.findViewById(R.id.alarmRepeatCheck);
        this.sound = (ImageTextButton) v.findViewById(R.id.alarmSound);
        this.vibrate = (CheckBox) v.findViewById(R.id.alarmVibrateCheck);
        this.name = (ImageTextButton) v.findViewById(R.id.alarmName);
        this.delete = (ImageTextButton) v.findViewById(R.id.alarmDelete);

        this.expandbutton.setTag(this);
        this.collapsebutton.setTag(this);
        this.time.setTag(this);
        // this.hourminute.setTag(this);
        // this.meridian.setTag(this);
        this.switched.setTag(this);
        this.repeattext.setTag(this);
        // this.repeatdays.setTag(this);
        this.repeat.setTag(this);
        this.sound.setTag(this);
        this.vibrate.setTag(this);
        this.name.setTag(this);
        this.delete.setTag(this);
    }

    /**
     * @brief Initialize the alarm card.
     */
    public void init()
    {
        this.expandable.setVisibility(View.GONE);
        this.expandable.setEnabled(false);
    }

    /**
     * @brief Expand the alarm card.
     */
    public void expand()
    {
        this.summary.setVisibility(View.GONE);
        this.summary.setEnabled(false);
        this.expandable.setVisibility(View.VISIBLE);
        this.expandable.setEnabled(true);
        setColor(NacUtility.getThemeAttrColor(this.context,
                                              R.attr.colorCardExpanded));
    }

    /**
     * @brief Collapse the alarm card.
     */
    public void collapse()
    {
        this.summary.setVisibility(View.VISIBLE);
        this.summary.setEnabled(true);
        this.expandable.setVisibility(View.GONE);
        this.expandable.setEnabled(false);
        setColor(NacUtility.getThemeAttrColor(this.context,
                                              R.attr.colorCard));
    }

    /**
     * @brief Set the alarm attributes.
     */
    public void setAlarm(Alarm alarm)
    {
        this.mAlarm = alarm;
        this.setTime(alarm);
        this.setEnabled(alarm);
        this.setRepeatText(alarm);
        this.setRepeat(alarm);
        this.setRepeatDays(alarm);
        this.setSound(alarm);
        this.setVibrate(alarm);
        this.setName(alarm);
    }

    /**
     * @brief Set the time.
     */
    public void setTime(Alarm alarm)
    {
        Locale locale = Locale.getDefault();
        int h = alarm.getHour();
        int m = alarm.getMinute();
        boolean format = DateFormat.is24HourFormat(this.context);
        String hour = String.valueOf(alarm.toFormat(h, format));
        String minute = String.format(locale, "%02d", m);
        String meridian = alarm.getMeridian(h, format);
        this.hourminute.setText(hour+":"+minute);
        this.meridian.setText(meridian);
    }

    /**
     * @brief Set whether the alarm is enabled or not.
     */
    public void setEnabled(Alarm alarm)
    {
        this.switched.setChecked(alarm.getEnabled());
    }

    /**
     * @brief Set the text indicating which days to repeat the alarm.
     */
    public void setRepeatText(Alarm alarm)
    {
        this.repeattext.setText(alarm.getDaysString());
    }

    /**
     * @brief Set whether the repeat check box is enabled or not.
     */
    public void setRepeat(Alarm alarm)
    {
        this.repeat.setChecked(alarm.getRepeat());
    }

    /**
     * @brief Set the days to repeat the alarm.
     */
    public void setRepeatDays(Alarm alarm)
    {
        if (!alarm.getRepeat())
        {
            return;
        }
        int days = alarm.getDays();
        for (int i=0; i < 7; i++)
        {
            if (((days >> i) & 1) != 0)
            {
                this.repeatdays.enableButton(i);
            }
        }
    }

    /**
     * @brief Set the alarm sound.
     */
    public void setSound(Alarm alarm)
    {
        // this.sound.setText(alarm.getSound());
    }

    /**
     * @brief Set whether the vibrate check box is enabled or not.
     */
    public void setVibrate(Alarm alarm)
    {
        this.vibrate.setChecked(alarm.getVibrate());
    }

    /**
     * @brief Set the alarm name.
     */
    public void setName(Alarm alarm)
    {
        this.name.setText(alarm.getName());
    }

    /**
     * @brief Set the color of the card.
     */
    public void setColor(int color)
    {
        this.card.setCardBackgroundColor(color);
    }

    /**
     * @brief Set the color of the card using the resource ID.
     */
    public void setResourceColor(int id)
    {
        Resources r = context.getResources();
        int c = r.getColor(id);
        this.setColor(c);
    }

    /**
     * @brief Set the listener to display a context menu.
     * 
     * @details The listener is called when the alarm card is long clicked.
     */
    public void setShowMenuListener()
    {
        this.activity.registerForContextMenu(this.card);
        this.card.setOnCreateContextMenuListener(this.ShowMenuListener);
    }

    /**
     * @brief Set the listener to expand the alarm card.
     * 
     * @details The listener is called when clicking the expand button.
     */
    public void setExpandListener(View.OnClickListener listener)
    {
        this.expandbutton.setOnClickListener(listener);
    }

    /**
     * @brief Set the listener to collapse the alarm card.
     * 
     * @details The listener is called when clicking the collapse button.
     */
    public void setCollapseListener(View.OnClickListener listener)
    {
        this.collapsebutton.setOnClickListener(listener);
    }

    /**
     * @brief Set the listener to delete the alarm card.
     * 
     * @details The listener is called when clicking the delete button.
     */
    public void setDeleteListener(View.OnClickListener listener)
    {
        this.delete.setOnClickListener(listener);
    }

    /**
     * @brief Set the listener to display a dialog to pick the time for the
     *        alarm.
     * 
     * @details The listener is called when clicking the time element in the
     *          alarm card.
     */
    public void setTimePickerListener(View.OnClickListener listener)
    {
        this.time.setOnClickListener(listener);
    }

    /**
     * @brief Set the listener for when the switch is clicked.
     * 
     * @deteails The listener is called when clicking the switch button.
     */
    public void setSwitchListener(CompoundButton.OnCheckedChangeListener listener)
    {
        this.switched.setOnCheckedChangeListener(listener);
    }

    /**
     * @brief Set the listener for when repeat is selected.
     * 
     * @details The listener is called when selecting the repeat checkbox.
     */
    public void setRepeatListener(CompoundButton.OnCheckedChangeListener listener)
    {
        this.repeat.setOnCheckedChangeListener(listener);
    }




    public void setRepeatDaysListener()
    {
        this.repeatdays.setOnClickListener(this.RepeatDaysButtonListener);
    }

    /**
     * @brief Button click listener.
     */
    private View.OnClickListener RepeatDaysButtonListener =
        new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                byte day = mAlarm.indexToDay((int)v.getTag());
                repeatdays.toggleButton((Button)v);
                mAlarm.toggleDay(day);
                setRepeatText(mAlarm);
            }
        };



    /**
     * @brief Set the listener to display a dialog to set the sound to play when
     *        the alarm is triggered.
     * 
     * @details The listener is called when clicking the sound element in the
     *          alarm card.
     */
    public void setSoundSetListener(View.OnClickListener listener)
    {
        this.sound.setOnClickListener(listener);
    }

    /**
     * @brief Set the listener for when vibrate is selected.
     * 
     * @details The listener is called when selecting the vibrate checkbox.
     */
    public void setVibrateListener(CompoundButton.OnCheckedChangeListener listener)
    {
        this.vibrate.setOnCheckedChangeListener(listener);
    }

    /**
     * @brief Set the listener to display a dialog to set the name of the alarm.
     * 
     * @details The listener is called when clicking the name element in the
     *          alarm card.
     */
    public void setNameSetListener(View.OnClickListener listener)
    {
        this.name.setOnClickListener(listener);
    }

    /**
     * @brief Display the context menu for the selected alarm card.
     */
    View.OnCreateContextMenuListener ShowMenuListener =
        new View.OnCreateContextMenuListener()
        {
            @Override
            public void onCreateContextMenu(ContextMenu menu, View v,
                                            ContextMenuInfo menuInfo)
            {
                MenuInflater inflater = activity.getMenuInflater();
                inflater.inflate(R.menu.alarm_card, menu);
            }
        };

}
