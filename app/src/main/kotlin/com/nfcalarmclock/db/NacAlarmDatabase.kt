package com.nfcalarmclock.db

import android.annotation.SuppressLint
import android.content.Context
import androidx.lifecycle.LifecycleCoroutineScope
import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.DeleteColumn
import androidx.room.Room.databaseBuilder
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import androidx.room.migration.AutoMigrationSpec
import androidx.sqlite.db.SupportSQLiteDatabase
import com.nfcalarmclock.R
import com.nfcalarmclock.alarm.db.NacAlarm
import com.nfcalarmclock.alarm.db.NacAlarmDao
import com.nfcalarmclock.alarm.db.NacAlarmTypeConverters
import com.nfcalarmclock.db.NacAlarmDatabase.AddAutoDismissAndSnoozeSettingsToAllAlarmsMigration
import com.nfcalarmclock.db.NacAlarmDatabase.ChangeFlashlightOnOffDurationTypeToStringMigration
import com.nfcalarmclock.db.NacAlarmDatabase.ClearAllStatisticsMigration
import com.nfcalarmclock.db.NacAlarmDatabase.ClearNfcTagTableMigration
import com.nfcalarmclock.db.NacAlarmDatabase.DropNfcTagTableMigration
import com.nfcalarmclock.db.NacAlarmDatabase.RemoveUseTtsColumnMigration
import com.nfcalarmclock.db.NacOldDatabase.Companion.read
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTag
import com.nfcalarmclock.alarm.options.nfc.db.NacNfcTagDao
import com.nfcalarmclock.system.scheduler.NacScheduler
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
import com.nfcalarmclock.util.NacUtility
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import java.io.File
import java.lang.IllegalStateException
import javax.inject.Singleton

/**
 * Store alarms in a Room database.
 */
@Database(version = 28,
	entities = [
		NacAlarm::class,
		NacAlarmCreatedStatistic::class,
		NacAlarmDeletedStatistic::class,
		NacAlarmDismissedStatistic::class,
		NacAlarmMissedStatistic::class,
		NacAlarmSnoozedStatistic::class,
		NacNfcTag::class],
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
		AutoMigration(from = 12, to = 13),
		AutoMigration(from = 13, to = 14),
		AutoMigration(from = 14, to = 15),
		AutoMigration(from = 15, to = 16),
		AutoMigration(from = 16, to = 17, spec = ClearNfcTagTableMigration::class),
		AutoMigration(from = 17, to = 18, spec = DropNfcTagTableMigration::class),
		AutoMigration(from = 18, to = 19, spec = ClearNfcTagTableMigration::class),
		AutoMigration(from = 19, to = 20, spec = AddAutoDismissAndSnoozeSettingsToAllAlarmsMigration::class),
		AutoMigration(from = 20, to = 21),
		AutoMigration(from = 21, to = 22, spec = ChangeFlashlightOnOffDurationTypeToStringMigration::class),
		AutoMigration(from = 22, to = 23),
		AutoMigration(from = 23, to = 24),
		AutoMigration(from = 24, to = 25),
		AutoMigration(from = 25, to = 26),
		AutoMigration(from = 26, to = 27),
		AutoMigration(from = 27, to = 28)]
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
	 * Store NFC tags in the database.
	 */
	abstract fun nfcTagDao(): NacNfcTagDao

	/**
	 * Added default snooze and auto dismiss settings to all alarms, since they will now
	 * be able to be changed on a per alarm basis.
	 */
	internal class AddAutoDismissAndSnoozeSettingsToAllAlarmsMigration : AutoMigrationSpec
	{
		override fun onPostMigrate(db: SupportSQLiteDatabase)
		{
			val sharedPreferences =
				try
				{
					// Get the shared preferences
					NacSharedPreferences(context!!)
				}
				catch (e: IllegalStateException)
				{
					null
				}

			// Get default auto dismiss and snooze values
			val autoDismissTime = sharedPreferences?.oldAutoDismissTime ?: 15
			val snoozeDuration = sharedPreferences?.oldSnoozeDurationValue ?: 5
			val maxSnoozes = sharedPreferences?.oldMaxSnoozeValue ?: -1
			val easySnooze = if (sharedPreferences?.easySnooze == true) 1 else 0

			// Add default values to all alarms
			db.execSQL("UPDATE alarm SET auto_dismiss_time=$autoDismissTime")
			db.execSQL("UPDATE alarm SET snooze_duration=$snoozeDuration")
			db.execSQL("UPDATE alarm SET max_snooze=$maxSnoozes")
			db.execSQL("UPDATE alarm SET should_use_easy_snooze=$easySnooze")
		}
	}

	/**
	 * Change the type of the flashlight on/off duration from Int to String.
	 */
	internal class ChangeFlashlightOnOffDurationTypeToStringMigration : AutoMigrationSpec
	{
		override fun onPostMigrate(db: SupportSQLiteDatabase)
		{
			// Create new table
			db.execSQL("CREATE TABLE IF NOT EXISTS alarm_new (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `is_active` INTEGER NOT NULL, `time_active` INTEGER NOT NULL, `snooze_count` INTEGER NOT NULL, `is_enabled` INTEGER NOT NULL, `hour` INTEGER NOT NULL, `minute` INTEGER NOT NULL, `snooze_hour` INTEGER NOT NULL, `snooze_minute` INTEGER NOT NULL, `days` INTEGER NOT NULL, `should_repeat` INTEGER NOT NULL, `should_vibrate` INTEGER NOT NULL, `should_use_nfc` INTEGER NOT NULL, `should_use_flashlight` INTEGER NOT NULL DEFAULT 0, `flashlight_strength_level` INTEGER NOT NULL DEFAULT 0, `flashlight_on_duration` TEXT NOT NULL DEFAULT '0', `flashlight_off_duration` TEXT NOT NULL DEFAULT '0', `nfc_tag_id` TEXT NOT NULL, `media_path` TEXT NOT NULL, `media_title` TEXT NOT NULL, `media_type` INTEGER NOT NULL, `should_shuffle_media` INTEGER NOT NULL DEFAULT 0, `should_recursively_play_media` INTEGER NOT NULL DEFAULT 0, `volume` INTEGER NOT NULL, `audio_source` TEXT NOT NULL, `name` TEXT NOT NULL, `should_say_current_time` INTEGER NOT NULL DEFAULT 0, `should_say_alarm_name` INTEGER NOT NULL DEFAULT 0, `tts_frequency` INTEGER NOT NULL, `should_gradually_increase_volume` INTEGER NOT NULL, `gradually_increase_volume_wait_time` INTEGER NOT NULL DEFAULT 5, `should_restrict_volume` INTEGER NOT NULL, `auto_dismiss_time` INTEGER NOT NULL DEFAULT 0, `should_dismiss_early` INTEGER NOT NULL, `dismiss_early_time` INTEGER NOT NULL, `time_of_dismiss_early_alarm` INTEGER NOT NULL, `snooze_duration` INTEGER NOT NULL DEFAULT 0, `max_snooze` INTEGER NOT NULL DEFAULT 0, `should_use_easy_snooze` INTEGER NOT NULL DEFAULT 0, `should_show_reminder` INTEGER NOT NULL DEFAULT 0, `time_to_show_reminder` INTEGER NOT NULL DEFAULT 5, `reminder_frequency` INTEGER NOT NULL DEFAULT 0, `should_use_tts_for_reminder` INTEGER NOT NULL DEFAULT 0)")

			// Copy the data
			db.execSQL("INSERT INTO alarm_new (id, is_active, time_active, snooze_count, is_enabled, hour, minute, snooze_hour, snooze_minute, days, should_repeat, should_vibrate, should_use_nfc, should_use_flashlight, flashlight_strength_level, flashlight_on_duration, flashlight_off_duration, nfc_tag_id, media_path, media_title, media_type, should_shuffle_media, should_recursively_play_media, volume, audio_source, name, should_say_current_time, should_say_alarm_name, tts_frequency, should_gradually_increase_volume, gradually_increase_volume_wait_time, should_restrict_volume, auto_dismiss_time, should_dismiss_early, dismiss_early_time, time_of_dismiss_early_alarm, snooze_duration, max_snooze, should_use_easy_snooze, should_show_reminder, time_to_show_reminder, reminder_frequency, should_use_tts_for_reminder) SELECT id, is_active, time_active, snooze_count, is_enabled, hour, minute, snooze_hour, snooze_minute, days, should_repeat, should_vibrate, should_use_nfc, should_use_flashlight, flashlight_strength_level, flashlight_on_duration, flashlight_off_duration, nfc_tag_id, media_path, media_title, media_type, should_shuffle_media, should_recursively_play_media, volume, audio_source, name, should_say_current_time, should_say_alarm_name, tts_frequency, should_gradually_increase_volume, gradually_increase_volume_wait_time, should_restrict_volume, auto_dismiss_time, should_dismiss_early, dismiss_early_time, time_of_dismiss_early_alarm, snooze_duration, max_snooze, should_use_easy_snooze, should_show_reminder, time_to_show_reminder, reminder_frequency, should_use_tts_for_reminder FROM alarm")

			// Drop the old table
			db.execSQL("DROP TABLE alarm")

			// Rename the new table to the old name
			db.execSQL("ALTER TABLE alarm_new RENAME TO alarm")

		}
	}

	/**
	 * Clear all statistics when auto-migrating.
	 */
	internal class ClearAllStatisticsMigration : AutoMigrationSpec
	{
		override fun onPostMigrate(db: SupportSQLiteDatabase)
		{
			val sharedPreferences = NacSharedPreferences(context!!)

			sharedPreferences.appStartStatistics = true
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
	 * Delete everything in the NFC tag table if it exists when auto-migrating.
	 */
	internal class ClearNfcTagTableMigration: AutoMigrationSpec
	{
		override fun onPostMigrate(db: SupportSQLiteDatabase)
		{
			db.execSQL("DELETE FROM nfc_tag")
		}
	}

	/**
	 * Drop the NFC tag table if it exists when auto-migrating.
	 */
	internal class DropNfcTagTableMigration: AutoMigrationSpec
	{
		override fun onPostMigrate(db: SupportSQLiteDatabase)
		{
			db.execSQL("DROP TABLE IF EXISTS nfc_tag")
			db.execSQL("CREATE TABLE IF NOT EXISTS `nfc_tag` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `name` TEXT NOT NULL, `nfc_id` TEXT NOT NULL)")
			db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_nfc_tag_nfc_id` ON `nfc_tag` (`nfc_id`)")
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
		const val DB_NAME = "NfcAlarmClock.db"

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
		 * Close the database.
		 */
		fun close(context: Context)
		{
			// Close the database
			getInstance(context).close()

			// Clear the instance
			INSTANCE = null
		}

		/**
		 * Copy alarms from another database.
		 */
		private suspend fun copyAlarmsFromDb(
			db: NacAlarmDatabase,
			importDb: NacAlarmDatabase
		)
		{
			// Get the dao
			val alarmDao = db.alarmDao()
			val allAlarms = alarmDao.getAllAlarms()

			// Copy all the alarms
			importDb.alarmDao().getAllAlarms().forEach { a ->

				// Make sure none of the alarms in the database already match the
				// one that will be inserted
				if (allAlarms.all { !it.fuzzyEquals(a) })
				{
					a.id = 0
					alarmDao.insert(a)
				}

			}
		}

		/**
		 * Copy created statistics from another database.
		 */
		private suspend fun copyCreatedStatisticsFromDb(
			db: NacAlarmDatabase,
			importDb: NacAlarmDatabase
		)
		{
			// Get the dao and all the stats
			val dao = db.alarmCreatedStatisticDao()
			val allStats = dao.getAll()

			// Copy created statistics
			importDb.alarmCreatedStatisticDao().getAll().forEach { stat ->

				// Make sure none of the stats in the database already match the
				// one that will be inserted
				if (allStats.all { !it.equalsExceptId(stat) })
				{
					stat.id = 0
					dao.insert(stat)
				}

			}
		}

		/**
		 * Copy deleted statistics from another database.
		 */
		private suspend fun copyDeletedStatisticsFromDb(
			db: NacAlarmDatabase,
			importDb: NacAlarmDatabase
		)
		{
			// Get the dao and all the stats
			val dao = db.alarmDeletedStatisticDao()
			val allStats = dao.getAll()

			// Copy deleted statistics
			importDb.alarmDeletedStatisticDao().getAll().forEach { stat ->

				// Make sure none of the stats in the database already match the
				// one that will be inserted
				if (allStats.all { !it.equalsExceptId(stat) })
				{
					stat.id = 0
					dao.insert(stat)
				}

			}
		}

		/**
		 * Copy dismissed statistics from another database.
		 */
		private suspend fun copyDismissedStatisticsFromDb(
			db: NacAlarmDatabase,
			importDb: NacAlarmDatabase
		)
		{
			// Get the dao and all the stats
			val dao = db.alarmDismissedStatisticDao()
			val allStats = dao.getAll()

			// Copy dismissed statistics
			importDb.alarmDismissedStatisticDao().getAll().forEach { stat ->

				// Make sure none of the stats in the database already match the
				// one that will be inserted
				if (allStats.all { !it.equalsExceptId(stat) })
				{
					stat.id = 0
					dao.insert(stat)
				}

			}
		}

		/**
		 * Copy data from another database.
		 */
		fun copyFromDb(
			context: Context,
			dbFile: File,
			lifecycleScope: LifecycleCoroutineScope)
		{
			// Open the main app database
			val db = getInstance(context)

			// Open the imported database file
			val importDb = databaseBuilder(
				context.applicationContext,
				NacAlarmDatabase::class.java,
				dbFile.path)
				.build()

			lifecycleScope.launch {

				// Copy all the alarms
				copyAlarmsFromDb(db, importDb)

				// Copy created statistics
				copyCreatedStatisticsFromDb(db, importDb)

				// Copy deleted statistics
				copyDeletedStatisticsFromDb(db, importDb)

				// Copy dismissed statistics
				copyDismissedStatisticsFromDb(db, importDb)

				// Copy missed statistics
				copyMissedStatisticsFromDb(db, importDb)

				// Copy snoozed statistics
				copySnoozedStatisticsFromDb(db, importDb)

				// Copy NFC tags
				copyNfcTagsFromDb(db, importDb)

				// Close the import database
				importDb.close()

				// Show success message
				NacUtility.quickToast(context, R.string.message_import_completed)

			}
		}

		/**
		 * Copy missed statistics from another database.
		 */
		private suspend fun copyMissedStatisticsFromDb(
			db: NacAlarmDatabase,
			importDb: NacAlarmDatabase
		)
		{
			// Get the dao and all the stats
			val dao = db.alarmMissedStatisticDao()
			val allStats = dao.getAll()

			// Copy missed statistics
			importDb.alarmMissedStatisticDao().getAll().forEach { stat ->

				// Make sure none of the stats in the database already match the
				// one that will be inserted
				if (allStats.all { !it.equalsExceptId(stat) })
				{
					stat.id = 0
					dao.insert(stat)
				}

			}
		}

		/**
		 * Copy NFC tags from another database.
		 */
		private suspend fun copyNfcTagsFromDb(
			db: NacAlarmDatabase,
			importDb: NacAlarmDatabase
		)
		{
			// Get the dao and all the stats
			val dao = db.nfcTagDao()
			val allNfcTags = dao.getAllNfcTags()

			// Copy NFC tags
			importDb.nfcTagDao().getAllNfcTags().forEach { tag ->

				// Make sure none of the stats in the database already match the
				// one that will be inserted
				if (allNfcTags.all { !it.equalsExceptId(tag) })
				{
					tag.id = 0
					dao.insert(tag)
				}

			}
		}

		/**
		 * Copy snoozed statistics from another database.
		 */
		private suspend fun copySnoozedStatisticsFromDb(
			db: NacAlarmDatabase,
			importDb: NacAlarmDatabase
		)
		{
			// Get the dao and all the stats
			val dao = db.alarmSnoozedStatisticDao()
			val allStats = dao.getAll()

			// Copy snoozed statistics
			importDb.alarmSnoozedStatisticDao().getAll().forEach { stat ->

				// Make sure none of the stats in the database already match the
				// one that will be inserted
				if (allStats.all { !it.equalsExceptId(stat) })
				{
					stat.id = 0
					dao.insert(stat)
				}

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

			// Iterate over each alarm
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

	/**
	 * Provide the NFC tag DAO.
	 */
	@Provides
	fun provideNfcTagDao(db: NacAlarmDatabase) : NacNfcTagDao
	{
		return db.nfcTagDao()
	}

}