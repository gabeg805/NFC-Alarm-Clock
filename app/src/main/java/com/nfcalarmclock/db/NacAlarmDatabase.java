package com.nfcalarmclock.db;

import android.content.Context;
import androidx.annotation.NonNull;
import androidx.room.AutoMigration;
import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters;
import androidx.sqlite.db.SupportSQLiteDatabase;

import com.nfcalarmclock.alarm.NacAlarm;
import com.nfcalarmclock.alarm.NacAlarmDao;
import com.nfcalarmclock.alarm.NacAlarmTypeConverters;
import com.nfcalarmclock.audio.media.NacMedia;
import com.nfcalarmclock.statistics.NacAlarmCreatedStatistic;
import com.nfcalarmclock.statistics.NacAlarmCreatedStatisticDao;
import com.nfcalarmclock.statistics.NacAlarmDeletedStatistic;
import com.nfcalarmclock.statistics.NacAlarmDeletedStatisticDao;
import com.nfcalarmclock.statistics.NacAlarmDismissedStatistic;
import com.nfcalarmclock.statistics.NacAlarmDismissedStatisticDao;
import com.nfcalarmclock.statistics.NacAlarmMissedStatistic;
import com.nfcalarmclock.statistics.NacAlarmMissedStatisticDao;
import com.nfcalarmclock.statistics.NacAlarmSnoozedStatistic;
import com.nfcalarmclock.statistics.NacAlarmSnoozedStatisticDao;
import com.nfcalarmclock.statistics.NacStatisticTypeConverters;
import com.nfcalarmclock.system.NacCalendar;
import com.nfcalarmclock.system.NacScheduler;

import java.io.File;
import java.util.concurrent.Executors;
import java.util.concurrent.ExecutorService;
import java.util.List;

/**
 * Store alarms in a Room database.
 */
//@Database(version=1, exportSchema=true,
//	entities={NacAlarm.class})
//@TypeConverters({NacAlarmTypeConverters.class})
@Database(version=2, exportSchema=true,
	entities={NacAlarm.class, NacAlarmCreatedStatistic.class,
		NacAlarmDeletedStatistic.class, NacAlarmDismissedStatistic.class,
		NacAlarmMissedStatistic.class, NacAlarmSnoozedStatistic.class},
	autoMigrations={
			@AutoMigration(from=1, to=2)
		})
@TypeConverters({NacAlarmTypeConverters.class,
	NacStatisticTypeConverters.class})
public abstract class NacAlarmDatabase
	extends RoomDatabase
{

	/**
	 * Store alarms in the database.
	 */
	public abstract NacAlarmDao alarmDao();

	/**
	 * Store created alarm statistics in the database.
	 */
	public abstract NacAlarmCreatedStatisticDao alarmCreatedStatisticDao();

	/**
	 * Store deleted alarm statistics in the database.
	 */
	public abstract NacAlarmDeletedStatisticDao alarmDeletedStatisticDao();

	/**
	 * Store dismissed alarm statistics in the database.
	 */
	public abstract NacAlarmDismissedStatisticDao alarmDismissedStatisticDao();

	/**
	 * Store missed alarm statistics in the database.
	 */
	public abstract NacAlarmMissedStatisticDao alarmMissedStatisticDao();

	/**
	 * Store snoozed alarm statistics in the database.
	 */
	public abstract NacAlarmSnoozedStatisticDao alarmSnoozedStatisticDao();

	/**
	 * Name of the database.
	 */
	public static final String DB_NAME = "NfcAlarmClock.db";

	/**
	 * Lock object for the single instance.
	 */
	private static final Object LOCK = new Object();

	/**
	 * Number of executor threads.
	 */
	//private static final int NUMBER_OF_THREADS = 4;

	/**
	 * Executor service.
	 */
	private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
	//private static final ExecutorService EXECUTOR =
	//	Executors.newFixedThreadPool(NUMBER_OF_THREADS);

	/**
	 * Singleton instance of the database.
	 */
	private static NacAlarmDatabase sInstance;

	/**
	 * Application context.
	 *
	 * This will only be used when the Room database is created, in order to
	 * migrate from the old SQLite database to the Room database. Otherwise, this
	 * will be null.
	 */
	private static Context sContext;

	/**
	 * Cancel old alarms.
	 *
	 * @param  context  Application context.
	 */
	public static void cancelOldAlarms(Context context)
	{
		for (int i=1; i < 400; i++)
		{
			NacScheduler.cancel(context, i);
		}
	}

	/**
	 * Delete the old database.
	 */
	protected static void deleteOldDatabase(Context context)
	{
		if (!NacOldDatabase.exists(context))
		{
			return;
		}

		File file = context.getDatabasePath(NacOldDatabase.DATABASE_NAME);
		file.delete();
	}

	/**
	 * Check if the Room database exists or not.
	 *
	 * @param  context  Application context.
	 *
	 * @return True if the Room database file exists, and False otherwise.
	 */
	public static boolean exists(Context context)
	{
		File file = context.getDatabasePath(DB_NAME);
		return file.exists();
	}

	/**
	 * @return The application context.
	 */
	private static Context getContext()
	{
		return sContext;
	}

	/**
	 * @return The application context.
	 */
	public static ExecutorService getExecutor()
	{
		return EXECUTOR;
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
				Context appContext = context.getApplicationContext();
				sInstance = Room.databaseBuilder(appContext, NacAlarmDatabase.class,
					DB_NAME)
					//.allowMainThreadQueries()
					.addCallback(sDatabaseCallback)
					//.fallbackToDestructiveMigration()
					.build();

				sContext = appContext;
			}

			return sInstance;
		}
	}

	/**
	 * Insert the initial alarm into the database.
	 */
	protected static void insertInitialAlarm(Context context)
	{
		NacAlarmDatabase db = getInstance();
		NacAlarmDao dao = db.alarmDao();
		NacAlarm alarm = new NacAlarm.Builder()
			.setId(0)
			.setIsEnabled(true)
			.setHour(8)
			.setMinute(0)
			.setDays(NacCalendar.Days.valueToDays(62))
			.setRepeat(true)
			.setVibrate(true)
			.setUseNfc(false)
			.setNfcTagId("")
			.setMediaType(NacMedia.TYPE_NONE)
			.setMediaPath("")
			.setMediaTitle("")
			.setVolume(75)
			.setAudioSource("Media")
			.setName("Work")
			.build();

		getExecutor().execute(() -> dao.insert(alarm));
		NacScheduler.update(context, alarm);
	}

	/**
	 * Migrate data from the old database into the new database.
	 */
	protected static void migrateOldDatabase(Context context)
	{
		if (!NacOldDatabase.exists(context))
		{
			return;
		}

		NacAlarmDatabase db = getInstance();
		NacAlarmDao dao = db.alarmDao();
		List<NacAlarm> alarms = NacOldDatabase.read(context);

		for (NacAlarm a : alarms)
		{
			a.setId(0);
			getExecutor().execute(() -> dao.insert(a));
			NacScheduler.update(context, a);
		}
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

			Context context = getContext();

			if (NacOldDatabase.exists(context))
			{
				cancelOldAlarms(context);
				migrateOldDatabase(context);
				deleteOldDatabase(context);
			}
			else
			{
				insertInitialAlarm(context);
			}

			sContext = null;
		}

		/**
		 */
		@Override
		public void onOpen(@NonNull SupportSQLiteDatabase db)
		{
			super.onOpen(db);

			sContext = null;
		}

	};

}
