package com.nfcalarmclock.system

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.ServiceConnection
import android.content.pm.PackageManager
import android.net.Uri
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.os.UserManagerCompat
import com.nfcalarmclock.system.broadcasts.shutdown.NacShutdownBroadcastReceiver
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.io.File
import kotlin.coroutines.CoroutineContext
import kotlin.coroutines.EmptyCoroutineContext

/**
 * Receiver for the time tick intent. This is called when the time increments
 * every minute.
 */
fun createTimeTickReceiver(
	listener: (Context, Intent) -> Unit
): BroadcastReceiver
{
	return object : BroadcastReceiver()
	{

		/**
		 * Called when the broadcast is received.
		 */
		override fun onReceive(context: Context, intent: Intent)
		{
			listener(context, intent)
		}

	}
}

/**
 * Disable the alias for the main activity so that tapping an NFC tag
 * DOES NOT open the main activity.
 */
fun disableActivityAlias(context: Context)
{
	// Build the component name
	val aliasName = "${context.packageName}.main.NacMainAliasActivity"
	val componentName = ComponentName(context, aliasName)

	// Disable the alias
	context.packageManager.setComponentEnabledSetting(componentName,
		PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
		PackageManager.DONT_KILL_APP)
}

/**
 * Enable alias for the main activity so that tapping an NFC tag will open
 * the main activity.
 */
fun enableActivityAlias(context: Context)
{
	// Build the component name
	val aliasName = "${context.packageName}.main.NacMainAliasActivity"
	val componentName = ComponentName(context, aliasName)

	// Enable the alias
	context.packageManager.setComponentEnabledSetting(componentName,
		PackageManager.COMPONENT_ENABLED_STATE_ENABLED,
		PackageManager.DONT_KILL_APP)
}

/**
 * Get the device protected storage context to use for direct boot.
 *
 * If direct boot is not supported on the device, then a normal context will be returned.
 */
fun getDeviceProtectedStorageContext(context: Context, appContext: Boolean = false): Context
{
	// Check if context is already for device protected storage (used by direct boot)
	return if (context.isDeviceProtectedStorage)
	{
		context
	}
	// Context is for normal credential storage
	else
	{
		// Check if should get app context
		if (appContext)
		{
			// Return device protected storage context with an app context
			context.applicationContext.createDeviceProtectedStorageContext()
		}
		else
		{
			// Return device protected storage context
			context.createDeviceProtectedStorageContext()
		}
	}
}

/**
 * Check if the deviec is user unlocked or not.
 */
fun isUserUnlocked(context: Context): Boolean
{
	// Check the status
	return UserManagerCompat.isUserUnlocked(context)
}

/**
 * Register a broadcast receiver.
 */
@SuppressLint("UnspecifiedRegisterReceiverFlag")
fun registerMyReceiver(
	context: Context,
	broadcastReceiver: BroadcastReceiver,
	intentFilter: IntentFilter,
	flags: Int = ContextCompat.RECEIVER_EXPORTED)
{
	// Register the receiver
	ContextCompat.registerReceiver(context, broadcastReceiver, intentFilter, flags)
}

/**
 * Register a shutdown broadcast receiver.
 */
fun registerMyShutdownBroadcastReceiver(
	context: Context,
	shutdownBroadcastReceiver: NacShutdownBroadcastReceiver)
{
	// Register the shutdown receiver
	val shutdownIntentFilter = IntentFilter()

	shutdownIntentFilter.addAction(Intent.ACTION_SHUTDOWN)
	shutdownIntentFilter.addAction(Intent.ACTION_REBOOT)
	registerMyReceiver(context, shutdownBroadcastReceiver, shutdownIntentFilter)
}

/**
 * Unregister a broadcast receiver.
 */
fun unregisterMyReceiver(context: Context, broadcastReceiver: BroadcastReceiver)
{
	try
	{
		// Unregister the receiver
		context.unregisterReceiver(broadcastReceiver)
	}
	catch (_: IllegalArgumentException)
	{
	}
}

/**
 * Bind to a service.
 */
fun Context.bindToService(cls: Class<*>, serviceConnection: ServiceConnection)
{
	// Bind to the active timer service
	val intent = Intent(this, cls)

	this.bindService(intent, serviceConnection, 0)
}

/**
 * Send an email.
 */
fun Context.sendEmail(
	action: String,
	type: String = "",
	subject: String = "",
	to: String = "",
	attachment: File? = null,
	attachmentList: Array<File>? = null,
	onError: () -> Unit = {}
)
{
	// Build the email intent
	val intent = Intent(action)
		.apply {
			this.type = type

			// Subject
			putExtra(Intent.EXTRA_SUBJECT, subject)

			// Send to address
			if (to.isNotEmpty())
			{
				putExtra(Intent.EXTRA_EMAIL, arrayOf(to))
			}

			// Single attachment
			if (attachment != null)
			{
				val uri = FileProvider.getUriForFile(
					this@sendEmail,
					"com.nfcalarmclock.fileprovider",
					attachment
				)

				putExtra(Intent.EXTRA_STREAM, uri)
			}

			// Multiple attachments
			if (attachmentList != null)
			{
				val uriList = ArrayList<Uri>()

				attachmentList.forEach { a ->
					val u = FileProvider.getUriForFile(
						this@sendEmail,
						"com.nfcalarmclock.fileprovider",
						a
					)

					uriList.add(u)
				}

				putParcelableArrayListExtra(Intent.EXTRA_STREAM, uriList)

			}

			// Allow another app to read the attached URIs. Only needed if there are attachments
			if ((attachment != null) || (attachmentList != null))
			{
				addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
			}
		}

	// Ensure that there is an activity that is able to handle the intent
	if (intent.resolveActivity(packageManager) != null)
	{
		startActivity(intent)
	}
	// Error occurred
	else
	{
		onError()
	}
}

/**
 * Extension function to have a broadcast receiver execute something asynchronously.
 */
fun BroadcastReceiver.goAsync(
	context: CoroutineContext = EmptyCoroutineContext,
	block: suspend CoroutineScope.() -> Unit
)
{
	val pendingResult = goAsync()

	// Must run globally; there's no teardown callback.
	@OptIn(DelicateCoroutinesApi::class)
	GlobalScope.launch(context) {
		try
		{
			block()
		}
		finally
		{
			pendingResult.finish()
		}
	}

}