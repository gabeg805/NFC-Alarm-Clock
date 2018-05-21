package com.nfcalarmclock;

import android.os.Bundle;
import android.support.v4.app.DialogFragment;
// import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

// import android.support.v7.app.AppCompatActivity;
import android.app.Activity;
import android.widget.TimePicker;
// import java.util.Calendar;
// import java.util.Date;
// import java.util.Random;
// import android.view.View;
// import android.view.ViewGroup;
import android.util.Log;
import android.app.Dialog;
import android.app.AlertDialog;
// // import android.app.TimePickerDialog;
// import android.text.format.DateFormat;
// import android.widget.TimePicker.OnTimeChangedListener;

// import android.support.annotation.NonNull;
// import android.content.ContentValues;
// import android.database.sqlite.SQLiteDatabase;
import android.content.DialogInterface;


import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;

import android.support.v7.app.AppCompatActivity;

// HACK THE PLANET!!!!!!!

/**
 * @brief Add an alarm.
 */
public class AlarmAddActivity
    extends AppCompatActivity
{

    private static final String NAME = "NFCAlarmClock";

    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_alarm_add);

        AlarmAddTimeFragment addalarm = new AlarmAddTimeFragment();
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();
        transaction.add(addalarm, "AddAlarm");
        // transaction.add(R.id.fragment_yo, addalarm);
        transaction.replace(R.id.alarm_add_fragment_container, addalarm);
        // transaction.addToBackStack(null);
        transaction.commit();
        // AddAlarmFragment timePicker = new AddAlarmFragment();
        // timePicker.show(getSupportFragmentManager(), "time picker");
    }

    // @Override
    // public Dialog onCreateDialog(Bundle savedInstanceState)
    // {
    //     // String title = getArguments().getString("title");
    //     AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
    //     // builder.setTitle("Set an alarm");

    //     LayoutInflater inflater = LayoutInflater.from(getContext());
    //     View view = inflater.inflate(R.layout.fragment_add_alarm, null);
    //     builder.setView(view);

    //     builder.setPositiveButton("Next", new DialogInterface.OnClickListener()
    //         {
    //             @Override
    //             public void onClick(DialogInterface dialog, int which)
    //             {
    //                 // AlarmDaysDialogFragment days = new AlarmDaysDialogFragment();
    //                 // FragmentTransaction transaction = getActivity().getSupportFragmentManager().beginTransaction();
    //                 // transaction.replace(0, days, "SetRepeat");
    //                 // transaction.commit();
    //             }
    //         });
    //     builder.setNegativeButton("Cancel", new DialogInterface.OnClickListener()
    //         {
    //             @Override
    //             public void onClick(DialogInterface dialog, int which)
    //             {
    //                 dialog.dismiss();
    //             }
    //         });

    //     return builder.create();
    // }

    // @NonNull
    // @Override
    // public Dialog onCreateDialog(Bundle savedInstanceState)
    // {
    //     Activity activity = getActivity();
    //     Calendar cal = Calendar.getInstance();
    //     int hour = cal.get(Calendar.HOUR_OF_DAY);
    //     int minute = cal.get(Calendar.MINUTE);
    //     boolean is24format = DateFormat.is24HourFormat(activity);
    //     TimePickerDialog dialog = new TimePickerDialog(activity, this, hour,
    //                                                    minute, is24format);
    //     dialog.setButton(DialogInterface.BUTTON_POSITIVE, "Next", dialog);
    //     return dialog;
    // }

    // @Override
    // public void onTimeSet(TimePicker view, int hourOfDay, int minute)
    // {
    //     String message = "HourS: "+Integer.toString(hourOfDay)+" | Minute: "+Integer.toString(minute);
    //     Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
    //     // Log.e(NAME, message);

    //     // AlarmDatabase mDbHelper = new AlarmDatabase(getContext());
    //     // SQLiteDatabase db = mDbHelper.getWritableDatabase();
    //     // ContentValues values = new ContentValues();

    //     // // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_ID, title);
    //     // // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_ID, title);
    //     // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_ENABLED, 1);
    //     // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_HOUR, hourOfDay);
    //     // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_MINUTE, minute);

    //     // long newRowId = db.insert(AlarmDatabaseContract.AlarmTable.TABLE_NAME, null, values);
    // }

}
