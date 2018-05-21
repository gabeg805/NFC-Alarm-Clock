package com.nfcalarmclock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TimePicker;
import android.widget.Toast;

import android.app.Activity;
import android.widget.TimePicker.OnTimeChangedListener;

// import java.util.Calendar;
// import java.util.Date;
// import android.text.format.DateFormat;
// import android.content.ContentValues;
// import android.database.sqlite.SQLiteDatabase;

/**
 * @brief Add an alarm.
 */
public class AlarmAddTimeFragment
    extends Fragment
    implements TimePicker.OnTimeChangedListener
{

    private static final String NAME = "NFCAlarmClock";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        return inflater.inflate(R.layout.frg_alarm_add_time, null);
    }

    @Override
    public void onViewCreated (View view, Bundle savedInstanceState)
    {
        TimePicker timepicker = view.findViewById(R.id.alarm_add_time_picker);
        timepicker.setOnTimeChangedListener(this);
    }

    @Override
    public void onTimeChanged(TimePicker view, int hourOfDay, int minute)
    {
        String message = "HourS: "+Integer.toString(hourOfDay)+" | Minute: "+Integer.toString(minute);
        Toast.makeText(getActivity(), message, Toast.LENGTH_LONG).show();
        // Log.e(NAME, message);

        // AlarmDatabase mDbHelper = new AlarmDatabase(getContext());
        // SQLiteDatabase db = mDbHelper.getWritableDatabase();
        // ContentValues values = new ContentValues();

        // // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_ID, title);
        // // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_ID, title);
        // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_ENABLED, 1);
        // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_HOUR, hourOfDay);
        // values.put(AlarmDatabaseContract.AlarmTable.COLUMN_MINUTE, minute);

        // long newRowId = db.insert(AlarmDatabaseContract.AlarmTable.TABLE_NAME, null, values);
    }

}
