package com.nfcalarmclock;


import android.app.TimePickerDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.format.DateFormat;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;
import java.util.Calendar;
import java.util.Locale;

/**
 * @brief Pick an alarm time.
 */
public class AlarmTimePicker
    extends DialogFragment
    implements TimePickerDialog.OnTimeSetListener
{

    private Alarm mAlarm;
    private TextView mTime;
    private TextView mMeridian;

    public void init(Alarm alarm, TextView time, TextView meridian)
    {
        this.mAlarm = alarm;
        this.mTime = time;
        this.mMeridian = meridian;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        final Calendar c = Calendar.getInstance();
        int hour = c.get(Calendar.HOUR_OF_DAY);
        int minute = c.get(Calendar.MINUTE);
        return new TimePickerDialog(getActivity(), this, hour, minute,
                                    is24HourFormat());
    }

    public void onTimeSet(TimePicker view, int hourOfDay, int minuteOfHour)
    {
        int hour = hourOfDay;
        int minute = minuteOfHour;
        String time = "";
        String meridian = "";

        if (!is24HourFormat())
        {
            if (hourOfDay >= 12)
            {
                hour = hourOfDay % 12;
                meridian = "PM";
            }
            else
            {
                meridian = "AM";
            }
            hour = (hour == 0) ? 12 : hour;
        }
        time = String.valueOf(hour)+":"+String.format(Locale.getDefault(),
                                                      "%02d", minute);

        this.mAlarm.setHour(hourOfDay);
        this.mAlarm.setMinute(minuteOfHour);
        this.mTime.setText(time);
        this.mMeridian.setText(meridian);

        Toast.makeText(getActivity(), "Time: "+time+" "+meridian,
                       Toast.LENGTH_SHORT).show();
    }

    private boolean is24HourFormat()
    {
        return DateFormat.is24HourFormat(getActivity());
    }

}
