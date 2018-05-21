package com.nfcalarmclock;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import android.app.Activity;

// import java.util.Calendar;
// import java.util.Date;
// import android.text.format.DateFormat;
// import android.content.ContentValues;
// import android.database.sqlite.SQLiteDatabase;

/**
 * @brief Add an alarm.
 */
public class AlarmAddDaysFragment
    extends Fragment
{

    private static final String NAME = "NFCAlarmClock";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.frg_alarm_add_days, null);
        return view;
    }

}
