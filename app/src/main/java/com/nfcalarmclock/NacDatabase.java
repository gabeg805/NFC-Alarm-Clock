package com.nfcalarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * NFC Alarm Clock database.
 */
public class NacDatabase
	extends SQLiteOpenHelper
{

	/**
	 * The context.
	 */
	private Context mContext;

	/**
	 * The database.
	 */
	private SQLiteDatabase mDatabase;

	/**
	 */
	public NacDatabase(Context context)
	{
		super(context, NacDatabaseContract.DATABASE_NAME, null,
			NacDatabaseContract.DATABASE_VERSION);

		this.mContext = context;
		this.mDatabase = null;
	}

	/**
	 * Add to the database.
	 */
	public long add(NacAlarm alarm)
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();

		return this.add(db, alarm);
	}

	/**
	 * @see add
	 */
	public long add(SQLiteDatabase db, NacAlarm alarm)
	{
		ContentValues cv = this.getContentValues(alarm);
		String table = this.getTable();
		long result = 0;

		db.beginTransaction();

		try
		{
			result = db.insert(table, null, cv);

			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Delete a row from the database.
	 */
	public long delete(NacAlarm alarm)
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();

		return this.delete(db, alarm);
	}

	/**
	 * @see delete
	 */
	public long delete(SQLiteDatabase db, NacAlarm alarm)
	{
		String table = this.getTable();
		String where = this.getWhereClause();
		String[] args = this.getWhereArgs(alarm);
		long result = 0;

		db.beginTransaction();

		try
		{
			result = db.delete(table, where, args);

			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Check if the given alarm exists in the database.
	 *
	 * @param  alarm  The alarm to check existence against.
	 *
	 * @return True if the alarm exists in the database and False otherwise.
	 */
	public boolean exists(NacAlarm alarm)
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();
		String table = this.getTable();
		String where = NacDatabaseContract.AlarmTable.COLUMN_ID + "=? AND "
			+ NacDatabaseContract.AlarmTable.COLUMN_ENABLED + "=? AND "
			+ NacDatabaseContract.AlarmTable.COLUMN_24HOURFORMAT + "=? AND "
			+ NacDatabaseContract.AlarmTable.COLUMN_DAYS + "=? AND "
			+ NacDatabaseContract.AlarmTable.COLUMN_REPEAT + "=? AND "
			+ NacDatabaseContract.AlarmTable.COLUMN_VIBRATE + "=? AND "
			+ NacDatabaseContract.AlarmTable.COLUMN_SOUND + "=? AND "
			+ NacDatabaseContract.AlarmTable.COLUMN_NAME + "=?";
		String[] args = new String[] {
			String.valueOf(alarm.getId()),
			String.valueOf(alarm.getEnabled() ? 1 : 0),
			String.valueOf(alarm.get24HourFormat() ? 1 : 0),
			String.valueOf(NacCalendar.daysToValue(alarm.getDays())),
			String.valueOf(alarm.getRepeat() ? 1 : 0),
			String.valueOf(alarm.getVibrate() ? 1 : 0),
			alarm.getSound(),
			alarm.getName() };
		String limit = "1";

		Cursor cursor = db.query(table, null, where, args, null, null, null,
			limit);
		boolean exists = (cursor.getCount() > 0);

		cursor.close();

		return exists;
	}

	/**
	 * @return A ContentValues object based on the given alarm.
	 */
	private ContentValues getContentValues(NacAlarm alarm)
	{
		ContentValues cv = new ContentValues();
		int id = alarm.getId();
		boolean enabled = alarm.getEnabled();
		boolean format = alarm.get24HourFormat();
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		int days = NacCalendar.daysToValue(alarm.getDays());
		boolean repeat = alarm.getRepeat();
		boolean vibrate = alarm.getVibrate();
		String sound = alarm.getSound();
		String name = alarm.getName();
		//int tag = alarm.getTag();

		cv.put(NacDatabaseContract.AlarmTable.COLUMN_ID, id);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_ENABLED, enabled);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_24HOURFORMAT, format);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_HOUR, hour);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_MINUTE, minute);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_DAYS, days);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_REPEAT, repeat);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_VIBRATE, vibrate);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_SOUND, sound);
		cv.put(NacDatabaseContract.AlarmTable.COLUMN_NAME, name);
		// cv.put(NacDatabaseContract.AlarmTable.COLUMN_NFCTAG, tag);

		return cv;
	}

	/**
	 * @return The context.
	 */
	private Context getContext()
	{
		return this.mContext;
	}

	/**
	 * @return The SQLiteDatabase.
	 */
	private SQLiteDatabase getDatabase()
	{
		return this.mDatabase;
	}

	/**
	 * @return The table name.
	 */
	private String getTable()
	{
		return NacDatabaseContract.AlarmTable.TABLE_NAME;
	}

	/**
	 * @param  value  The value to convert to a where clause.
	 *
	 * @return Where arguments for the where clause.
	 */
	private String[] getWhereArgs(int value)
	{
		String id = String.valueOf(value);

		return new String[] {id};
	}

	/**
	 * @param  alarm  The alarm to convert to a where clause.
	 *
	 * @return Where arguments for the where clause.
	 */
	private String[] getWhereArgs(NacAlarm alarm)
	{
		String id = String.valueOf(alarm.getId());

		return new String[] {id};
	}

	/**
	 * @return Where clause using all fields of an alarm.
	 */
	private String getWhereClause()
	{
		return NacDatabaseContract.AlarmTable.COLUMN_ID + "=?";
	}

	/**
	 * Create the database for the first time.
	 *
	 * Add an example alarm when the app is first installed (this is presumed by
	 * the database being created).
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(NacDatabaseContract.AlarmTable.CREATE_TABLE);

		Context context = this.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(1)
			.setHour(8)
			.setMinute(0)
			.setRepeat(shared.getRepeat())
			.setDays(shared.getDays())
			.setVibrate(shared.getVibrate())
			.setSound(shared.getSound())
			.setName("Work")
			.set24HourFormat(false)
			.build();

		this.add(db, alarm);
	}

	/**
	 * Downgrade the database.
	 */
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		onUpgrade(db, oldVersion, newVersion);
	}

	/**
	 * Upgrade the database.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		db.execSQL(NacDatabaseContract.AlarmTable.DELETE_TABLE);
		onCreate(db);
	}

	/**
	 * Print all alarms in the database.
	 */
	public void print()
	{
		this.setDatabase();

		List<NacAlarm> list = this.read();

		for (NacAlarm a : list)
		{
			a.print();
			NacUtility.print("\n");
		}
	}

	/**
	 * Read all alarms from the database.
	 */
	public List<NacAlarm> read()
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();
		String table = this.getTable();
		Cursor cursor = db.query(table, null, null, null, null, null, null);
		List<NacAlarm> list = new ArrayList<>();

		while(cursor.moveToNext())
		{
			int id = cursor.getInt(1);
			boolean enabled = (cursor.getInt(2) != 0);
			boolean format = (cursor.getInt(3) != 0);
			int hour = cursor.getInt(4);
			int minute = cursor.getInt(5);
			int days = cursor.getInt(6);
			boolean repeat = (cursor.getInt(7) != 0);
			boolean vibrate = (cursor.getInt(8) != 0);
			String sound = cursor.getString(9);
			String name = cursor.getString(10);

			list.add(new NacAlarm.Builder()
				.setId(id)
				.setHour(hour)
				.setMinute(minute)
				.setDays(days)
				.setRepeat(repeat)
				.setVibrate(vibrate)
				.setSound(sound)
				.setName(name)
				.setEnabled(enabled)
				.set24HourFormat(format)
				.build());
		}

		cursor.close();

		return list;
	}

	/**
	 * Set the database if it is not currently set.
	 */
	private void setDatabase()
	{
		if (this.getDatabase() == null)
		{
			this.mDatabase = getWritableDatabase();
		}
	}

	/**
	 * Swap the IDs of two alarms.
	 *
	 * IDs will dictate the order in which alarms are displayed when the app is
	 * started.
	 *
	 * @param  fromAlarm  An alarm that was moved.
	 * @param  toAlarm	An alarm that was moved.
	 */
	public long swap(NacAlarm fromAlarm, NacAlarm toAlarm)
	{
		this.setDatabase();
 
		int fromId = fromAlarm.getId();
		int toId = toAlarm.getId();
 
		fromAlarm.setId(toId);
		toAlarm.setId(fromId);
 
 		SQLiteDatabase db = this.getDatabase();
		String table = this.getTable();
		ContentValues fromCv = this.getContentValues(fromAlarm);
		ContentValues toCv = this.getContentValues(toAlarm);
		String where = this.getWhereClause();
		String[] fromArgs = this.getWhereArgs(toId);
		String[] toArgs = this.getWhereArgs(fromId);
		long result = 0;

		db.beginTransaction();

		try
		{
			long fromRows = db.update(table, fromCv, where, fromArgs);
			long toRows = db.update(table, toCv, where, toArgs);
			result = fromRows + toRows; 

			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}

		return result;
	}

	/**
	 * Update a row in the database.
	 */
	public long update(NacAlarm alarm)
	{
		this.setDatabase();

 		SQLiteDatabase db = this.getDatabase();

		return this.update(db, alarm);
	}

	/**
	 * @see update
	 */
	public long update(SQLiteDatabase db, NacAlarm alarm)
	{
		String table = this.getTable();
		ContentValues cv = this.getContentValues(alarm);
		String where = this.getWhereClause();
		String[] args = this.getWhereArgs(alarm);
		long result = 0;

		db.beginTransaction();

		try
		{
			result = db.update(table, cv, where, args);

			db.setTransactionSuccessful();
		}
		finally
		{
			db.endTransaction();
		}

		return result;
	}

}
