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
		//boolean format = alarm.get24HourFormat();
		int hour = alarm.getHour();
		int minute = alarm.getMinute();
		int days = NacCalendar.Days.daysToValue(alarm.getDays());
		boolean repeat = alarm.getRepeat();
		boolean vibrate = alarm.getVibrate();
		int soundType = alarm.getSoundType();
		String soundPath = alarm.getSoundPath();
		String soundName = alarm.getSoundName();
		String name = alarm.getName();

		cv.put(Contract.AlarmTable.COLUMN_ID, id);
		cv.put(Contract.AlarmTable.COLUMN_ENABLED, enabled);
		cv.put(Contract.AlarmTable.COLUMN_HOUR, hour);
		cv.put(Contract.AlarmTable.COLUMN_MINUTE, minute);
		cv.put(Contract.AlarmTable.COLUMN_DAYS, days);
		cv.put(Contract.AlarmTable.COLUMN_REPEAT, repeat);
		cv.put(Contract.AlarmTable.COLUMN_VIBRATE, vibrate);
		cv.put(Contract.AlarmTable.COLUMN_SOUND_TYPE, soundType);
		cv.put(Contract.AlarmTable.COLUMN_SOUND_PATH, soundPath);
		cv.put(Contract.AlarmTable.COLUMN_SOUND_NAME, soundName);
		cv.put(Contract.AlarmTable.COLUMN_NAME, name);

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
	 * @return The row of the database corresponding to the alarm.
	 */
	private Cursor getRow(NacAlarm alarm)
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();
		String table = this.getTable();
		int id = alarm.getId();
		String where = this.getWhereClause();
		String[] whereArgs = this.getWhereArgs(id);

		return db.query(table, null, where, whereArgs, null, null, null);
	}

	/**
	 * @return The row ID.
	 */
	private int getRowId(NacAlarm alarm)
	{
		Cursor cursor = this.getRow(alarm);
		int id = -1;

		if ((cursor != null) && (cursor.getCount() == 1))
		{
			cursor.moveToFirst();

			id = cursor.getInt(0);

			cursor.close();
		}

		return id;
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
	 * @return The where clause for matching with the row ID.
	 */
	private String getWhereIdClause()
	{
		return Contract.AlarmTable._ID + "=?";
	}

	/**
	 * @return The where clause for matching with the alarm ID.
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
		String soundPath = shared.getSound();
		String soundName = NacSound.getName(context, soundPath);
		int soundType = NacSound.getType(soundPath);
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(1)
			.setHour(8)
			.setMinute(0)
			.setRepeat(shared.getRepeat())
			.setDays(shared.getDays())
			.setVibrate(shared.getVibrate())
			.setSoundType(soundType)
			.setSoundPath(soundPath)
			.setSoundName(soundName)
			.setName("Work")
			.build();

		this.add(db, alarm);
	}

	/**
	 * Downgrade the database.
	 */
	public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		NacUtility.printf("onDowngrade! %d %d", oldVersion, newVersion);
		onUpgrade(db, oldVersion, newVersion);
	}

	/**
	 * Upgrade the database.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		NacUtility.printf("onUpgrade OF THE DATABASE! %d %d", oldVersion, newVersion);

		List<NacAlarm> alarms = this.read(db, oldVersion);
		int size = alarms.size();

		db.execSQL(Contract.AlarmTable.DELETE_TABLE);

		if (size == 0)
		{
			onCreate(db);
		}
		else
		{
			db.execSQL(Contract.AlarmTable.CREATE_TABLE);

			for (NacAlarm a : alarms)
			{
				this.add(db, a);
			}
		}
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

		return this.read(db, Contract.DATABASE_VERSION);
	}

	public List<NacAlarm> read(SQLiteDatabase db, int version)
	{
		Context context = this.getContext();
		String table = this.getTable();
		Cursor cursor = db.query(table, null, null, null, null, null, null);
		List<NacAlarm> list = new ArrayList<>();

		while(cursor.moveToNext())
		{
			int id;
			boolean enabled;
			int hour;
			int minute;
			int days;
			boolean repeat;
			boolean vibrate;
			int soundType;
			String soundPath;
			String soundName;
			String name;

			if (version == 1)
			{
				id = cursor.getInt(1);
				enabled = (cursor.getInt(2) != 0);
				//boolean format = (cursor.getInt(3) != 0);
				hour = cursor.getInt(4);
				minute = cursor.getInt(5);
				days = cursor.getInt(6);
				repeat = (cursor.getInt(7) != 0);
				vibrate = (cursor.getInt(8) != 0);
				soundPath = cursor.getString(9);
				name = cursor.getString(10);
				soundType = NacSound.getType(soundPath);
				soundName = NacSound.getName(context, soundPath);

			}
			else
			{
				id = cursor.getInt(1);
				enabled = (cursor.getInt(2) != 0);
				hour = cursor.getInt(3);
				minute = cursor.getInt(4);
				days = cursor.getInt(5);
				repeat = (cursor.getInt(6) != 0);
				vibrate = (cursor.getInt(7) != 0);
				soundType = cursor.getInt(8);
				soundPath = cursor.getString(9);
				soundName = cursor.getString(10);
				name = cursor.getString(11);
			}

			list.add(new NacAlarm.Builder()
				.setId(id)
				.setEnabled(enabled)
				.setHour(hour)
				.setMinute(minute)
				.setDays(days)
				.setRepeat(repeat)
				.setVibrate(vibrate)
				.setSoundType(soundType)
				.setSoundPath(soundPath)
				.setSoundName(soundName)
				.setName(name)
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

		int fromId = this.getRowId(fromAlarm);
		int toId = this.getRowId(toAlarm);
 
 		SQLiteDatabase db = this.getDatabase();
		String table = this.getTable();
		ContentValues fromCv = this.getContentValues(fromAlarm);
		ContentValues toCv = this.getContentValues(toAlarm);
		String where = this.getWhereIdClause();
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
		public static final int DATABASE_VERSION = 2;

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
			 * Type of sound played.
			 */
			public static final String COLUMN_SOUND_TYPE = "SoundType";

			/**
			 * Path to the sound that is played when the alarm goes off.
			 */
			public static final String COLUMN_SOUND_PATH = "SoundPath";

			/**
			 * Name of the sound played.
			 */
			public static final String COLUMN_SOUND_NAME = "SoundName";

			/**
			 * Name of the alarm.
			 */
			public static final String COLUMN_NAME = "Name";

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
				+ COLUMN_HOUR + " INTEGER,"
				+ COLUMN_MINUTE + " INTEGER,"
				+ COLUMN_DAYS + " INTEGER,"
				+ COLUMN_REPEAT + " INTEGER,"
				+ COLUMN_VIBRATE + " INTEGER,"
				+ COLUMN_SOUND_TYPE + " INTEGER,"
				+ COLUMN_SOUND_PATH + " TEXT,"
				+ COLUMN_SOUND_NAME + " TEXT,"
				+ COLUMN_NAME + " TEXT"
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
				COLUMN_HOUR,
				COLUMN_MINUTE,
				COLUMN_DAYS,
				COLUMN_REPEAT,
				COLUMN_VIBRATE,
				COLUMN_SOUND_TYPE,
				COLUMN_SOUND_PATH,
				COLUMN_SOUND_NAME,
				COLUMN_NAME,
			};

		}

	}

}
