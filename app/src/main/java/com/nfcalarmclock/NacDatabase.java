package com.nfcalarmclock;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.util.Log;
import java.util.ArrayList;
import java.util.Arrays;
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
	public long add(Alarm alarm)
	{
		this.setDatabase();

		String table = NacDatabaseContract.AlarmTable.TABLE_NAME;
		ContentValues cv = this.getContentValues(alarm);
		long id = this.mDatabase.insert(table, null, cv);

		if (id < 0)
		{
			Log.e("NFCAlarmClock", "Uh oh! Error with adding to database.");
		}
		else
		{
			Log.e("NFCAlarmClock", "Added to database successfully!");
		}

		return id;
	}

	/**
	 * @brief Delete a row from the database.
	 */
	public int delete(Alarm alarm)
	{
		NacUtility.print("Deleting alarm from NacDatabase.");
		alarm.print();
		this.setDatabase();

		String table = NacDatabaseContract.AlarmTable.TABLE_NAME;
		//ContentValues cv = this.getContentValues(alarm);
		String where = this.getWhereClause();
		String[] args = this.getWhereArgs(alarm);

		NacUtility.printf("Where Clause : %s", where);
		NacUtility.printf("Where Args   : %s", Arrays.toString(args));

		int rows = this.mDatabase.delete(table, where, args);
		NacUtility.printf("Delete removed %d rows.", rows);

		return rows;
	}

	/**
	 * @brief Update a row in the database.
	 */
	public int update(Alarm alarm)
	{
		NacUtility.print("Updating NacDatabase.");
		alarm.print();
		this.setDatabase();

		String table = NacDatabaseContract.AlarmTable.TABLE_NAME;
		ContentValues cv = this.getContentValues(alarm);
		String where = this.getWhereClause();
		String[] args = this.getWhereArgs(alarm);

		NacUtility.printf("Where Clause : %s", where);
		NacUtility.printf("Where Args   : %s", Arrays.toString(args));

		int rows = this.mDatabase.update(table, cv, where, args);
		NacUtility.printf("Update changed %d rows.", rows);

		return rows;
	}

	/**
	 * @brief Print all alarms in the database.
	 */
	public void print()
	{
		this.setDatabase();

		List<Alarm> list = this.read();

		for (Alarm a : list)
		{
			a.print();
			NacUtility.print("\n");
		}
	}

	/**
	 * @brief Read all alarms from the database.
	 */
	public List<Alarm> read()
	{
		this.setDatabase();

		String table = NacDatabaseContract.AlarmTable.TABLE_NAME;
		Cursor cursor = this.mDatabase.query(table, null, null, null, null,
			null, null);
		List<Alarm> list = new ArrayList<>();

		while(cursor.moveToNext())
		{
			Alarm alarm = new Alarm();
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

			alarm.setId(id);
			alarm.setEnabled(enabled);
			alarm.set24HourFormat(format);
			alarm.setHour(hour);
			alarm.setMinute(minute);
			alarm.setDays(days);
			alarm.setRepeat(repeat);
			alarm.setVibrate(vibrate);
			alarm.setSound(sound);
			alarm.setName(name);
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
		else
		{
			this.mDatabase = this.getWritableDatabase();
		}
	}

	/**
	 * @brief Return a ContentValues object based on the given alarm.
	 */
	private ContentValues getContentValues(Alarm alarm)
	{
		ContentValues cv = new ContentValues();
		int id = alarm.getId();
		boolean enabled = alarm.getEnabled();
		boolean format = alarm.get24HourFormat();
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		int days = alarm.getDays();
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
	 * @return Where clause using all fields of an alarm.
	 */
	private String getWhereClause()
	{
		return NacDatabaseContract.AlarmTable.COLUMN_ID + "=?";
		//return
		//	NacDatabaseContract.AlarmTable.COLUMN_ID + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_ENABLED + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_24HOURFORMAT + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_HOUR + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_MINUTE + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_DAYS + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_REPEAT + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_VIBRATE + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_SOUND + "=? AND " +
		//	NacDatabaseContract.AlarmTable.COLUMN_NAME + "=?";
		//	//NacDatabaseContract.AlarmTable.COLUMN_NFCTAG + "=? "
	}

	/**
	 * @param  alarm  The alarm to convert to a where clause.
	 *
	 * @return Where arguments for the where clause.
	 */
	private String[] getWhereArgs(Alarm alarm)
	{
		String id = String.valueOf(alarm.getId());
		//String enabled = String.valueOf(alarm.getEnabled() ? 1 : 0);
		//String format = String.valueOf(alarm.get24HourFormat() ? 1 : 0);
		//String hour = String.valueOf(alarm.getHour());
		//String minute = String.valueOf(alarm.getMinute());
		//String days = String.valueOf(alarm.getDays());
		//String repeat = String.valueOf(alarm.getRepeat() ? 1 : 0);
		//String vibrate = String.valueOf(alarm.getVibrate() ? 1 : 0);
		//String sound = alarm.getSound();
		//String name = alarm.getName();
		//String tag = String.valueOf(alarm.getTag());

		return new String[] {id};
		//return new String[] {id, enabled, format, hour, minute, days, repeat,
		//	vibrate, sound, name};
	}

}
