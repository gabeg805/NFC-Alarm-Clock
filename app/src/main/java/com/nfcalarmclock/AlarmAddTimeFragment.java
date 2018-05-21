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
import android.widget.TimePicker;

// import java.util.Calendar;
// import java.util.Date;
// // import android.app.TimePickerDialog;
// import android.text.format.DateFormat;
// import android.widget.TimePicker.OnTimeChangedListener;
// import android.content.ContentValues;
// import android.database.sqlite.SQLiteDatabase;

/**
 * @brief Add an alarm.
 */
public class AlarmAddTimeFragment
    extends Fragment
{

    private static final String NAME = "NFCAlarmClock";

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState)
    {
        View view = inflater.inflate(R.layout.fragment_alarm_add_time, null);
        return view;
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
