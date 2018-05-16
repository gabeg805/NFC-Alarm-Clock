package com.nfcalarmclock;

import android.content.ContentResolver;
import android.net.Uri;
import android.provider.BaseColumns;

public final class AlarmDatabaseContract
{

    public static final String AUTHORITY = "com.nfcalarmclock";
    public static final String SCHEME = "content://";
    public static final String SLASH = "/";
    public static final String DATABASE_NAME = "NFCAlarms.db";
    public static final int DATABASE_VERSION = 1;

    /* @details To prevent someone from accidentally instantiating the contract
     *          class, make the constructor private.
     */
    private AlarmDatabaseContract()
    {
    }

    /* Inner class that defines the table contents */
    public static final class AlarmTable
        implements BaseColumns
    {
        private AlarmTable()
        {
        }

        public static final String TABLE_NAME = "NfcAlarms";

        public static final String COLUMN_ID = "Id";
        public static final String COLUMN_ENABLED = "Enabled";
        public static final String COLUMN_HOUR = "Hour";
        public static final String COLUMN_MINUTE = "Minute";
        public static final String COLUMN_NFCTAG = "NfcTag";
        public static final String COLUMN_MUSIC = "Music";

        /*
         * URI Definitions
         */

        /**
         * The content style URI
         */
        public static final Uri CONTENT_URI = Uri.parse(SCHEME + AUTHORITY
                                                        + SLASH + TABLE_NAME);

        /**
         * The content URI base for a single row. An ID must be appended.
         */
        public static final Uri CONTENT_ID_URI_BASE = Uri.parse(SCHEME
                                                                + AUTHORITY
                                                                + SLASH
                                                                + TABLE_NAME
                                                                + SLASH);

        /**
         * The default sort order for this table
         */
        public static final String DEFAULT_SORT_ORDER = COLUMN_ID + " ASC";

        /*
         * MIME type definitions
         */

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
         * SQL Statement to create the routes table
         */
        public static final String CREATE_TABLE =
            "CREATE TABLE " + TABLE_NAME
            + " ("
            + _ID + " INTEGER PRIMARY KEY,"
            + COLUMN_ID + " TEXT,"
            + COLUMN_ENABLED + " TEXT,"
            + COLUMN_HOUR + " TEXT,"
            + COLUMN_MINUTE + " TEXT,"
            + COLUMN_NFCTAG + " TEXT," 
            + COLUMN_MUSIC + " TEXT"
            + ");";

        /**
         * SQL statement to delete the table
         */
        public static final String DELETE_TABLE =
            "DROP TABLE IF EXISTS " + TABLE_NAME;

        /**
         * Array of all the columns. Makes for cleaner code
         */
        public static final String[] KEY_ARRAY = {
            COLUMN_ID,
            COLUMN_ENABLED,
            COLUMN_HOUR,
            COLUMN_MINUTE,
            COLUMN_NFCTAG,
            COLUMN_MUSIC
        };
    }

}
