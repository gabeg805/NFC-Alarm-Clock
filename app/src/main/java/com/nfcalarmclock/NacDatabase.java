package com.nfcalarmclock;

import android.content.Context;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteException;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * NFC Alarm Clock database.
 */
@SuppressWarnings("UnusedReturnValue")
public class NacDatabase
	extends SQLiteOpenHelper
{

	/**
	 * Database version.
	 */
	public static final int DATABASE_VERSION = 5;

	/**
	 * Database name.
	 */
	public static final String DATABASE_NAME = "NFCAlarms.db";

	/**
	 * The context.
	 */
	private final Context mContext;

	/**
	 * Check if the database was upgraded.
	 */
	private boolean mWasUpgraded;

	/**
	 */
	public NacDatabase(Context context)
	{
		super(context, DATABASE_NAME, null, DATABASE_VERSION);
		this.mContext = context;
		this.mWasUpgraded = false;
	}

	/**
	 * @see #add(SQLiteDatabase, int, NacAlarm)
	 */
	public long add(NacAlarm alarm)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		return this.add(db, alarm);
	}

	/**
	 * @see #add(SQLiteDatabase, int, NacAlarm)
	 */
	public long add(SQLiteDatabase db, NacAlarm alarm)
	{
		return this.add(db, db.getVersion(), alarm);
	}

	/**
	 * Add the alarm to the database with the given version.
	 *
	 * @return The number of rows added. Should normally be 1, if successful.
	 *
	 * @param  db       The SQLite database.
	 * @param  version  The database version number.
	 * @param  alarm    The alarm to add.
	 */
	public long add(SQLiteDatabase db, int version, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		ContentValues cv = this.getContentValues(version, alarm);
		String table = this.getAlarmTable();
		long result;

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
	 * @see #delete(SQLiteDatabase, NacAlarm)
	 */
	public long delete(NacAlarm alarm)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		return this.delete(db, alarm);
	}

	/**
	 * Delete the given alarm from the database.
	 *
	 * @return The number of rows deleted. Should normally be 1, if successful.
	 *
	 * @param  db     The SQLite database.
	 * @param  alarm  The alarm to delete.
	 */
	public long delete(SQLiteDatabase db, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		String table = this.getAlarmTable();
		String where = this.getWhereClause();
		String[] args = this.getWhereArgs(alarm);
		long result;

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
	 * @return True if the database file exists, and False otherwise.
	 */
	public static boolean exists(Context context)
	{
		File file = context.getDatabasePath(DATABASE_NAME);
		return file.exists();
	}

	/**
	 * @see #findActiveAlarm(SQLiteDatabase)
	 */
	public static NacAlarm findActiveAlarm(Context context)
	{
		NacDatabase db = new NacDatabase(context);
		NacAlarm activeAlarm = db.findActiveAlarm();
		db.close();
		return activeAlarm;
	}

	/**
	 * @see #findActiveAlarm(SQLiteDatabase)
	 */
	public NacAlarm findActiveAlarm()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		return this.findActiveAlarm(db);
	}

	/**
	 * Find the alarm that is currently active.
	 * 
	 * @return The alarm that is active.
	 *
	 * @param  db  The SQLite database.
	 */
	public NacAlarm findActiveAlarm(SQLiteDatabase db)
	{
		List<NacAlarm> alarms = this.findActiveAlarms(db);
		return ((alarms != null) && (alarms.size() > 0)) ? alarms.get(0) : null;
	}

	/**
	 * Find the list of alarms that are currently active.
	 * 
	 * @return The list of alarms that are currently active.
	 *
	 * @param  db  The SQLite database.
	 */
	public List<NacAlarm> findActiveAlarms()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		return this.findActiveAlarms(db);
	}

	/**
	 * Find the list of alarms that are currently active.
	 * 
	 * @return The list of alarms that are currently active.
	 *
	 * @param  db  The SQLite database.
	 */
	public List<NacAlarm> findActiveAlarms(SQLiteDatabase db)
	{
		String table = this.getAlarmTable();
		String where = this.getWhereClauseActive();
		String[] whereArgs = this.getWhereArgsActive();
		List<NacAlarm> list = new ArrayList<>();
		Cursor cursor;

		try
		{
			cursor = db.query(table, null, where, whereArgs, null, null, null);
		}
		catch (SQLiteException e)
		{
			return null;
		}

		if (cursor == null)
		{
			return list;
		}

		while (cursor.moveToNext())
		{
			NacAlarm alarm = this.toAlarm(cursor, db.getVersion());
			list.add(alarm);
		}

		cursor.close();
		return list;
	}

	/**
	 * Find the alarm with the given ID.
	 *
	 * @return The alarm that is found.
	 *
	 * @param  id  The alarm ID.
	 */
	public NacAlarm findAlarm(long id)
	{
		SQLiteDatabase db = this.getWritableDatabase();
		String table = this.getAlarmTable();
		String where = this.getWhereClause();
		String[] whereArgs = this.getWhereArgs(id);
		String limit = "1";
		Cursor cursor = db.query(table, null, where, whereArgs, null, null, null,
			limit);
		int version = db.getVersion();
		NacAlarm alarm = null;

		if (cursor == null)
		{
			return null;
		}
		else if (cursor.moveToFirst())
		{
			alarm = this.toAlarm(cursor, version);
		}

		cursor.close();
		return alarm;
	}

	/**
	 * @see #findAlarm(int)
	 */
	public NacAlarm findAlarm(NacAlarm alarm)
	{
		return (alarm != null) ? this.findAlarm(alarm.getId()) : null;
	}

	///**
	// * Find the alarm with the given hour and minute.
	// */
	//public NacAlarm findAlarm(Calendar calendar)
	//{
	//	String hour = String.valueOf(calendar.get(Calendar.HOUR_OF_DAY));
	//	String minute = String.valueOf(calendar.get(Calendar.MINUTE));
	//	NacCalendar.Day day = NacCalendar.Days.toWeekDay(
	//		calendar.get(Calendar.DAY_OF_WEEK));
	//	NacAlarm alarm = null;

	//	String[] whereArgs = new String[] { hour, minute };
	//	String whereClause = Contract.AlarmTable.COLUMN_HOUR + "=? AND "
	//		+ Contract.AlarmTable.COLUMN_MINUTE + "=?";

	//	SQLiteDatabase db = this.getWritableDatabase();
	//	int version = db.getVersion();
	//	String table = this.getAlarmTable();
	//	Cursor cursor = db.query(table, null, whereClause, whereArgs, null, null,
	//		null);

	//	while (cursor.moveToNext())
	//	{
	//		NacAlarm a = this.toAlarm(cursor, version);
	//		if ((a != null) && a.getDays().contains(day))
	//		{
	//			alarm = a;
	//			break;
	//		}
	//	}

	//	cursor.close();
	//	return alarm;
	//}

	/**
	 * @see #findAlarm(int)
	 */
	public static NacAlarm findAlarm(Context context, long id)
	{
		if (context == null)
		{
			return null;
		}

		NacDatabase db = new NacDatabase(context);
		NacAlarm foundAlarm = db.findAlarm(id);
		db.close();
		return foundAlarm;
	}

	/**
	 * @see #findAlarm(Context, int)
	 */
	public static NacAlarm findAlarm(Context context, NacAlarm alarm)
	{
		return (alarm != null) ? NacDatabase.findAlarm(context, alarm.getId()) : null;
	}

	///**
	// * @see #findAlarm(Calendar)
	// */
	//public static NacAlarm findAlarm(Context context, Calendar calendar)
	//{
	//	if ((context == null) || (calendar == null))
	//	{
	//		return null;
	//	}

	//	NacDatabase db = new NacDatabase(context);
	//	NacAlarm foundAlarm = db.findAlarm(calendar);
	//	db.close();
	//	return foundAlarm;
	//}

	/**
	 * @return The alarm table name.
	 */
	@SuppressWarnings("SameReturnValue")
    private String getAlarmTable()
	{
		return Contract.AlarmTable.TABLE_NAME;
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

		switch (version)
		{
			case 5:
				cv.put(Contract.AlarmTable.COLUMN_NFC_TAG, alarm.getNfcTagId());
				cv.put(Contract.AlarmTable.COLUMN_IS_ACTIVE, alarm.isActive());
			case 4:
			default:
				cv.put(Contract.AlarmTable.COLUMN_ID, alarm.getId());
				cv.put(Contract.AlarmTable.COLUMN_ENABLED, alarm.isEnabled());
				cv.put(Contract.AlarmTable.COLUMN_HOUR, alarm.getHour());
				cv.put(Contract.AlarmTable.COLUMN_MINUTE, alarm.getMinute());
				cv.put(Contract.AlarmTable.COLUMN_DAYS, NacCalendar.Days.daysToValue(alarm.getDays()));
				cv.put(Contract.AlarmTable.COLUMN_REPEAT, alarm.shouldRepeat());
				cv.put(Contract.AlarmTable.COLUMN_VIBRATE, alarm.shouldVibrate());
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_PATH, alarm.getMediaPath());
				cv.put(Contract.AlarmTable.COLUMN_NAME, alarm.getName());
				cv.put(Contract.AlarmTable.COLUMN_VOLUME, alarm.getVolume());
				cv.put(Contract.AlarmTable.COLUMN_AUDIO_SOURCE, alarm.getAudioSource());
				cv.put(Contract.AlarmTable.COLUMN_USE_NFC, alarm.shouldUseNfc());
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_TYPE, alarm.getMediaType());
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_NAME, alarm.getMediaTitle());
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
	 * @param  value  The value to convert to a where clause.
	 *
	 * @return Where arguments for the where clause.
	 */
	private String[] getWhereArgs(long value)
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
	 * @return Where arguments for when the alarm is active.
	 */
	private String[] getWhereArgsActive()
	{
		return new String[] {"1"};
	}

	/**
	 * @return The where clause for matching with the alarm ID.
	 */
	private String getWhereClause()
	{
		return Contract.AlarmTable.COLUMN_ID + "=?";
	}

	/**
	 * @return The where clause for matching with the is active column.
	 */
	private String getWhereClauseActive()
	{
		return Contract.AlarmTable.COLUMN_IS_ACTIVE + "=?";
	}

	/**
	 * Refresh the cached database when an update occurs, otherwise, return the
	 * database as you would normally.
	 */
	@Override
	public SQLiteDatabase getWritableDatabase()
	{
		SQLiteDatabase db = super.getWritableDatabase();

		if (this.wasUpgraded())
		{
			db.close();
			db = super.getWritableDatabase();
			this.mWasUpgraded = false;
		}

		return db;
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
		db.execSQL(Contract.AlarmTable.CREATE_TABLE_V5);

		Context context = this.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		NacSharedConstants cons = new NacSharedConstants(context);
		String mediaPath = shared.getMediaPath();
		String mediaTitle = NacMedia.getTitle(context, mediaPath);
		int mediaType = NacMedia.getType(context, mediaPath);
		String name = cons.getExampleName();
		NacAlarm alarm = new NacAlarm.Builder(shared)
			.setId(1)
			.setHour(8)
			.setMinute(0)
			.setDays(shared.getDays())
			.setRepeat(shared.getRepeat())
			.setUseNfc(shared.getUseNfc())
			.setVibrate(shared.getVibrate())
			.setVolume(shared.getVolume())
			.setAudioSource(shared.getAudioSource())
			.setMediaTitle(mediaTitle)
			.setMediaPath(mediaPath)
			.setMediaType(mediaType)
			.setName(name)
			.setNfcTagId("")
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
			case 4:
				db.execSQL(Contract.AlarmTable.CREATE_TABLE_V4);
				break;
			case 5:
			default:
				db.execSQL(Contract.AlarmTable.CREATE_TABLE_V5);
				break;
		}

		for (NacAlarm a : alarms)
		{
			this.add(db, newVersion, a);
		}

		this.mWasUpgraded = true;
	}

	/**
	 * @see #read(SQLiteDatabase, int)
	 */
	public static List<NacAlarm> read(Context context)
	{
		NacDatabase db = new NacDatabase(context);
		List<NacAlarm> alarms = db.read();

		db.close();
		return alarms;
	}

	/**
	 * @see #read(SQLiteDatabase, int)
	 */
	public List<NacAlarm> read()
	{
		SQLiteDatabase db = this.getWritableDatabase();
		int version = db.getVersion();
		return this.read(db, version);
	}

	/**
	 * Read the database and return all the alarms.
	 *
	 * TODO: Add this to a method somewhere. Maybe get alarms from cursor?
	 *
	 * @return All alarms in the database.
	 *
	 * @param  db       The SQLite database.
	 * @param  version  The database version number.
	 */
	public List<NacAlarm> read(SQLiteDatabase db, int version)
	{
		String table = this.getAlarmTable();
		List<NacAlarm> list = new ArrayList<>();
		Cursor cursor;

		try
		{
			cursor = db.query(table, null, null, null, null, null, null);
		}
		catch (SQLiteException e)
		{
			return null;
		}

		if (cursor == null)
		{
			return list;
		}

		while (cursor.moveToNext())
		{
			NacAlarm alarm = this.toAlarm(cursor, version);
			list.add(alarm);
		}

		cursor.close();
		return list;
	}

	/**
	 * Convert a Cursor object to an alarm.
	 *
	 * This assumes the cursor object is already in position to retrieve data.
	 */
	public NacAlarm toAlarm(Cursor cursor, int version)
	{
		if (cursor == null)
		{
			return null;
		}

		Context context = this.getContext();
		NacSharedPreferences shared = new NacSharedPreferences(context);
		NacAlarm.Builder builder = new NacAlarm.Builder(shared);

		switch (version)
		{
			case 5:
				builder.setNfcTagId(cursor.getString(15));
					//.setIsActive(cursor.getInt(16) != 0);
			case 4:
			default:
				builder.setId(cursor.getInt(1))
					.setIsEnabled((cursor.getInt(2) != 0))
					.setHour(cursor.getInt(3))
					.setMinute(cursor.getInt(4))
					.setDays(cursor.getInt(5))
					.setRepeat((cursor.getInt(6) != 0))
					.setUseNfc((cursor.getInt(7) != 0))
					.setVibrate((cursor.getInt(8) != 0))
					.setVolume(cursor.getInt(9))
					.setAudioSource(cursor.getString(10))
					.setMediaType(cursor.getInt(11))
					.setMediaPath(cursor.getString(12))
					.setMediaTitle(cursor.getString(13))
					.setName(cursor.getString(14));
				break;
		}

		return builder.build();
	}

	/**
	 * @see #update(SQLiteDatabase, NacAlarm)
	 */
	public long update(NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		SQLiteDatabase db = this.getWritableDatabase();
		return this.update(db, alarm);
	}

	/**
	 * Update the given list of alarms in the database.
	 *
	 * @return The number of alarms that were updated.
	 *
	 * @param  db     The SQLite database.
	 * @param  alarm  The alarm to update.
	 */
	public long update(SQLiteDatabase db, NacAlarm alarm)
	{
		if (alarm == null)
		{
			return -1;
		}

		int version = db.getVersion();
		String table = this.getAlarmTable();
		ContentValues cv = this.getContentValues(version, alarm);
		String where = this.getWhereClause();
		String[] args = this.getWhereArgs(alarm);
		long result;

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
			public static final String COLUMN_MEDIA_TYPE = "SoundType";

			/**
			 * Path to the media file.
			 */
			public static final String COLUMN_MEDIA_PATH = "SoundPath";

			/**
			 * Title of the media.
			 */
			public static final String COLUMN_MEDIA_NAME = "SoundName";

			/**
			 * Name of the alarm.
			 */
			public static final String COLUMN_NAME = "Name";

			/**
			 * NFC tag.
			 */
			public static final String COLUMN_NFC_TAG = "NfcTag";

			/**
			 * NFC tag.
			 */
			public static final String COLUMN_IS_ACTIVE = "IsActive";

			/**
			 * SQL Statement to create the table (version 5).
			 */
			public static final String CREATE_TABLE_V5 =
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
				+ COLUMN_MEDIA_TYPE + " INTEGER,"
				+ COLUMN_MEDIA_PATH + " TEXT,"
				+ COLUMN_MEDIA_NAME + " TEXT,"
				+ COLUMN_NAME + " TEXT,"
				+ COLUMN_NFC_TAG + " TEXT,"
				+ COLUMN_IS_ACTIVE + " INTEGER"
				+ ");";

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
				+ COLUMN_MEDIA_TYPE + " INTEGER,"
				+ COLUMN_MEDIA_PATH + " TEXT,"
				+ COLUMN_MEDIA_NAME + " TEXT,"
				+ COLUMN_NAME + " TEXT"
				+ ");";

			/**
			 * SQL statement to delete the table
			 */
			public static final String DELETE_TABLE =
				"DROP TABLE IF EXISTS " + TABLE_NAME;

		}

	}

}
