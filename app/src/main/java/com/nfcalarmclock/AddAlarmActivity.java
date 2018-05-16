package com.nfcalarmclock;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.widget.Toast;
import android.view.LayoutInflater;

import android.annotation.TargetApi;
import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.os.Build;
import android.widget.Button;
import android.widget.TextView;
import android.widget.TimePicker;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;
import android.view.View;
import android.util.Log;

import android.support.v4.app.DialogFragment;
import android.app.Dialog;
import android.app.TimePickerDialog;

import android.text.format.DateFormat;
import android.widget.TimePicker.OnTimeChangedListener;

import android.support.annotation.NonNull;
// HACK THE PLANET!!!!!!!

import android.content.DialogInterface;

/**
 * @brief Add an alarm.
 */
public class AddAlarmActivity
    extends DialogFragment
   implements TimePickerDialog.OnTimeSetListener
{

    private static final String NAME = "NFCAlarmClock";

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState)
    {
        Activity activity = getActivity();
        Calendar cal = Calendar.getInstance();
        int hour = cal.get(Calendar.HOUR_OF_DAY);
        int minute = cal.get(Calendar.MINUTE);
        return new TimePickerDialog(activity, this, hour, minute,
                                    DateFormat.is24HourFormat(activity));
    }

    @Override
    public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    {
        Log.e(NAME, "HourS: " + Integer.toString(hourOfDay) + " | Minute: " + Integer.toString(minute));

        AlarmDatabase mDbHelper = new AlarmDatabase(getContext());
    }

}
