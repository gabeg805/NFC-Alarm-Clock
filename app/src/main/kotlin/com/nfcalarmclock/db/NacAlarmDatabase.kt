package com.nfcalarmclock.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacAlarmDao
import com.nfcalarmclock.alarm.db.NacAlarmTypeConverters
import com.nfcalarmclock.db.NacAlarmDatabase.ClearAllStatisticsMigration
import com.nfcalarmclock.db.NacAlarmDatabase.RemoveUseTtsColumnMigration
import com.nfcalarmclock.db.NacOldDatabase.Companion.read
import com.nfcalarmclock.scheduler.NacScheduler
import com.nfcalarmclock.shared.NacSharedPreferences
import com.nfcalarmclock.statistics.db.NacAlarmCreatedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmCreatedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmDeletedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmDeletedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmDismissedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmDismissedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmMissedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmMissedStatisticDao
import com.nfcalarmclock.statistics.db.NacAlarmSnoozedStatistic
import com.nfcalarmclock.statistics.db.NacAlarmSnoozedStatisticDao
import com.nfcalarmclock.statistics.db.NacStatisticTypeConverters
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import javax.inject.Singleton

/**
 * Store alarms in a Room database.
 */
@Database(version = 13,
	entities = [NacAlarm::class, NacAlarmCreatedStatistic::class,
		NacAlarmDeletedStatistic::class, NacAlarmDismissedStatistic::class,
		NacAlarmMissedStatistic::class, NacAlarmSnoozedStatistic::class],
	autoMigrations = [
		AutoMigration(from = 1,  to = 2),
		AutoMigration(from = 2,  to = 3, spec = ClearAllStatisticsMigration::class),
		AutoMigration(from = 3,  to = 4),
		AutoMigration(from = 4,  to = 5),
		AutoMigration(from = 5,  to = 6),
		AutoMigration(from = 6,  to = 7),
		AutoMigration(from = 7,  to = 8),
		AutoMigration(from = 8,  to = 9),
		AutoMigration(from = 9,  to = 10),
		AutoMigration(from = 10, to = 11, spec = RemoveUseTtsColumnMigration::class),
		AutoMigration(from = 11, to = 12),
		AutoMigration(from = 12, to = 13)]
)
@TypeConverters(NacAlarmTypeConverters::class, NacStatisticTypeConverters::class)
abstract class NacAlarmDatabase
	: RoomDatabase()
{

	/**
	 * Store alarms in the database.
	 */
	abstract fun alarmDao(): NacAlarmDao

	/**
	 * Store created alarm statistics in the database.
	 */
	abstract fun alarmCreatedStatisticDao(): NacAlarmCreatedStatisticDao

	/**
	 * Store deleted alarm statistics in the database.
	 */
	abstract fun alarmDeletedStatisticDao(): NacAlarmDeletedStatisticDao

	/**
	 * Store dismissed alarm statistics in the database.
	 */
	abstract fun alarmDismissedStatisticDao(): NacAlarmDismissedStatisticDao

	/**
	 * Store missed alarm statistics in the database.
	 */
	abstract fun alarmMissedStatisticDao(): NacAlarmMissedStatisticDao

	/**
	 * Store snoozed alarm statistics in the database.
	 */
	abstract fun alarmSnoozedStatisticDao(): NacAlarmSnoozedStatisticDao

	/**
	 * Clear all statistics when auto-migrating.
	 */
	internal class ClearAllStatisticsMigration : AutoMigrationSpec
	{

		/**
		 * Called after migration.
		 */
		override fun onPostMigrate(db: SupportSQLiteDatabase)
		{
			val shared = NacSharedPreferences(context!!)

			shared.editAppStartStatistics(true)
			db.execSQL("DROP TABLE alarm_created_statistic")
			db.execSQL("DROP TABLE alarm_deleted_statistic")
			db.execSQL("DROP TABLE alarm_dismissed_statistic")
			db.execSQL("DROP TABLE alarm_missed_statistic")
			db.execSQL("DROP TABLE alarm_snoozed_statistic")
			db.execSQL("CREATE TABLE IF NOT EXISTS alarm_created_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL)")
			db.execSQL("CREATE TABLE IF NOT EXISTS alarm_deleted_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, hour INTEGER NOT NULL, minute INTEGER NOT NULL, name TEXT DEFAULT '')")
			db.execSQL("CREATE TABLE IF NOT EXISTS alarm_dismissed_statistic (used_nfc INTEGER NOT NULL, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, alarm_id INTEGER, hour INTEGER NOT NULL, minute INTEGER NOT NULL, name TEXT DEFAULT '', FOREIGN KEY(alarm_id) REFERENCES alarm(id) ON UPDATE NO ACTION ON DELETE SET NULL )")
			db.execSQL("CREATE TABLE IF NOT EXISTS alarm_missed_statistic (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, alarm_id INTEGER, hour INTEGER NOT NULL, minute INTEGER NOT NULL, name TEXT DEFAULT '', FOREIGN KEY(alarm_id) REFERENCES alarm(id) ON UPDATE NO ACTION ON DELETE SET NULL )")
			db.execSQL("CREATE TABLE IF NOT EXISTS alarm_snoozed_statistic (duration INTEGER NOT NULL DEFAULT 0, id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, timestamp INTEGER NOT NULL, alarm_id INTEGER, hour INTEGER NOT NULL, minute INTEGER NOT NULL, name TEXT DEFAULT '', FOREIGN KEY(alarm_id) REFERENCES alarm(id) ON UPDATE NO ACTION ON DELETE SET NULL )")
		}

	}

	/**
	 * Remove the "Use TTS" column when auto-migrating.
	 */
	@DeleteColumn(tableName = "alarm", columnName = "should_use_tts")
	internal class RemoveUseTtsColumnMigration : AutoMigrationSpec

	/**
	 * Static stuff.
	 */
	companion object
	{

		/**
		 * Name of the database.
		 */
		private const val DB_NAME = "NfcAlarmClock.db"

		/**
		 * Singleton instance of the database.
		 */
		@SuppressLint("StaticFieldLeak")
		@Volatile
		private var INSTANCE: NacAlarmDatabase? = null

		/**
		 * Application context.
		 *
		 * This will only be used when the Room database is created, in order to
		 * migrate from the old SQLite database to the Room database. Otherwise, this
		 * will be null.
		 */
		@SuppressLint("StaticFieldLeak")
		private var context: Context? = null

		/**
		 * Callback for populating the database, for testing purposes.
		 */
		private val sDatabaseCallback: Callback = object : Callback()
		{

			/**
			 * Called when the database is created
			 */
			override fun onCreate(db: SupportSQLiteDatabase)
			{
				super.onCreate(db)

				// Check if the old database exists
				if (NacOldDatabase.exists(context!!))
				{
					val coroutineScope = CoroutineScope(Dispatchers.IO)

					// Start process to delete the old database
					cancelOldAlarms(context!!)
					migrateOldDatabase(context!!, coroutineScope)
					deleteOldDatabase(context!!)

					// Cancel the coroutine scope
					coroutineScope.cancel()
				}

				// Done with context object. Set it to null
				context = null
			}

			/**
			 * Called when the database is opened
			 */
			override fun onOpen(db: SupportSQLiteDatabase)
			{
				super.onOpen(db)

				// An extra check to make sure that the context object is set to null
				context = null
			}

		}

		/**
		 * Cancel old alarms.
		 *
		 * @param  context  Application context.
		 */
		fun cancelOldAlarms(context: Context)
		{
			// Arbitrary large number
			for (i in 1..399)
			{
				// Cancel old type of alarm
				NacScheduler.cancelOld(context, i)

				// Cancel current type of alarm
				val alarm = NacAlarm.build()
				alarm.id = i.toLong()

				NacScheduler.cancel(context, alarm)
			}
		}

		/**
		 * Delete the old database.
		 */
		protected fun deleteOldDatabase(context: Context)
		{
			// Old database does not exist
			if (!NacOldDatabase.exists(context))
			{
				return
			}

			// Get the path of the old database
			val file = context.getDatabasePath(NacOldDatabase.DATABASE_NAME)

			// Delete the old database
			file.delete()
		}

		/**
		 * Check if the Room database exists or not.
		 *
		 * @param  context  Application context.
		 *
		 * @return True if the Room database file exists, and False otherwise.
		 */
		fun exists(context: Context): Boolean
		{
			val file = context.getDatabasePath(DB_NAME)

			return file.exists()
		}

		/**
		 * Create a static instance of the database.
		 *
		 * @param  context  Application context.
		 *
		 * @return A static instance of the database.
		 */
		fun getInstance(context: Context): NacAlarmDatabase
		{
			return INSTANCE ?:
				synchronized(this)
				{
					val appContext = context.applicationContext
					val instance = databaseBuilder(appContext, NacAlarmDatabase::class.java, DB_NAME)
						.addCallback(sDatabaseCallback)
						//.allowMainThreadQueries()
						//.fallbackToDestructiveMigration()
						.build()

					Companion.context = appContext
					INSTANCE = instance

					instance
				}
		}

		/**
		 * Migrate data from the old database into the new database.
		 */
		protected fun migrateOldDatabase(context: Context, coroutineScope: CoroutineScope)
		{
			// Check if the old database exists
			if (!NacOldDatabase.exists(context))
			{
				return
			}

			val db = INSTANCE
			val dao = db!!.alarmDao()
			val alarms = read(context)

			// Itereate over each alarm
			for (a in alarms!!)
			{
				// Set the alarm ID to 0
				a!!.id = 0

				// Execute the stuff
				coroutineScope.launch {
					dao.insert(a)
				}
			}
		}

	}

}

/**
 * Hilt module to provide attributes from the database.
 */
@InstallIn(SingletonComponent::class)
@Module
class NacAlarmDatabaseModule
{

	/**
	 * Provide the database.
	 */
	@Singleton
	@Provides
	fun provideDatabase(@ApplicationContext context: Context) : NacAlarmDatabase
	{
		return NacAlarmDatabase.getInstance(context)
	}

	/**
	 * Provide the alarm DAO.
	 */
	@Provides
	fun provideAlarmDao(db: NacAlarmDatabase) : NacAlarmDao
	{
		return db.alarmDao()
	}

	/**
	 * Provide the created statistic DAO.
	 */
	@Provides
	fun provideCreatedStatisticDao(db: NacAlarmDatabase) : NacAlarmCreatedStatisticDao
	{
		return db.alarmCreatedStatisticDao()
	}

	/**
	 * Provide the deleted statistic DAO.
	 */
	@Provides
	fun provideDeletedStatisticDao(db: NacAlarmDatabase) : NacAlarmDeletedStatisticDao
	{
		return db.alarmDeletedStatisticDao()
	}

	/**
	 * Provide the dismissed statistic DAO.
	 */
	@Provides
	fun provideDismissedStatisticDao(db: NacAlarmDatabase) : NacAlarmDismissedStatisticDao
	{
		return db.alarmDismissedStatisticDao()
	}

	/**
	 * Provide the missed statistic DAO.
	 */
	@Provides
	fun provideMissedStatisticDao(db: NacAlarmDatabase) : NacAlarmMissedStatisticDao
	{
		return db.alarmMissedStatisticDao()
	}

	/**
	 * Provide the snoozed statistic DAO.
	 */
	@Provides
	fun provideSnoozedStatisticDao(db: NacAlarmDatabase) : NacAlarmSnoozedStatisticDao
	{
		return db.alarmSnoozedStatisticDao()
	}

}