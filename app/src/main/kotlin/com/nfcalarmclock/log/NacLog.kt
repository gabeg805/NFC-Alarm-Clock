package com.nfcalarmclock.log

import android.content.Context
import com.nfcalarmclock.shared.NacSharedPreferences
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.logging.FileHandler
import java.util.logging.Formatter
import java.util.logging.Level
import java.util.logging.LogRecord
import java.util.logging.Logger

/**
 * Logger to log to a file.
 */
object NacLog
{

	/**
	 * Global tag.
	 */
	private const val GLOBAL_TAG = "NfcAlarmClock"

	/**
	 * Whether logging should be done or not.
	 */
	var shouldWriteToLog: Boolean = true

	/**
	 * File logger.
	 */
	private var fileLogger: Logger? = null

	/**
	 * Information on where this log message was called.
	 */
	private val callerInfo: String
		get()
		{
			// Get the current stack trace
			val stackTrace = Thread.currentThread().stackTrace

			// Find the index where this class is being used
			val index = stackTrace.indexOfFirst { it.className == NacLog::class.java.name }

			// In a method so get the class name and method, such as:
			// NacMainActivity:onCreate
			return if ((index > 0) && (index+3 in stackTrace.indices))
			{
				val element = stackTrace[index+3]
				val className = element.className.substringAfterLast('.')
				"${className.padEnd(40, ' ').substring(0, 40)}${element.methodName.padEnd(30, ' ').substring(0, 30)}"
			}
			// Default to returning the global tag when the stack has not been setup yet
			else
			{
				GLOBAL_TAG
			}
		}

	/**
	 * Close the logger.
	 */
	fun close() {
		try
		{
			// Flush the buffer to write it to the log and then close the logger
			fileLogger?.handlers?.forEach { handler ->
				handler.flush()
				handler.close()
			}
		}
		catch (_: Exception) {}
	}

	/**
	 * Debug log message.
	 */
	fun d(message: String)
	{
		// Do not log
		if (!shouldWriteToLog)
		{
			return
		}

		// Log to file
		logToFile(Level.FINE, "$callerInfo  $message")
	}

	/**
	 * Error log message.
	 */
	fun e(message: String, throwable: Throwable? = null)
	{
		// Do not log
		if (!shouldWriteToLog)
		{
			return
		}


		// Log to file
		logToFile(Level.SEVERE, "$callerInfo  $message", throwable)
	}

	/**
	 * Info log message.
	 */
	fun i(message: String, throwable: Throwable? = null)
	{
		// Do not log
		if (!shouldWriteToLog)
		{
			return
		}


		// Log to file
		logToFile(Level.INFO, "$callerInfo  $message", throwable)
	}

	/**
	 * Get the log directory.
	 *
	 * The directory will be in the private app dir, e.g.:
	 *
	 * /data/user/0/com.nfcalarmclock/files/logs/
	 *
	 * @return The log directory.
	 */
	fun getDirectory(context: Context): File
	{
		return File(context.filesDir, "logs")

	}

	/**
	 * Initialize logger.
	 */
	fun init(context: Context, sharedPreferences: NacSharedPreferences)
	{
		// Set the write flag
		shouldWriteToLog = sharedPreferences.shouldWriteToLog

		// Do not initialize log
		if (!shouldWriteToLog)
		{
			return
		}

		// Proceed with initializing the log
		try
		{
			// Log directory in private app dir
			val logDirectory = getDirectory(context)

			// Create directory if it does not exist
			if (!logDirectory.exists())
			{
				logDirectory.mkdirs()
			}

			// Create the file handler. A rotating file handler of 3 files, each 1MB in size
			val logPattern = "${logDirectory.absolutePath}/app.%g.log"
			val limitInBytes = 1 * 1024 * 1024
			val fileCount = 3
			val append = true
			val fileHandler = FileHandler(logPattern, limitInBytes, fileCount, append)
				.apply {
					formatter = object : Formatter() {

						/**
						 * Date format.
						 */
						private val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault())

						/**
						 * Format of each message.
						 */
						override fun format(record: LogRecord): String
						{
							// Message attributes
							val timestamp = dateFormat.format(Date(record.millis))
							val level = record.level.name
							val message = record.message

							// Append the stack trace if an exception is attached
							val exceptionMessage = record.thrown
								?.let { throwable ->
									"\n" + android.util.Log.getStackTraceString(throwable)
								} ?: ""

							// Example message:
							// 2026-08-28 18:12:34.567 FINE NacMainActivity onCreate Example message
							return "$timestamp  ${level.padEnd(7, ' ')}  $message$exceptionMessage\n"
						}

					}
				}

			// Initialize the logger
			fileLogger = Logger.getLogger("AppFileLogger")
				.apply {
					level = Level.ALL
					addHandler(fileHandler)
					useParentHandlers = false
				}
		}
		catch (_: Exception) {}
	}

	/**
	 * Log to file.
	 */
	private fun logToFile(
		level: Level,
		formattedMessage: String,
		throwable: Throwable? = null
	)
	{
		fileLogger?.let { logger ->
			// Exception message
			if (throwable != null)
			{
				logger.log(level, formattedMessage, throwable)
			}
			// Normal message
			else
			{
				logger.log(level, formattedMessage)
			}
		}
	}

	/**
	 * Warning log message.
	 */
	fun w(message: String, throwable: Throwable? = null)
	{
		// Do not log
		if (!shouldWriteToLog)
		{
			return
		}


		// Log to file
		logToFile(Level.WARNING, "$callerInfo  $message", throwable)
	}

}