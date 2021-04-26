package com.nfcalarmclock;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Store alarms in a Room database.
 */
@Database(entities={NacAlarm.class}, version=1, exportSchema=false)
@TypeConverters({NacAlarmTypeConverters.class})
public abstract class NacAlarmDatabase
	extends RoomDatabase
{

	/**
	 * Listener for when the database is created.
	 */
	public interface OnDatabaseCreatedListener
	{
		public void onDatabaseCreated(NacAlarmDatabase db);
	}

	/**
	 * Store alarms in the datbase.
	 */
	public abstract NacAlarmDao alarmDao();

	/**
	 * Name of the database.
	 */
	public static final String DB_NAME = "NfcAlarmClock.db";

	/**
	 * Lock object for the single instance.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Singleton instance of the database.
	 */
	private static NacAlarmDatabase sInstance;

	/**
	 * Flag indicating of the database was created for the first time or not.
	 */
	private static boolean sWasCreated;

	/**
	 */
	private static List<OnDatabaseCreatedListener> sOnDatabaseCreatedListeners;

	/**
	 */
	public static void addOnDatabaseCreatedListener(OnDatabaseCreatedListener listener)
	{
		if (listener != null)
		{
			getOnDatabaseCreatedListeners().add(listener);
		}
	}

	/**
	 * Call the list of listeners when the database is created.
	 */
	public static void callOnDatabaseCreatedListeners()
	{
		for (OnDatabaseCreatedListener listener : getOnDatabaseCreatedListeners())
		{
			listener.onDatabaseCreated(getInstance());
		}
	}

	/**
	 * @return True if the database file exists, and False otherwise.
	 */
	public static boolean exists(Context context)
	{
		File file = context.getDatabasePath(DB_NAME);
		return file.exists();
	}

	/**
	 * @return A static instance of the database.
	 */
	public static NacAlarmDatabase getInstance()
	{
		return sInstance;
	}

	/**
	 * Create a static instance of the database.
	 *
	 * @param  context  Application context.
	 *
	 * @return A static instance of the database.
	 */
	public static NacAlarmDatabase getInstance(Context context)
	{
		synchronized (LOCK)
		{
			if (sInstance == null)
			{
				NacUtility.printf("Instantiating the new room database!");
				Context appContext = context.getApplicationContext();
				sInstance = Room.databaseBuilder(appContext, NacAlarmDatabase.class,
					DB_NAME)
					//.allowMainThreadQueries()
					.addCallback(sDatabaseCallback)
					.build();

				sWasCreated = false;
				sOnDatabaseCreatedListeners = new ArrayList<>();
			}

			return sInstance;
		}
	}

	/**
	 * Create a static instance of the database.
	 *
	 * @param  context  Application context.
	 *
	 * @return A static instance of the database.
	 */
	public static NacAlarmDatabase getInstance(Context context, OnDatabaseCreatedListener listener)
	{
		synchronized (LOCK)
		{
			if (sInstance == null)
			{
				NacUtility.printf("Instantiating the new room database!");
				Context appContext = context.getApplicationContext();
				sInstance = Room.databaseBuilder(appContext, NacAlarmDatabase.class,
					DB_NAME)
					//.allowMainThreadQueries()
					.addCallback(sDatabaseCallback)
					.build();

				sWasCreated = false;
				sOnDatabaseCreatedListeners = new ArrayList<>();
			}

			addOnDatabaseCreatedListener(listener);
			return sInstance;
		}
	}

	/**
	 * @return A list of listeners to call when the database is created.
	 */
	protected static List<OnDatabaseCreatedListener> getOnDatabaseCreatedListeners()
	{
		return sOnDatabaseCreatedListeners;
	}

	/**
	 * @return True if the database was created for the first time, and False
	 *     otherwise.
	 */
	public static boolean wasCreated()
	{
		return sWasCreated;
	}

	/**
	 * Callback for populating the database, for testing purposes.
	 */
	private static RoomDatabase.Callback sDatabaseCallback =
		new RoomDatabase.Callback()
	{

		/**
		 */
		@Override
		public void onCreate(@NonNull SupportSQLiteDatabase db)
		{
			super.onCreate(db);

			NacUtility.printf("New room database was created!");
			sWasCreated = true;

			callOnDatabaseCreatedListeners();
		}

	};

}
