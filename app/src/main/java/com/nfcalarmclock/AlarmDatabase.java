package com.nfcalarmclock;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class AlarmDatabase
    extends SQLiteOpenHelper
{

    public AlarmDatabase(Context context)
    {
        super(context, AlarmDatabaseContract.DATABASE_NAME, null,
              AlarmDatabaseContract.DATABASE_VERSION);
    }

    // Method is called during creation of the database
    @Override
    public void onCreate(SQLiteDatabase db)
    {
        db.execSQL(AlarmDatabaseContract.AlarmTable.CREATE_TABLE);
    }

    // Method is called during an upgrade of the database
    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        db.execSQL(AlarmDatabaseContract.AlarmTable.DELETE_TABLE);
        onCreate(db);
    }

    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
    {
        onUpgrade(db, oldVersion, newVersion);
    }

}
