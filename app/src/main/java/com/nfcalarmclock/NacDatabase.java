package com.nfcalarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.List;

/**
 * @brief NFC Alarm Clock database.
 */
public class NacDatabase
    extends SQLiteOpenHelper
{

    /**
     * @brief Database.
     */
    private SQLiteDatabase mDatabase = null;

    /**
     * @brief Construct the SQLite object.
     */
    public NacDatabase(Context context)
    {
        super(context, NacDatabaseContract.DATABASE_NAME, null,
              NacDatabaseContract.DATABASE_VERSION);
    }

    /**
     * @brief Create the database for the first time.
     */
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(NacDatabaseContract.AlarmTable.CREATE_TABLE);
    }

    /**
     * @brief Upgrade the database.
     */
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(NacDatabaseContract.AlarmTable.DELETE_TABLE);
        onCreate(db);
    }

    /**
     * @brief Downgrade the database.
     */
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

    /**
     * @brief Add to the database.
     */
    public void add(Alarm alarm)
    {
        this.setDatabase();
        ContentValues values = new ContentValues();
        boolean enabled = alarm.getEnabled();
        int hour = alarm.getHour();
        int minute = alarm.getMinute();
        int days = alarm.getDays();
        boolean repeat = alarm.getRepeat();
        boolean vibrate = alarm.getVibrate();
        String sound = alarm.getSound();
        String name = alarm.getName();
        values.put(NacDatabaseContract.AlarmTable.COLUMN_ENABLED, enabled);
        values.put(NacDatabaseContract.AlarmTable.COLUMN_HOUR, hour);
        values.put(NacDatabaseContract.AlarmTable.COLUMN_MINUTE, minute);
        values.put(NacDatabaseContract.AlarmTable.COLUMN_DAYS, days);
        values.put(NacDatabaseContract.AlarmTable.COLUMN_REPEAT, repeat);
        values.put(NacDatabaseContract.AlarmTable.COLUMN_VIBRATE, vibrate);
        values.put(NacDatabaseContract.AlarmTable.COLUMN_SOUND, sound);
        values.put(NacDatabaseContract.AlarmTable.COLUMN_NAME, name);
        // values.put(NacDatabaseContract.AlarmTable.COLUMN_NFCTAG, tag);
        long id = this.mDatabase.insert(NacDatabaseContract.AlarmTable.TABLE_NAME,
                                        null, values);
        if (id < 0)
        {
            Log.e("NFCAlarmClock", "Uh oh! Error with adding to database.");
        }
        else
        {
            Log.e("NFCAlarmClock", "Added to database successfully!");
        }
    }

    /**
     * @brief Read all alarms from the database.
     */
    public List<Alarm> read()
    {
        this.setDatabase();
        Cursor cursor = this.mDatabase.query(NacDatabaseContract.AlarmTable.TABLE_NAME,
                                             null, null, null, null, null,
                                             null);
        List<Alarm> list = new ArrayList<>();
        while(cursor.moveToNext())
        {
            Alarm alarm = new Alarm();
            boolean enabled = (cursor.getInt(1) != 0);
            int hour = cursor.getInt(2);
            int minute = cursor.getInt(3);
            int days = cursor.getInt(4);
            boolean repeat = (cursor.getInt(5) != 0);
            boolean vibrate = (cursor.getInt(6) != 0);
            String sound = cursor.getString(7);
            String name = cursor.getString(8);
            alarm.setEnabled(enabled);
            alarm.setHour(hour);
            alarm.setMinute(minute);
            alarm.setDays(days);
            alarm.setRepeat(repeat);
            alarm.setVibrate(vibrate);
            alarm.setSound(sound);
            alarm.setName(name);
            alarm.print();
            list.add(alarm);
        }
        cursor.close();
        return list;
    }

    /**
     * @brief Set the database if it is not currently set.
     */
    private void setDatabase()
    {
        if (this.mDatabase != null)
        {
            return;
        }
        this.mDatabase = this.getWritableDatabase();
    }

}
