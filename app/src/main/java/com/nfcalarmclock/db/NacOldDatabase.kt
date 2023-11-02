package com.nfcalarmclock.db

import android.content.ContentValues
import android.content.Context
import android.database.Cursor
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteException
import android.database.sqlite.SQLiteOpenHelper
import android.provider.BaseColumns
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.media.NacMedia
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.util.NacCalendar

/**
 * NFC Alarm Clock database.
 */
class NacOldDatabase(

	/**
	 * The context.
	 */
	private val context: Context

) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION)
{

	/**
	 * The alarm table.
	 */
	private val alarmTable : String
		get() = Contract.AlarmTable.TABLE_NAME

	/**
	 * Check if the database was upgraded.
	 */
	private var wasUpgraded: Boolean = false

	/**
	 * Where arguments for when the alarm is active.
	 */
	private val whereArgsActive: Array<String>
		get() = arrayOf("1")

	/**
	 * The where clause for matching with the alarm ID.
	 */
	private val whereClause: String
		get() = Contract.AlarmTable.COLUMN_ID + "=?"

	/**
	 * The where clause for matching with the is active column.
	 */
	private val whereClauseActive: String
		get() = Contract.AlarmTable.COLUMN_IS_ACTIVE + "=?"

	/**
	 * @see .add
	 */
	fun add(alarm: NacAlarm?): Long
	{
		val db = this.writableDatabase

		return this.add(db, alarm)
	}

	/**
	 * @see .add
	 */
	fun add(db: SQLiteDatabase, alarm: NacAlarm?): Long
	{
		return this.add(db, db.version, alarm)
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
	fun add(db: SQLiteDatabase, version: Int, alarm: NacAlarm?): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		val cv = getContentValues(version, alarm)
		val result: Long

		// Begin database transaction
		db.beginTransaction()

		// Execute database action
		try
		{
			result = db.insert(alarmTable, null, cv)
			db.setTransactionSuccessful()
		}
		// End database transaction
		finally
		{
			db.endTransaction()
		}

		return result
	}

	/**
	 * @see .delete
	 */
	fun delete(alarm: NacAlarm?): Long
	{
		val db = this.writableDatabase

		return this.delete(db, alarm)
	}

	/**
	 * Delete the given alarm from the database.
	 *
	 * @return The number of rows deleted. Should normally be 1, if successful.
	 *
	 * @param  db     The SQLite database.
	 * @param  alarm  The alarm to delete.
	 */
	fun delete(db: SQLiteDatabase, alarm: NacAlarm?): Long
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return -1
		}

		val args = this.getWhereArgs(alarm)
		val result: Long

		// Begin database transaction
		db.beginTransaction()

		// Execute database action
		try
		{
			result = db.delete(alarmTable, whereClause, args).toLong()
			db.setTransactionSuccessful()
		}
		// End database transaction
		finally
		{
			db.endTransaction()
		}

		return result
	}

	/**
	 * @see .findActiveAlarm
	 */
	fun findActiveAlarm(): NacAlarm?
	{
		val db = this.writableDatabase

		return this.findActiveAlarm(db)
	}

	/**
	 * Find the alarm that is currently active.
	 *
	 * @return The alarm that is active.
	 *
	 * @param  db  The SQLite database.
	 */
	fun findActiveAlarm(db: SQLiteDatabase): NacAlarm?
	{
		val alarms = this.findActiveAlarms(db)

		return if (!alarms.isNullOrEmpty()) alarms[0] else null
	}

	/**
	 * Find the list of alarms that are currently active.
	 *
	 * @return The list of alarms that are currently active.
	 */
	fun findActiveAlarms(): List<NacAlarm?>?
	{
		val db = this.writableDatabase

		return this.findActiveAlarms(db)
	}

	/**
	 * Find the list of alarms that are currently active.
	 *
	 * @return The list of alarms that are currently active.
	 *
	 * @param  db  The SQLite database.
	 */
	fun findActiveAlarms(db: SQLiteDatabase): List<NacAlarm?>?
	{
		val list: MutableList<NacAlarm?> = ArrayList()
		val cursor: Cursor?

		// Query the database for the cursor
		try
		{
			cursor = db.query(alarmTable, null, whereClauseActive, whereArgsActive, null, null, null)
		}
		// Exception occurred. Stop everything
		catch (e: SQLiteException)
		{
			return null
		}

		// Unable to get cursor, return empty list
		if (cursor == null)
		{
			return list
		}

		// Iterate over each item in cursor
		while (cursor.moveToNext())
		{
			// Get the alarm in the cursor
			val alarm = toAlarm(cursor, db.version)

			// Add the alarm to the list
			list.add(alarm)
		}

		// Close the cursor
		cursor.close()

		return list
	}

	/**
	 * Find the alarm with the given ID.
	 *
	 * @return The alarm that is found.
	 *
	 * @param  id  The alarm ID.
	 */
	fun findAlarm(id: Long): NacAlarm?
	{
		val db = this.writableDatabase
		val whereArgs = this.getWhereArgs(id)
		val limit = "1"
		var alarm: NacAlarm? = null
		val cursor = db.query(alarmTable, null, whereClause, whereArgs, null, null, null, limit)

		// Check if cursor is null
		if (cursor == null)
		{
			return null
		}
		// Increment the cursor to the starting location
		else if (cursor.moveToFirst())
		{
			// Get the alarm from the cursor
			alarm = toAlarm(cursor, db.version)
		}

		// Close the cursor
		cursor.close()

		return alarm
	}

	/**
	 * @see .findAlarm
	 */
	fun findAlarm(alarm: NacAlarm?): NacAlarm?
	{
		return if (alarm != null) this.findAlarm(alarm.id) else null
	}

	/**
	 * @return A ContentValues object based on the given alarm.
	 *
	 * Change this every new database version.
	 */
	private fun getContentValues(version: Int, alarm: NacAlarm?): ContentValues?
	{
		// Check if alarm is null
		if (alarm == null)
		{
			return null
		}

		// Create a content values object
		val cv = ContentValues()

		when (version)
		{
			5 ->
			{
				cv.put(Contract.AlarmTable.COLUMN_NFC_TAG, alarm.nfcTagId)
				cv.put(Contract.AlarmTable.COLUMN_IS_ACTIVE, alarm.isActive)
				cv.put(Contract.AlarmTable.COLUMN_ID, alarm.id)
				cv.put(Contract.AlarmTable.COLUMN_ENABLED, alarm.isEnabled)
				cv.put(Contract.AlarmTable.COLUMN_HOUR, alarm.hour)
				cv.put(Contract.AlarmTable.COLUMN_MINUTE, alarm.minute)
				cv.put(Contract.AlarmTable.COLUMN_DAYS, NacCalendar.Days.daysToValue(alarm.days))
				cv.put(Contract.AlarmTable.COLUMN_REPEAT, alarm.shouldRepeat)
				cv.put(Contract.AlarmTable.COLUMN_VIBRATE, alarm.shouldVibrate)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_PATH, alarm.mediaPath)
				cv.put(Contract.AlarmTable.COLUMN_NAME, alarm.name)
				cv.put(Contract.AlarmTable.COLUMN_VOLUME, alarm.volume)
				cv.put(Contract.AlarmTable.COLUMN_AUDIO_SOURCE, alarm.audioSource)
				cv.put(Contract.AlarmTable.COLUMN_USE_NFC, alarm.shouldUseNfc)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_TYPE, alarm.mediaType)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_NAME, alarm.mediaTitle)
			}

			4 ->
			{
				cv.put(Contract.AlarmTable.COLUMN_ID, alarm.id)
				cv.put(Contract.AlarmTable.COLUMN_ENABLED, alarm.isEnabled)
				cv.put(Contract.AlarmTable.COLUMN_HOUR, alarm.hour)
				cv.put(Contract.AlarmTable.COLUMN_MINUTE, alarm.minute)
				cv.put(Contract.AlarmTable.COLUMN_DAYS, NacCalendar.Days.daysToValue(alarm.days))
				cv.put(Contract.AlarmTable.COLUMN_REPEAT, alarm.shouldRepeat)
				cv.put(Contract.AlarmTable.COLUMN_VIBRATE, alarm.shouldVibrate)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_PATH, alarm.mediaPath)
				cv.put(Contract.AlarmTable.COLUMN_NAME, alarm.name)
				cv.put(Contract.AlarmTable.COLUMN_VOLUME, alarm.volume)
				cv.put(Contract.AlarmTable.COLUMN_AUDIO_SOURCE, alarm.audioSource)
				cv.put(Contract.AlarmTable.COLUMN_USE_NFC, alarm.shouldUseNfc)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_TYPE, alarm.mediaType)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_NAME, alarm.mediaTitle)
			}

			else ->
			{
				cv.put(Contract.AlarmTable.COLUMN_ID, alarm.id)
				cv.put(Contract.AlarmTable.COLUMN_ENABLED, alarm.isEnabled)
				cv.put(Contract.AlarmTable.COLUMN_HOUR, alarm.hour)
				cv.put(Contract.AlarmTable.COLUMN_MINUTE, alarm.minute)
				cv.put(Contract.AlarmTable.COLUMN_DAYS, NacCalendar.Days.daysToValue(alarm.days))
				cv.put(Contract.AlarmTable.COLUMN_REPEAT, alarm.shouldRepeat)
				cv.put(Contract.AlarmTable.COLUMN_VIBRATE, alarm.shouldVibrate)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_PATH, alarm.mediaPath)
				cv.put(Contract.AlarmTable.COLUMN_NAME, alarm.name)
				cv.put(Contract.AlarmTable.COLUMN_VOLUME, alarm.volume)
				cv.put(Contract.AlarmTable.COLUMN_AUDIO_SOURCE, alarm.audioSource)
				cv.put(Contract.AlarmTable.COLUMN_USE_NFC, alarm.shouldUseNfc)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_TYPE, alarm.mediaType)
				cv.put(Contract.AlarmTable.COLUMN_MEDIA_NAME, alarm.mediaTitle)
			}
		}

		return cv
	}

	/**
	 * @param  value  The value to convert to a where clause.
	 *
	 * @return Where arguments for the where clause.
	 */
	private fun getWhereArgs(value: Long): Array<String>
	{
		val id = value.toString()

		return arrayOf(id)
	}

	/**
	 * @param  alarm  The alarm to convert to a where clause.
	 *
	 * @return Where arguments for the where clause.
	 */
	private fun getWhereArgs(alarm: NacAlarm?): Array<String>?
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return null
		}

		// Get the alarm ID as a string
		val id = alarm.id.toString()

		return arrayOf(id)
	}

	/**
	 * Refresh the cached database when an update occurs, otherwise, return the
	 * database as you would normally.
	 */
	override fun getWritableDatabase(): SQLiteDatabase
	{
		// Get the database
		var db = super.getWritableDatabase()

		// Check if the database was upgraded
		if (wasUpgraded)
		{
			// Close the database
			db.close()

			// Re-open the database
			db = super.getWritableDatabase()

			// Disable the was upgraded flag
			wasUpgraded = false
		}

		return db
	}

	/**
	 * Create the database for the first time.
	 *
	 *
	 * Add an example alarm when the app is first installed (this is presumed by
	 * the database being created).
	 *
	 *
	 * Change this every new database version.
	 */
	override fun onCreate(db: SQLiteDatabase)
	{
		// Create the database
		db.execSQL(Contract.AlarmTable.CREATE_TABLE_V5)

		val shared = NacSharedPreferences(context)
		val mediaPath = shared.mediaPath
		val mediaTitle = NacMedia.getTitle(context, mediaPath)
		val mediaType = NacMedia.getType(context, mediaPath)
		val name = context.getString(R.string.example_name)

		// Build an alarm
		val alarm = NacAlarm.Builder(shared)
			.setId(1)
			.setHour(8)
			.setMinute(0)
			.setDays(shared.days)
			.setRepeat(shared.repeat)
			.setUseNfc(shared.useNfc)
			.setVibrate(shared.vibrate)
			.setVolume(shared.volume)
			.setAudioSource(shared.audioSource)
			.setMediaTitle(mediaTitle)
			.setMediaPath(mediaPath)
			.setMediaType(mediaType)
			.setName(name)
			.setNfcTagId("")
			.build()

		// Add the alarm to the database
		this.add(db, alarm)
	}

	/**
	 * Downgrade the database.
	 */
	override fun onDowngrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
	{
		onUpgrade(db, oldVersion, newVersion)
	}

	/**
	 * Upgrade the database to the most up-to-date version.
	 *
	 *
	 * Change this every new database version.
	 */
	override fun onUpgrade(db: SQLiteDatabase, oldVersion: Int, newVersion: Int)
	{
		// Get all alarms in the database
		val alarms = this.read(db, oldVersion)

		// Delete the alarm table
		db.execSQL(Contract.AlarmTable.DELETE_TABLE)

		// Determine which new table to create
		when (newVersion)
		{
			4 -> db.execSQL(Contract.AlarmTable.CREATE_TABLE_V4)
			5 -> db.execSQL(Contract.AlarmTable.CREATE_TABLE_V5)
			else -> db.execSQL(Contract.AlarmTable.CREATE_TABLE_V5)
		}

		// Iterate over each alarm
		for (a in alarms!!)
		{
			this.add(db, newVersion, a)
		}

		// Set the flag indicating that an upgrade occurred
		wasUpgraded = true
	}

	/**
	 * @see .read
	 */
	fun read(): List<NacAlarm?>?
	{
		val db = this.writableDatabase
		val version = db.version

		return this.read(db, version)
	}

	/**
	 * Read the database and return all the alarms.
	 *
	 *
	 * TODO: Add this to a method somewhere. Maybe get alarms from cursor?
	 *
	 * @return All alarms in the database.
	 *
	 * @param  db       The SQLite database.
	 * @param  version  The database version number.
	 */
	fun read(db: SQLiteDatabase, version: Int): List<NacAlarm?>?
	{
		val list: MutableList<NacAlarm?> = ArrayList()
		val cursor: Cursor?

		// Query the database for the cursor
		try
		{
			cursor = db.query(alarmTable, null, null, null, null, null, null)
		}
		// Exception occurred. Stop everything
		catch (e: SQLiteException)
		{
			return null
		}

		// Unable to get cursor, return empty list
		if (cursor == null)
		{
			return list
		}

		// Iterate over each item in cursor
		while (cursor.moveToNext())
		{
			// Get the alarm in the cursor
			val alarm = toAlarm(cursor, version)

			// Add the alarm to the list
			list.add(alarm)
		}

		// Close the cursor
		cursor.close()

		return list
	}

	/**
	 * Convert a Cursor object to an alarm.
	 *
	 *
	 * This assumes the cursor object is already in position to retrieve data.
	 */
	fun toAlarm(cursor: Cursor?, version: Int): NacAlarm?
	{
		// Check if cursor is null
		if (cursor == null)
		{
			return null
		}

		// Build an alarm
		val shared = NacSharedPreferences(context)
		val builder = NacAlarm.Builder(shared)

		// Determine which version to use
		when (version)
		{
			5 ->
			{
				builder.setNfcTagId(cursor.getString(15))
				builder.setId(cursor.getInt(1).toLong())
					.setIsEnabled(cursor.getInt(2) != 0)
					.setHour(cursor.getInt(3))
					.setMinute(cursor.getInt(4))
					.setDays(cursor.getInt(5))
					.setRepeat(cursor.getInt(6) != 0)
					.setUseNfc(cursor.getInt(7) != 0)
					.setVibrate(cursor.getInt(8) != 0)
					.setVolume(cursor.getInt(9))
					.setAudioSource(cursor.getString(10))
					.setMediaType(cursor.getInt(11))
					.setMediaPath(cursor.getString(12))
					.setMediaTitle(cursor.getString(13))
					.setName(cursor.getString(14))
			}

			4 -> builder.setId(cursor.getInt(1).toLong())
				.setIsEnabled(cursor.getInt(2) != 0)
				.setHour(cursor.getInt(3))
				.setMinute(cursor.getInt(4))
				.setDays(cursor.getInt(5))
				.setRepeat(cursor.getInt(6) != 0)
				.setUseNfc(cursor.getInt(7) != 0)
				.setVibrate(cursor.getInt(8) != 0)
				.setVolume(cursor.getInt(9))
				.setAudioSource(cursor.getString(10))
				.setMediaType(cursor.getInt(11))
				.setMediaPath(cursor.getString(12))
				.setMediaTitle(cursor.getString(13))
				.setName(cursor.getString(14))

			else -> builder.setId(cursor.getInt(1).toLong())
				.setIsEnabled(cursor.getInt(2) != 0)
				.setHour(cursor.getInt(3))
				.setMinute(cursor.getInt(4))
				.setDays(cursor.getInt(5))
				.setRepeat(cursor.getInt(6) != 0)
				.setUseNfc(cursor.getInt(7) != 0)
				.setVibrate(cursor.getInt(8) != 0)
				.setVolume(cursor.getInt(9))
				.setAudioSource(cursor.getString(10))
				.setMediaType(cursor.getInt(11))
				.setMediaPath(cursor.getString(12))
				.setMediaTitle(cursor.getString(13))
				.setName(cursor.getString(14))
		}

		return builder.build()
	}

	/**
	 * @see .update
	 */
	fun update(alarm: NacAlarm?): Long
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return -1
		}

		val db = this.writableDatabase

		return this.update(db, alarm)
	}

	/**
	 * Update the given list of alarms in the database.
	 *
	 * @return The number of alarms that were updated.
	 *
	 * @param  db     The SQLite database.
	 * @param  alarm  The alarm to update.
	 */
	fun update(db: SQLiteDatabase, alarm: NacAlarm?): Long
	{
		// Check if the alarm is null
		if (alarm == null)
		{
			return -1
		}

		val cv = getContentValues(db.version, alarm)
		val args = this.getWhereArgs(alarm)
		val result: Long

		// Begin database transaction
		db.beginTransaction()

		// Execute database action
		try
		{
			result = db.update(alarmTable, cv, whereClause, args).toLong()
			db.setTransactionSuccessful()
		}
		// End database transaction
		finally
		{
			db.endTransaction()
		}

		return result
	}

	/**
	 * Static stuff.
	 */
	companion object
	{

		/**
		 * Database version.
		 */
		const val DATABASE_VERSION = 5

		/**
		 * Database name.
		 */
		const val DATABASE_NAME = "NFCAlarms.db"

		/**
		 * Database contract.
		 *
		 * Constructor is private to prevent someone from instantiating the contract class.
		 */
		class Contract private constructor()
		{

			/**
			 * Define the table contents.
			 */
			object AlarmTable : BaseColumns
			{

				/**
				 * Table name.
				 */
				const val TABLE_NAME = "NfcAlarms"

				/**
				 * ID of the alarm.
				 */
				const val COLUMN_ID = "Id"

				/**
				 * Enabled indicator.
				 */
				const val COLUMN_ENABLED = "Enabled"

				/**
				 * Hour.
				 */
				const val COLUMN_HOUR = "Hour"

				/**
				 * Minute.
				 */
				const val COLUMN_MINUTE = "Minute"

				/**
				 * Days the alarm is scheduled to run.
				 */
				const val COLUMN_DAYS = "Days"

				/**
				 * Repeat indicator.
				 */
				const val COLUMN_REPEAT = "Repeat"

				/**
				 * Use NFC indicator.
				 */
				const val COLUMN_USE_NFC = "UseNfc"

				/**
				 * Vibrate the phone indicator.
				 */
				const val COLUMN_VIBRATE = "Vibrate"

				/**
				 * Volume level.
				 */
				const val COLUMN_VOLUME = "Volume"

				/**
				 * Volume level.
				 */
				const val COLUMN_AUDIO_SOURCE = "AudioSource"

				/**
				 * Type of media.
				 */
				const val COLUMN_MEDIA_TYPE = "SoundType"

				/**
				 * Path to the media file.
				 */
				const val COLUMN_MEDIA_PATH = "SoundPath"

				/**
				 * Title of the media.
				 */
				const val COLUMN_MEDIA_NAME = "SoundName"

				/**
				 * Name of the alarm.
				 */
				const val COLUMN_NAME = "Name"

				/**
				 * NFC tag.
				 */
				const val COLUMN_NFC_TAG = "NfcTag"

				/**
				 * NFC tag.
				 */
				const val COLUMN_IS_ACTIVE = "IsActive"

				/**
				 * SQL Statement to create the table (version 5).
				 */
				const val CREATE_TABLE_V5 = ("CREATE TABLE " + TABLE_NAME
					+ " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
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
					+ ");")

				/**
				 * SQL Statement to create the table (version 4).
				 */
				const val CREATE_TABLE_V4 = ("CREATE TABLE " + TABLE_NAME
					+ " ("
					+ BaseColumns._ID + " INTEGER PRIMARY KEY,"
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
					+ ");")

				/**
				 * SQL statement to delete the table
				 */
				const val DELETE_TABLE = "DROP TABLE IF EXISTS $TABLE_NAME"

			}

		}

		/**
		 * @return True if the database file exists, and False otherwise.
		 */
		@JvmStatic
		fun exists(context: Context): Boolean
		{
			val file = context.getDatabasePath(DATABASE_NAME)
			return file.exists()
		}

		/**
		 * @see .findActiveAlarm
		 */
		fun findActiveAlarm(context: Context): NacAlarm?
		{
			val db = NacOldDatabase(context)
			val activeAlarm = db.findActiveAlarm()

			db.close()

			return activeAlarm
		}

		/**
		 * @see .findAlarm
		 */
		fun findAlarm(context: Context?, id: Long): NacAlarm?
		{
			// Check if context is null
			if (context == null)
			{
				return null
			}

			// Create the database object
			val db = NacOldDatabase(context)

			// Find the alarm
			val foundAlarm = db.findAlarm(id)

			// Close the database
			db.close()

			return foundAlarm
		}

		/**
		 * @see .findAlarm
		 */
		fun findAlarm(context: Context?, alarm: NacAlarm?): NacAlarm?
		{
			return if (alarm != null) findAlarm(context, alarm.id) else null
		}

		/**
		 * @see .read
		 */
		@JvmStatic
		fun read(context: Context): List<NacAlarm?>?
		{
			// Create the database object
			val db = NacOldDatabase(context)

			// Read the list of all alarms
			val alarms = db.read()

			// Close the database
			db.close()

			return alarms
		}

	}

}