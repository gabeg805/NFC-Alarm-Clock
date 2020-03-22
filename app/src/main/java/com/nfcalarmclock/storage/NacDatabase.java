package com.nfcalarmclock;

import android.content.Context;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.net.Uri;
import android.os.Bundle;
import android.provider.BaseColumns;
import androidx.core.app.JobIntentService;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
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
	 * Check if the database was upgraded.
	 */
	private boolean mWasUpgraded;

	/**
	 */
	public NacDatabase(Context context)
	{
		super(context, Contract.DATABASE_NAME, null,
			Contract.DATABASE_VERSION);

		this.mContext = context;
		this.mDatabase = null;
		this.mWasUpgraded = false;
	}

	/**
	 * @see add
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
		return this.add(db, db.getVersion(), alarm);
	}

	/**
	 * Add to the database.
	 */
	public long add(SQLiteDatabase db, int version, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		ContentValues cv = this.getContentValues(version, alarm);
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
	 * Find the alarm.
	 */
	public NacAlarm findAlarm(int id)
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();
		String table = this.getTable();
		String where = this.getWhereClause();
		String[] whereArgs = this.getWhereArgs(id);
		Cursor cursor = db.query(table, null, where, whereArgs, null, null,
			null);
		int version = db.getVersion();
		NacAlarm alarm = null;

		if (cursor != null)
		{
			cursor.moveToFirst();

			alarm = this.toAlarm(cursor, version);

			cursor.close();
		}

		return alarm;
	}

	public NacAlarm findAlarm(Calendar calendar)
	{
		this.setDatabase();

		String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
		String minute = String.valueOf(calendar.get(Calendar.MINUTE));
		NacCalendar.Day day = NacCalendar.Days.toWeekDay(
			calendar.get(Calendar.DAY_OF_WEEK));
		NacAlarm alarm = null;

		String[] whereArgs = new String[] { hour, minute };
		String whereClause = Contract.AlarmTable.COLUMN_HOUR + "=? AND "
			+ Contract.AlarmTable.COLUMN_MINUTE + "=?";

		SQLiteDatabase db = this.getDatabase();
		int version = db.getVersion();
		String table = this.getTable();
		Cursor cursor = db.query(table, null, whereClause, whereArgs, null,
			null, null);

		while (cursor.moveToNext())
		{
			NacAlarm a = this.toAlarm(cursor, version);

			if ((alarm != null) && alarm.getDays().contains(day))
			{
				alarm = a;
				break;
			}
		}

		cursor.close();

		return alarm;
	}

	/**
	 * @return A ContentValues object based on the given alarm.
	 *
	 * Change this every new database version.
	 */
	private ContentValues getContentValues(int version, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return null;
		}

		ContentValues cv = new ContentValues();

		cv.put(Contract.AlarmTable.COLUMN_ID, alarm.getId());
		cv.put(Contract.AlarmTable.COLUMN_ENABLED, alarm.getEnabled());
		cv.put(Contract.AlarmTable.COLUMN_HOUR, alarm.getHour());
		cv.put(Contract.AlarmTable.COLUMN_MINUTE, alarm.getMinute());
		cv.put(Contract.AlarmTable.COLUMN_DAYS, NacCalendar.Days.daysToValue(alarm.getDays()));
		cv.put(Contract.AlarmTable.COLUMN_REPEAT, alarm.getRepeat());
		cv.put(Contract.AlarmTable.COLUMN_VIBRATE, alarm.getVibrate());
		cv.put(Contract.AlarmTable.COLUMN_NAME, alarm.getName());
		cv.put(Contract.AlarmTable.COLUMN_SOUND_PATH, alarm.getMediaPath());

		switch (version)
		{
			case 0:
			case 4:
				cv.put(Contract.AlarmTable.COLUMN_VOLUME, alarm.getVolume());
				cv.put(Contract.AlarmTable.COLUMN_AUDIO_SOURCE, alarm.getAudioSource());
			case 3:
				cv.put(Contract.AlarmTable.COLUMN_USE_NFC, alarm.getUseNfc());
			case 2:
				cv.put(Contract.AlarmTable.COLUMN_SOUND_TYPE, alarm.getMediaType());
				cv.put(Contract.AlarmTable.COLUMN_SOUND_NAME, alarm.getMediaTitle());
			case 1:
			default:
				break;
		}


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
	 *
	 * Change this every new database version.
	 */
	@Override
	public void onCreate(SQLiteDatabase db)
	{
		db.execSQL(Contract.AlarmTable.CREATE_TABLE_V4);

		Context context = this.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		String mediaPath = shared.getMediaPath();
		String mediaTitle = NacMedia.getTitle(context, mediaPath);
		int mediaType = NacMedia.getType(context, mediaPath);
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(1)
			.setHour(8)
			.setMinute(0)
			.setDays(shared.getDays())
			.setRepeat(shared.getRepeat())
			.setUseNfc(shared.getUseNfc())
			.setVibrate(shared.getVibrate())
			.setVolume(shared.getVolume())
			.setAudioSource(shared.getAudioSource())
			.setMediaType(mediaType)
			.setMediaPath(mediaPath)
			.setMediaTitle(mediaTitle)
			.setName("Work")
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
	 * Upgrade the database to the most up-to-date version.
	 *
	 * Change this every new database version.
	 */
	@Override
	public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion)
	{
		List<NacAlarm> alarms = this.read(db, oldVersion);

		db.execSQL(Contract.AlarmTable.DELETE_TABLE);

		switch (newVersion)
		{
			case 1:
				db.execSQL(Contract.AlarmTable.CREATE_TABLE_V1);
				break;
			case 2:
				db.execSQL(Contract.AlarmTable.CREATE_TABLE_V2);
				break;
			case 3:
				db.execSQL(Contract.AlarmTable.CREATE_TABLE_V3);
				break;
			case 4:
			default:
				db.execSQL(Contract.AlarmTable.CREATE_TABLE_V4);
				break;
		}

		for (NacAlarm a : alarms)
		{
			this.add(db, newVersion, a);
		}

		this.mWasUpgraded = true;
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
	 * @see read
	 */
	public List<NacAlarm> read()
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();
		int version = db.getVersion();

		return this.read(db, version);
	}

	/**
	 * Read all alarms from the database.
	 */
	public List<NacAlarm> read(SQLiteDatabase db, int version)
	{
		String table = this.getTable();
		Cursor cursor = db.query(table, null, null, null, null, null, null);
		List<NacAlarm> list = new ArrayList<>();

		while (cursor.moveToNext())
		{
			NacAlarm alarm = this.toAlarm(cursor, version);

			list.add(alarm);
		}

		cursor.close();

		return list;
	}

	/**
	 * Sort the database.
	 */
	//public void sort(List<NacAlarm> current, List<NacAlarm> sorted)
	public void sort(List<NacAlarm> sorted)
	{
		this.setDatabase();

		//NacUtility.printf("SOOOOOOOOOOOOOOOOOOOOOOOOOOORT");
		//SQLiteDatabase db = this.getDatabase();
		//String table = this.getTable();
		//Cursor cursor = db.query(table, null, null, null, null, null, null);

		//while (cursor.moveToNext())
		//{
		//	NacUtility.printf("ID : %d", cursor.getInt(0));
		//	NacAlarm alarm = this.toAlarm(cursor, 0);
		//	alarm.print();
		//}

		//cursor.close();
		//NacUtility.printf("SOOOOOOOOOOOOOOOOOOOOOOOOOOORT");

		for (int i=0; i < sorted.size(); i++)
		{
			//sorted.get(i).print();
			this.updateRow(i+1, sorted.get(i));
		}

		//NacUtility.printf("AHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHHH");

		//SQLiteDatabase db = this.getDatabase();
		//String table = this.getTable();
		//Cursor cursor = db.query(table, null, null, null, null, null, null);

		//while (cursor.moveToNext())
		//{
		//	NacUtility.printf("ID : %d", cursor.getInt(0));
		//	NacAlarm alarm = this.toAlarm(cursor, 0);
		//	alarm.print();
		//}

		//NacUtility.printf("ENNNNNNNNNNNNNNNNNNNNNNNNNNNNNNNND");
	}

	/**
	 * Set the database if it is not currently set.
	 */
	public void setDatabase()
	{
		if (this.getDatabase() == null)
		{
			this.mDatabase = getWritableDatabase();

			if (this.wasUpgraded())
			{
				this.mWasUpgraded = false;
				this.mDatabase.close();
				this.mDatabase = getWritableDatabase();
			}
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
		int version = db.getVersion();
		String table = this.getTable();
		ContentValues fromCv = this.getContentValues(version, fromAlarm);
		ContentValues toCv = this.getContentValues(version, toAlarm);
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
	 * Convert a Cursor object to an alarm.
	 */
	public NacAlarm toAlarm(Cursor cursor, int version)
	{
		Context context = this.getContext();
		NacAlarm alarm = new NacAlarm();
		int offset = -1;

		if (cursor == null)
		{
			return null;
		}

		switch (version)
		{
			case 0:
			case 4:
				alarm.setId(cursor.getInt(1));
				alarm.setEnabled((cursor.getInt(2) != 0));
				alarm.setHour(cursor.getInt(3));
				alarm.setMinute(cursor.getInt(4));
				alarm.setDays(cursor.getInt(5));
				alarm.setRepeat((cursor.getInt(6) != 0));
				alarm.setUseNfc((cursor.getInt(7) != 0));
				alarm.setVibrate((cursor.getInt(8) != 0));
				alarm.setVolume(cursor.getInt(9));
				alarm.setAudioSource(cursor.getString(10));
				alarm.setMediaType(cursor.getInt(11));
				alarm.setMediaPath(cursor.getString(12));
				alarm.setMediaTitle(cursor.getString(13));
				alarm.setName(cursor.getString(14));
				break;
			case 3:
				offset = 1;
				alarm.setUseNfc((cursor.getInt(7) != 0));
			case 2:
				offset = (offset < 0) ? 0: offset;
				alarm.setId(cursor.getInt(1));
				alarm.setEnabled((cursor.getInt(2) != 0));
				alarm.setHour(cursor.getInt(3));
				alarm.setMinute(cursor.getInt(4));
				alarm.setDays(cursor.getInt(5));
				alarm.setRepeat((cursor.getInt(6) != 0));
				alarm.setVibrate((cursor.getInt(7+offset) != 0));
				alarm.setMediaType(cursor.getInt(8+offset));
				alarm.setMediaPath(cursor.getString(9+offset));
				alarm.setMediaTitle(cursor.getString(10+offset));
				alarm.setName(cursor.getString(11+offset));
				break;
			case 1:
			default:
				alarm.setId(cursor.getInt(1));
				alarm.setEnabled((cursor.getInt(2) != 0));
				// Index 3: 24 hour format
				alarm.setHour(cursor.getInt(4));
				alarm.setMinute(cursor.getInt(5));
				alarm.setDays(cursor.getInt(6));
				alarm.setRepeat((cursor.getInt(7) != 0));
				alarm.setVibrate((cursor.getInt(8) != 0));
				alarm.setMediaPath(cursor.getString(9));
				alarm.setName(cursor.getString(10));
				// Index 11: NFC tag
				alarm.setMediaType(NacMedia.getType(context,
					alarm.getMediaPath()));
				alarm.setMediaTitle(NacMedia.getTitle(context,
					alarm.getMediaPath()));
				break;
		}

		alarm.resetChangeTracker();

		return alarm;
	}

	/**
	 * Update the row in the database with the given alarm information.
	 */
	public long update(NacAlarm alarm)
	{
		this.setDatabase();

		List<NacAlarm> list = new ArrayList<>();

		list.add(alarm);

		return this.update(list);
	}

	/**
	 * @see update
	 */
	public long update(List<NacAlarm> alarms)
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();

		return this.update(db, alarms);
	}

	/**
	 * @see update
	 */
	public long update(SQLiteDatabase db, NacAlarm alarm)
	{
		List<NacAlarm> list = new ArrayList<>();

		list.add(alarm);

		return this.update(db, list);
	}

	/**
	 * @see update
	 */
	public long update(SQLiteDatabase db, List<NacAlarm> alarms)
	{
		if ((alarms == null) || (alarms.size() == 0))
		{
			return -1;
		}

		int version = db.getVersion();
		String table = this.getTable();
		String where = this.getWhereClause();
		long result = 0;

		db.beginTransaction();

		try
		{
			for (NacAlarm a : alarms)
			{
				if (a == null)
				{
					return -1;
				}

				ContentValues cv = this.getContentValues(version, a);
				String[] args = this.getWhereArgs(a);
				result += db.update(table, cv, where, args);
			}

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
	public long updateRow(int row, NacAlarm alarm)
	{
		this.setDatabase();

		SQLiteDatabase db = this.getDatabase();

		return this.updateRow(db, row, alarm);
	}

	/**
	 * @see updateRow
	 */
	public long updateRow(SQLiteDatabase db, int row, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		int version = db.getVersion();
		String table = this.getTable();
		ContentValues cv = this.getContentValues(version, alarm);
		String where = this.getWhereIdClause();
		String[] args = this.getWhereArgs(row);
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
	 * @return True if the database was upgraded, and False otherwise.
	 */
	public boolean wasUpgraded()
	{
		return this.mWasUpgraded;
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
		public static final int DATABASE_VERSION = 4;

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
			 * Use NFC indicator.
			 */
			public static final String COLUMN_USE_NFC = "UseNfc";

			/**
			 * Vibrate the phone indicator.
			 */
			public static final String COLUMN_VIBRATE = "Vibrate";

			/**
			 * Volume level.
			 */
			public static final String COLUMN_VOLUME = "Volume";

			/**
			 * Volume level.
			 */
			public static final String COLUMN_AUDIO_SOURCE = "AudioSource";

			/**
			 * Type of media.
			 */
			public static final String COLUMN_SOUND_TYPE = "SoundType";

			/**
			 * Path to the media file.
			 */
			public static final String COLUMN_SOUND_PATH = "SoundPath";

			/**
			 * Title of the media.
			 */
			public static final String COLUMN_SOUND_NAME = "SoundName";

			/**
			 * Name of the alarm.
			 */
			public static final String COLUMN_NAME = "Name";

			/**
			 * Hour format.
			 */
			public static final String COLUMN_24HOURFORMAT = "HourFormat";

			/**
			 * Path to the media file.
			 */
			public static final String COLUMN_SOUND = "Sound";

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
			 * SQL Statement to create the table (version 4).
			 */
			public static final String CREATE_TABLE_V4 =
				"CREATE TABLE " + TABLE_NAME
				+ " ("
				+ _ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_ID + " INTEGER,"
				+ COLUMN_ENABLED + " INTEGER,"
				+ COLUMN_HOUR + " INTEGER,"
				+ COLUMN_MINUTE + " INTEGER,"
				+ COLUMN_DAYS + " INTEGER,"
				+ COLUMN_REPEAT + " INTEGER,"
				+ COLUMN_USE_NFC + " INTEGER,"
				+ COLUMN_VIBRATE + " INTEGER,"
				+ COLUMN_VOLUME + " INTEGER,"
				+ COLUMN_AUDIO_SOURCE + " TEXT,"
				+ COLUMN_SOUND_TYPE + " INTEGER,"
				+ COLUMN_SOUND_PATH + " TEXT,"
				+ COLUMN_SOUND_NAME + " TEXT,"
				+ COLUMN_NAME + " TEXT"
				+ ");";

			/**
			 * SQL Statement to create the table (version 3).
			 */
			public static final String CREATE_TABLE_V3 =
				"CREATE TABLE " + TABLE_NAME
				+ " ("
				+ _ID + " INTEGER PRIMARY KEY,"
				+ COLUMN_ID + " INTEGER,"
				+ COLUMN_ENABLED + " INTEGER,"
				+ COLUMN_HOUR + " INTEGER,"
				+ COLUMN_MINUTE + " INTEGER,"
				+ COLUMN_DAYS + " INTEGER,"
				+ COLUMN_REPEAT + " INTEGER,"
				+ COLUMN_USE_NFC + " INTEGER,"
				+ COLUMN_VIBRATE + " INTEGER,"
				+ COLUMN_SOUND_TYPE + " INTEGER,"
				+ COLUMN_SOUND_PATH + " TEXT,"
				+ COLUMN_SOUND_NAME + " TEXT,"
				+ COLUMN_NAME + " TEXT"
				+ ");";

			/**
			 * SQL Statement to create the table (version 2).
			 */
			public static final String CREATE_TABLE_V2 =
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
			 * SQL Statement to create the routes table.
			 */
			public static final String CREATE_TABLE_V1 =
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
				COLUMN_HOUR,
				COLUMN_MINUTE,
				COLUMN_DAYS,
				COLUMN_REPEAT,
				COLUMN_USE_NFC,
				COLUMN_VIBRATE,
				COLUMN_VOLUME,
				COLUMN_AUDIO_SOURCE,
				COLUMN_SOUND_TYPE,
				COLUMN_SOUND_PATH,
				COLUMN_SOUND_NAME,
				COLUMN_NAME,
			};

		}

	}

	/**
	 * Execute database tasks in the background.
	 */
	public static class BackgroundService
		extends JobIntentService
	{

		/**
		 * Job ID.
		 */
		public static final int JOB_ID = 1000;

		/**
		 * Add an alarm.
		 */
		public static void addAlarm(Context context, NacAlarm alarm)
		{
			NacDatabase db = new NacDatabase(context);
			NacScheduler scheduler = new NacScheduler(context);
			NacSharedPreferences shared = new NacSharedPreferences(context);
			int id = alarm.getId();

			db.add(alarm);
			db.close();
			scheduler.update(alarm);
			shared.editSnoozeCount(id, 0);
		}

		/**
		 * Delete an alarm.
		 */
		public static void deleteAlarm(Context context, NacAlarm alarm)
		{
			NacDatabase db = new NacDatabase(context);
			NacScheduler scheduler = new NacScheduler(context);
			NacSharedPreferences shared = new NacSharedPreferences(context);
			int id = alarm.getId();

			db.delete(alarm);
			db.close();
			scheduler.cancel(alarm);
			shared.editSnoozeCount(id, 0);
		}

		/**
		 */
		public static void enqueueWork(Context context, Intent work)
		{
			enqueueWork(context, NacDatabase.BackgroundService.class,
				JOB_ID, work);
		}

		/**
		 */
		@Override
		protected void onHandleWork(Intent intent)
		{
			String key = intent.getDataString();

			if (key.equals("swap"))
			{
				NacAlarm[] alarms = NacIntent.getAlarms(intent);

				BackgroundService.swapAlarms(this, alarms[0], alarms[1]);
			}
			else
			{
				NacAlarm alarm = NacIntent.getAlarm(intent);

				if (key.equals("add"))
				{
					BackgroundService.addAlarm(this, alarm);
				}
				else if (key.equals("delete"))
				{
					BackgroundService.deleteAlarm(this, alarm);
				}
				else if (key.equals("update"))
				{
					BackgroundService.updateAlarm(this, alarm);
				}
			}
		}

		/**
		 * Swap two alarms.
		 */
		public static void swapAlarms(Context context, NacAlarm fromAlarm,
			NacAlarm toAlarm)
		{
			NacDatabase db = new NacDatabase(context);
			NacScheduler scheduler = new NacScheduler(context);
			NacSharedPreferences shared = new NacSharedPreferences(context);
			int fromId = fromAlarm.getId();
			int toId = toAlarm.getId();
			int fromSnoozeCount = shared.getSnoozeCount(fromId);
			int toSnoozeCount = shared.getSnoozeCount(toId);

			scheduler.cancel(fromAlarm);
			scheduler.cancel(toAlarm);
			db.swap(fromAlarm, toAlarm);
			db.close();
			scheduler.add(fromAlarm);
			scheduler.add(toAlarm);
			shared.editSnoozeCount(fromId, toSnoozeCount);
			shared.editSnoozeCount(toId, fromSnoozeCount);
		}

		/**
		 * Update an alarm.
		 */
		public static void updateAlarm(Context context, NacAlarm alarm)
		{
			NacDatabase db = new NacDatabase(context);
			NacScheduler scheduler = new NacScheduler(context);

			db.update(alarm);
			db.close();
			scheduler.update(alarm);
		}

	}

}
