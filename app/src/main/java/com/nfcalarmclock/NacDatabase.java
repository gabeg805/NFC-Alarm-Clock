package com.nfcalarmclock;

import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.provider.BaseColumns;
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
		super(context, Contract.DATABASE_NAME, null,
			Contract.DATABASE_VERSION);

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
		if (alarm == null)
		{
			return -1;
		}

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
		if (alarm == null)
		{
			return -1;
		}

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

		if (alarm == null)
		{
			return false;
		}

		SQLiteDatabase db = this.getDatabase();
		String table = this.getTable();
		String where = Contract.AlarmTable.COLUMN_ID + "=? AND "
			+ Contract.AlarmTable.COLUMN_ENABLED + "=? AND "
			+ Contract.AlarmTable.COLUMN_24HOURFORMAT + "=? AND "
			+ Contract.AlarmTable.COLUMN_DAYS + "=? AND "
			+ Contract.AlarmTable.COLUMN_REPEAT + "=? AND "
			+ Contract.AlarmTable.COLUMN_VIBRATE + "=? AND "
			+ Contract.AlarmTable.COLUMN_SOUND + "=? AND "
			+ Contract.AlarmTable.COLUMN_NAME + "=?";
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
		if (alarm == null)
		{
			return null;
		}

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

		cv.put(Contract.AlarmTable.COLUMN_ID, id);
		cv.put(Contract.AlarmTable.COLUMN_ENABLED, enabled);
		cv.put(Contract.AlarmTable.COLUMN_24HOURFORMAT, format);
		cv.put(Contract.AlarmTable.COLUMN_HOUR, hour);
		cv.put(Contract.AlarmTable.COLUMN_MINUTE, minute);
		cv.put(Contract.AlarmTable.COLUMN_DAYS, days);
		cv.put(Contract.AlarmTable.COLUMN_REPEAT, repeat);
		cv.put(Contract.AlarmTable.COLUMN_VIBRATE, vibrate);
		cv.put(Contract.AlarmTable.COLUMN_SOUND, sound);
		cv.put(Contract.AlarmTable.COLUMN_NAME, name);
		// cv.put(Contract.AlarmTable.COLUMN_NFCTAG, tag);

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
		return Contract.AlarmTable.TABLE_NAME;
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
		if (alarm == null)
		{
			return null;
		}

		String id = String.valueOf(alarm.getId());

		return new String[] {id};
	}

	/**
	 * @return Where clause using all fields of an alarm.
	 */
	private String getWhereClause()
	{
		return Contract.AlarmTable.COLUMN_ID + "=?";
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
		db.execSQL(Contract.AlarmTable.CREATE_TABLE);

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
		db.execSQL(Contract.AlarmTable.DELETE_TABLE);
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
 
 		if ((fromAlarm == null) || (toAlarm == null))
		{
			return -1;
		}

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
		if (alarm == null)
		{
			return -1;
		}

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

	/**
	 * Database contract.
	 */
	public static final class Contract
	{

		/**
		 * Full app name.
		 */
		public static final String AUTHORITY = "com.nfcalarmclock";

		/**
		 * Scheme.
		 */
		public static final String SCHEME = "content://";

		/**
		 * Database name.
		 */
		public static final String DATABASE_NAME = "NFCAlarms.db";

		/**
		 * Database version.
		 */
		public static final int DATABASE_VERSION = 1;

		/**
		 * prevent someone from instantiating the contract class.
		 */
		private Contract()
		{
		}

		/**
		 * Define the table contents.
		 */
		public static final class AlarmTable
			implements BaseColumns
		{

			/**
			 * Prevent someone from instantiating the table class.
			 */
			private AlarmTable()
			{
			}

			/**
			 * Table name.
			 */
			public static final String TABLE_NAME = "NfcAlarms";

			/**
			 * ID of the alarm.
			 */
			public static final String COLUMN_ID = "Id";

			/**
			 * Enabled indicator.
			 */
			public static final String COLUMN_ENABLED = "Enabled";

			/**
			 * Hour format.
			 */
			public static final String COLUMN_24HOURFORMAT = "HourFormat";

			/**
			 * Hour.
			 */
			public static final String COLUMN_HOUR = "Hour";

			/**
			 * Minute.
			 */
			public static final String COLUMN_MINUTE = "Minute";

			/**
			 * Days the alarm is scheduled to run.
			 */
			public static final String COLUMN_DAYS = "Days";

			/**
			 * Repeat indicator.
			 */
			public static final String COLUMN_REPEAT = "Repeat";

			/**
			 * Vibrate the phone indicator.
			 */
			public static final String COLUMN_VIBRATE = "Vibrate";

			/**
			 * Sound played when the alarm is run.
			 */
			public static final String COLUMN_SOUND = "Sound";

			/**
			 * Name of the alarm.
			 */
			public static final String COLUMN_NAME = "Name";

			/**
			 * NFC tag.
			 */
			public static final String COLUMN_NFCTAG = "NfcTag";

			/**
			 * The content style URI
			 */
			public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
				+ "/" + TABLE_NAME);

			/**
			 * The content URI base for a single row. An ID must be appended.
			 */
			public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME
				+ AUTHORITY + "/" + TABLE_NAME + "/");

			/**
			 * The default sort order for this table
			 */
			public static final String DEFAULT_SORT_ORDER = COLUMN_HOUR
				+ " ASC";

			/**
			 * The MIME type of {@link #CONTENT_URI} providing rows
			 */
			public static final String CONTENT_TYPE =
				ContentResolver.CURSOR_DIR_BASE_TYPE
					+ "/vnd.com.nfcalarmclock";

			/**
			 * The MIME type of a {@link #CONTENT_URI} single row
			 */
			public static final String CONTENT_ITEM_TYPE =
				ContentResolver.CURSOR_ITEM_BASE_TYPE
					+ "/vnd.com.nfcalarmclock";

			/**
			 * SQL Statement to create the routes table.
			 */
			public static final String CREATE_TABLE =
				"CREATE TABLE " + TABLE_NAME
				+ " ("
				+ _ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_ID + " INTEGER,"
				+ COLUMN_ENABLED + " INTEGER,"
				+ COLUMN_24HOURFORMAT + " INTEGER,"
				+ COLUMN_HOUR + " INTEGER,"
				+ COLUMN_MINUTE + " INTEGER,"
				+ COLUMN_DAYS + " INTEGER,"
				+ COLUMN_REPEAT + " INTEGER,"
				+ COLUMN_VIBRATE + " INTEGER,"
				+ COLUMN_SOUND + " TEXT,"
				+ COLUMN_NAME + " TEXT,"
				+ COLUMN_NFCTAG + " TEXT"
				+ ");";

			/**
			 * SQL statement to delete the table
			 */
			public static final String DELETE_TABLE =
				"DROP TABLE IF EXISTS " + TABLE_NAME;

			/**
			 * Array of all the columns.
			 */
			public static final String[] KEY_ARRAY = {
				COLUMN_ID,
				COLUMN_ENABLED,
				COLUMN_24HOURFORMAT,
				COLUMN_HOUR,
				COLUMN_MINUTE,
				COLUMN_DAYS,
				COLUMN_REPEAT,
				COLUMN_VIBRATE,
				COLUMN_SOUND,
				COLUMN_NAME,
				COLUMN_NFCTAG
			};

		}

	}

}
