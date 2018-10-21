package com.nfcalarmclock;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

/**
 * Database contract.
 */
public final class NacDatabaseContract
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
	private NacDatabaseContract()
	{
	}

	/**
	 * Define the table contents.
	 */
	public static final class AlarmTable
		implements BaseColumns
	{

		/**
		 * prevent someone from instantiating the table class.
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
		public static final String DEFAULT_SORT_ORDER = COLUMN_HOUR + " ASC";

		/**
		 * The MIME type of {@link #CONTENT_URI} providing rows
		 */
		public static final String CONTENT_TYPE =
			ContentResolver.CURSOR_DIR_BASE_TYPE + "/vnd.com.nfcalarmclock";

		/**
		 * The MIME type of a {@link #CONTENT_URI} single row
		 */
		public static final String CONTENT_ITEM_TYPE =
			ContentResolver.CURSOR_ITEM_BASE_TYPE + "/vnd.com.nfcalarmclock";

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
